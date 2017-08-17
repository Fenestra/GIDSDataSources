package models

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, FileOutputStream}

import com.westat.sfo.{SFOReader, ElementCounter}
import com.westat.{FileCache, StringUtilities}
import play.api.Logger
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer
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

//-------------
trait CustomContentItem {
  def id : String
  def text : String
  def dataElementName : String
  def children : List[CustomContentItem]
  def appendItem(anItem : CustomContentItem) : CustomContentItem
  def appendResponse(aResp : CustomContentItem) : CustomContentItem
}
/*
trait CustomContentItem {
  def id : String
  def text : String
  def dataElementName : String
  def children : List[CustomContentItem]
  def appendItem(anItem : CustomContentItem) : CustomContentItem
  def appendResponse(aResp : CustomContentItem) : CustomContentItem
}
/
case class CCItemParent(anID : String, itemKind : String, mediaKind : String, aList : List[CustomContentItem]) extends CustomContentItem
case class CCItemResponse(anID : String, dataElementName : String, aList : List[CustomContentItem]) extends CustomContentItem

case class CCQuestionTitle(anID : String, aText : String) extends CustomContentItem
case class CCQuestionWording(anID : String, aText : String) extends CustomContentItem

case class CCItemNumber(anID : String, aText : String) extends CustomContentItem
case class CCItemWording(anID : String, aText : String) extends CustomContentItem
case class CCItemHeaderColumnRef(anID : String, aText : String) extends CustomContentItem
case class CCItemHeaderKeycode(anID : String, aText : String) extends CustomContentItem

case class CCResponseLabel(anID : String, dataElementName : String, aText : String) extends CustomContentItem
case class CCResponseKeycode(anID : String, dataElementName : String, aText : String) extends CustomContentItem
case class CCElectronicResponse(anID : String, dataElementName : String, attributesID : String) extends CustomContentItem
case class CCPaperResponse(anID : String, dataElementName : String, attributesID : String) extends CustomContentItem
*/
//-------------


case class QuestionItem(id : String, title : String, quest_wording : Option[String], item_number : Option[String],
item_wording : Option[String], hdr_column_ref : Option[String], hdr_keycode : Option[String],
responses_id : Option[String], response_label : Option[String], response_instruction : Option[String],
keycode : Option[String], ag_id_paper : Option[String], ag_id_electronic : Option[String],
ag_id_dc : Option[String], de_name : Option[String], instance_offset : Option[Float],
instance_index : Option[Float], question_number : Option[String], ref_period : String)


/*
case class QuestionItem(id : String, title : String, questionWording : String, number : String,
                        itemWording : String, headerColumnRef : String, headerKeycode : String,
                        responseID : String, responseLabel : String, responseInstruction : String,
                        keycode : String, paperAttributeID : String, ElectronicAttributeID : String,
                        dcAttributeID : String, dataElementName : String, instanceOffset : Float,
                        instanceIndex : Float, questionNumber : String, ref_period : String)


result is a list or linkedlist, so perhaps result.id is the questID?
if (Result as ICustomContentBranch).children = emptyList then
begin
questionTitleNode := TCustomContentQuestionTitle.create(Result.id, itemKind, mediaKind, questionTitle);
questionWordingNode := TCustomContentQuestionWording.create(Result.id, itemKind, mediaKind, questionWording);
Result := nodeClass.create(Result.id, itemKind, mediaKind, list([questionTitleNode, questionWordingNode]))
end;

if itemID <> '' then
begin
if not assigned(Result.find(itemID, RELATIVE_INDEX_DEFAULT)) then
begin
itemNumberNode := TCustomContentItemNumber.create(itemID, itemKind, mediaKind, itemNumber);
itemWordingNode := TCustomContentItemWording.create(itemID, itemKind, mediaKind, itemWording);
itemHeaderColumnRefNode := TCustomContentItemHeaderColumnRef.create(itemID, itemKind, mediaKind, itemHeaderColumnRef);
itemHeaderKeycodeNode := TCustomContentItemHeaderKeycode.create(itemID, itemKind, mediaKind, itemHeaderKeycode);
itemNode := TCustomContentItem.create(itemId, itemKind, mediaKind,
list([itemNumberNode, itemWordingNode, itemHeaderColumnRefNode, itemHeaderKeycodeNode]));
Result := (Result as ICreateCustomContentBranch).appendItem(itemNode)
end;

if responseID <> '' then
begin
responseLabelNode := TCustomContentResponseLabel.create(responseID, itemKind, mediaKind, responseDataElementName,
responseLabel);
responseKeycodeNode := TCustomContentResponseKeycode.create(responseID, itemKind, mediaKind, responseDataElementName,
responseKeycode);
if mediaKind = mekElectronic then
responseAreaNode := TCustomContentElectronicResponseArea.createFromElectronicAttributes(responseID, itemKind,
responseDataElementName, responseElectronicAttributesID)
else
responseAreaNode := TCustomContentPaperResponseArea.createFromPaperAttributes(responseID, itemKind,
responseDataElementName, responsePaperAttributesID);
responseNode := TCustomContentResponse.create(responseID, itemKind, mediaKind,
list([responseLabelNode, responseKeycodeNode, responseAreaNode]), responseDataElementName);
Result := (Result as ICreateCustomContentBranch).appendResponse(responseNode)
end

end
*/


