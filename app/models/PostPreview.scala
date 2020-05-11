package models

import org.joda.time.{ DateTime, DateTimeZone }

import org.asciidoctor.Asciidoctor.Factory
import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.DocumentHeader

case class PostPreview(title: String, author: String, date: DateTime)

object PostPreview {

  val asciidoctor = Factory.create()

  def fromString(content: String): PostPreview = {
    val header = asciidoctor.readDocumentHeader(content)
    val title = header.getDocumentTitle().getMain()
    val author = header.getAuthor().getFullName()
    val date = new DateTime(header.getRevisionInfo().getDate())
    PostPreview(title, author, date)
  }

}
