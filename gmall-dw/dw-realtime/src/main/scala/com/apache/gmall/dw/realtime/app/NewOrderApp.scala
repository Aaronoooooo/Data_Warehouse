package com.apache.gmall.dw.realtime.app

import com.alibaba.fastjson.JSON
import com.apache.gmall.dw.realtime.MyKafkaUtil
import com.apache.gmall.dw.realtime.bean.OrderInfo
import com.apache.gmall.dwcommon.constant.GmallConstant
import com.apache.gmall.dwcommon.util.MyEsUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object NewOrderApp {
  def main(args: Array[String]): Unit = {
    val sparkconf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("gmall0901_new_order")
    val sc = new SparkContext(sparkconf)
    val ssc = new StreamingContext(sc, Seconds(5))

    val recordDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstant.KAFKA_TOPIC_ORDER, ssc)
    val orderInfoDstream: DStream[OrderInfo] = recordDstream.map(_.value()).map { jsonString =>
      val orderInfo: OrderInfo = JSON.parseObject(jsonString, classOf[OrderInfo])
      //处理时间
      val datetimeArry: Array[String] = orderInfo.createTime.split(" ")
      orderInfo.createDate = datetimeArry(0)
      val timearray: Array[String] = datetimeArry(1).split(":")
      orderInfo.createHour = timearray(0)
      orderInfo.createHourMinute = timearray(0) + ":" + timearray(1)
      orderInfo
    }
    //保存到ES
    orderInfoDstream.foreachRDD { rdd =>
      rdd.foreachPartition { orderInfoItr =>
        //每个分区组成一个批次保存一次
        MyEsUtil.insertBulk(GmallConstant.ES_INDEX_ORDER, orderInfoItr.toList)
      }
    }
    ssc.start()
    ssc.awaitTermination()


  }
}
