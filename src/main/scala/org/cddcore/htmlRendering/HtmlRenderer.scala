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
  def apply(): ReportDetails = ???
}
trait ReportDetails {
  def css: String
  def reportStart(title: String, iconUrl: String, date: Date): String
  def reportEnd: String
  def reportDateFormatter: DateFormat
}


case class RenderContext(urlMap: Any, reportDate: Date, iconUrl: String, pathToConclusion: List[Reportable] = List(), reportDetails: ReportDetails = ReportDetails(), misc: Map[String,Any] = Map())(implicit cddDisplayProcessor: CddDisplayProcessor) {
  val cdp = cddDisplayProcessor
  override def toString = getClass.getSimpleName()
}

object HtmlRenderer extends DecisionTreeBuilderForTests2[RenderContext, StartChildEndType, Elem] {

  val engineReportSingleItemRenderer = Engine[RenderContext, List[Reportable], StartChildEndType, String]().title("Single Engine report").
    build


}
