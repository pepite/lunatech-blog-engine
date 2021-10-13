package models

import org.joda.time.{ DateTime, DateTimeZone }
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.DocumentHeader;
import models.Author._
import play.api.libs.json._
import scala.util.control.Exception._

object Post {
  implicit val postWrites = Json.writes[Post]
  val asciidoctor = Factory.create()
}

case class Post(slug: String, mainImage: String, content: String, author: Option[Author] = None) {
  
  lazy val htmlContent: String = Post.asciidoctor.convert(
      content,
      new java.util.HashMap[String, Object]())

  val header = Post.asciidoctor.readDocumentHeader(content)
  
  lazy val authorName: String = header.getAuthor().getFullName()

  lazy val publicationDate: DateTime = new DateTime(header.getRevisionInfo().getDate())

  val title: String = header.getDocumentTitle().getMain()

  lazy val lang: String = allCatch.opt(header.getAttributes().get("lang").toString()).getOrElse("en")

  lazy val tags: Option[Array[String]] = allCatch.opt(header.getAttributes().get("tags").toString().drop(1).dropRight(1).split(",") :+ lang)

  lazy val excerpt: String = Post.asciidoctor.convert(content.split("\n").drop(6).take(4).mkString(" "), new java.util.HashMap[String, Object]())

  def toJson() = {
    JsObject(
    Seq(
      "publication_date"     -> JsString(publicationDate.toString("yyyy-MM-dd")),
      "title"     -> JsString(title),
      "slug"     -> JsString(slug),
      "lang" -> JsString(lang),
      "image_url"     -> JsString(mainImage),
      "author"     -> JsString(author.map(_.name.getOrElse(authorName)).getOrElse(authorName)),
      "tags" ->  JsArray(tags.getOrElse(Array.empty).toSeq.map(JsString(_)))
    ))
  }


}
