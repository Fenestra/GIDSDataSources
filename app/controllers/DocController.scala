package controllers

import javax.inject._
import models._
import play.api.libs.json
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


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

  def showDocument(id : String) = Action.async {
    Document.renderSfo(id).map(info => Ok(views.html.svg("Documents", info)))
  }

  def documentsByRefDiv(refdiv : String) = Action.async {
    // array of id:123..., refPeriod:"2002 CENSUS", division:"General"
    Document.byRefDivAsString(refdiv).map(info => Ok(info))
  }

  def svgImage(id : String) = Action.async {
    Document.svgForID(id).map(info => Ok(info))
  }

  def pdfImage(id : String) = Action.async {
    Document.pdfForID(id).map(res => if (res.pdf != null)
      Ok(res.pdf).as("application/pdf")
    else
      Ok(res.svg).as("image/svg+xml")
    )
  }

  def pageBuilder = Action {
    Ok(views.html.pageBuilder("Page Builder"))
  }

  //  receives layout objects in a JSON list, saves them, and returns id of the associated pdf
  def renderLayout = Action.async { implicit request =>
    val name = "testjson"

    val req = request.body.asJson
    req match {
      case Some(json) =>
        println(s"renderLayout got json of: ${json}")
//        println(QuiDB().saveGuiLayout(name, json.toString()))

        var theList : List[WPage] = null
        json.validate[List[WPage]].map(ch => theList = ch).getOrElse( println(s"problem validating to t") )
//        println(s"thelist is $theList")
        QuickLayouts.renderLayoutFromWebObjs(name, theList).map(info => Ok(info).as("application/pdf"))
      case None => Future(Ok("Failed to render the layout for "+name))
    }
  }
/*
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
