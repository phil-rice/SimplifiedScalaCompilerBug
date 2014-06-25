package org.cddcore.htmlRendering

import java.text.DateFormat

import java.text.SimpleDateFormat
import java.util.Date

import scala.xml.Elem

import org.cddcore.engine._
import org.cddcore.engine.builder._
import org.cddcore.utilities._
import org.cddcore.utilities.StartChildEndType._

object SampleContexts {
  import scala.language.implicitConversions
  implicit def toContext(report: Report) = context(report)

  def context(report: Report, pathToConclusions: List[Reportable] = List()): RenderContext = ???


}
