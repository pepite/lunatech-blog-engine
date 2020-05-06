package models

import scala.util.parsing.combinator._

class WordPressParser extends RegexParsers {

  def key: Parser[String] = """[a-z_]+""".r ^^ { _.toString }
  def value: Parser[String] = """[a-zA-Z0-9/_\-\.\ %]*""".r ^^ { _.toString }
  def attribute: Parser[(String, String)] = key ~ "=\"" ~ value ~ "\"" ^^ { case k ~ _ ~ v ~ _ => (k, v)}

  def vcRowBegin: Parser[Map[String, String]] = "[vc_row" ~ attribute.* ~ "]" ^^ { case _ ~ attributes ~ _ => attributes.toMap }
  def vcRowEnd: Parser[String] = "[/vc_row]"

  def vcColumnBegin: Parser[Map[String, String]] = "[vc_column" ~ attribute.* ~ "]" ^^ { case _ ~ attributes ~ _ => attributes.toMap }
  def vcColumnEnd: Parser[String] = "[/vc_column]"

  def vcColumnText: Parser[String] = "[vc_column_text]" ~ """[^\[\]]*""".r ~ "[/vc_column_text]" ^^ { case _ ~ text ~ _ => text }

  def imageWithAnimation: Parser[Map[String, String]] = "[image_with_animation" ~ attribute.* ~ "]" ^^ { case _ ~ attributes ~ _ => attributes.toMap }

  def vcRow: Parser[String] = vcRowBegin ~ vcColumnBegin ~ (vcColumnText | imageWithAnimation).* ~ vcColumnEnd ~ vcRowEnd ^^ {
    case rowAttributes ~ columnAttributes ~ text ~ _ ~ _ => text.toString
  }
}

object WordPressParser extends WordPressParser


