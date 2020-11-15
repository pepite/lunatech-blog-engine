package models

import org.joda.time.{ DateTime, DateTimeZone }
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.DocumentHeader;

object Post {
  val asciidoctor = Factory.create()
}

case class Post(slug: String, mainImage: String, content: String) {
  
  val header = Post.asciidoctor.readDocumentHeader(content)
  val pattern = "<p>(.*)</p>".r

  def getFirstParagraph(): String = Post.asciidoctor.convert(
    content,
    new java.util.HashMap[String, Object]()).slice(0, 160) + "..."

  def getContent(): String = Post.asciidoctor.convert(
      content,
      new java.util.HashMap[String, Object]())

  def getAuthor(): String = header.getAuthor().getFullName()

  def getPublicationDate(): DateTime = new DateTime(header.getRevisionInfo().getDate())

  def getTitle(): String = header.getDocumentTitle().getMain()

  def getTags() = header.getAttributes().get("tags")
}
