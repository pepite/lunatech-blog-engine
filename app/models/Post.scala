package models

import org.joda.time.{ DateTime, DateTimeZone }
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.DocumentHeader;
import models.Author._
import play.api.libs.json._

object Post {
  implicit val postWrites = Json.writes[Post]
  val asciidoctor = Factory.create()
}

case class Post(slug: String, mainImage: String, content: String, author: Option[Author] = None) {
  
  lazy val htmlContent: String = Post.asciidoctor.convert(
      content,
      new java.util.HashMap[String, Object]())

  lazy val header = Post.asciidoctor.readDocumentHeader(content)
  
  lazy val authorName: String = header.getAuthor().getFullName()

  lazy val publicationDate: DateTime = new DateTime(header.getRevisionInfo().getDate())

  lazy val title: String = header.getDocumentTitle().getMain()

  lazy val tags = header.getAttributes().get("tags")

  def toJson() = {
    JsObject(
    Seq(
      "publication_date"     -> JsString(publicationDate.toString("dd-MM-yyy")),
      "title"     -> JsString(title),
      "slug"     -> JsString(slug),
      "image_url"     -> JsString(mainImage),
      "author"     -> JsString(author.map(_.name.getOrElse(authorName)).getOrElse(authorName))
    ))
  }


}
