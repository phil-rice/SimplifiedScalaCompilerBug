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
  

 
}