object QuestionItem {
  implicit val questionItemReads = Json.reads[QuestionItem]
  implicit val questionItemWrites = Json.writes[QuestionItem]

  def questionItems(questID : String) : List[QuestionItem] = {
    QuiDB().loadQuestionItems(questID)
  }

  private val storedQItemsJson =
    """
      [{"id":"D078FF39-F25C-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"A.","item_wording":"Is your company owned or controlled by another company  ","responses_id":"D078FF39-F25F-91A7-E002-F10EC8809F01","response_label":"Yes","response_instruction":"- <I>Complete lines B and C and return this form with your completed $$00 Economic Census form.</I>","keycode":"0005","ag_id_paper":"35E5C34A-9D1B-406B-AC87-D027F9E05184","ag_id_electronic":"2A291202-6FA4-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-E5B4-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_DOM_OWN_YES","ref_period":"1997 CENSUS"},{"id":"D078FF39-F25C-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"A.","item_wording":"Is your company owned or controlled by another company  ","responses_id":"D078FF39-F25E-91A7-E002-F10EC8809F01","response_label":"No","response_instruction":"- <I>Discard this form (NC-99513) and return your completed $$00 Economic Census form.</I>","keycode":"0006","ag_id_paper":"35E5C34A-9D1B-406B-AC87-D027F9E05184","ag_id_electronic":"2A291202-6FA4-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-E5B2-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_DOM_OWN_NO","ref_period":"1997 CENSUS"},{"id":"D078FF39-F261-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"<B>OR</B>  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F264-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"does your company operate at more than one physical location?  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F267-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"B.","item_wording":"Ownership or control  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F26A-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"1.","item_wording":"Does another company hold more than 50 percent of the voting stock of your company <B>or</B> have the power to control the management and policies of your company?  ","responses_id":"D078FF39-F26D-91A7-E002-F10EC8809F01","response_label":"Yes","response_instruction":"- <I>Enter the following information for the owning or controlling company</I>","keycode":"0008","ag_id_paper":"35E5C34A-9D1B-406B-AC87-D027F9E05184","ag_id_electronic":"2A291202-6FA4-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-EE2A-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_YES","ref_period":"1997 CENSUS"},{"id":"D078FF39-F26A-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"1.","item_wording":"Does another company hold more than 50 percent of the voting stock of your company <B>or</B> have the power to control the management and policies of your company?  ","responses_id":"D078FF39-F26C-91A7-E002-F10EC8809F01","response_label":"No","response_instruction":"- <I>Go to line C</I>","keycode":"0009","ag_id_paper":"35E5C34A-9D1B-406B-AC87-D027F9E05184","ag_id_electronic":"2A291202-6FA4-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-E5B8-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_NO","ref_period":"1997 CENSUS"},{"id":"D078FF39-F26F-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"Enter EIN of owning or controlling company <I>(9 digits)</I> ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F273-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F275-91A7-E002-F10EC8809F01","keycode":"0080","ag_id_paper":"63C0BD88-3281-42AF-9370-6F3131AF5C0C","ag_id_electronic":"2FFA476D-94E4-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-E90A-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_NAME","ref_period":"1997 CENSUS"},{"id":"D078FF39-F273-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F274-91A7-E002-F10EC8809F01","keycode":"0081","ag_id_paper":"EB12A4BE-72F7-48B0-B49A-6CD85597B5F7","ag_id_electronic":"2A291202-6FA3-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-E7D8-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_EIN","ref_period":"1997 CENSUS"},{"id":"D078FF39-F278-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F279-91A7-E002-F10EC8809F01","keycode":"0082","ag_id_paper":"63C0BD88-3281-42AF-9370-6F3131AF5C0C","ag_id_electronic":"2FFA476D-94E4-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-E908-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_ADDR_STREET","ref_period":"1997 CENSUS"},{"id":"D078FF39-F27C-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F27D-91A7-E002-F10EC8809F01","keycode":"0083","ag_id_paper":"2CA684E8-8B03-4279-A924-A22B73EAEFEB","ag_id_electronic":"2FFA476D-94DD-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-EE26-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_ADDR_CITY","ref_period":"1997 CENSUS"},{"id":"D078FF39-F27C-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F27E-91A7-E002-F10EC8809F01","keycode":"0084","ag_id_paper":"A5F37A59-592F-461C-A43A-C94D0F370ACD","ag_id_electronic":"2A291202-6FA8-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-DC60-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_ADDR_ST","ref_period":"1997 CENSUS"},{"id":"D078FF39-F27C-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F27F-91A7-E002-F10EC8809F01","keycode":"0085","ag_id_paper":"7ACC82BA-84E5-481F-AE4F-3062385EA01F","ag_id_electronic":"2A291202-6FA9-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-EE28-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_ADDR_ZIP","ref_period":"1997 CENSUS"},{"id":"D078FF39-F282-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"2.","item_wording":"Percent of voting stock held by owning <B>or</B> controlling company <I>(Mark \"X\" only ONE box.)</I> ","responses_id":"D078FF39-F287-91A7-E002-F10EC8809F01","response_label":"Less than 50%","keycode":"0027","ag_id_paper":"35E5C34A-9D1B-406B-AC87-D027F9E05184","ag_id_electronic":"2A291202-6FA4-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-E5BE-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_STOCK_LT50","ref_period":"1997 CENSUS"},{"id":"D078FF39-F282-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"2.","item_wording":"Percent of voting stock held by owning <B>or</B> controlling company <I>(Mark \"X\" only ONE box.)</I> ","responses_id":"D078FF39-F285-91A7-E002-F10EC8809F01","response_label":"50%","keycode":"0028","ag_id_paper":"35E5C34A-9D1B-406B-AC87-D027F9E05184","ag_id_electronic":"2A291202-6FA4-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-E5BA-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_STOCK_EQ50","ref_period":"1997 CENSUS"},{"id":"D078FF39-F282-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"2.","item_wording":"Percent of voting stock held by owning <B>or</B> controlling company <I>(Mark \"X\" only ONE box.)</I> ","responses_id":"D078FF39-F286-91A7-E002-F10EC8809F01","response_label":"More than 50%","keycode":"0029","ag_id_paper":"35E5C34A-9D1B-406B-AC87-D027F9E05184","ag_id_electronic":"2A291202-6FA4-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-E5BC-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_STOCK_GT50","ref_period":"1997 CENSUS"},{"id":"D078FF39-F289-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"C.","item_wording":"Number of establishments operated at the end of $$00 under the EIN shown in the mailing address or as corrected in <reverse-circle text=\"2\"/> on the first page of the $$00 Economic Census form  ","responses_id":"D078FF39-F28B-91A7-E002-F10EC8809F01","keycode":"0087","ag_id_paper":"71E3C857-D032-4EE0-8DC1-E12343D98F0E","ag_id_electronic":"30215AF2-C8C6-8C51-E040-18ACC76053E2","ag_id_dc":"1CB5B524-E7BA-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_ESTAB","ref_period":"1997 CENSUS"},{"id":"D078FF39-F28E-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"60","item_wording":"  <B><I>If more than one establishment:</I></B>","ref_period":"1997 CENSUS"},{"id":"D078FF39-F291-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"70","item_wording":"  <I>Provide the physical location address and other information requested on the back of this form for each location.</I>","ref_period":"1997 CENSUS"},{"id":"D078FF39-F294-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"80","item_wording":"  <I>Provide the headquarter's location first, followed by all other locations.</I>","ref_period":"1997 CENSUS"},{"id":"D078FF39-F297-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"90","item_wording":"  <I>Data for establishments operated during $$00, but not in operation at the end of the year, should be included with the headquarter's location.</I>","ref_period":"1997 CENSUS"},{"id":"D078FF39-F29A-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  <I>The sum of all sales, shipments, receipts, or revenue and employment and payroll for all locations should equal the amounts reported in <reverse-circle text=\"4\"/> and <reverse-circle text=\"6\"/> of the $$00 Economic Census form.</I>","ref_period":"1997 CENSUS"},{"id":"D078FF39-F29D-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  <I>For employees that worked at more than one location, report the employment and payroll data for the employees at the ONE location where they spent most of their working time.</I>","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2A0-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_number":"C.","item_wording":"Number of establishments operated at the end of $$00 under the EIN shown in the mailing address or as corrected in <reverse-circle text=\"2\"/> on the first page of the $$00 Economic Census form - Continued  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2A3-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  <I><B>BEFORE YOU BEGIN:</B>  If this EIN had more than 3 physical locations at the end of $$00, copy this page and provide the requested data for all of your locations.</I>","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2A6-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F2A7-91A7-E002-F10EC8809F01","keycode":"0088","ag_id_paper":"63C0BD88-3281-42AF-9370-6F3131AF5C0C","ag_id_electronic":"2FFA476D-94E4-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-E906-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_NAME","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2AA-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F2AB-91A7-E002-F10EC8809F01","keycode":"0089","ag_id_paper":"63C0BD88-3281-42AF-9370-6F3131AF5C0C","ag_id_electronic":"2FFA476D-94E4-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-E904-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_ADDR_STREET","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2AE-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F2AF-91A7-E002-F10EC8809F01","keycode":"0090","ag_id_paper":"2CA684E8-8B03-4279-A924-A22B73EAEFEB","ag_id_electronic":"2FFA476D-94DD-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-E8E4-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_ADDR_CITY","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2B2-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F2B3-91A7-E002-F10EC8809F01","keycode":"0091","ag_id_paper":"A5F37A59-592F-461C-A43A-C94D0F370ACD","ag_id_electronic":"2A291202-6FA8-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-DC5E-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_ADDR_ST","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2B2-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F2B4-91A7-E002-F10EC8809F01","keycode":"0092","ag_id_paper":"7ACC82BA-84E5-481F-AE4F-3062385EA01F","ag_id_electronic":"2A291202-6FA9-0417-E040-18ACC96076A7","ag_id_dc":"1CB5B524-DC6E-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_ADDR_ZIP","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2B7-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F2B8-91A7-E002-F10EC8809F01","keycode":"0703","ag_id_paper":"3C93B4E7-D0F5-4376-95F6-5CB319AAA7AF","ag_id_electronic":"2FFA476D-94E3-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-E958-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_NAICS_SELFDSG_WRT","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2BB-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"Sales, shipments, receipts, or revenue  ","responses_id":"D078FF39-F2BD-91A7-E002-F10EC8809F01","keycode":"0093","ag_id_paper":"07220218-D5F8-491E-AEDB-30B8C90A8533","ag_id_electronic":"2FE0C9D9-3F8C-0CEC-E040-18ACC5605351","ag_id_dc":"1CB5B524-E12C-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_RCPT_TOT","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2C0-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"Number of paid employees for pay period including March 12  ","responses_id":"D078FF39-F2C3-91A7-E002-F10EC8809F01","keycode":"0094","ag_id_paper":"71E3C857-D032-4EE0-8DC1-E12343D98F0E","ag_id_electronic":"2FE0C9D9-3F85-0CEC-E040-18ACC5605351","ag_id_dc":"1CB5B524-E7AE-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_EMP_MAR12","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2C6-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"First quarter payroll <I>(Jan-Mar, $$00)</I> ","responses_id":"D078FF39-F2CA-91A7-E002-F10EC8809F01","keycode":"0096","ag_id_paper":"07220218-D5F8-491E-AEDB-30B8C90A8533","ag_id_electronic":"2FE0C9D9-3F8C-0CEC-E040-18ACC5605351","ag_id_dc":"1CB5B524-E12A-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_PAY_QTR1","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2CD-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"Annual payroll  ","responses_id":"D078FF39-F2CF-91A7-E002-F10EC8809F01","keycode":"0098","ag_id_paper":"07220218-D5F8-491E-AEDB-30B8C90A8533","ag_id_electronic":"2FE0C9D9-3F8C-0CEC-E040-18ACC5605351","ag_id_dc":"1CB5B524-EE2C-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_PAY_ANN","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2D3-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","responses_id":"D078FF39-F2D4-91A7-E002-F10EC8809F01","keycode":"0086","ag_id_paper":"63C0BD88-3281-42AF-9370-6F3131AF5C0C","ag_id_electronic":"2FFA476D-94E4-B36C-E040-18ACC96034F5","ag_id_dc":"1CB5B524-EEF4-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_ADDR_MUN","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2D7-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2DA-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2DD-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2E0-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2E3-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":" <I>(Jan-Mar, $$00)</I> ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2E6-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2E9-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2EC-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2EF-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2F2-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2F5-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2F8-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2FB-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F2FE-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F301-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"D078FF39-F304-91A7-E002-F10EC8809F01","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"014592F3-C945-2E03-E012-186ACB8075B0","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"014592F3-C949-2E03-E012-186ACB8075B0","title":"OWNERSHIP OR CONTROL","item_wording":"  ","ref_period":"1997 CENSUS"},{"id":"24F4CCB1-7A07-2231-E040-18ACC9603315","title":"OWNERSHIP OR CONTROL","responses_id":"24F4CCB1-7A0B-2231-E040-18ACC9603315","ag_id_paper":"22756041-82EE-E285-E040-18ACC7601C3A","ag_id_electronic":"22756041-82EF-E285-E040-18ACC7601C3A","ag_id_dc":"1CB5B524-DDA2-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER","ref_period":"1997 CENSUS"},{"id":"24F4CCB1-7A0C-2231-E040-18ACC9603315","title":"OWNERSHIP OR CONTROL","responses_id":"24F4CCB1-7A10-2231-E040-18ACC9603315","ag_id_paper":"22756041-82EE-E285-E040-18ACC7601C3A","ag_id_electronic":"22756041-82EF-E285-E040-18ACC7601C3A","ag_id_dc":"1CB5B524-DDA4-73B5-E040-18ACC5601885","de_name":"AFFIL_OWNER_STOCK","ref_period":"1997 CENSUS"},{"id":"24F4CCB1-7A11-2231-E040-18ACC9603315","title":"OWNERSHIP OR CONTROL","responses_id":"24F4CCB1-7A15-2231-E040-18ACC9603315","ag_id_paper":"22756041-82EE-E285-E040-18ACC7601C3A","ag_id_electronic":"22756041-82EF-E285-E040-18ACC7601C3A","ag_id_dc":"1CB5B524-DDA0-73B5-E040-18ACC5601885","de_name":"AFFIL_SPL_DOM_OWN","ref_period":"1997 CENSUS"}]
    """

