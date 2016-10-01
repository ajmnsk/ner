package org.aj.ner

import org.aj.ner.SNerPipeline.Pipeline
import org.scalatest.{FunSpecLike, Matchers}

/**
  * Created by ajlnx on 9/24/16.
  */
class NerTest extends FunSpecLike with Matchers {

  val sner = SNer()
  val input = Array[String](
    "Democratic presidential nominee Hillary Clinton.",
    "A homeowner in Los Angeles, dubbed the \"Wet Prince of Bel Air\" for reportedly using 11.8 million gallons of water last year during California's drought, remains unidentified by authorities, but the Center of Investigative Reporting has narrowed the list of possible perpetrators to seven.",
    "Republican presidential nominee Aa Bb.")

  val phc = EmbeddedToken("PERSON", "Hillary Clinton")
  val lla = EmbeddedToken("LOCATION", "Los Angeles")
  val ola = EmbeddedToken("ORGANIZATION", "Los Angeles")
  val aabb = EmbeddedToken("PERSON", "Aa Bb")

  describe("StanfordCoreNLP module only to process input and identify PERSON, and LOCATION tags in a text") {

    val pipeLine: Pipeline = SNerPipeline()

    it("Should be able to identify EmbeddedToken(PERSON,Hillary Clinton)") {
      val tokens = sner.processArray(pipeLine, input)
      tokens.contains(phc) should equal(true)
    }

    it("Should be able to identify EmbeddedToken(LOCATION,Los Angeles)") {
      val tokens = sner.processArray(pipeLine, input, Set("LOCATION"))
      tokens.contains(lla) should equal(true)
    }

    it("Should not be able to identify Aa Bb as a PERSON") {
      val tokens = sner.processArray(pipeLine, input, Set("PERSON"))
      tokens.contains(aabb) should equal(false)
    }

  }

  describe("StanfordCoreNLP module with RegexNER to improve accuracy, or override CoreNLP results.") {

    val regexnerPathTest = Some("./model/myTokensRegex.txt")
    val nerPathTest = Some("./lib/english.all.3class.distsim.crf.ser.gz")
    val pipeLine = SNerPipeline(regexnerPath = regexnerPathTest)

    it("Should still be able to identify EmbeddedToken(PERSON,Hillary Clinton).") {
      val tokens = sner.processString(pipeLine, "Democratic presidential nominee Hillary Clinton.", Set("PERSON"))
      tokens.contains(phc) should equal(true)
    }

    it("Should be able to identify Los Angeles as EmbeddedToken(ORGANIZATION,Los Angeles), LOCATION value should be overriden via RegexNER source file.") {
      val tokens = sner.processArray(pipeLine, input, Set("ORGANIZATION"))
      tokens.contains(ola) should equal(true)
    }

    it("Should be able to identify Aa Bb as EmbeddedToken(PERSON,Aa Bb). RegexNER is used to improve the coverage.") {
      val tokens = sner.processArray(pipeLine, input, Set("PERSON"))
      tokens.contains(aabb) should equal(true)
    }

  }


}
