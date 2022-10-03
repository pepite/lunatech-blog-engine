package controllers

import models.Post
import play.api.Configuration
import play.api.cache.SyncCacheApi
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.libs.json._

import javax.inject.Inject

class RestController @Inject()(
                                controllerComponents: ControllerComponents,
                                configuration: Configuration,
                                cache: SyncCacheApi
                              ) extends AbstractController(controllerComponents) {

  def posts(limit: Int, page: Int, tag: Option[String], author: Option[String], lang: Option[String]): Action[AnyContent] = Action {
    getPosts(tag, author) match {
      case None =>
        BadRequest("nothing in the cache")
      case Some(posts) =>
        val result = slicePosts(applyPostsFilters(posts, lang), limit, page).map(_.toJson)
        Ok(Json.toJson(result))
    }
  }

  private def getPosts(tag: Option[String], author: Option[String]) = {
    def filterTags(posts: Option[Seq[Post]], tagName: String) = posts match {
      case Some(postsVal) => Some(postsVal.filter(_.tags.exists(_.contains(tagName))))
      case None => None
    }

    (tag, author) match {
      case (Some(tagName), None) => cache.get[Seq[Post]]("tag-" + tagName)
      case (None, Some(authorName)) => cache.get[Seq[Post]]("author-" + authorName)
      case (Some(tagName), Some(authorName)) => filterTags(cache.get[Seq[Post]]("author-" + authorName), tagName)
      case (None, None) => cache.get[Seq[Post]]("posts")
    }
  }

  private def applyPostsFilters(posts: Seq[Post], lang: Option[String]) = {
    def langFilter(input: Post) = lang match {
      case Some(value) => input.lang == value
      case None => true
    }

    posts.filter(langFilter)
  }

  private def slicePosts(posts: Seq[Post], limit: Int, page: Int) = {
    posts.slice((page - 1) * limit, page * limit)
  }
}
