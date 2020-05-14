package services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._
import play.api.libs.json
import play.api.libs.ws._

import org.asciidoctor.Asciidoctor.Factory
import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.DocumentHeader

class GithubService (ws: WSClient, configuration: Configuration) {

  val accessToken = configuration.getOptional[String]("github.accessToken")
  val organization = configuration.get[String]("github.organisation")
  val repository = configuration.get[String]("github.repository")
  val branch = configuration.getOptional[String]("github.branch").getOrElse("master")
  val postsSHA = configuration.get[String]("github.posts.sha")

  private val postsUrl = s"https://api.github.com/repos/$organization/$repository/git/trees/$postsSHA"
  private val filesUrl = s"https://raw.githubusercontent.com/$organization/$repository/$branch/posts"
  private val imageUrl = s"https://raw.githubusercontent.com/$organization/$repository/$branch/media"
  private val authorUrl = s"https://api.github.com/users"

  val asciidoctor = Factory.create()  

  def listFiles: Future[Seq[String]] = {
    ws.url(postsUrl).get()
      .map { response =>
        (response.json \ "tree" \\ "path").map(_.as[String]).filter(_.endsWith(".adoc"))
      }
  }

  def getAuthorFullName(content: String): Future[String] = {
    val username = asciidoctor.readDocumentHeader(content).getAuthor().getFullName()
    ws.url(s"$authorUrl/$username").get()
      .map { response =>
        (response.json \ "name").as[String]
      }
  }

  def getFileContent(file: String): Future[String] = {
    ws.url(s"$filesUrl/$file").get()
      .map(_.body)
  }

  def imageUrl(file: String): String = s"""$imageUrl/${file.dropRight(".adoc".length)}/background.png""" 

}
