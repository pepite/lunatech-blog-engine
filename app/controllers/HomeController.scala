package controllers

import github4s.Github
import github4s.Github._
import github4s.jvm.Implicits._
import models._
import play.api._
import play.api.cache.SyncCacheApi
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import scalaj.http.HttpResponse

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
                                cc: ControllerComponents,
                                ws: WSClient,
                                configuration: Configuration,
                                cache: SyncCacheApi
                              ) extends AbstractController(cc) {

  private val accessToken = configuration.get[String]("accessToken")
  private val organization = configuration.get[String]("githubOrganisation")
  private val repository = configuration.get[String]("githubRepository")
  private val branch = configuration.get[String]("githubBranch")
  private val background = configuration.get[String]("blogBackground")
  private val perPage = configuration.get[Int]("blogsPerPage")

  private def slicePosts(posts: Seq[Post], page: Int): Seq[Post] = {
    posts.slice((page - 1) * perPage, page * perPage)
  }

  /**
   *
   *
   */
  def index = Action.async { implicit request: Request[AnyContent] =>
    val page = 1
    cache.get[Seq[Post]]("posts") match {
      case None =>
        Future.successful(BadRequest("nothing in the cache"))
      case Some(result) =>
        Future.successful(Ok(views.html.index(background, slicePosts(result, page), perPage)))
    }
  }

  def byTags(name: String) = Action.async { implicit request: Request[AnyContent] =>
    cache.get[Seq[Post]]("tag-" + name) match {
      case None =>
        Future.successful(BadRequest("nothing in the cache"))
      case Some(result) =>
        Future.successful(Ok(views.html.index(background, result, -1)))
    }
  }

  def byAuthor(name: String) = Action.async { implicit request: Request[AnyContent] =>
    cache.get[Seq[Post]]("author-" + name) match {
      case None =>
        Future.successful(BadRequest("nothing in the cache"))
      case Some(result) =>
        Future.successful(Ok(views.html.index(background, result, -1)))
    }
  }

  def posts(page: Int) = Action.async { implicit request: Request[AnyContent] =>
    cache.get[Seq[Post]]("posts") match {
      case None =>
        Future.successful(BadRequest("nothing in the cache"))
      case Some(result) =>
        Future.successful(Ok(Json.toJson(slicePosts(result, page).map(_.toJson))))
    }
  }

  def feed() = Action.async { implicit request: Request[AnyContent] =>
    cache.get[Seq[Post]]("posts") match {
      case None =>
        Future.successful(BadRequest("nothing in the cache"))
      case Some(result) =>
        Future.successful(Ok(views.html.feed(result.slice(0, 30))).as("application/xml"))
    }
  }

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

  def view(name: String): Action[AnyContent] = Action.async { implicit req: Request[AnyContent] =>
    val request: WSRequest = ws.url(s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/posts/${name}.adoc")
    request.get().flatMap { r => {
      val post = Post(s"/${name}", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media/${name}/background.png", r.body)
      // TODO Change me
      if (post.title == null) {
        Future(Ok(views.html.notFound()))
      } else {
        cache.get(post.authorName) match {
          case None =>
            val getUser = Github(Option(accessToken)).users.get(post.authorName).execFuture[HttpResponse[String]]()
            getUser.map {
              case Left(_) => Ok(views.html.post(post))
              case Right(re) =>
                val user = re.result
                // TODO: cache author
                val author = Author(
                  user.login,
                  user.avatar_url,
                  user.html_url,
                  user.name,
                  user.email,
                  user.company,
                  user.blog,
                  user.location,
                  user.bio)
                cache.set(post.authorName, author)
                val postWithAuthor = Post(s"/${name}", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media/${name}/background.png",
                  r.body, Some(author))
                Ok(views.html.post(postWithAuthor))
            }
          case author =>
            val postWithAuthor = Post(s"/${name}", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media/${name}/background.png",
              r.body, author)
            Future.successful(Ok(views.html.post(postWithAuthor)))
        }

      }
    }
    }.recover { case e: Exception => e.printStackTrace(); Ok(views.html.notFound()) }
  }
}
