package org.aj.ner

import java.io._

/**
  * Module to test library
  */
object SNerExecute {

  private def getParameters(): (Set[String], Option[String], Option[String], String, String) = {

    val props = System.getProperties
    println(s"\nSystem properties: ${props}\n")

    val tags = props.getProperty("tagsToCollect", "")
    val tagsToCollect =
      if (tags.isEmpty) SNer.tagsToCollect
      else props.getProperty("tagsToCollect").split(",").map(r => r.trim).toSet

    val rpath = props.getProperty("regexnerPath", "")
    val regexnerPath =
      if (rpath.isEmpty) SNer.getRegexnerPath
      else Some(props.getProperty("regexnerPath"))

    val npath = props.getProperty("nerPath", "")
    val nerPath =
      if (npath.isEmpty) SNer.getNerPath
      else Some(props.getProperty("nerPath"))

    val spath = props.getProperty("sourcePath", "")
    val sourcePath =
      if (spath.isEmpty) throw new IllegalArgumentException("sourcePath paramter value was not provided!")
      else props.getProperty("sourcePath")

    val dpath = props.getProperty("destinationPath", "")
    val destinationPath =
      if (dpath.isEmpty) throw new IllegalArgumentException("destinationPath paramter value was not provided!")
      else props.getProperty("destinationPath")

    (tagsToCollect, regexnerPath, nerPath, sourcePath, destinationPath)

  }

  def main(args: Array[String]): Unit = {

    val (tagsToCollect, regexnerPath, nerPath, sourcePath, destinationPath) = getParameters()

    println("tag(s) to collect: " + tagsToCollect)
    println(s"sourcePath: $sourcePath, destinationPath: $destinationPath")
    println(s"regexnerPath: $regexnerPath, nerPath: $nerPath")

    val pipeLine = SNerPipeline(regexnerPath, nerPath)
    val sner = SNer()

    println("-----------------STARTED-------------------------")

    val tokens = sner.processFile(pipeLine, sourcePath, tagsToCollect)
    val writer = new PrintWriter(new File(destinationPath))
    tokens foreach (r => writer.write(r.toString + "\n"))
    writer.close()

    println("-----------------FINISHED------------------------")

  }
}
