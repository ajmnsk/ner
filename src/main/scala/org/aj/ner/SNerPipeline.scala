package org.aj.ner

import java.util.Properties
import edu.stanford.nlp.pipeline.StanfordCoreNLP

object SNerPipeline {

  type Pipeline = StanfordCoreNLP

  /**
    * Creates StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and on.
    * Default CoreNLP / NER models are used If no parameter(s) passed.
    * You can re-palce default models by providing your own list of models.
    * Default models can be a part of your list, just need to indicate their location, for example:
    *   ./lib/english.all.3class.distsim.crf.ser.gz,./models/my-model.ser.gz
    *
    * @param regexnerPath path to a file with tokens. Can be used to improve coverage, override default models results.
    * @param nerPath path to a comma delimited list of models, if passed, it will be used instead of default CoreNLP / NER models
    * @return
    */
  def apply(regexnerPath: Option[String] = None, nerPath: Option[String] = None): Pipeline = {

    val props = new Properties()
    regexnerPath match {
      case Some(value) =>
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner")
        if (new java.io.File(value).exists == false)
          throw new IllegalArgumentException(s"File '${value}' for regexner.mapping does not exists!")
        props.put("regexner.mapping", value)
      case _ =>
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner")
    }
    nerPath match {
      case Some(value) => props.put("ner.model", value)
      case _ => None
    }

    new StanfordCoreNLP(props)

  }

}
