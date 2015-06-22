package logicalguess

import java.io.StringReader

import com.opencsv.CSVReader
import logicalguess.NLPUtils._
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.feature.{Normalizer, IDF, HashingTF}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/**
 * Created by logicalguess on 6/21/15.
 */
object SparkUtils {

  case class LabeledDocument(id: String, body: Seq[String], label: String, numericLabel: Int)

  def tfidfTransformer(data: RDD[LabeledDocument],norm: Boolean = false): RDD[LabeledPoint] = {
    /**
     * Implements TFIDF via Sparks built in methods. Because idfModel requires and RDD[Vector] we are not able to pass directly in
     * a RDD[LabeledPoint]. A work around is to save the LabeledPoint.features to a var (hashedData), transform the data, then  zip
     * the labeled dataset and the transformed IDFs and project them to a new LabeledPoint

      Data: RDD of type LabledDocument
     */
    val tf = new HashingTF()
    val freqs = data.map(x => (LabeledPoint(x.numericLabel, tf.transform(x.body)))).cache()
    val hashedData = freqs.map(_.features)
    val idfModel = new IDF().fit(hashedData)
    val idf = idfModel.transform(hashedData)
    val LabeledVectors = if (norm == true) {
      val l2 = new Normalizer()
      idf.zip(freqs).map(x => LabeledPoint(x._2.label, l2.transform(x._1)))
    } else {
      idf.zip(freqs).map(x => LabeledPoint(x._2.label, x._1))
    }
    LabeledVectors
  }

  def labeledTexts(input: RDD[String], separator: Char): RDD[LabeledDocument] = input.map { line =>
    val reader = new CSVReader(new StringReader(line), separator)

    try {
      val nextLine: Option[List[String]] = Option(reader.readNext()).map(_.toList)

      val columnCount = 3
      val idColumn = 0
      val labelColumn = 1
      val textColumn = 2

      nextLine match {
        case Some(line) if line.length == columnCount =>
          val id = line(idColumn)
          val label = line(labelColumn)
          val text = line(textColumn)
          val textCleaned = LineCleaner.clean(text)
          val processedDoc = tokenizeAndStem(text, Set[String]())
          val result = LabeledDocument(id, processedDoc, label, label.toInt)

          Some(result)
        case Some(line) if line.length != columnCount =>
          // The `line` was not parsed correctly and we ignore subsequently.
          Option.empty[LabeledDocument]
        case None =>
          Option.empty[LabeledDocument]
      }
    } catch {
      case ex: Exception =>
        val msg = s"Error:\n${ex.getMessage}"
        println(msg)
        Option.empty[LabeledDocument]
    }
  }.filter(_.isDefined).map(_.get)


  def evaluateModel(predictionAndLabels: RDD[(Double, Double)], msg: String) = {
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val cfMatrix = metrics.confusionMatrix

    println(msg)

    printf(
      s"""
         |=================== Confusion matrix ==========================
         |          | %-15s                     %-15s
         |----------+----------------------------------------------------
         |Actual = 0| %-15f                     %-15f
         |Actual = 1| %-15f                     %-15f
         |===============================================================
         """.stripMargin, "Predicted = 0", "Predicted = 1",
      cfMatrix.apply(0, 0), cfMatrix.apply(0, 1), cfMatrix.apply(1, 0), cfMatrix.apply(1, 1))

    println("\nACCURACY " + ((cfMatrix(0,0) + cfMatrix(1,1))/(cfMatrix(0,0) + cfMatrix(0,1) + cfMatrix(1,0) + cfMatrix(1,1))))


    //cfMatrix.toArray

    val fpr = metrics.falsePositiveRate(0)
    val tpr = metrics.truePositiveRate(0)

    println(
      s"""
         |False positive rate = $fpr
          |True positive rate = $tpr
     """.stripMargin)

    val m = new BinaryClassificationMetrics(predictionAndLabels)
    println("PR " + m.areaUnderPR())
    println("AUC " + m.areaUnderROC())
  }


}
