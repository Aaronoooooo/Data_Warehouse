package com.apache.gmall.dwcanal.client;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.google.common.base.CaseFormat;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.apache.gmall.dwcommon.constant.GmallConstant;
import com.apache.gmall.dwcommon.util.MyKafkaSender;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

public class CanalClient {
    public static void main(String[] args) {
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("supreme0", 11111), "example", "", "");
        //死循环
        while (true) {
            //尝试建立 canal服务器
            canalConnector.connect();
            //订阅 数据表
            canalConnector.subscribe("gmall0901.order_info");
            //获取发现的sql 得到message
            Message message = canalConnector.get(100);
            System.out.println("获得" + message.getEntries().size() + "个sql");
            if (message.getEntries().size() == 0) {
                System.out.println("无事发生");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //展开所有的sql
                for (CanalEntry.Entry entry : message.getEntries()) {
                    //只要对数据造成影响的sql
                    if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                        //对entry进行反序列化 得到rowchange
                        try {
                            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                            //从rowchange 得到变化的行集
                            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                            //得到事件类型
                            CanalEntry.EventType eventType = rowChange.getEventType();
                            //得到表明
                            String tableName = entry.getHeader().getTableName();
                            handle(tableName, eventType, rowDatasList);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    public static void handle(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDatesList) {
        if ("order_info".equals(tableName) && eventType == CanalEntry.EventType.INSERT && rowDatesList.size() > 0) {
            for (CanalEntry.RowData rowDate : rowDatesList) {//展开行集
                List<CanalEntry.Column> afterColumnsList = rowDate.getAfterColumnsList();//得到变化后列集合
                JSONObject jsonObject = new JSONObject();
                for (CanalEntry.Column column : afterColumnsList) {//展开列集
                    System.out.println(column.getName() + ":::" + column.getValue());

                    //做成json格式
                    String colNameCamel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column.getName());

                    jsonObject.put(colNameCamel, column.getValue());
                }
                    //发送到kafka中
                MyKafkaSender.send(GmallConstant.KAFKA_TOPIC_ORDER, jsonObject.toJSONString());

            }
        }
    }
}
