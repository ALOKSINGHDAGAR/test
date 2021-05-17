

import scala.concurrent.duration._
import java.util.concurrent.ThreadLocalRandom
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps

class ComputerWorld extends Simulation {

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io")
    .acceptHeader("""text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")
    .acceptEncodingHeader("""gzip, deflate""")
    .acceptLanguageHeader("""en-gb,en;q=0.5""")
    .userAgentHeader("""Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:31.0) Gecko/20100101 Firefox/31.0""")

  val computerDbScn = scenario("Computer Scenario")
    .during(5 minutes) {
      exec(http("getComputers")
        .get("/computers")
        .check(
          status is 200,
          regex("""\d+ computers found"""),
          css("#add", "href").saveAs("addComputer")))

        .exec(http("addNewComputer")
          .get("${addComputer}")
          .check(substring("Add a computer")))

        .exec(_.set("homeComputer", s"homeComputer_${ThreadLocalRandom.current.nextInt(Int.MaxValue)}"))
        .exec(http("postComputers")
          .post("/computers")
          .formParam("name", "${homeComputer}")
          .formParam("introduced", "2015-10-10")
          .formParam("discontinued", "2017-10-10")
          .formParam("company", "")
          .check(substring("${homeComputer}")))
    }

  setUp(computerDbScn.inject(
    constantUsersPerSec(2) during(20 second)
  ).throttle(reachRps(4) in (20 second), holdFor(5 minutes)).protocols(httpProtocol))
}
