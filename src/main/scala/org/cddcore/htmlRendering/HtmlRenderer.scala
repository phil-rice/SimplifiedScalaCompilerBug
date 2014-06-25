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

object ReportDetails {
  def apply() = new SimpleReportDetails
}
trait ReportDetails {
  def css: String
  def reportStart(title: String, iconUrl: String, date: Date): String
  def reportEnd: String
  def reportDateFormatter: DateFormat
}

class SimpleReportDetails(
  val css: String = Files.getFromClassPath(classOf[ReportDetails], "cdd.css"),
  val reportStartTemplate: String = Files.getFromClassPath(classOf[ReportDetails], "reportStart"),
  val reportEnd: String = Files.getFromClassPath(classOf[ReportDetails], "reportEnd"),
  val reportDateFormatter: DateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm")) extends ReportDetails {
  def reportStart(title: String, iconUrl: String, date: Date) =
    reportStartTemplate.
      replace("$REPORT_TITLE$", title).
      replace("$REPORT_DATE$", reportDateFormatter.format(date)).
      replace("$CSS$", css).
      replace("$ICON_URL$", iconUrl)

}

case class RenderContext(urlMap: UrlMap, reportDate: Date, iconUrl: String, pathToConclusion: List[Reportable] = List(), reportDetails: ReportDetails = ReportDetails(), misc: Map[String,Any] = Map())(implicit cddDisplayProcessor: CddDisplayProcessor) {
  val cdp = cddDisplayProcessor
  override def toString = getClass.getSimpleName()
}

object HtmlRenderer extends DecisionTreeBuilderForTests2[RenderContext, StartChildEndType, Elem] {
  import SampleContexts._
  import BuilderPimper._
  def indent(path: List[Reportable]) = path.indexWhere(_.isInstanceOf[DecisionTree[_, _]]) match {
    case 1 => ""
    case i => s"<div class='indent'>${List.fill(i - 1)("&#160;").mkString("")}</div>"
  }

  type PathAndTag = (List[Reportable], StartChildEndType)

  import TemplateLike._

  val icon = Engine[RenderContext, Reportable, String]().title("icon").description("returns the html for an image for the icon for the scenario").
    build

  //  println("Icon:\n" + icon)

  val linkAndIcon = Engine[RenderContext, Reportable, String]().title("linkAndIcon").description("Displays a suitable icon in a link for the reportable").
    build

  val titleAndIcon = Engine[RenderContext, Reportable, String]().title("titleAndIcon").description("Finds a suitable titleAndIcon for a reportable. Includes links to go to item, and the id from the urlmap").
    build

  private def findTraceItemConclusion(path: List[Reportable]): List[(AnyTraceItem, AnyConclusion)] = {
    val result = path.flatMap {
      case ti: AnyTraceItem => {
        val r = ti.toEvidence[AnyConclusion].collect { case c: AnyConclusion => List((ti, c)) }.getOrElse(Nil)
        r
      }
      case _ => List()
    }
    result
  }

  val conclusionPath = Engine[RenderContext, List[Reportable], List[Reportable]].title("conclusionPath").
    build

  val engineAndDocumentsSingleItemRenderer = Engine[RenderContext, List[Reportable], StartChildEndType, String]().
    title("Engine and Documents Single Item Renderer").
    build

  val engineReportSingleItemRenderer = Engine[RenderContext, List[Reportable], StartChildEndType, String]().title("Single Engine report").
    build

  val traceReportSingleItemRenderer = Engine[RenderContext, List[Reportable], StartChildEndType, String]().title("Trace Item Report").
    build

  val useCaseOrScenarioReportRenderer = Engine[RenderContext, List[Reportable], StartChildEndType, String]().title("Single Use Case Report").
    build

  val rendererFor =
    Engine[Reportable, Engine3[RenderContext, List[Reportable], StartChildEndType, String, String]]().title("Select the renderer for use for this reportable").
    
      build

}
