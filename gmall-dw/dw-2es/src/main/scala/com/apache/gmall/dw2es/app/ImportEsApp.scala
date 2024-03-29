package com.apache.gmall.dw2es.app

import com.apache.gmall.dw2es.bean.SaleDetailDaycount
import com.apache.gmall.dwcommon.constant.GmallConstant
import com.apache.gmall.dwcommon.util.MyEsUtil
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

object ImportEsApp {
  def main(args: Array[String]): Unit = {
    //0 准备环境
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("gmall0901_dw2es")
    val sparkSession: SparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()
    //1 读取宽表 sql =>RDD[bean]
    sparkSession.sql("use gmall0901")
    import sparkSession.implicits._
    val saleRDD: RDD[SaleDetailDaycount] = sparkSession.sql("select user_id,sku_id,user_gender,cast(user_age as int) " + "user_age,user_level,cast( sku_price as double) sku_price,sku_name,sku_tm_id, " + "sku_category3_id,sku_category2_id,sku_category1_id,sku_category3_name,sku_category2_name," + "sku_category1_name,spu_id,sku_num,cast(order_count as bigint) order_count,cast(order_amount as double)" + " order_amount,dt from gmall0901.dws_sale_detail_daycount where dt='2019-02-28'").as[SaleDetailDaycount].rdd
    //2 把RDD存入ES
    saleRDD.foreachPartition { saleDetailItr =>
      val saleDetailDaycountList = new ListBuffer[SaleDetailDaycount]()
      for (saleDetail <- saleDetailItr) {
        saleDetailDaycountList += saleDetail
        //数据份批导入ES,没批小于等于10条
        if (saleDetailDaycountList.size > 0 && saleDetailDaycountList.size % 10 == 0) {
          MyEsUtil.insertBulk(GmallConstant.ES_INDEX_SALE, saleDetailDaycountList.toList);
          saleDetailDaycountList.clear()
        }
      }
      if (saleDetailDaycountList.size > 0) {
        MyEsUtil.insertBulk(GmallConstant.ES_INDEX_SALE, saleDetailDaycountList.toList);
      }
    }
  }
}
