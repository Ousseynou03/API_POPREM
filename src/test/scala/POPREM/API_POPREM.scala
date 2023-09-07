package poprem
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps



class API_POPREM  extends  Simulation{

  private val  host : String =System.getProperty("urlCible", "https:")
/*  private val  VersionAppli: String  = System.getProperty("VersionApp", "Vxx.xx.xx")
  private val  TpsMonteEnCharge: Int = System.getProperty("tpsMonte", "2").toInt
  private val  DureeMax : Int = System.getProperty("dureeMax", "10").toInt + TpsMonteEnCharge
  private val  TpsPalier: Int = System.getProperty("tpsPalier", (2*TpsMonteEnCharge).toString ).toInt
  private val NbrIter: Int = System.getProperty("nbIter", "300").toInt
  private val  LeCoeff : Int = System.getProperty("coeff", "100").toInt
  private val  nbVu : Int = 1 * LeCoeff*/




  val FichierPath: String = System.getProperty("dataDir", "data/")
  val FichierDataMatricule: String = "matricule.csv"
  val FichierDataTempory: String = "temporality.csv"
  val FichierDataSalesUnitld: String = "salesUnitId.csv"
  val FichierDataorgLevelCode: String = "orgLevelCode.csv"

  val matricule = csv(FichierPath + FichierDataMatricule).circular

  val tempory = csv(FichierPath + FichierDataTempory).circular

  val salesUnitId = csv(FichierPath + FichierDataSalesUnitld).circular

  val orgLevelCode = csv(FichierPath + FichierDataorgLevelCode).circular


  val httpProtocol = http.baseUrl(host)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .userAgentHeader("TESTS-DE-PERF")


  def Autentication() =
  {
    exec(http("POST")
      .post("https://ssotest.interne.galerieslafayette.com/auth/realms/GL-ENTREPRISE/protocol/openid-connect/token")
      //.formParam("client_id", "poprem-pp-front")
      .formParam("username", "p_portapve")
      .formParam("password", "Service12345!")
      .formParam("grant_type", "password")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("access_token")))
  }

  def getparameters(): ChainBuilder = {
      exec(http("Get_parameters")
        .get("/api/parameters")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200)))
    }


  def Get_employees_matricules(): ChainBuilder = {
      exec(http("Get_employees_matriculates")
        .get("/api/employees/${matricule}")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200)))
  }
  def Get_employees_matricules_peers():ChainBuilder  = {
      exec(http("Get_employees_matricules_peers")
        .get("/api/employees/${matricule}/peers")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer #{access_token}")
        .check(status.is(200)))

  }

  def Get_employees_performance(): ChainBuilder = {
    exec(http("Get_employees_today")
      .get("/api/performance/employees/#{matricule}/temporality/#{temporality}")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer #{access_token}")
      .check(status.is(200))
    )
  }

  def Get_employees_salesUnitId(): ChainBuilder = {
      exec(http("Get_employees_salesUnitId")
        .get("/api/performance/sales-units/#{salesUnitId}/temporality/#{temporality}")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200))
      )
  }

  def Get_employees_orgLevelCode(): ChainBuilder = {
    exec(http("Get_employees_orgLevelCode")
      .get("/api/performance/teams/#{orgLevelCode}/temporality/#{temporality}")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer #{access_token}")
      .check(status.is(200)))
  }



  def Get_employees_bonus(): ChainBuilder = {

    exec(http("Get_matricule_employe_bonus")
      .get("/api/bonus/employees/#{matricule}/temporality/#{temporality}")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer #{access_token}")
      .check(status.is(200)))
  }
  def Get_employees_salesUnitId_bonus(): ChainBuilder = {

    exec(http("Get_employees_salesUnitId_bonus")
      .get("/api/bonus/sales-units/#{salesUnitId}/temporality/#{temporality}")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer #{access_token}")
      .check(status.is(200)))
  }

  def Get_employees_orgLevelCode_bonus(): ChainBuilder = {
    exec(http("Get_employees_orgLevelCode_bonus")
      .get("/api/bonus/teams/#{orgLevelCode}/temporality/#{temporality}")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer #{access_token}")
      .check(status.is(200)))
  }




  val scnApiPoprem  = scenario("TEST_PERF_API_POPREM")
    .exec(Autentication())
    .pause(1)
    .feed(matricule)
    .feed(tempory)
    .feed(salesUnitId)
    .feed(orgLevelCode)
    .exec(getparameters())
    .pause(1)
    .exec(Get_employees_matricules())
    .pause(1)
    .exec(Get_employees_matricules_peers())
    .pause(1)
    .exec(Get_employees_performance())
    .pause(1)
    .exec(Get_employees_salesUnitId())
    .pause(1)
    .exec(Get_employees_orgLevelCode())
    .pause(1)
    .exec(Get_employees_bonus())
    .pause(1)
    .exec(Get_employees_salesUnitId_bonus())
    .pause(1)


  setUp(
    scnApiPoprem.inject(atOnceUsers(1))
  )
    .protocols(httpProtocol)

}

