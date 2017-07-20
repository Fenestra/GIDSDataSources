package models

/**
 * Created by lee on 2/18/2015.
 */


import play.api.Logger
import play.api.db._
import play.api.Play.current
import play.api.libs.json.Json
import scala.collection.mutable.ListBuffer
import anorm._
import anorm.SqlParser._
import com.westat._


case class QuiDB() {
  private val dbRunner = StatementRunner()

  def customerList : String = {
    var result = new ListBuffer[String]
/*
    val statement =
      """
        select id, refPeriod, manifestID, form, qnr_id, lastUpdate
        from gridRows;
      """
*/
    val statement =
      """
        select category as info
        from categories;
      """
    DB.withConnection { implicit connection =>
      val objs : List[String] = {
        SQL(statement).as( str("info") *)
      }
      result.insertAll(0, objs)
    }
    return result.mkString("\r\n")
  }


  def loadQuestionnaires : List[Questionnaire] = {
    var result = List[Questionnaire]()
    val statement =
      """
        select QNR_id, REF_PERIOD, FORM_NUMBER, FORM_DISPLAY_NUMBER, TITLE, SUB_TITLE, STATUS
        from questionnaires order by REF_PERIOD, FORM_NUMBER
      """
    DB.withConnection { implicit connection =>
      val objs : List[Questionnaire] = {
        SQL(statement).as( qnrListParser *)
      }
      result = objs
    }
    result
  }

  def loadQnrsForSurveyRef(survey : String, refPeriod: String) : List[Questionnaire] = {
    var result = List[Questionnaire]()
    val statement =
      """
        select QNR_id, REF_PERIOD, FORM_NUMBER, FORM_DISPLAY_NUMBER, TITLE, SUB_TITLE, STATUS
        from questionnaires
        where ref_period = {refPeriod} and survey = {survey}
        order by FORM_NUMBER
      """
    DB.withConnection { implicit connection =>
      val objs : List[Questionnaire] = {
        SQL(statement).on('survey -> survey, 'refPeriod -> refPeriod).as( qnrListParser *)
      }
      result = objs
    }
    result
  }

  val qnrListParser = {
    get[String]("qnr_id") ~
    get[String] ("ref_period") ~
    get[String] ("form_number") ~
    get[Option[String]] ("form_display_number") ~
    get[String] ("title") ~
    get[Option[String]] ("sub_title") ~
    get[String] ("status") map {
      case qnr_id~ref_period~form_number~form_display_number~title~sub_title~status =>
        Questionnaire(qnr_id, ref_period, form_number, form_display_number, title, sub_title, status)
    }
  }

  def loadFdpList : List[FdpInfo] = {
    var result = List[FdpInfo]()
    val statement =
      """
        select fdp_id, ref_period, fdp_form_number
        from electronic_form_definitions order by ref_period, fdp_form_number
      """
    DB.withConnection { implicit connection =>
      val objs : List[FdpInfo] = {
        SQL(statement).as( fdpInfoParser *)
      }
      result = objs
    }
    result
  }

  val fdpInfoParser = {
    get[String]("fdp_id") ~
      get[String] ("ref_period") ~
      get[String] ("fdp_form_number") map {
      case fdp_id~ref_period~fdp_form_number => FdpInfo(fdp_id, ref_period, fdp_form_number)
    }
  }

/*
      result := TDataPipe.PerformSelectAsString(SessionID, format(
      'select qtqq.title ' +
      '     , qtqq.quest_wording ' +
      '     , cci.item_id ' +
      '     , cci.item_number ' +
      '     , cci.item_wording ' +
      '     , cci.hdr_column_ref ' +
      '     , cci.hdr_keycode ' +
      '     , cr.responses_id ' +
      '     , cr.response_label ' +
      '     , cr.response_instruction ' +
      '     , cr.keycode ' +
      '     , cr.ag_id_paper ' +
      '     , cr.ag_id_electronic ' +
      '     , cr.ag_id_dc ' +
      '     , de.de_name ' +
      '     , qtqq.instance_offset ' +
      '     , qtqq.instance_index ' +
      '     , qtqq.question_number ' +
      '     , qtqq.ref_period ' +
      '  from ( select distinct ' +
      '                qt.quest_id ' +
      '              , qt.title ' +
      '              , qt.quest_wording ' +
      '              , qt.ref_period ' +
      '              , qt.instance_offset ' +
      '              , qt.instance_index ' +
      '              , qq.question_number ' +
      '           from question_title qt ' +
      '                  left outer join questionnaire_questions qq on (qt.quest_id = qq.quest_id) ' +
      '          where qt.quest_id = %s ' +
      '       ) qtqq ' +
      '         left outer join custom_content_items cci on (qtqq.quest_id = cci.quest_id) ' +
      '           left outer join custom_responses cr on (cci.item_id = cr.item_id) ' +
      '             left outer join data_elements de on (cr.de_id = de.de_id) ' +
      'order by cci.item_sequence ' +
      '       , cr.occurrence_sequence ', [quotedStr(anID)]), anID)


   */

  def loadQuestionItems(questID : String) : List[QuestionItem] = {
    var result = List[QuestionItem] ()
    val statement =
      """
      select qtqq.title, qtqq.quest_wording, cci.item_id, cci.item_number, cci.item_wording,
       cci.hdr_column_ref, cci.hdr_keycode, cr.responses_id, cr.response_label, cr.response_instruction,
       cr.keycode, cr.ag_id_paper, cr.ag_id_electronic, cr.ag_id_dc, de.de_name, qtqq.instance_offset,
       qtqq.instance_index, qtqq.question_number, qtqq.ref_period
         from ( select distinct qt.quest_id, qt.title, qt.quest_wording, qt.ref_period, qt.instance_offset,
            qt.instance_index, qq.question_number
            from question_title qt left outer join questionnaire_questions qq on (qt.quest_id = qq.quest_id)
                  where qt.quest_id = {questID} ) qtqq
               left outer join custom_content_items cci on (qtqq.quest_id = cci.quest_id)
               left outer join custom_responses cr on (cci.item_id = cr.item_id)
               left outer join data_elements de on (cr.de_id = de.de_id)
         order by cci.item_sequence , cr.occurrence_sequence
      """
    DB.withConnection { implicit connection =>
      val objs : List[QuestionItem] = {
        SQL(statement).on('questID -> questID).as( questItemParser *)
      }
      result = objs
    }
    result
  }

  val questItemParser = {
      get[String] ("title") ~
      get[Option[String]] ("quest_wording") ~
      get[String]("item_id") ~
      get[Option[String]] ("item_number") ~
      get[Option[String]] ("item_wording") ~
      get[Option[String]] ("hdr_column_ref") ~
      get[Option[String]] ("hdr_keycode") ~
      get[Option[String]] ("responses_id") ~
      get[Option[String]] ("response_label") ~
      get[Option[String]] ("response_instruction") ~
      get[Option[String]] ("keycode") ~
      get[Option[String]] ("ag_id_paper") ~
      get[Option[String]] ("ag_id_electronic") ~
      get[Option[String]] ("ag_id_dc") ~
      get[Option[String]] ("de_name") ~
      get[Option[Float]] ("instance_offset") ~
      get[Option[Float]] ("instance_index") ~
      get[Option[String]] ("question_number") ~
      get[String] ("ref_period") map {
      case title~quest_wording~item_id~item_number~item_wording~hdr_column_ref~hdr_keycode~responses_id~response_label~
        response_instruction~keycode~ag_id_paper~ag_id_electronic~ag_id_dc~de_name~instance_offset~instance_index~
        question_number~ref_period =>
        QuestionItem.createItem(title, quest_wording, item_id, item_number, item_wording, hdr_column_ref, hdr_keycode, responses_id,
          response_label, response_instruction, keycode, ag_id_paper, ag_id_electronic, ag_id_dc, de_name, instance_offset,
          instance_index, question_number, ref_period)
    }
  }

  def loadQItemBlockRefs(questID : String) : List[BlockRef] = {
    var result = List[BlockRef]()
    val statement =
      """
        SELECT   ITEM_TYPE, HEADER_REB_ID
        FROM     CUSTOM_HEADER_REB_ASSN
        WHERE    QUEST_ID = {questID}
      """
    DB.withConnection { implicit connection =>
      val objs : List[BlockRef] = {
        SQL(statement).on('questID -> questID).as( blockRefParser *)
      }
      result = objs.tail
    }
    result
  }

  val blockRefParser = {
    get[String]("item_type") ~
    get[String]("header_reb_id") map {
      case item_type~header_reb_id => BlockRef(item_type, header_reb_id)
    }
  }

  def loadQuestionsForQnr(qnrID : String) : List[QnrQuestion] = {
    var result = List[QnrQuestion]()
    val statement =
      """
        select c.category, qt.version, qt.title, qt.quest_id
        from questionnaire_questions qq, question_title qt, CATEGORY_CONTENT cc, CATEGORIES c
        where qq.qnr_id = {qnrID} and qt.quest_id = qq.quest_id
        and cc.category_content_id = qt.category_content_id AND c.category_id = cc.category_id
        order by qq.quest_sequence
      """
    DB.withConnection { implicit connection =>
      val objs : List[QnrQuestion] = {
        SQL(statement).on('qnrID -> qnrID).as( qQnrParser *)
      }
      result = objs
    }
    result
  }

  val qQnrParser = {
    get[String]("category") ~
      get[Option[Float]] ("version") ~
      get[Option[String]] ("title") ~
      get[String] ("quest_id") map {
      case category~version~title~quest_id => QnrQuestion(quest_id, category, version, title)
    }
  }

  // so edit question is actually question_Title table and looking for quest_id
  def loadQuestionTitle(questID : String) : Option[QuestionTitle] = {
    val statement = "select * from question_title where quest_id = {questID}"
    DB.withConnection { implicit connection =>
      val obj : QuestionTitle = {
        SQL(statement).on('questID -> questID).as( qTitleParser single)
      }
      return Some(obj)
    }
    None
  }


  val qTitleParser = {
    get[String] ("quest_id") ~
    get[String] ("category_content_id") ~
    get[Option[Float]] ("version") ~
    get[String] ("header_id") ~
    get[String] ("status") ~
    get[String] ("quest_layout") ~
    get[Option[String]] ("title") ~
    get[String] ("quest_wording") map {
    case quest_id~category_content_id~version~header_id~status~quest_layout~title~quest_wording =>
      QuestionTitle(quest_id, category_content_id, version, header_id, status, quest_layout, title, quest_wording)
    }
  }


  def loadFdpDetail(id : String) : FdpDetail = {
    var result : FdpDetail = null
    val statement =
      """
        SELECT location_add_class, industry_class, services_class, ald_id, cert_qnr_id,
        display_blank_ein, display_blank_storenum
        FROM ELECTRONIC_FORM_DEFINITIONS WHERE fdp_id = '%s'
      """.format(id)
    DB.withConnection { implicit connection =>
      val obj : FdpDetail = {
        SQL(statement).as( fdpDetailParser single)
      }
      result = obj
    }
    result
  }

  val fdpDetailParser = {
    get[Option[String]]("location_add_class") ~
    get[Option[String]] ("industry_class") ~
    get[String] ("services_class") ~
    get[String] ("ald_id") ~
    get[String] ("cert_qnr_id") ~
    get[String] ("display_blank_ein") ~
    get[String] ("display_blank_storenum") map {
      case location_add_class~industry_class~services_class~ald_id~cert_qnr_id~display_blank_ein~display_blank_storenum
         => FdpDetail(location_add_class, industry_class, services_class, ald_id, cert_qnr_id, display_blank_ein, display_blank_storenum)
    }
  }

  def loadSurveys : List[String] = {
    var result = new ListBuffer[String]
    val statement = "select distinct survey from questionnaires;"
    DB.withConnection { implicit connection =>
      val objs : List[String] = {
        SQL(statement).as( str("survey") *)
      }
      return objs
    }
    result.toList
  }

  def loadRefPeriods : List[RefPeriod] = {
    var result = new ListBuffer[RefPeriod]
    val statement =
      """
        select distinct ref_period, survey
        from questionnaires
      """
    DB.withConnection { implicit connection =>
      val objs : List[RefPeriod] = {
        SQL(statement).as( refPeriodParser *)
      }
      return objs
    }
    result.toList
  }

  val refPeriodParser = {
    get[String] ("ref_period") ~
    get[String] ("survey") map {
      case ref_period~survey => RefPeriod.RefPeriodWYear(ref_period, survey)
    }
  }


  def loadRefPeriodDivisions : List[RefPeriodDivision] = {
    var result = new ListBuffer[RefPeriodDivision]
    val statement = "select ref_period, division from documents group by ref_period, division"
    DB.withConnection { implicit connection =>
      val objs : List[RefPeriodDivision] = {
        SQL(statement).as( refPeriodDivParser *)
      }
      return objs
    }
    result.toList
  }

  val refPeriodDivParser = {
      get[String] ("ref_period") ~
      get[String] ("division") map {
      case ref_period~division => RefPeriodDivision(ref_period, division)
    }
  }

  def docsByRefDiv(refPeriod : String, division : String) : List[Document] = {
    var result = new ListBuffer[Document]
    val statement =
      """
        select doc_id, document_type, document_number, description
        from documents
        where ref_period='%s' and division = '%s'
      """.format(refPeriod, division)
    DB.withConnection { implicit connection =>
      val objs : List[Document] = {
        SQL(statement).as( documentParser *)
      }
      return objs
    }
    result.toList
  }

  val documentParser = {
      get[String] ("doc_id") ~
      get[String] ("document_type") ~
      get[String] ("document_number") ~
      get[String] ("description") map {
      case doc_id~document_type~document_number~description => Document(doc_id, document_type, document_number, description)
    }
  }

  def sfoById(id : String) : Array[Byte] = {
    var result : Array[Byte] = null
//    select doc_layout -- or designer_sfo or encapsulated_sfo
    val statement = """select doc_layout from document_layouts where doc_id = {id} and page_size like 'LETTER%' """
    println(s"stmt is $statement")
    DB.withConnection { implicit connection =>
      val obj : Array[Byte] = {
        try {
          SQL(statement).on('id -> id).as(byteArray("doc_layout") single)
        } catch {
          case ex : Exception =>  return null
        }
        //    SQL(statement).as( scalar[String].single )
      }
 /*     dbRunner.selectString(statement) match {
        case ErrorResult(msg) => {
          Logger.error(msg)
          ""
        }
        case SuccessResult(msg) => msg
      }
*/
      result = obj
    }
    return result
  }

}
