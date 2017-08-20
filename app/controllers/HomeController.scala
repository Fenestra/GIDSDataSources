package controllers

import java.io.{InputStreamReader, BufferedReader, IOException}
import javax.inject._
import models._
import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Welcome to the DataSources application."))
  }

  def test = Action.async {
    var cust = Customer("Lee", "hey", "9/28/2016", 1)
    var cust2 = cust.copy()
    cust2 = cust2.update(cust2.firstName, "Meininger", cust2.date, cust2.recVersion)
    var custUnder = cust.update(cust.firstName, "Nothing", cust.date, cust2.recVersion)
    cust = cust.update(cust.firstName, "Something", cust.date, cust2.recVersion)
    cust.customerList.map(info =>
    Ok(views.html.index("Cust is updated to Something "+cust+"\nCustUnder is updated to Nothing "
      +custUnder+"\nCust2 is updated to Meininger (this is the only one that should be changed) "+cust2+"\n" + info))
    )
  }

  def echo(text : String) = Action {
    println("Controller.echo called "+text)
    var result = text
    /*
    try {
   //     Runtime.getRuntime().exec("HostProcessor " + text)
   //   Runtime.getRuntime().exec("c:/devtools/fop/trunk/fop.bat "+command+" "+parameter);
      val data = "eJxzZGBgmMDIwMAGpIMZHBl8GQIYfBhcGXiB/NC8zLLUomIFnxAFU9Nnz54x2DAgAXYgVnY1A0EAggELZw=="
      val pb = new ProcessBuilder("RotatedTextExe", data, text, "ignore me")
      pb.redirectErrorStream(true)
      val p = pb.start()
      p.waitFor()
      val br = new BufferedReader(new InputStreamReader(p.getInputStream))
      var line = ""
      val sb = new StringBuilder
      while ( line != null) {
        sb.append(line+"\n")
        line = br.readLine()
      }
      result = sb.toString
    }
    catch {
      case ex : Exception =>
         println("Error calling HostProcessor.exe "+ ex.getMessage)
    }
    */
    Ok(views.html.index(result))
  }

  def loadQuestionnaires = Action.async {
//    println("loadQuestionnaires")
    Questionnaire.qnrListAsString.map(info =>
      Ok(views.html.index(info))
    )
  }

  def loadQuestionnaire(id : String) = Action.async {
//    println(s"loadQuestionnaire for $id")
    Questionnaire.getQnr(id).map(info =>
      Ok(views.html.index(info))
    )
  }

  def loadQnrQuestions(id : String) = Action.async {
//    println(s"loadQnrQuestions for $id")
    QnrQuestion.qnrQuestionsAsString(id).map(info =>
      Ok(views.html.index(info))
    )
  }

  def loadFdps = Action.async {
//    println("loadFdps")
    FdpInfo.fdpListAsString.map(info =>
      Ok(views.html.index(info))
    )
  }

  def fdpMetadata(id: String) = Action.async {
//    println(s"fdpMetadata for $id")
    FdpDetail.loadMetadata(id).map(info =>
      Ok(views.html.index(info))
    )
  }

}
