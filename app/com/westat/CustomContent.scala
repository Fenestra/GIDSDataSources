package com.westat

import models.QuestionItem

import scala.collection.mutable.ListBuffer

/**
 * Created by Owner on 3/10/2017.
 */

//-------------

object ItemKinds extends Enumeration {
  val itkCustomContent = Value("CustomContent")
  val itkHeader = Value("Header")
  val itkPagemaster = Value("PageMaster")
  val itkDocument = Value("Document")
}

object MediaKinds extends Enumeration {
  val mkPaper = Value("PAPER")
  val mkElectronic = Value("ELECTRONIC")
}

trait CustomContentItem {
  def id : String
  def text : String
  def dataElementName : String
}

trait CustomContentParent extends CustomContentItem {
  protected var anItemKind = ItemKinds.itkCustomContent
  protected var aMediaKind = MediaKinds.mkPaper
  def itemKind : ItemKinds.Value = anItemKind
  def mediaKind : MediaKinds.Value = aMediaKind

  var children = List[CustomContentItem]()
  def appendItem(anItem : CustomContentItem) : CustomContentParent = {
    val lst = new ListBuffer[CustomContentItem]
    lst.appendAll(children)
    lst += anItem
    children = lst.toList
    this
  }
  def appendResponse(aResp : CustomContentItem) : CustomContentParent = {
    appendItem(aResp)
  }
  def childrenString : String = {
    children.mkString("children(", "\n", ")")
  }
  def debugString : String = {
    toString + childrenString
  }
}

class CustomContentBase(anID : String, aText : String, aDEName : String) extends CustomContentItem {
  def this(anID : String, aText : String) = {
    this(anID, aText, "")
  }
  def id : String = anID
  def text : String = aText
  def dataElementName : String = aDEName
  override def toString : String = getClass.getSimpleName+s"($anID, $aText, $aDEName)"
}

case class CCItemParent(anID : String, aitemKind : ItemKinds.Value, amediaKind : MediaKinds.Value,
                        aList : List[CustomContentItem]) extends CustomContentParent {
  anItemKind = aitemKind
  aMediaKind = amediaKind
  children = aList
  def id : String = anID
  def text : String = itemKind + " - " + mediaKind
  def dataElementName : String = ""
}
case class CCItemResponse(anID : String, aDEName : String, aList : List[CustomContentItem]) extends CustomContentParent {
  children = aList
  def id : String = anID
  def text : String = aDEName
  def dataElementName : String = aDEName
}

class CCQuestionTitle(anID : String, aText : String) extends CustomContentBase(anID, aText)
class CCQuestionWording(anID : String, aText : String) extends CustomContentBase(anID, aText)

class CCItemNumber(anID : String, aText : String) extends CustomContentBase(anID, aText)
class CCItemWording(anID : String, aText : String) extends CustomContentBase(anID, aText)
class CCItemHeaderColumnRef(anID : String, aText : String) extends CustomContentBase(anID, aText)
class CCItemHeaderKeycode(anID : String, aText : String) extends CustomContentBase(anID, aText)

class CCResponseLabel(anID : String, aText : String, dataElementName : String) extends CustomContentBase(anID, aText, dataElementName)
class CCResponseKeycode(anID : String, aText : String, dataElementName : String) extends CustomContentBase(anID, aText, dataElementName)
class CCElectronicResponse(anID : String, aText : String, dataElementName : String) extends CustomContentBase(anID, aText, dataElementName)
class CCPaperResponse(anID : String, aText : String, dataElementName : String) extends CustomContentBase(anID, aText, dataElementName)

//-------------

object CustomContent {

  def createQuestionTitle(anID : String, aText : String) : CCQuestionTitle = new CCQuestionTitle(anID, aText)
  def createQuestionWording(anID : String, aText : String) : CCQuestionWording = new CCQuestionWording(anID, aText)

  def createItemNumber(anID : String, aText : String) : CCItemNumber = new CCItemNumber(anID, aText)
  def createItemWording(anID : String, aText : String) : CCItemWording = new CCItemWording(anID, aText)
  def createItemHeaderColumnRef(anID : String, aText : String) : CCItemHeaderColumnRef = new CCItemHeaderColumnRef(anID, aText)
  def createItemHeaderKeycode(anID : String, aText : String) : CCItemHeaderKeycode = new CCItemHeaderKeycode(anID, aText)

  def createResponseLabel(anID : String, aText : String, dataElementName : String) : CCResponseLabel = new CCResponseLabel(anID, aText, dataElementName)
  def createResponseKeycode(anID : String, aText : String, dataElementName : String) : CCResponseKeycode = new CCResponseKeycode(anID, aText, dataElementName)
  def createElectronicResponse(anID : String, aText : String, dataElementName : String) : CCElectronicResponse = new CCElectronicResponse(anID, aText, dataElementName)
  def createPaperResponse(anID : String, aText : String, dataElementName : String) : CCPaperResponse = new CCPaperResponse(anID, aText, dataElementName)

