package models

import play.api.Logger
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by lee on 10/7/16.
  */
case class Customer(firstName : String, lastName : String, date : String, recVersion : Int) {
  def update(aFirst : String, aLast : String, aDate : String, aRecordVer : Int) : Customer = {
     if (aRecordVer != recVersion) {
       Logger.info("Customer recVersion has been updated by someone else")
       return this
     }
     Customer(aFirst, aLast, aDate, recVersion+1)  
  }

  def customerList : Future[String] = {
     Future(QuiDB().customerList)
  }


}

case class Questionnaire(id : String, refPeriod : String, formNumber : String, formDisplayNumber : Option[String], title : String, subTitle : Option[String], status : String) {
  def displayString : String = {
    "%s %10s %s %10s %.15s %10s %10s".format(id, refPeriod, formNumber,
      formDisplayNumber.getOrElse(""), title, subTitle.getOrElse(""), status)
  }
}

object Questionnaire {
  implicit val qnrReads = Json.reads[Questionnaire]
  implicit val qnrWrites = Json.writes[Questionnaire]

  def qnrList : List[Questionnaire] = {
    QuiDB().loadQuestionnaires
  }

  def qnrListAsString : Future[String] = {
    Future(qnrList.map(q => q.displayString).mkString("\n"))
  }

  def qnrListAsJson : Future[String] = {
    Future(Json.toJson(qnrList).toString())
  }

  def getQnr(id : String) : Future[String] = {
    Future(qnrList.find(q => q.id == id) match {
      case Some(q : Questionnaire) => Json.toJson(q).toString()
      case None  => s"""{error:"qnr ($id) not found"} """
    })
  }
/*
  def surveyList : List[String] = {
    QuiDB().loadSurveys
  }
*/
  def surveyListAsString : Future[String] = {
    Future(Json.toJson(QuiDB().loadSurveys).toString)
  }

  def qnrListForSurveyRef(survey : String, refPeriod : String) : Future[String] = {
    Future(QuiDB().loadQnrsForSurveyRef(survey, refPeriod).map(q => Json.toJson(q)).toString())
  }
}

case class QnrQuestion(id : String, category : String, version : Option[Float], title : Option[String]) {
  def displayString : String = {
    "%.35s %2.1f %.25s %s".format(category, version.getOrElse(-1.0), title.getOrElse("notitle"), id)
  }
}

object QnrQuestion {
  implicit val qnrQuestionReads = Json.reads[QnrQuestion]
  implicit val qnrQuestionWrites = Json.writes[QnrQuestion]

  def questionList(qnrID : String) : List[QnrQuestion] = {
    QuiDB().loadQuestionsForQnr(qnrID)
  }

  def qnrQuestionsAsString(qnrID : String) : Future[String] = {
    Future(questionList(qnrID).map(q => Json.toJson(q).toString()+"\n"+q.displayString).mkString("\n"))
  }

  def qnrQuestionsAsJson(qnrID : String) : Future[String] = {
    Future(Json.toJson(questionList(qnrID)).toString)
  }
}

case class FdpInfo(id : String, refPeriod : String, formNumber : String) {
  def displayString : String = {
    "%s %10s %s".format(id, refPeriod, formNumber)
  }
}

object FdpInfo {
  implicit val fdpInfoReads = Json.reads[FdpInfo]
  implicit val fdpInfoWrites = Json.writes[FdpInfo]

  def fdpList : List[FdpInfo] = {
    QuiDB().loadFdpList
  }

  def fdpListAsString : Future[String] = {
    Future(Json.toJson(fdpList).toString())
  }
}

case class FdpDetail(location_add_class : Option[String], industry_class : Option[String], services_class : String,
                     ald_id : String, cert_qnr_id : String, display_blank_ein : String, display_blank_storenum : String)

object FdpDetail {
  implicit val fdpDetailReads = Json.reads[FdpDetail]
  implicit val fdpDetailWrites = Json.writes[FdpDetail]

  def loadMetadata(id : String) : Future[String] = {
    Future(Json.toJson(QuiDB().loadFdpDetail(id)).toString)
  }
}

case class QuestionTitle(id : String, categoryContentID : String, version : Option[Float],
      headerID : String, status : String, questLayout : String, title : Option[String], questWording : String)

object QuestionTitle {
  implicit val qTitleReads = Json.reads[QuestionTitle]
  implicit val qTitleWrites = Json.writes[QuestionTitle]
}

case class RefPeriod(refPeriod : String, survey : String, year : String)

object RefPeriod {
  implicit val refPeriodReads = Json.reads[RefPeriod]
  implicit val refPeriodWrites = Json.writes[RefPeriod]

  def RefPeriodWYear(refPeriod : String, survey : String) : RefPeriod = {
    val year = refPeriod.substring(0, 4)
    RefPeriod(refPeriod, survey, year)
  }

  def refPeriodList : List[RefPeriod] = {
    QuiDB().loadRefPeriods
  }

  def refPeriodsAsString : Future[String] = {
    Future(Json.toJson(refPeriodList).toString)
    //    Future(refPeriods.mkString("\n"))
  }

}
