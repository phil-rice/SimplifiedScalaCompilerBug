package org.cddcore.htmlRendering

import java.util.Date
import org.cddcore.engine._
import org.cddcore.utilities._
import StartChildEndType._
import EngineTools._
import java.io.File
import org.cddcore.engine.builder.Conclusion
import org.cddcore.engine.builder.AnyConclusion

object Report {
  def apply(title: Option[String] = None, description: Option[String] = None, nodes: List[Reportable] = List()) =
    new SimpleReport(title, description, nodes)

  def html(report: Report, engine: Function3[RenderContext, List[Reportable], StartChildEndType, String], renderContext: RenderContext): String =
    Lists.traversableToStartChildEnd(report.reportPaths).foldLeft("") { case (html, (path, cse)) => html + engine(renderContext, path, cse) }

}

trait Report extends TitledReportable {
  def title: Option[String]
  def description: Option[String]
  def reportPaths: List[List[Reportable]]
  def urlMapPaths: List[List[Reportable]] = reportPaths
}

case class SimpleReport(
  val title: Option[String],
  val description: Option[String],
  val nodes: List[Reportable],
  val textOrder: Int = Reportable.nextTextOrder) extends Report with NestedHolder[Reportable] {
  val reportPaths = pathsIncludingSelf.toList
  override def toString = s"Report(${title.getOrElse("None")}, nodes=(${nodes.mkString(",")}))"
}
trait ReportWriter {
  def print(url: String, main: Option[Reportable], html: String)
}

class ReportOrchestrator(rootUrl: String, title: String, engines: List[Engine], date: Date = new Date, reportWriter: ReportWriter = null) {
  import EngineTools._
  import Strings._
  val rootReport: Report = ???
  val engineReports = engines.foldLeft(List[Report]())((list, e) => Report() :: list).reverse
  val urlMap = UrlMap(rootUrl) ++ rootReport.urlMapPaths
  val iconUrl = Strings.url(rootUrl, title, "index.html")
  val renderContext = RenderContext(urlMap, date, iconUrl)

  def makeReports =
    Exceptions({
      val t = rootReport.reportPaths
      reportWriter.print(iconUrl, None, Report.html(rootReport, HtmlRenderer.engineReportSingleItemRenderer, renderContext))

      for (e <- engines; path: List[Reportable] <- e.asRequirement.pathsIncludingSelf.toList) {
        val r = path.head
        val url = urlMap(r)
        val report: Report = ???
        val renderer = HtmlRenderer.engineReportSingleItemRenderer
        val actualPathToConclusion = pathToConclusion(path)
        val newRenderContext = renderContext.copy(pathToConclusion = actualPathToConclusion)
        val html = Report.html(report, renderer, newRenderContext)
        reportWriter.print(url, Some(r), html)
      }
    }, (e) => { System.err.println("Failed in make report"); e.printStackTrace; if (e.getCause != null) { System.err.println("Cause"); e.getCause().printStackTrace; } })

  def pathToConclusion[Params, R](path: List[Reportable]): List[Reportable] = {
    def engineFromTestsFor(ed: EngineDescription[Params, R]) = {
      def fromEngine(e: Engine): List[EngineFromTests[Params, R]] = e match {
        case e: EngineFromTests[Params, R] if (e.asRequirement.eq(ed)) => List(e)
        case f: FoldingEngine[Params, R, _] => f.engines.collect { case e: EngineFromTests[Params, R] if (e.asRequirement.eq(ed)) => e }
        case d: DelegatedEngine[Params, R] => fromEngine(d.delegate)
        case _ => List()
      }
      engines.flatMap(fromEngine(_)).head
    }
    def pathFrom(e: EngineFromTests[Params, R], params: Params) = e.evaluator.findPathToConclusionWithParams(e.tree, params)

    path.head match {
      case s: Scenario[Params, R] => path.collect { case ed: EngineDescription[Params, R] => pathFrom(engineFromTestsFor(ed), s.params) }.head
      case _ => List()
    }
  }
}



