package models

import org.asciidoctor.{Asciidoctor, Options}
import org.asciidoctor.Asciidoctor.Factory
import org.asciidoctor.ast.{Document, DocumentHeader}
import org.joda.time.DateTime
import play.api.libs.json._

import scala.util.Try
import scala.util.control.Exception._

object Post {
  implicit val postWrites: OWrites[Post] = Json.writes[Post]
  val asciidoctor: Asciidoctor = Factory.create()
}

case class Post(slug: String, mainImage: String, content: String, author: Option[Author] = None) {

  val document: Document = Post.asciidoctor.load(content, Options.builder().build())

  lazy val htmlContent: String = Post.asciidoctor.convert(
    content,
    Options.builder().build())

  lazy val authorName: String = document.getAuthors.get(0).getFullName

  lazy val publicationDate: DateTime = new DateTime(document.getRevisionInfo.getDate)

  val title: String = document.getTitle

  lazy val lang: String = allCatch.opt(document.getAttributes.get("lang").toString).getOrElse("en")

  lazy val tags: Option[Array[String]] = allCatch.opt(document.getAttributes.get("tags").toString.drop(1).dropRight(1).split(",") :+ lang)

  lazy val excerpt: String = Try(content.split("\n").toList
    .dropWhile(_.nonEmpty)
    .dropWhile(_.isEmpty)
    .dropWhile(line => line.isEmpty || !line(0).isLetterOrDigit)
    .head).getOrElse("")

  def toJson: JsObject = {
    JsObject(
      Seq(
        "publication_date" -> JsString(publicationDate.toString("dd MMM yyyy")),
        "title" -> JsString(title),
        "slug" -> JsString(slug),
        "lang" -> JsString(lang),
        "image_url" -> JsString(mainImage),
        "author" -> JsString(author.map(_.name.getOrElse(authorName)).getOrElse(authorName)),
        "author_name" -> JsString(authorName),
        "author_img" -> JsString(author.map(_.avatar_url).getOrElse("null")),
        "tags" -> JsArray(tags.getOrElse(Array.empty).toSeq.map(JsString)),
        "excerpt" -> JsString(excerpt)
      ))
  }
}
