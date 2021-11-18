package models

import play.api.libs.json._

object Author {
  implicit val authorWrites: OWrites[Author] = Json.writes[Author]
}

case class Author(
                   login: String,
                   avatar_url: String,
                   html_url: String,
                   name: Option[String] = None,
                   email: Option[String] = None,
                   company: Option[String] = None,
                   blog: Option[String] = None,
                   location: Option[String] = None,
                   bio: Option[String] = None
                 )
