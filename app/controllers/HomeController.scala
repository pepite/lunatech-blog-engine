package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import org.joda.time.{ DateTime, DateTimeZone }
import models._
import github4s.Github
import github4s.jvm.Implicits._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import github4s.Github._
import scalaj.http.HttpResponse

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.duration._

import play.api.mvc._
import play.api.libs.ws._
import play.api.http.HttpEntity

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.ExecutionContext


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient, configuration: Configuration) extends AbstractController(cc) {


  val accessToken = configuration.get[String]("github.accessToken")
  val organization = configuration.get[String]("github.organisation")
  val repository = configuration.get[String]("github.repository")
  val branch = configuration.get[String]("github.branch")
  val background = configuration.get[String]("background")

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action.async { implicit request: Request[AnyContent] =>

      val getContents = Github(Some(accessToken)).repos.getContents(organization, repository, "posts", Some(branch)).execFuture[HttpResponse[String]]()

      val x = getContents.flatMap { repo =>
          repo match {
            case Left(e) => {
              Future(BadRequest(e.getMessage))
            }
            // Those are our blog post
            case Right(r) => {
              // Build our posts
              val posts = r.result.map { d =>
                val url = d.download_url.get
                val name = url.slice(url.lastIndexOf("/"), url.lastIndexOf(".adoc"))
                val request: WSRequest = ws.url(url)
                val posts = request.get().flatMap { r =>
                  val post = Post(s"/posts$name", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media/${name}/background.png",
                  r.body)
                  val getUser = Github(Some(accessToken)).users.get(post.getAuthor).execFuture[HttpResponse[String]]()
                  val postWithAuthor = getUser.map {
                      case Left(e) => None
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

                        Some((post, author))
                      }
                    }
                  postWithAuthor
                }
              posts
            }
            Future.sequence(posts.toList).map { p =>
              Ok(views.html.index(background, p.flatten))
            }
        }
      }
    }
    x
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
        val getUser = Github(Some(accessToken)).users.get(post.getAuthor).execFuture[HttpResponse[String]]()
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
