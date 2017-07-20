package controllers

import javax.inject._
import models._
import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

//case class RefDiv(id : String, description : String)

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class DocController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def documentHome = Action {
   // val refList = List(RefDiv("1", "2012 SURVEY - General"), RefDiv("2", "2012 SURVEY - SSD"), RefDiv("3", "2013 SURVEY - General"))
    Ok(views.html.documentHome("Documents", RefPeriodDivision.refPeriodDivisionsList))
  }
/*
  def renderDocument(id : String) = Action.async {
    // val refList = List(RefDiv("1", "2012 SURVEY - General"), RefDiv("2", "2012 SURVEY - SSD"), RefDiv("3", "2013 SURVEY - General"))
    Ok("SFOReaderviews.html.documentHome(Documents, RefPeriodDivision.refPeriodDivisionsList)")
  }
  *
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

    def loadQuestionnaires = Action.async {
  //    println("loadQuestionnaires")
      Questionnaire.qnrListAsString.map(info =>
        Ok(views.html.index(info))
      )
    }
  */

}
