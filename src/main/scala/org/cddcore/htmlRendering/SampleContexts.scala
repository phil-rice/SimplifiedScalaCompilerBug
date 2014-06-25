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

  val testDate = new Date(2000, 1, 1)
  val rootUrl = "RootUrl"
  val iconUrl = "IconUrl"
  val emptyUrlMap = UrlMap(rootUrl)

  def context(report: Report, pathToConclusions: List[Reportable] = List()): RenderContext = {
    val urlMap = emptyUrlMap ++ report.urlMapPaths
    val rc = RenderContext(urlMap, SampleContexts.testDate, iconUrl, pathToConclusions)
    rc
  }

  val doc1 = Document(title = Some("doc1title"), url = Some("doc1Url"))
  val doc1Report = Report(Some("doc1Report"), None, List(doc1))

  val docNoTitle = Document(url = Some("doc1Url"))
  val doc1NoTitlereport = Report(Some("doc1Report"), None, List(docNoTitle))

  val eBlank = Engine[Int, Int]().build
  val eBlankED = eBlank.asRequirement
  val eBlankReport : Report = ???

  val eBlankTitle = Engine[Int, Int]().title("EBlankTitle").build
  val eBlankTitleED = eBlankTitle.asRequirement
  val eBlankTitleReport : Report = ???

  val eBlankTitleDoc1 = Engine[Int, Int]().title("EBlankTitle").reference("", doc1).build
  val eBlankTitleDoc1ED = eBlankTitleDoc1.asRequirement.asInstanceOf[EngineDescription[_, _]]

  val eWithUsecasesAndScenarios = Engine[Int, Int].title("eWithUsecasesAndScenarios").
    useCase("useCase0", "useCase0Description").scenario(0).expected(0).
    useCase("useCase1").scenario(1).expected(2).code { (x) => x * 2 }.because { (x) => x > 0 }.
    scenario(2).expected(4).
    build
  val eWithUsecasesAndScenariosEd = eWithUsecasesAndScenarios.asRequirement
  val engineReport: Report = ???
  import ReportableHelper._

  val uc0 = eWithUsecasesAndScenariosEd.useCases(0)
  val uc1 = eWithUsecasesAndScenariosEd.useCases(1)
  val uc0s0 = eWithUsecasesAndScenariosEd.scenarios(0)
  val uc1s1 = eWithUsecasesAndScenariosEd.scenarios(1)
  val uc1s2 = eWithUsecasesAndScenariosEd.scenarios(2)

  val tree = eWithUsecasesAndScenarios.asInstanceOf[Engine1FromTests[Int, Int]].tree
  val decision = tree.root.asDecision
  val conclusionYes = decision.yes.asConclusion
  val conclusionNo = decision.no.asConclusion

  val folding = Engine.foldList[Int, Int].title("Folding Engine Title").
    childEngine("ce0").scenario(0).expected(0).
    childEngine("ce1").scenario(1).expected(2).code { (x) => x * 2 }.because { (x) => x > 0 }.
    scenario(2).expected(4).
    build.asInstanceOf[FoldingEngine1[Int, Int, List[Int]]]

 
  type TI = TraceItem[Any, Any, Any, Any]
  implicit def toTraceItem(x: (Any, Any, Any, List[TI])) =
    x match { case (engine, params, result, nodes) => new TraceItem[Any, Any, Any, Any](engine, params, Right(result), None, nodes, 0) }
  val (result, trace) = Engine.trace(folding(1))
  val foldingTraceReport: Report = ???
 
  val ce0TI: TI = (folding.engines(0), 1, 0, List())
  val ce1TI: TI = (folding.engines(1), 1, 2, List())
  val foldingTI: TI = (folding, 1, List(0, 2), List(ce0TI, ce1TI))
  val actualFoldingTI = trace(0)
  val actualCe0TI = actualFoldingTI.nodes(0)
  val actualCe1TI = actualFoldingTI.nodes(1)

  val reqNoTitle = RequirementForTest(textOrder = 666)
  val reqWithTitle = RequirementForTest(title = Some("ReqTitle"))

  val reqWithTitleReport = Report.apply(Some("ReportTitle"), None, List(reqWithTitle))

}
