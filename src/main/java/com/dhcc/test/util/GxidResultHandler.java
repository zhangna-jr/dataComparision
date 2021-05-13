package com.dhcc.test.util;

import com.dhcc.test.dao.DateSource2.DateSourceDao2;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GxidResultHandler implements ResultHandler<HashMap> {

    // 这是每批处理的大小
    private final static int BATCH_SIZE = 10;
    private int size;
    private int count;

    // 存储每批数据的临时容器
    private List<HashMap> gxids = new ArrayList<>();
    Calendar cal = Calendar.getInstance();
    Date date = cal.getTime();
    String dateTime = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS").format(date);
    String filename = dateTime.replace(":", "").replace("/", "") ;
    BufferedWriter bufferedWriter = null;
    {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter("outputfile/" + filename+ ".txt"));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                bufferedWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Autowired
    private DateSourceDao2 dateSourceDao2;


    public GxidResultHandler(DateSourceDao2 Dao2){
        dateSourceDao2 = Dao2;
    }

    @Override
    public void handleResult(ResultContext<? extends HashMap> resultContext) {
        // 这里获取流式查询每次返回的单条结果
        HashMap gxid = resultContext.getResultObject();
        gxids.add(gxid);
        size++;
        if (size == BATCH_SIZE) {
            count ++;
            handle(count);
        }
    }
    private void handle(int count) {
        System.out.println("========进入handle=======");
        long counts = (count-1) * BATCH_SIZE;
        try {
            // 在这里可以对你获取到的批量结果数据进行需要的业务处理
            //try{

                for (int i = 0; i < gxids.size(); i++) {
                    long allCount = counts + (long)i;
                    //获取第i条数据的所有属性名称
                    Set keys = gxids.get(i).keySet();
                    String txDate = gxids.get(i).get("TXDATE").toString();
                    String tranceNo = gxids.get(i).get("TRANCENO").toString();
                    HashMap valueTwo = dateSourceDao2.selectByDateAndTranceNo(txDate, tranceNo);
                    if (valueTwo != null) {
                        bufferedWriter.write("-------第" + allCount + "条数据对比，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                        System.out.println("-------第" + allCount + "条数据对比，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                    } else {
                        //该条数据主表中有从表中无
                        bufferedWriter.write("-------第" + allCount + "条数据对比,该条数据主表有从表无，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                        System.out.println("-------第" + allCount + "条数据对比，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                    }
                    for (Object key : keys) {
                        //根据属性名称获取属性值
                        Object keyValue = gxids.get(i).get(key);
                        if (keyValue == null || "".equals(keyValue)) {
                            //主表中第i条数据key对应的值为空
                            if (valueTwo != null) {
                                if (valueTwo.get(key) != null && !"".equals(valueTwo.get(key))) {
                                    //主表无，从表有，则为FALSE
                                    bufferedWriter.write(key + ":false" + ",");
                                    bufferedWriter.write("主表："+keyValue+",从表："+valueTwo.get(key)+ "\n");
                                } else {
                                    //主表无，从表无
                                    bufferedWriter.write(key + ":true" + ",");
                                    bufferedWriter.write("主表："+keyValue+",从表："+valueTwo.get(key)+ "\n");
                                }
                            } else {
                                //当从表中该条记录valueTwo为null时，只记录81行数据就行
                                continue;
                            }
                        } else {
                            //主表中第i条数据key对应的值不为空
                            String valueMain = keyValue.toString();
                            //获取从表中第i条数据key对应的value值
                            String valueTran = "";
                            if (valueTwo != null) {
                                if (valueTwo.get(key) != null && !"".equals(valueTwo.get(key))) {
                                    valueTran = valueTwo.get(key).toString();
                                }
                            } else {
                                //当从表中该条记录valueTwo为null时，只记录81行数据就行
                                continue;
                            }
                            if (valueMain.equals(valueTran)) {
                                bufferedWriter.write(key + ":true" + ",");
                                bufferedWriter.write("主表："+valueMain+",从表："+valueTran+ "\n");
                            } else {
                                bufferedWriter.write(key + ":false" + "\n");
                                bufferedWriter.write("主表："+valueMain+",从表："+valueTran+ "\n");
                            }
                        }
                    }
                }
           /* }catch(Exception e){
                e.printStackTrace();
                //清除表数据
                //dateSourceDao2.truncateTable();
                //删除表
                //dateSourceDao2.dropTable();
            }*/
        } catch (Exception e){
            try {
                bufferedWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }finally {
            // 处理完每批数据后后将临时清空
            this.size = 0;
            gxids.clear();
        }
    }

    public void end(){
        handle(count+1);// 处理最后一批不到BATCH_SIZE的数据
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
