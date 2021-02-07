package modules

import scala.concurrent.Future
import javax.inject._
import play.api.inject.ApplicationLifecycle

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
import play.api.cache.SyncCacheApi

import play.api.mvc._
import play.api.libs.ws._
import play.api.http.HttpEntity

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.ExecutionContext
import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs.json._
import scala.util.control.Exception._
// This creates an `ApplicationStart` object once at start-up and registers hook for shut-down.
@Singleton
class ApplicationStart @Inject()(
  ws: WSClient, 
  configuration: Configuration,
  cache: SyncCacheApi
) (lifecycle: ApplicationLifecycle) {


 val accessToken = configuration.get[String]("accessToken")
  val organization = configuration.get[String]("githubOrganisation")
  val repository = configuration.get[String]("githubRepository")
  val branch = configuration.get[String]("githubBranch")
  val background = configuration.get[String]("blogBackground")
  //val cacheTtl = configuration.get[Duration]("cacheTtl")
  val perPage = configuration.get[Int]("blogsPerPage")

  private def getContents() = Github(Option(accessToken)).repos.getContents(organization, repository, "posts", Option(branch)).execFuture[HttpResponse[String]]()


  {
    // Parse all and put into cache
    println("loading posts…")
    val fPosts = getPosts()
    fPosts.map { posts =>  
      cache.set("posts", posts)
      println("posts loaded!")
      // Contruct our author cache
      getPostByAuthor(posts).map { case (author, posts) =>
        cache.set("author-" + author, posts)
      }
      
      getPostByTag(posts).map { case (tag, posts) =>
        cache.set("tag-" + tag, posts)
      }
    }
    Await.result(fPosts, 5 minutes)
  }

  private def getPostByAuthor(posts: Seq[Post]): Map[String, Seq[Post]] = {
      posts.groupBy(_.authorName)
  }

  private def getPostByTag(posts: Seq[Post]): Map[String, Seq[Post]] = {
      generateMap(posts)
  }

  def generateMap(list: Seq[Post]): Map[String, Seq[Post]] = {
    var m: Map[String, Seq[Post]] = Map.empty
    for (p <- list) {
      for (t <- p.tags.getOrElse(Array.empty)) {
        if(m.keySet.contains(t)) {
          m = m ++ Map(t -> (m(t) :+ p ))
        } else {
          m = m ++ Map(t -> Seq(p))
        }
      }
    }
    m
  }

  // Recursive version is too slow...
  def generateMap(list: Seq[Post], map: Map[String, Seq[Post]]) : Map[String, Seq[Post]] = list match {
    case x :: y => 
       val r = x.tags.getOrElse(Array.empty).toSet
       if(map.keySet.intersect(r) == r) { 
            r.map { tag =>
                generateMap(y, map ++ Map(tag -> (map(tag) :+ x ))) 
            }.headOption.getOrElse(generateMap(y, map ))
        } else {
            r.map { tag =>
                  generateMap(y, map ++ Map(tag -> Seq(x)))
            }.headOption.getOrElse(generateMap(y, map ))
        }
    case Nil => map
  }

  private def getPosts(page: Int = 1):Future[Seq[Post]] = getContents().flatMap { repo =>
         
          repo match {
            case Left(e) => { 
              println(e.getMessage)
              Future.successful(Seq.empty)
              //Future(BadRequest(e.getMessage))
            }
            // Those are our blog post
            case Right(r) => {
              // Build our posts
               // get only the pageSize
              val posts = r.result.toList.reverse.map { d =>
                val url = d.download_url.get
                val name = url.slice(url.lastIndexOf("/"), url.lastIndexOf(".adoc"))
                val request: WSRequest = ws.url(url)
                val posts = request.get().flatMap { r =>
                  val p = allCatch.opt(Post(s"/posts$name", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media/${name}/background.png",
                    r.body))
                  p match {
                    case Some(post) => {
                        val authorName = post.authorName
                        cache.get(authorName) match {
                            case None => {
                                val getUser = Github(Option(accessToken)).users.get(authorName).execFuture[HttpResponse[String]]()
                                val postWithAuthor = getUser.map {
                                    case Left(e) => {
                                        p
                                    }
                        
                                    case Right(rUser) => {
                                        val user = rUser.result
                                        // TODO: cache author
                                        val author = Author(user.login,
                                            user.avatar_url,
                                            user.html_url,
                                            user.name,
                                            user.email,
                                            user.company,
                                            user.blog,
                                            user.location,
                                            user.bio)
                                        cache.set(authorName, author)
                                        val post = allCatch.opt(Post(s"/posts$name", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media${name}/background.png",
                                        r.body, Some(author)))
                                        post
                                    }
                                }
                                postWithAuthor
                            }
                            case author => {
                                val post = allCatch.opt(Post(s"/posts$name", s"https://raw.githubusercontent.com/${organization}/${repository}/${branch}/media${name}/background.png",
                                            r.body, author))
                                Future.successful(post)
                            }
                        }
                    }
                    case None => {
                        Future.successful(None)
                    }
                }
             }
             posts
            }
            Future.sequence(posts.toList).map { p =>
              p.flatten.toSeq
            }
        }
      }
    }
  

}