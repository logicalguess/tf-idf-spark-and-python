package logicalguess

import edu.stanford.nlp.util.logging.RedwoodConfiguration
import org.apache.spark.mllib.classification.{LogisticRegressionWithSGD, LogisticRegressionWithLBFGS, SVMWithSGD, NaiveBayes}
import org.apache.spark.mllib.evaluation.{MulticlassMetrics, BinaryClassificationMetrics}
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.{GradientBoostedTrees, DecisionTree}

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.rdd.RDD

import SparkUtils._


object TFIDFSpark {

  final val CSVSeparator = '\t'
  final val path = "./src/main/resources/data/labeledTrainData.tsv"

  def main(args: Array[String]) = {
    RedwoodConfiguration.empty().capture(Console.err).apply();

    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("TfIdfSpark")
      .set("spark.driver.memory", "3g")
      .set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)

    val input: RDD[String] = sc.textFile(path)//.sample(false, 0.2)
    val data = labeledTexts(input, CSVSeparator)

    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    //create features
    val X_train = tfidfTransformer(training).cache()
    val X_test = tfidfTransformer(test).cache()

    //Train / Predict
    //val model = NaiveBayes.train(X_train,lambda = 1.0)
    //val model = SVMWithSGD.train(X_train, 100)
    val model = LogisticRegressionWithSGD.train(X_train, 100)
    //val model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(X_train)

  //val model = DecisionTree.trainClassifier(X_train, numClasses=2, categoricalFeaturesInfo=Map[Int, Int](),
  //  impurity="gini", maxDepth=3, maxBins=32)

    //val model = GradientBoostedTrees.train(X_train, BoostingStrategy.defaultParams("Classification"))

    val predictionAndLabels = X_test.map(x => (model.predict(x.features), x.label))

    //val accuracy = 1.0 *  predictionAndLabels.filter(x => x._1 == x._2).count() / X_test.count()
    //println("ACCURACY " + accuracy)

    evaluateModel(predictionAndLabels, "Results")
  }
}
