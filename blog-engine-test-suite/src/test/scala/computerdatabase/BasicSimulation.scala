package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://lunatech-blog.cleverapps.io") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val scn = scenario("Scenario Name") // A scenario is a chain of requests and pauses
    .exec(http("index page")
      .get("/"))
    .pause(5 seconds)
    .exec(http("blog post details")
     .get("/posts/2020-10-02-the-tale-about-hibernate-and-temporal-tables"))
       
  setUp(scn.inject(
    constantUsersPerSec(10) during(2 minute)
  ).protocols(httpProtocol)) 
  //setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
}