  def questionItemsTestList : List[QuestionItem] = {
    Json.parse(storedQItemsJson).as[List[QuestionItem]]
  }

  def questionItemsAsJson(questID : String) : Future[String] = {
    Future(Json.toJson(questionItems(questID)).toString)
  }

  def createItem(title : String, quest_wording : Option[String], item_id : String, item_number : Option[String],
                          item_wording : Option[String], hdr_column_ref : Option[String], hdr_keycode : Option[String],
                          responses_id : Option[String], response_label : Option[String], response_instruction : Option[String],
                          keycode : Option[String], ag_id_paper : Option[String], ag_id_electronic : Option[String],
                          ag_id_dc : Option[String], de_name : Option[String], instance_offset : Option[Float],
                          instance_index : Option[Float], question_number : Option[String], ref_period : String) : QuestionItem = {
    // should adjust the values accordingly and then create new object with multiple children
/*
    QuestionItem(item_id, title, quest_wording.getOrElse(""), item_number.getOrElse(""), item_wording.getOrElse(""),
      hdr_column_ref.getOrElse(""), hdr_keycode.getOrElse(""), responses_id.getOrElse(""), response_label.getOrElse(""),
      response_instruction.getOrElse(""), keycode.getOrElse("§"), ag_id_paper.getOrElse(""), ag_id_electronic.getOrElse(""),
      ag_id_dc.getOrElse(""), de_name.getOrElse(""), instance_offset.getOrElse(0), instance_index.getOrElse(0),
      question_number.getOrElse(""), ref_period)
*/
    QuestionItem(item_id, title, quest_wording, item_number, item_wording,
      hdr_column_ref, hdr_keycode, responses_id, response_label,
      response_instruction, keycode, ag_id_paper, ag_id_electronic,
      ag_id_dc, de_name, instance_offset, instance_index,
      question_number, ref_period)
  }

}

