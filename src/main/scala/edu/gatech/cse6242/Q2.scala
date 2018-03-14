package edu.gatech.cse6242

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._


object Q2 {

	def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("Q2"))
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    	// read the file
    	val file = sc.textFile("hdfs://localhost:8020" + args(0))

    // split each document into words
    val data = file.map{line =>
      val splits = line.split("\t")
      (splits(0).toInt, splits(1).toInt, splits(2).toInt)
    }

    // Create a dataframe
    var df = data.toDF("source","target","weight")

    // Determine weights of in-nodes (incoming edge weights)
    var in_nodes = df.groupBy("target")
      .agg(sum("weight"))
      .withColumnRenamed("sum(weight)", "incoming")
      .withColumnRenamed("target", "node")
      .orderBy("node")

    // DO the same for out-nodes (outgoing edge weights)
    var out_nodes = df.groupBy("source")
      .agg(sum("weight"))
      .withColumnRenamed("sum(weight)", "outgoing")
      .withColumnRenamed("source", "node")
      .orderBy("node")

    // Get list of all nodes
    var df_nodeList = in_nodes.unionAll(out_nodes)
      .drop("incoming")
      .drop("outgoing")
      .orderBy("node")

    // Only keep the unique values
    var df_nodes = df_nodeList.distinct

    val df_nodes2 = df_nodes.withColumnRenamed("node", "mstr_node")

    //left join node list and in_nodes/out_nodes to get comprehensive list
    val df_weight = df_nodes2.join(in_nodes, df_nodes2("mstr_node") === in_nodes("node"), "left")
      .join(out_nodes, df_nodes2("mstr_node") === out_nodes("node"), "left")
      .orderBy(df_nodes2("mstr_node"))

    // keep only columns we need; get rid of redundant columns
    val df_weight2 = df_weight.select("mstr_node", "incoming", "outgoing")

    // convert null values to zeroes
    val df_weight3 = df_weight2.na.fill(0, Seq("incoming"))
    val df_weight4 = df_weight3.na.fill(0, Seq("outgoing"))


    //put it all together in final result df and filter out nodes with weight=1
    val df_result = df_weight4.select(df_weight("mstr_node"), df_weight4("incoming") - df_weight4("outgoing"))
      .withColumnRenamed("(incoming - outgoing)", "weight")
      .withColumnRenamed("mstr_node", "node")
      .filter("weight > 5")

    // store output on given HDFS path.
    	// YOU NEED TO CHANGE THIS
      val results = df_result.map(r=>r.mkString("\t"))
    	results.saveAsTextFile("hdfs://localhost:8020" + args(1))
  	}
}
