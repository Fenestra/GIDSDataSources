package models

import com.westat.gids.GidsFont
import com.westat.sfo.TextAlignments
import com.westat.{ConvertSvgToPdf, Length, Location, StringUtilities}
import com.westat.layoutLanguage._
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Owner on 8/31/2017
 */

case class WFont(name : String, size : String, bold : Boolean, italic : Boolean)
object WFont {
  implicit val wFontReads = Json.reads[WFont]
  implicit val wFontWrites = Json.writes[WFont]
}

case class WItem(kind : String, description : String, width : Double, height : Double,
                 font : WFont, data : String) {
  val color = "black"
  val alignment = "left"
  def fontweight = {
    if (font.bold)
      "700"
   else
      ""
  }
  def gidsfont : GidsFont = {
    GidsFont(font.name, color, fontweight, Length.dimension(font.size+"pt"), font.italic)
  }
}
object WItem {
  implicit val wItemReads = Json.reads[WItem]
  implicit val wItemWrites = Json.writes[WItem]
}

case class WGroup(kind : String, orientation : String, description : String, width : Double, height : Double,
                  min : Int, data : String, children : List[WItem])
object WGroup {
  implicit val wGroupReads = Json.reads[WGroup]
  implicit val wGroupWrites = Json.writes[WGroup]
}

case class WPage(pagename : String, pagenum : Int, description : String, width : Double, height : Double,
                 pagetype : String, pagecolor : String, children : List[WGroup]) {
  private var page : Page = _
  private var loc : Location = _
  private var firstLoc = true
  private var lastGroup : WGroup = null
  private val spacer = Length.dimension("0.25in")

  private def startGroupLocation = {
    loc = Location.create(".5in", "1in", (page.width - Length.dimension("1in")).asInchesString, "1.5in")
  }
  private def nextLoc : Location = {
    if (firstLoc) {
      firstLoc = false
      loc
    }
    else {
      lastGroup.orientation match {
        case "horizontal" => loc = loc.moveDown( Length.dimension(lastGroup.height.toString+"in") + spacer )
        case "vertical"  =>  loc = loc.moveRight( Length.dimension(lastGroup.width.toString+"in") + spacer )
      }
      loc
    }
  }

  private def makeGroup(grp : WGroup) : Group = {
    println(s"makeGroup $grp")
    val group = Group(grp.description, LayoutOrientations.valueForOrientationString(grp.orientation), nextLoc, Length.dimension(grp.min.toString+"in"))
    grp.children.foreach(it => {
      group.addItem(GraphicText(it.gidsfont, it.color, TextAlignments.valueForTextAlignString(it.alignment), it.data))
    })
    group
  }

  private def buildPage : Page = {
    page = Pages.pageForName(pagetype) match {
      case Some(p) => p
      case None => null
    }
//    if ((page == null) || (page.color != pagecolor))
//      page = page.copy(color = pagecolor)
    startGroupLocation
    children.foreach(grp => {
      page.addGroup(makeGroup(grp))
      lastGroup = grp
    })
    page
  }

  def toSVG : String = {
    page = buildPage
    page.toSVG
  }
}
object WPage {
  implicit val wPageReads = Json.reads[WPage]
  implicit val wPageWrites = Json.writes[WPage]
}

//-----------------------------------------------------------------------------------

//sending [{"pagename":"page1","pagenum":1,"description":"Page 1 Letter-portrait #000000","width":8.5,"height":11,"pagetype":"Letter-portrait","pagecolor":"#000000",
// "children":[
//   {"kind":"group","orientation":"horizontal","description":"Group horizontal w:8.5 h:2 min:1","width":8.5,"height":"2","min":"1","data":"horizontal",
//   "children":[
//     {"kind":"text","description":"Text w:1 h:1 Univers LT55 10 (Here is some sample text.)","width":"1","height":"1",
//       "font":{"name":"Univers LT55","size":"10","bold":"boldfalse","italic":"italicfalse"},
//       "data":"Here is some sample text."}]}]}]


object QuickLayouts {

  def renderLayoutFromWebObjs(name : String, list : List[WPage]) : Future[Array[Byte]] = {
    println(s"QuickLayouts.renderLayoutFromWebObjs $name")
    StringUtilities.writeFile(name+".json", Json.stringify(Json.toJson(list)))
 //   list.foreach(p => println(p) )
 //   Future(list.head.toSVG)
    Future(ConvertSvgToPdf.convertToBA(list.head.toSVG).pdf)
  }
}
