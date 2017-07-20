package controllers

import javax.inject._
import com.westat.CustomContent
import com.westat.sfo.SFOReader
import models._
import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's API.
 */
@Singleton
class DSController @Inject() extends Controller {

  def loadQuestionnaires = Action.async {
//    println("loadQuestionnaires")
    Questionnaire.qnrListAsJson.map(info =>
      Ok(info)
    )
  }

  def loadQuestionnaire(id : String) = Action.async {
//    println(s"loadQuestionnaire for $id")
    Questionnaire.getQnr(id).map(info =>
      Ok(info)
    )
  }

  def loadQnrQuestions(id : String) = Action.async {
//    println(s"loadQnrQuestions for $id")
    QnrQuestion.qnrQuestionsAsJson(id).map(info =>
      Ok(info)
    )
  }

  def loadQuestionItems(id : String) = Action.async {
        println(s"loadQuestionItems for $id")
    CustomContent.loadTest
    QuestionItem.questionItemsAsJson(id).map(info =>
      Ok(info)
    )
  }

  def loadFdps = Action.async {
//    println("loadFdps")
    FdpInfo.fdpListAsString.map(info =>
      Ok(info)
    )
  }

  def fdpMetadata(id: String) = Action.async {
//    println(s"fdpMetadata for $id")
    FdpDetail.loadMetadata(id).map(info =>
      Ok(info)
    )
  }

  def surveyList = Action.async {
    //array of survey strings
    Questionnaire.surveyListAsString.map(info => Ok(info))
  }

  def refPeriodList = Action.async {
    // array of name:"2002CENSUS", survey:"ECON", year:"2002"
    RefPeriod.refPeriodsAsString.map(info => Ok(info))
  }

  def qnrSubList(survey : String, refPeriod : String) = Action.async {
    //    println("loadQuestionnaires")
    Questionnaire.qnrListForSurveyRef(survey, refPeriod).map(info =>
      Ok(info)
    )
  }

  def refPeriodDivisions = Action.async {
    // array of id:123..., refPeriod:"2002 CENSUS", division:"General"
    RefPeriodDivision.refPeriodDivisionsAsString.map(info => Ok(info))
  }

  def documentsByRefDiv(refdiv : String) = Action.async {
    // array of id:123..., refPeriod:"2002 CENSUS", division:"General"
    Document.byRefDivAsString(refdiv).map(info => Ok(info))
  }

  def renderDocument(id : String) = Action.async {
//    Document.renderSfo(id).map(info => Ok(views.html.svg("/gidsapi/assets/images/"+info)))
    Document.renderSfo(id).map(info => Ok("/gidsapi/assets/images/"+info))
 //   SFOReader(Document.sfo(id)).readAll.map(info => Ok(info))
  }

}
