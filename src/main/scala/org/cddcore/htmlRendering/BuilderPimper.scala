package org.cddcore.htmlRendering

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import scala.xml.Elem
import org.cddcore.engine._
import org.cddcore.engine.builder._
import org.cddcore.utilities._
import org.cddcore.utilities.StartChildEndType._
import java.io.File
import scala.language.implicitConversions

object BuilderPimper {
  implicit def toPimper(builder: Builder3[RenderContext, List[Reportable], StartChildEndType, String, String]): BuilderPimper = new BuilderPimper(builder: Builder3[RenderContext, List[Reportable], StartChildEndType, String, String])
}

class BuilderPimper(builder: Builder3[RenderContext, List[Reportable], StartChildEndType, String, String]) {
  import HtmlRenderer._
  import SampleContexts._
  import BuilderPimper._
  def scenario(report: Report, item: Reportable, sce: StartChildEndType, pathToConclusion: List[Reportable] = List()): Builder3[RenderContext, List[Reportable], StartChildEndType, String, String] = {
    val reportPaths = report.reportPaths
    val path = reportPaths.find(_.head == item) match {
      case Some(p) => p
      case _ =>
        throw new IllegalArgumentException(s"\nReport: $report\nLast: $item\n${reportPaths.mkString("\n")}")
    }
    val rc = context(report).copy(pathToConclusion = pathToConclusion)
    builder.scenario(rc, path, sce)
  }
  
  def renderReport = builder.useCase("Reports have a huge template at the start, and end. The report title and date are substituted in").
    scenario(engineReport, engineReport, Start).
    expected(ReportDetails().reportStart("engineReportTitle", iconUrl, testDate)).
    matchOn { case (RenderContext(_, date, iconUrl, pathToConclusion, reportDetails, _), (r: Report) :: _, Start) => reportDetails.reportStart(r.titleString, iconUrl, date) }.

    scenario(engineReport, engineReport, End).
    expected(ReportDetails().reportEnd).
    matchOn { case (rc: RenderContext, (r: Report) :: _, End) => rc.reportDetails.reportEnd }
  

  def renderTraceItems = builder.useCase("A trace item ").
    scenario(foldingTraceReport, actualFoldingTI, Start).
    expected(s"\n<div class='traceItem'><div class='traceItemEngine'><table class='traceItemTable'><tr><td class='engineTitle'>Engine</td><td class='engineValue'>${titleAndIcon(foldingTraceReport, foldingED)}</td></tr><tr><td class='title'>Parameter</td><td class='value'>1</td></tr><tr><td class='title'>Result</td><td class='value'>Right(List(0, 2))</td></tr>" +
      "\n</table>\n").
    matchOn {
      case (rc, (ti @ TraceItem(engine: Engine, params, result, _, _, _, _)) :: _, Start) =>
        s"\n<div class='traceItem'><div class='traceItemEngine'>" +
          s"<table class='traceItemTable'>" +
          s"<tr><td class='engineTitle'>Engine</td><td class='engineValue'>${titleAndIcon(rc, EngineTools.toEngineTools(engine).asRequirement)}</td></tr>" +
          s"<tr><td class='title'>Parameter</td><td class='value'>${rc.cdp(params)}</td></tr>" +
          s"<tr><td class='title'>Result</td><td class='value'>${rc.cdp(result)}</td></tr>" +
          "\n</table>\n"
    }.

    scenario(foldingTraceReport, actualFoldingTI, End).
    expected("\n</div><!-- traceItemEngine --></div><!--traceItem -->\n").
    matchOnPrim({ case (rc, (ti: AnyTraceItem) :: _, End) => "\n</div><!-- traceItemEngine --></div><!--traceItem -->\n" }, "is trace item / End", "close off traceItemEngine and traceItem divs")

}