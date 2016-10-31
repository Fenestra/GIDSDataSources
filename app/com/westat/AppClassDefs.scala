package com.westat

import play.api.libs.json.{Json, JsValue}

/*
trait PersistentObject {
  def id : Long
  def recordVersion : Int
}

trait ValueObject {
  def fieldIsEmpty(name : String) : Boolean
  def stringValue(key : String) : String
  def setValue(key : String, value : Any) : ValueObject
}

trait PersistentValueObject extends ValueObject with PersistentObject

trait CodeType {
  def displayValue : String
}

trait QnrStatus extends CodeType

case object QNotStarted extends QnrStatus {
  def displayValue : String = { "Not Started" }
}
case object QInProgress extends QnrStatus {
  def displayValue : String = { "In Progress" }
}
case object QSubmitted  extends QnrStatus {
  def displayValue : String = { "Submitted" }
}
//case object QReviewed   extends QnrStatus
//case object QNotStarted extends QnrStatus

trait DataFormat extends CodeType

case object ZipFormat extends DataFormat {
  def displayValue : String = { "zip" }
}
case object RawFormat extends DataFormat {
  def displayValue : String = { "raw" }
}

trait Questionnaire extends PersistentValueObject {
  // common attributes that may have display values also
  def formType : String
  def cfn : String
  def visible : Boolean
  def status : QnrStatus
  def canSubmit : Boolean
  def setCfn(value : String) : Questionnaire

  def asJsonObj : JsValue
  def addSequences(seqName : String, seqKey : String, sourceQnr : Questionnaire) : ResultKind
  def addSeqToExisting(seqName : String, seqKey : String, sourceVal : JsValue) : ResultKind
  def copyQnr() : Questionnaire
  def deleteSequences(seqKey : String, keys : List[String]) : ResultKind
  def seqFieldIsEmpty(seqName : String, seqNum : String, key : String) : Boolean
  def sequenceList(seqName : String) : List[ValueObject]
  def sequenceLength(seqName : String) : Int
  def sequenceContaining(seqName : String, key : String, value : String) : Option[ValueObject]
  def sequenceValue(key : String, seqName : String = "root", seqNum : String = "1") : String
  def setQnrValue(key : String, value : String) : Questionnaire
  def setSequenceValue(key : String, value : String, seqName : String = "root", seqNum : Any = "1") : Questionnaire
  def valueListFor(names : List[String], seqName : String = "root", seqNum : Any = "1") : List[String]
  def setErrorsWarnings(errCt : Int, warnCt : Int)
  def toJson : String
  def afterSubmission : Questionnaire
}

trait QuestionnaireList {
  def asJsonList : String
  def asVisibleJsonList : String 
  def availableForms : List[String]
  def certificationQnr : Questionnaire
  def head : Questionnaire
  def qnr(index : Int) : Questionnaire
  def toList : List[Questionnaire]
  def allLocList : List[Questionnaire]
  def removeQnr(cfn : String) : QuestionnaireList
  def replaceQnr(qnr : Questionnaire) : QuestionnaireList
  def submittedList : List[JsValue]
  def qnrsForForm(ft : String) : List[Questionnaire]
  def visibleList : List[Questionnaire]
  def findQnr(cfn : String) : Questionnaire
  def insert(srcList : List[Questionnaire])
  def length : Int
  def useForms(aList : List[FormDef])
  def insertIntoAllRow(refPeriod : String, manifestID : String, seqName : String, q : Questionnaire) : ResultKind
  def insertIntoSeqRows(refPeriod : String, manifestID : String, form : String, seqName : String, q : Questionnaire) : ResultKind
  def saveAllGridRows(refPeriod : String, manifestID : String, seqName : String) : ResultKind
  def saveSeqGridRows(refPeriod : String, manifestID : String, form : String, seqName : String) : ResultKind
  def test : String
}

case class Sequence(name : String, canAdd : Boolean)

object Sequence {
  implicit val seqReads = Json.reads[Sequence]
  implicit val seqWrites = Json.writes[Sequence]
}


case class FormDef(form : String, name : String, addSeq : String = "", canAdd : Boolean = true,
                   visible : Boolean = false, validSequences : List[Sequence]) {
  def certificationForm : Boolean = {
    !visible && name.toLowerCase.contains("certif")
  }
  def displayName : String = {
    form + " - " + name
  }
  def addableSequenceName : String = {
    var result = ""
    validSequences.foreach(s => if (s.canAdd) result = s.name)
    result
  }
  def addableSequenceDE : String = {
    if (form.contains("MA-10000"))
      addableSequenceName + "[##]PROD_CLASS_CODE"
    else
      addableSequenceName + "[##]RECORD_CFN"
  }
  def sequenceKey(name : String) : String = {
    if (form.contains("MA-10000"))
      name + "[##]PROD_CLASS_CODE"
    else
      name + "[##]RECORD_CFN"
  }
}

object FormDef {
  implicit val formDefReads = Json.reads[FormDef]
  implicit val formDefWrites = Json.writes[FormDef]
}


case class FormType(formID : String, name : String) {
  def toJson : JsValue = {
    Json.toJson( Map("formID" -> formID, "name" -> name)  )
  }
}

case class GridRow(id : Long, cfn : String, seqName : String, seqKey : String, seqID : String)

object GridRow {
  implicit val gridRowReads = Json.reads[GridRow]
  implicit val gridRowWrites = Json.writes[GridRow]
}

case class ColumnDef(formType : String, seqName : String, tabNumber : Int, name : String, cols : List[String])

object ColumnDef {
  implicit val columnDefReads = Json.reads[ColumnDef]
  implicit val columnDefWrites = Json.writes[ColumnDef]
  val COL_STATUS   = "RECORD_STATUS"
  val COL_ERR_WARN = "ERRORS_WARNINGS"
  val COL_FORM     = "FORM_MAILED"
  val COL_CFN      = "RECORD_CFN"
}

trait ColumnManager {
  def tabForField(formType : String, aName : String) : Int
  def toList : List[ColumnDef]
}

case class Change( id : String, colName : String, value : String, seqName : String = "root", seqNum : String = "0")
//case class Change( id : String, colName : String, value : String, original : String = "", seqName : String = "root", seqNum : String = "0")

object Change {
  implicit val changeReads = Json.reads[Change]
  implicit val changeWrites = Json.writes[Change]
}

case class AddableLoc(formtype : String, cfn : String = "", seq : String = "", numLocs : Int = 0)

object AddableLoc {
  implicit val addableLocReads = Json.reads[AddableLoc]
  implicit val addableLocWrites = Json.writes[AddableLoc]
}

case class DeleteInfo(cfn : String, seqKey : String, seqVals : List[String])

object DeleteInfo {
  def readFromString(value : String) : DeleteInfo = {
    var words = value.split(' ')
    val acfn = words.head
    words = words(1).split(':')
    val aKey = words.head
    val sVals = words(1)
    DeleteInfo(acfn, aKey, sVals.split(',').toList)
  }
}
// 7200032014 nc_99003[##]RECORD_CFN:ADD_000022,ADD_000023

case class DeleteLocationsInfo(rowInfo : String, qnrs : String)

object DeleteLocationsInfo {
  implicit val delLocationsInfoReads = Json.reads[DeleteLocationsInfo]
  implicit val delLocationsInfoWrites = Json.writes[DeleteLocationsInfo]
}


case class PageInfo(form: String, seqName : String, start : Int, visiblePage : String, ewPage : String) {
  def toInfoString : String = {
    s"PageInfo(form:$form, seqName:$seqName, start:$start, visiblePage:"+StrUtil.shortString(visiblePage)+", ewPage:"+StrUtil.shortString(ewPage)+")"
  }
}

object PageInfo {
  implicit val pageInfoReads = Json.reads[PageInfo]
  implicit val pageInfoWrites = Json.writes[PageInfo]
}
*/
case object AppConstants {
  val SUCCESS = "success"
}

trait ResultKind {
  def isError : Boolean = false
}
case class SuccessResult(msg : String = AppConstants.SUCCESS) extends ResultKind
case class ErrorResult(msg : String) extends ResultKind {
  override def isError : Boolean = true
}
//case class QnrResult(qnr : Questionnaire) extends ResultKind
case class LongResult(value : Long) extends ResultKind
//case class QnrsResult(list : QuestionnaireList) extends ResultKind
case class AnyResult(value : Any) extends ResultKind


