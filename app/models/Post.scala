package models

import org.joda.time.{ DateTime, DateTimeZone }
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.DocumentHeader;

object Post {
  val asciidoctor = Factory.create()
}

case class Post(slug: String, mainImage: String, content: String) {
  
  val htmlContent: String = Post.asciidoctor.convert(
      content,
      new java.util.HashMap[String, Object]())

  val header = Post.asciidoctor.readDocumentHeader(content)
  
  val author: String = header.getAuthor().getFullName()

  val publicationDate: DateTime = new DateTime(header.getRevisionInfo().getDate())

  val title: String = header.getDocumentTitle().getMain()

  val tags = header.getAttributes().get("tags")
}
