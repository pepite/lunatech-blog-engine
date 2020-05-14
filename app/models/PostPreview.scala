package models

import org.joda.time.{ DateTime, DateTimeZone }

import org.asciidoctor.Asciidoctor.Factory
import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.DocumentHeader

case class PostPreview(title: String, author: String, date: DateTime, imageUrl: String, slug: String)

object PostPreview {

  val asciidoctor = Factory.create()

  def from(file: String, content: String, imageUrl: String, author: String): PostPreview = {
    val header = asciidoctor.readDocumentHeader(content)
    val title = header.getDocumentTitle().getMain()
    val date = new DateTime(header.getRevisionInfo().getDate())
    val slug = s"/posts/${file.dropRight(".adoc".length)}"
    PostPreview(title, author, date, imageUrl, slug)
  }
}
