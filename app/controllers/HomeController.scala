package controllers

import javax.inject._
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext

import play.api._
import play.api.mvc._
import play.api.libs.json
import play.api.libs.ws._
import play.api.http.HttpEntity

import org.joda.time.{ DateTime, DateTimeZone }
import github4s.Github
import github4s.jvm.Implicits._
import github4s.Github._
import scalaj.http.HttpResponse

import models._
import services.GithubService

@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient, configuration: Configuration)
    extends AbstractController(cc) {

  val accessToken = configuration.getOptional[String]("github.accessToken")
  val organization = configuration.get[String]("github.organisation")
  val repository = configuration.get[String]("github.repository")
  val branch = configuration.getOptional[String]("github.branch").getOrElse("master")
  val postsSHA = configuration.get[String]("github.posts.sha")
  val background = configuration.get[String]("background")

  val githubService = new GithubService(ws, configuration)

  def listPosts() = Action.async { implicit request =>
    githubService
      .listPosts
      .map(_.sortBy(- _.date.getMillis()))
      .map{ previews =>
        Ok(views.html.listPosts(background, previews))
      }
  }


  // read the blog post
  def media(post: String, name: String) = Action.async { implicit request: Request[AnyContent] =>

    val request: WSRequest = ws.url(s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media/$post/$name")
    // Make the request
    request.withMethod("GET").stream().map { response =>
      // Check that the response was successful
      if (response.status == 200) {

        // Get the content type
        val contentType = response.headers.get("Content-Type").flatMap(_.headOption)
          .getOrElse("application/octet-stream")

        // If there's a content length, send that, otherwise return the body chunked
        response.headers.get("Content-Length") match {
          case Some(Seq(length)) =>
            Ok.sendEntity(HttpEntity.Streamed(response.bodyAsSource, Some(length.toLong), Some(contentType)))
          case _ =>
            Ok.chunked(response.bodyAsSource).as(contentType)
        }
      } else {
        BadGateway
      }
    }
  }

  def view(name: String) = Action.async { implicit request: Request[AnyContent] =>
    val request: WSRequest = ws.url(s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/posts/${name}.adoc")
      request.get().flatMap { r => {
        val post = Post(s"/${name}", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media/${name}/background.png",
        r.body)
        val getUser = Github(accessToken).users.get(post.getAuthor).execFuture[HttpResponse[String]]()
        val x = getUser.map {
            case Left(e) => BadRequest(e.getMessage)
            case Right(r) => {
              val user = r.result
              val author = Author(user.login,
                    user.avatar_url,
                    user.html_url,
                    user.name,
                    user.email,
                    user.company,
                    user.blog,
                    user.location,
                    user.bio)

              Ok(views.html.post(post, author))
          }
        }
        x
      }
    }
  }

}
