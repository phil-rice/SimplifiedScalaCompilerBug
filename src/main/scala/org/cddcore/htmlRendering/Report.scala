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
  def html(report: Report, engine: Function3[RenderContext, List[Reportable], StartChildEndType, String], renderContext: RenderContext): String = ""

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
  val iconUrl = Strings.url(rootUrl, title, "index.html")
  val renderContext = RenderContext("", date, iconUrl)

  for (e <- engines; path: List[Reportable] <- e.asRequirement.pathsIncludingSelf.toList) {
    val r = path.head
    val url = ""
    val report: Report = ???
    val renderer = HtmlRenderer.engineReportSingleItemRenderer
    val newRenderContext = renderContext.copy(pathToConclusion = List())
    val html = Report.html(report, renderer, newRenderContext)
    reportWriter.print(url, Some(r), html)
  }

}