  def createCustomContent(content : CustomContentParent, title : String, quest_wording : Option[String], item_id : String, item_number : Option[String],
                          item_wording : Option[String], hdr_column_ref : Option[String], hdr_keycode : Option[String],
                          responses_id : Option[String], response_label : Option[String], response_instruction : Option[String],
                          keycode : Option[String], ag_id_paper : Option[String], ag_id_electronic : Option[String],
                          ag_id_dc : Option[String], de_name : Option[String], instance_offset : Option[Float],
                          instance_index : Option[Float], question_number : Option[String], ref_period : String) : CustomContentParent = {
    // should adjust the values acom/westat/CustomContent.scala:89ccordingly and then create new object with multiple children
    /*    QuestionItem(item_id, title, quest_wording.getOrElse(""), item_number.getOrElse(""), item_wording.getOrElse(""),
          hdr_column_ref.getOrElse(""), hdr_keycode.getOrElse(""), responses_id.getOrElse(""), response_label.getOrElse(""),
          response_instruction.getOrElse(""), keycode.getOrElse("Â§"), ag_id_paper.getOrElse(""), ag_id_electronic.getOrElse(""),
          ag_id_dc.getOrElse(""), de_name.getOrElse(""), instance_offset.getOrElse(0), instance_index.getOrElse(0),
          question_number.getOrElse(""), ref_period)
        result = result.appendcreateCustomContent(result, title : String, quest_wording : Option[String], item_id : String, item_number : Option[String],
          item_wording : Option[String], hdr_column_ref : Option[String], hdr_keycode : Option[String],
          responses_id : Option[String], response_label : Option[String], response_instruction : Option[String],
          keycode : Option[String], ag_id_paper : Option[String], ag_id_electronic : Option[String],
          ag_id_dc : Option[String], de_name : Option[String], instance_offset : Option[Float],
          instance_index : Option[Float], question_number : Option[String], ref_period : String
    */
    var result = content
    val itemKind = result.itemKind
    val mediaKind = result.mediaKind

    if (result.children.isEmpty) {
      result = result.appendItem( CCItemParent(item_id, itemKind, mediaKind, List(
        createQuestionTitle(item_id, title),
        createQuestionWording(item_id, quest_wording.getOrElse(""))
      )
      ))
    }
    println("   parent w first child "+result.debugString)
    println("")

    if (item_id.nonEmpty) {
      result = result.appendItem( CCItemParent(item_id, itemKind, mediaKind, List(
        createItemNumber(item_id, item_number.getOrElse("")),
        createItemWording(item_id, item_wording.getOrElse("")),
        createItemHeaderColumnRef(item_id, hdr_column_ref.getOrElse("")),
        createItemHeaderKeycode(item_id, hdr_keycode.getOrElse(""))
      )
      ))
    }
    println("   parent w second item "+result.debugString)
    println("")

    var respID = ""
    responses_id match {
      case Some(rid) => respID = rid
      case None => return result
    }

    //    if (instanceOffset <> 0) or (instanceIndex <> 0) then
    //      responseDataElementName := format('%s;%d', [responseDataElementName, instanceOffset + instanceIndex]);
    val dename = de_name.getOrElse("")
    result = result.appendResponse( CCItemResponse(respID, dename, List(
      createResponseLabel(respID, response_label.getOrElse(""), dename),
      createResponseKeycode(respID, keycode.getOrElse("Â§"), dename),
      createElectronicResponse(respID, ag_id_electronic.getOrElse(""), dename)
    )
    ))
    result
  }

    def loadTest = {
    val itemKind = ItemKinds.itkCustomContent
    val mediaKind = MediaKinds.mkElectronic
    var result : CustomContentParent = CCItemParent("loadTestID", itemKind, mediaKind, List())
    println("base parent "+result.debugString)
    println("")

    val qlist = QuestionItem.questionItemsTestList
    qlist.foreach(qi => result = createCustomContent(result, qi.title, qi.quest_wording, qi.id, qi.item_number,
      qi.item_wording, qi.hdr_column_ref, qi.hdr_keycode,
      qi.responses_id, qi.response_label, qi.response_instruction,
      qi.keycode, qi.ag_id_paper, qi.ag_id_electronic,
      qi.ag_id_dc, qi.de_name, qi.instance_offset,
      qi.instance_index, qi.question_number, qi.ref_period)
    )
 /*
    result = createCustomContent(result, "firstItem", Some("first qitem wording"), "firstid", None,
      Some("someitemwordng"), None, None,
      Some("firstresp"), Some("firstresplabel"), Some("firstrespinstruction"),
      Some("1234"), None, Some("agelectid"),
      None, Some("RECORD_NAME"), None,
      None, Some("questnum 1"), "2017")
*/    println("parent after loading all "+result.debugString)
    println("")
  }

  def test = {
    val itemKind = ItemKinds.itkCustomContent
    val mediaKind = MediaKinds.mkElectronic
    var parent : CustomContentParent = CCItemParent("parent", itemKind, mediaKind, List())
    println("base parent "+parent.debugString)
    println("")

    parent = parent.appendItem( CCItemParent("firstchild", itemKind, mediaKind, List(
      createQuestionTitle("firstchild", "first title"),
      createQuestionWording("firstchild", "first wording")
    )
    ))
    println("parent w first child "+parent.debugString)
    println("")

    parent = parent.appendItem( CCItemParent("secondchild", itemKind, mediaKind, List(
      createItemNumber("seconditem", "second number"),
      createItemWording("seconditem", "second wording"),
      createItemHeaderColumnRef("seconditem", "second hdr col ref"),
      createItemHeaderKeycode("seconditem", "second hdr keycode")
    )
    ))
    println("parent w second item "+parent.debugString)
    println("")

    val dename = "RECORD_NAME"
    parent = parent.appendResponse( CCItemResponse("thirdresp", dename, List(
      createResponseLabel("thirdresp", "Customer Name:", dename),
      createResponseKeycode("thirdresp", "no keycode", dename),
      createElectronicResponse("thirdresp", "electagid", dename)
    )
    ))
    println("parent w third resp "+parent.debugString)
    println("")
  }
}
