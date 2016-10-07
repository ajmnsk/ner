Named entity recognition sample using scala
===============================================

  Inspired by [Humphrey Sheil](http://www.informit.com/articles/article.aspx?p=2265404) article.
  This simple library is written in scala and using [Stanford CoreNLP suite of tools](http://stanfordnlp.github.io/CoreNLP/).
  Need to be familiar with [Stanford Named Entity Recognizer (NER)](http://nlp.stanford.edu/software/CRF-NER.shtml) to understand the usage.

## Build library

  Pull the code. Go to project folder (location of built.sbt).
  Run activator as following ```activator -J-Xmx2G -J-Xms2G``` to assign an increased JAVA heap needed by ```assembly``` plug in.
  Run ```assembly```.
  Once built, ner-assembly-0.0.1-SNAPSHOT.jar will become available in project's ```./target/scala-2.11/``` location.

## Library usage

  Copy ```ner-assembly-0.0.1-SNAPSHOT.jar``` into your project's ```./lib``` folder.

### Using CoreNLP only.

  Below sample is using default models coming with CoreNLP installation.

  ```
    import org.aj.ner._

    val input = Array[String](
      "Democratic presidential nominee Hillary Clinton.",
      "A homeowner in Los Angeles, dubbed the \"Wet Prince of Bel Air\" for reportedly using 11.8 million gallons of water last year during California's drought, remains unidentified by authorities, but the Center of Investigative Reporting has narrowed the list of possible perpetrators to seven.",
      "Republican presidential nominee Aa Bb.")

    val sner = SNer()
    val pipeLine: Pipeline = SNerPipeline() //create pipeline with default CoreNLP models
    val tokens: Set[EmbeddedToken] = sner.process(pipeLine, input) // omit last tagsToCollect parameter to capture all tokens.

    println(tokens)
    //Set(EmbeddedToken(LOCATION,Los Angeles), EmbeddedToken(DATE,last year), EmbeddedToken(PERSON,Hillary Clinton), EmbeddedToken(NUMBER,11.8 million), EmbeddedToken(ORGANIZATION,Center of Investigative Reporting), EmbeddedToken(NUMBER,seven), EmbeddedToken(LOCATION,California), EmbeddedToken(MISC,Democratic), EmbeddedToken(MISC,Republican))

  ```

### Using CoreNLP models with RegexNER

  RegexNER feature can be used to improve coverage, or override CoreNLP results.
  Sample below is using ./lib/myTokensRegex.txt as a source for RegexNER.
  Instructions in a file (below) override standard classification for ```Los Angeles``` from ```LOCATION``` to ```ORGANIZATION```, and assign ```PERSON``` classification to ```Aa Bb```:

  ```
  Los Angeles	ORGANIZATION	LOCATION
  Aa Bb	PERSON
  ```

  Samle usage:

  ```
    import org.aj.ner._

    val input = Array[String](
      "Democratic presidential nominee Hillary Clinton.",
      "A homeowner in Los Angeles, dubbed the \"Wet Prince of Bel Air\" for reportedly using 11.8 million gallons of water last year during California's drought, remains unidentified by authorities, but the Center of Investigative Reporting has narrowed the list of possible perpetrators to seven.",
      "Republican presidential nominee Aa Bb.")

    val sner = SNer()
    val pipeLineRegex: Pipeline = SNerPipeline(regexnerPath = Some("./lib/myTokensRegex.txt"))
    val tokensRegex: Set[EmbeddedToken] = sner.process(pipeLineRegex, input, Set("PERSON", "LOCATION", "ORGANIZATION"))

    println(tokensRegex)
    //Set(EmbeddedToken(ORGANIZATION,Los Angeles), EmbeddedToken(PERSON,Hillary Clinton), EmbeddedToken(PERSON,Aa Bb), EmbeddedToken(ORGANIZATION,Center of Investigative Reporting), EmbeddedToken(LOCATION,California))

  ```

### Using your own model(s)

  While creating an instance of SNerPipeline, you can override a list of model(s) used by assigning a new comma delimited list.
  Below sample still uses one of CoreNLP (english.all.3class.distsim.crf.ser.gz) models, plus one of her own.

  ```
    import org.aj.ner._

    val input = Array[String](
      "Democratic presidential nominee Hillary Clinton.",
      "A homeowner in Los Angeles, dubbed the \"Wet Prince of Bel Air\" for reportedly using 11.8 million gallons of water last year during California's drought, remains unidentified by authorities, but the Center of Investigative Reporting has narrowed the list of possible perpetrators to seven.",
      "Republican presidential nominee Aa Bb.")

    val sner = SNer()
    val pipeLine: Pipeline = SNerPipeline(nerPath = Some("./lib/english.all.3class.distsim.crf.ser.gz,./lib/my-model.ser.gz"))
    val tokens: Set[EmbeddedToken] = sner.process(pipeLine, input, Set("PERSON", "LOCATION", "ORGANIZATION"))

    println(tokens)

  ```

  If required you can still add RegexNER to the mix:

  ```
  val pipeLine: Pipeline = SNerPipeline(Some("./lib/myTokensRegex.txt"),
                              Some("./lib/english.all.3class.distsim.crf.ser.gz,./lib/my-model.ser.gz"))
  ```

## Author & license

If you have any questions regarding this project contact:
Andrei <ajmnsk@gmail.com>
For licensing info see LICENSE file in project's root directory.
