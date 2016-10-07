package org.aj.ner

import com.typesafe.config.ConfigFactory
import edu.stanford.nlp.ling.CoreAnnotations.{NamedEntityTagAnnotation, SentencesAnnotation, TextAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.io.Source

import SNerPipeline.Pipeline

object SNer {

  private val config = ConfigFactory.load("application")

  private val regexnerPath = config.getString("stanfordNer.regexnerPath")
  def getRegexnerPath = if (regexnerPath.size > 0) Some(regexnerPath) else None

  private val nerPath = config.getString("stanfordNer.nerPath")
  def getNerPath = if (nerPath.size > 0) Some(nerPath) else None

  private val tags = config.getStringList("stanfordNer.tagsToCollect").asScala
  def tagsToCollect = if (tags.size < 1) Set.empty[String] else tags.toSet

  def apply(): SNer = new SNer()

}

class SNer() {

  private val filterToken = (tags: Set[String], tag: String) => {
    tags.size match {
      case size if size == 0 => true
      case size if size > 0 && tags.contains(tag) => true
      case _ => false
    }
  }

  /**
    * Method to process a String, and return a collection of tokens if any
    *
    * @param pipeline initialized instance of StanfordCoreNLP object
    * @param data data input
    * @param tagsToCollect tags to collect, if not provided, no filtering will be done, and result will have all identified tokens
    * @return results
    */
  def process(pipeline: Pipeline, data: String, tagsToCollect: Set[String]): Set[EmbeddedToken] = {
    process(pipeline, Array(data), tagsToCollect)
  }

  /**
    * Method to process data from a file, and return a collection of tokens if any
    *
    * @param pipeline initialized instance of StanfordCoreNLP object
    * @param data path to a data file to process
    * @param tagsToCollect tags to collect, if not provided, no filtering will be done, and result will have all identified tokens
    * @return results
    */
  def processFile(pipeline: Pipeline, data: String, tagsToCollect: Set[String]): Set[EmbeddedToken] = {
    process(pipeline, Source.fromFile(data).getLines(), tagsToCollect)
  }

  /**
    * Method to process an Array of String(s), and return a collection of tokens if any
    *
    * @param pipeline initialized instance of StanfordCoreNLP object
    * @param data data as an Array of String(s)
    * @param tagsToCollect tags to collect, if not provided, no filtering will be done, and result will have all identified tokens
    * @return results
    */
  def process(pipeline: Pipeline, data: Array[String], tagsToCollect: Set[String]): Set[EmbeddedToken] = {
    process(pipeline, data.toIterator, tagsToCollect)
  }

  /**
    * Method to process data provided as an Iterator, and return a collection of tokens if any
    *
    * @param pipeline initialized instance of StanfordCoreNLP object
    * @param data data as an Array of String(s)
    * @param tagsToCollect tags to collect, if not provided, no filtering will be done, and result will have all identified tokens
    * @return results
    */
  def process(pipeline: Pipeline, data: Iterator[String], tagsToCollect: Set[String]): Set[EmbeddedToken] = {

    //collections for classified tokens
    val currentToken = collection.mutable.Set[String]()
    val currentWords = ListBuffer[String]()

    //filter to use if required to collect a specified list of tokens
    val filter = filterToken(tagsToCollect, _: String)

    val tokens = for {

      sentences <- getSentences(pipeline, data)
      coreMap <- sentences // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
      coreLabel <- getLabel(currentToken, currentWords, coreMap)
      token <- processLabel(currentToken, currentWords, coreLabel, filter)

    } yield token

    tokens.toSet
  }

  /**
    * Method gets all the sentences for passed-in data
    *
    * @param pipeline an instance of StanfordCoreNLP object
    * @param dataIterator Iterator for passed-in data
    * @return a collection of coreMap(s)
    */
  private def getSentences(pipeline: Pipeline, dataIterator: Iterator[String]) =
    for (record <- dataIterator) yield {
      //Annotators on the passed-in data
      val document: Annotation = new Annotation(record)
      pipeline.annotate(document)
      val sentences = document.get(classOf[SentencesAnnotation])
      sentences.asScala
  }

  /**
    * Method returns a collection of Labels
    *
    * @param currentToken a container to collect classified tokens
    * @param currentWords a container to collect values for classified tokens
    * @param coreMap a collection of labels
    * @return a collection of Labels
    */
  private def getLabel(currentToken: collection.mutable.Set[String], currentWords: ListBuffer[String], coreMap: CoreMap) = {

    currentToken.clear()
    currentWords.clear()

    coreMap.get(classOf[TokensAnnotation]).asScala
  }

  /**
    * Method to identify and collect classified token
    *
    * @param currentToken a container to collect classified tokens
    * @param currentWords a container to collect values for classified tokens
    * @param coreLabel label
    * @param filter filter to use, if required to collect a specified list of tokens
    * @return
    */
  private def processLabel(currentToken: collection.mutable.Set[String], currentWords: ListBuffer[String], coreLabel: CoreLabel, filter: String => Boolean) = {

    val namedEntityTagAnnotation = coreLabel.get(classOf[NamedEntityTagAnnotation])
    var zero = false

    val pair =
      if (namedEntityTagAnnotation.size > 1) {
        val word: String = coreLabel.get(classOf[TextAnnotation])
        Some((namedEntityTagAnnotation, word))
      }
      else {
        zero = true
        None
      }

    pair match {
      case Some(value) => {
        currentToken.size match {
          case s if s < 1 =>
            currentToken += value._1
            currentWords += value._2
          case _ =>
            if (currentToken.contains(value._1)) currentWords += value._2
        }
      }
      case _ => None
    }

    (zero, currentToken) match {
      case (z, c) if z == true && c.size > 0 =>

        val embeddedToken = EmbeddedToken(currentToken.head, currentWords.mkString(" "))

        currentToken.clear()
        currentWords.clear()

        if (filter(embeddedToken.name)) Some(embeddedToken)
        else None

      case _ => None
    }

  }

}