case class BlockRef(item_type : String, header_reb_id : String)

object BlockRef {
  implicit val blockRefReads = Json.reads[BlockRef]
  implicit val blockRefWrites = Json.writes[BlockRef]

  def blockRefList(questID : String) : List[BlockRef] = {
    QuiDB().loadQItemBlockRefs(questID)
  }

  def blockRefsAsJson(questID : String) : Future[String] = {
    Future(Json.toJson(blockRefList(questID)).toString)
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

// array of refPeriod:"2002 CENSUS", division:"General"
case class RefPeriodDivision(refPeriod : String, division : String) {
  def value : String = {
    refPeriod + " - " + division
  }
}

object RefPeriodDivision {
  implicit val refPeriodDivReads = Json.reads[RefPeriodDivision]
  implicit val refPeriodDivWrites = Json.writes[RefPeriodDivision]

  def refPeriodDivisionsList : List[RefPeriodDivision] = {
    QuiDB().loadRefPeriodDivisions
  }

  def refPeriodDivisionsAsString : Future[String] = {
    Future(Json.toJson(refPeriodDivisionsList).toString)
  }
}


case class Document(id : String, docType : String, docNumber : String, description : String)

object Document {
  implicit val documentReads = Json.reads[Document]
  implicit val documentWrites = Json.writes[Document]
  private val docCache = FileCache(".", "sfo")
  private val counter = ElementCounter()

  def byRefDiv(refPeriod : String, division : String) : List[Document] = {
    QuiDB().docsByRefDiv(refPeriod, division)
  }

  def byRefDivAsString(refdiv : String) : Future[String] = {
    val arr = refdiv.split(" - ")
    val refPeriod = arr(0)
    val division = arr(1)
    println(s"byRefDivAsString ref:$refPeriod< div:$division<")
    Future(Json.toJson(byRefDiv(refPeriod, division)).toString)
  }

  def sfo(id : String) : String = {
   println(s"sfo for $id")

    val result = QuiDB().sfoById(id)
    if (result == null)
      return "no sfo was found"
//    println("sfo size is "+result.length) //+" "+StringUtilities.shortString(result))
    StringUtilities.decodeAndExpandByteArray( result )
  }

  def renderSfo(id : String) : Future[List[String]] = {
    val sfoText = sfo(id)
// this is from when it we cached SFO
//    if (sfoText.length < 100)
//      return Future(sfoText)

    val reader = SFOReader(sfoText)
    reader.readAll
    println(reader.readChildren(counter))
    reader.writeSVGs(id)
  }
  /*
    make another html paqe with list of refs and divisions
    list of docs
    below docs, button to show doclayout

    select doc_id, ref_period, document_type, document_number, division, description
    from documents
      where ref_period="2013 SURVEY" and division = "SSSD"
    list of refperiods and divisions used in documents table
    select ref_period, division from documents group by ref_period, division

    select doc_layout -- or designer_sfo or encapsulated_sfo
    from document_layouts
      where doc_id = "58336C13-B518-289F-E040-18ACC560471D"
      */
}
