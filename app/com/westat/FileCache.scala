package com.westat

import java.io._
import scala.io.Source
import com.westat.StringUtilities

// Created by lee on 6/19/17.

case class FileCache(directory: String, extension : String) {
  private def filename(id : String) : String = {
    directory + "/" + id + "." + extension
  }

  def find(id : String) : Option[String] = {
    var sf : Source = null
    try {
      sf = Source.fromFile(filename(id))
    } catch {
      case ex : Exception => return None
    }
    println(s"find has sf of $sf")
    val contents = sf.mkString
    println(s"find has contents of ${StringUtilities.shortString(contents)}")
    Some(contents)
  }

  def write(id : String, contents : String) = {
    val file = new File(filename(id))
    val writer = new BufferedWriter(new FileWriter(file))
    writer.write(contents)
    writer.close()
  }

}
