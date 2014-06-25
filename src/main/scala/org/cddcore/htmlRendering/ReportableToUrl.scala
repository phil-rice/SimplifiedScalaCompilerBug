package org.cddcore.htmlRendering

import org.cddcore.utilities._
import org.cddcore.engine._

trait UrlMap {
  def rootUrl: String
  def rToName: KeyedMap[String]
  def toUrl: KeyedMap[String]
  def fromUrl: Map[String, List[Reportable]]
  def seen: Set[String]

  /** From a reportable to the Url representing it */
  def apply(r: Reportable): String = toUrl(r)
  /** From a reportable to the optional Url representing it */
  def get(r: Reportable): Option[String] = toUrl.get(r)

  /** From a url to the reportable that should be at that url */
  def apply(url: String) = fromUrl(url)
  /** From a url to the optional reportable that should be at that url */
  def get(url: String) = fromUrl.get(url)

  def getName(r: Reportable): String = rToName(r)
  /** Has the reportable got a url? */
  def contains(r: Reportable) = toUrl.contains(r)
  def containsName(s: String) = seen.contains(s)
  def reportableCount = toUrl.size
}

object UrlMap {
  def apply(rootUrl: String = "") = new SimpleReportableToUrl(rootUrl)
  def urlId(r: Reportable)(implicit conv: TemplateLike[Reportable]): String= ???
}

trait ReportableToUrl[RU <: ReportableToUrl[RU]] extends UrlMap {
  import EngineTools._
  import ReportableHelper._
  def copy(rootUrl: String, rToName: KeyedMap[String], toUrl: KeyedMap[String], fromUrl: Map[String, List[Reportable]], seen: Set[String]): RU
  private def asRu = this.asInstanceOf[RU]
  def addPath(path: List[Reportable])(implicit conv: TemplateLike[Reportable]): (RU, String) = { val result = this + path; (result, result(path.head)) }

  def ++(holder: NestedHolder[Reportable] with Reportable): RU = ???
  def ++(paths: Traversable[List[Reportable]])(implicit conv: TemplateLike[Reportable]): RU = ???
  def ++(engines: List[Engine]): ReportableToUrl[RU] = ???
  def +(path: List[Reportable])(implicit conv: TemplateLike[Reportable]): RU = ???

  protected def newName(seen: Set[String], r: Reportable)(implicit conv: TemplateLike[Reportable]) = ???
  
}

case class SimpleReportableToUrl(rootUrl: String = "", rToName: KeyedMap[String] = new KeyedMap[String](), toUrl: KeyedMap[String] = new KeyedMap[String](), fromUrl: Map[String, List[Reportable]] = Map(), seen: Set[String] = Set()) extends ReportableToUrl[SimpleReportableToUrl] {
  def copy(rootUrl: String, rToName: KeyedMap[String], toUrl: KeyedMap[String], fromUrl: Map[String, List[Reportable]], seen: Set[String]) =
    new SimpleReportableToUrl(rootUrl, rToName, toUrl, fromUrl, seen)
}

