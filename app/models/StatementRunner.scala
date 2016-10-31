package models

/**
 * Created by lee on 2/18/2015.
 */


import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import com.westat._

/*
Selects commonly need to return:
  Int of ID or count or whatever
  Long of ID or count only this, dont need Int
  String list of ids in CSV format
  List[QnrObjs] - lots of these, but could be part of QnrDB


object StatementRunner {
  val CENSUS2016 = "survey2016"
  def currentRefPeriod : String = CENSUS2016
}
*/
case class StatementRunner() {

  trait ExecutionKind

  case object EKInsert extends ExecutionKind
  case object EKUpdate extends ExecutionKind
  case object EKDelete extends ExecutionKind
  case object EKCommand extends ExecutionKind

  case object SelectLong extends ExecutionKind
  case object SelectString extends ExecutionKind

  def executeInsert(anSQL : SimpleSql[Row], defaultErrMsg : String = "") : ResultKind = {
    execStatement(EKInsert, anSQL, defaultErrMsg)
  }
  def executeUpdate(anSQL : SimpleSql[Row], defaultErrMsg : String = "") : ResultKind = {
    execStatement(EKUpdate, anSQL, defaultErrMsg)
  }
  def executeDelete(anSQL : SimpleSql[Row], defaultErrMsg : String = "") : ResultKind = {
    execStatement(EKDelete, anSQL, defaultErrMsg)
  }
  def executeCommand(anSQL : SimpleSql[Row], defaultErrMsg : String = "") : ResultKind = {
    execStatement(EKCommand, anSQL, defaultErrMsg)
  }
  def selectLong(anSQL : SimpleSql[Row], defaultErrMsg : String = "") : ResultKind = {
    execStatement(SelectLong, anSQL, defaultErrMsg)
  }
  def selectString(anSQL : SimpleSql[Row], defaultErrMsg : String = "") : ResultKind = {
    execStatement(SelectString, anSQL, defaultErrMsg)
  }

  def safeRun(f: => ResultKind) : ResultKind = {
    var result : ResultKind = ErrorResult("StatementRunner.safeRun returned failure")
    try {
      result = f
    }
    catch {
      case ex : Exception => {
        var st = ex.getStackTrace.mkString("\n") 
        var ste = ex.getStackTrace.head
        var fn = ste.toString //ste.getClassName + "." + ste.getMethodName + " line:" + ste.getLineNumber
        result = ErrorResult(errorMsg("Database error:", "")+"  "+ex.getMessage)
      }
    }
    result
  }

  private def errorMsg(msg : String, baseMsg : String) = if (baseMsg.nonEmpty) baseMsg else msg

  private def execStatement(ek : ExecutionKind, anSQL : SimpleSql[Row], msgBase : String) : ResultKind = {
    var result = AppConstants.SUCCESS
    try {
      DB.withConnection { implicit connection =>
        ek match {
          case EKInsert => {
            val lOptRes: Option[Long] = anSQL.executeInsert()
            lOptRes match {
              case None => return ErrorResult(errorMsg("Failed to insert.", msgBase))
              case Some(s) => result = s.toString // println(s"successfully inserted $res") //result = s.toString // success and this is the ID
            }
          }
          case EKUpdate => {
            val intRes: Int = anSQL.executeUpdate()
            if (intRes <= 0)
              return ErrorResult(errorMsg("Failed to update. "+anSQL.toString, msgBase))
            else
              result = intRes.toString // success and this in how many recs were updated
          }
          case EKDelete => {
            val delRes: Int = anSQL.executeUpdate()
            if (delRes < 1) return ErrorResult(errorMsg("Failed to delete.", msgBase))
            result = delRes.toString
          }
          case EKCommand => {
            val bRes: Boolean = anSQL.execute
            if (!bRes) return ErrorResult(errorMsg("Command failed.", msgBase))
          }
          case SelectLong => {
            val cnt : Long = anSQL.as(scalar[Long].single)
            if (cnt > 0)
              return LongResult(cnt)
            else
              return ErrorResult(errorMsg("SelectLong failed.", msgBase))
          }
          case SelectString => {
            val strRes : String = anSQL.as(scalar[String].single)
            return SuccessResult(strRes)
          }
        }

      }
    } catch {
      case ex: Exception => {  //println(s"insert exception $ex")
        return ErrorResult(errorMsg("Database error:", msgBase)+"  "+ex.getMessage) }
    }
    SuccessResult(result)
  }

}
