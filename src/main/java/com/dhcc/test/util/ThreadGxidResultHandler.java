package com.dhcc.test.util;

import com.dhcc.test.dao.DateSource2.DateSourceDao2;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
public class ThreadGxidResultHandler implements ResultHandler<HashMap> {

    private int threadSize;  //第几个线程
    private String outPutFileName;   //文件名称前缀
    //private StringBuffer catalog;  //文件第一行属性目录
    private int endOutPutFileName;
    // 这是每批处理的大小
    private final static int BATCH_SIZE = 5000;
    //private final static int BATCH_SIZE = 5;
    private int size;   //当size与BATCH_SIZE相等时进入业务处理层handle()
    private int count;  //该条sql第几次进入handle()

    // 存储每批数据的临时容器
    //private List<HashMap> gxids = new ArrayList<>();
    private List<HashMap> gxids = new CopyOnWriteArrayList<>();
    Calendar cal = Calendar.getInstance();
    Date date = cal.getTime();
    String dateTime = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS").format(date);
    String filename = dateTime.replace(":", "").replace("/", "");
    BufferedWriter bufferedWriter = null;

    @Autowired
    private DateSourceDao2 dateSourceDao2;

    public ThreadGxidResultHandler(DateSourceDao2 Dao2, int i, String outPutFileName1) {
        dateSourceDao2 = Dao2;
        threadSize = i;
        outPutFileName = outPutFileName1;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter("outputfile/" + outPutFileName + "-" + endOutPutFileName + "-" + filename + ".txt"));
            log.info("===============输出文件名称：sql" + threadSize + "-" + filename + ".txt");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                bufferedWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        log.info("====================threadSize:" + threadSize);

    }

    @Override
    public void handleResult(ResultContext<? extends HashMap> resultContext) {
        try{
            // 这里获取流式查询每次返回的单条结果
            HashMap gxid = resultContext.getResultObject();
            gxids.add(gxid);
            size++;
            if (size == BATCH_SIZE) {
                //int i = (count * 5) % 20;
                int i = (count*5000)  % 500000;
                if (i == 0) {
                    endOutPutFileName++;
                    //当数据量是50万的倍数时，写入新的文件
                    if (count != 0) {
                        //不是第一次创建缓冲流,先关闭前面的流，再重新创建
                        try {
                            bufferedWriter.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        try {
                            bufferedWriter = new BufferedWriter(new FileWriter("outputfile/" + outPutFileName + "-" + endOutPutFileName + "-" + filename + ".txt"));
                            log.info("===============输出文件名称：" + outPutFileName + "-" + endOutPutFileName + "-" + filename + ".txt");
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                bufferedWriter.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                handle(count,true);
                count++;
            }
        }catch(Exception e){
            e.printStackTrace();
            log.info("===============报错："+e.getMessage());
        }
    }

    private void handle(int count,boolean value) {
        //System.out.println("========进入handle："+threadSize+"======================");
        log.info("========进入handle，线程：" + threadSize + "======================");
        //long counts = (count) * BATCH_SIZE;
        try {
            Object[] keys = null;
            if (gxids.size() > 0) {
                keys = gxids.get(0).keySet().toArray();
                if ((count*5000)  % 500000 == 0 && value) {
                    //if ((count * 5) % 20 == 0 && value) {
                    for (int i = 0; i < keys.length; i++) {
                        if (i == keys.length - 1) {
                            bufferedWriter.write(keys[i] + "(迁入)," + keys[i] + "(迁出),比对结果" + "\n");
                            //catalog.append(keys[i] + "(迁入)," + keys[i] + "(迁出),比对结果" + "\n");
                        } else {
                            bufferedWriter.write(keys[i] + "(迁入)," + keys[i] + "(迁出),比对结果,");
                            //catalog.append(keys[i] + "(迁入)," + keys[i] + "(迁出),比对结果" + "\n");
                        }
                    }
                }
            }
            for (int i = 0; i < gxids.size(); i++) {
                //long allCount = counts + (long) i;
                //获取第i条数据的所有属性名称
                //Set keys = gxids.get(i).keySet();
                String txDate = gxids.get(i).get("TXDATE").toString();
                String tranceNo = gxids.get(i).get("TRANCENO").toString();
                String tableName = "MIDDLETABLE" + threadSize;
                //HashMap valueTwo = dateSourceDao2.selectByDateAndTranceNo(txDate, tranceNo);
                HashMap valueTwo = dateSourceDao2.selectMidByDateAndTranceNo(tableName, txDate, tranceNo);
                /*if (valueTwo != null) {
                    bufferedWriter.write("-------第" + allCount + "条数据对比，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                    System.out.println("-------第" + allCount + "条数据对比，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                } else {
                    //该条数据主表中有从表中无
                    bufferedWriter.write("-------第" + allCount + "条数据对比,该条数据主表有从表无，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                    System.out.println("-------第" + allCount + "条数据对比，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                }*/
                log.info("============valueTwo:"+valueTwo);
                for (int j = 0; j < keys.length; j++) {
                    if (j == keys.length - 1) {
                        //当循环到keys最后一个属性时，结尾要换行
                        //根据属性名称获取属性值
                        Object keyValue = gxids.get(i).get(keys[j]);
                        if (keyValue == null || "".equals(keyValue)) {
                            //主表中第i条数据key对应的值为空
                            log.info("========主表中第" + i + "条数据" + keys[j] + "对应的值为空======================");
                            if (valueTwo != null) {
                                if (valueTwo.get(keys[j]) != null && !"".equals(valueTwo.get(keys[j]))) {
                                    //主表无，从表有，则为FALSE
                                    bufferedWriter.write(valueTwo.get(keys[j]) + ",无,false" + "\n");
                                    //bufferedWriter.write(key + ":false" + ",");
                                    //bufferedWriter.write("主表："+keyValue+",从表："+valueTwo.get(key)+ "\n");
                                } else {
                                    //主表无，从表无
                                    bufferedWriter.write("无,无,true" + "\n");
                                /*bufferedWriter.write(key + ":true" + ",");
                                bufferedWriter.write("主表："+keyValue+",从表："+valueTwo.get(key)+ "\n");*/
                                }
                            } else {
                                //主表无，从表无
                                bufferedWriter.write("无,无,true" + "\n");
                                //continue;
                            }
                        } else {
                            //主表中第i条数据key对应的值不为空
                            String valueMain = keyValue.toString();
                            //获取从表中第i条数据key对应的value值
                            String valueTran = "无";
                            if (valueTwo != null) {
                                if (valueTwo.get(keys[j]) != null && !"".equals(valueTwo.get(keys[j]))) {
                                    valueTran = valueTwo.get(keys[j]).toString();
                                }
                            }
                            log.info("========属性名：" + keys[j] + "，主表值：" + valueMain + "，从表值："+valueTran);
                            if (valueMain.equals(valueTran)) {
                                bufferedWriter.write(valueTran + "," + valueMain + ",true" + "\n");
                            /*bufferedWriter.write(key + ":true" + ",");
                            bufferedWriter.write("主表："+valueMain+",从表："+valueTran+ "\n");*/
                            } else {
                                bufferedWriter.write(valueTran + "," + valueMain + ",false" + "\n");
                            /*bufferedWriter.write(key + ":false" + "\n");
                            bufferedWriter.write("主表："+valueMain+",从表："+valueTran+ "\n");*/
                            }
                        }
                    } else {
                        //根据属性名称获取属性值
                        Object keyValue = gxids.get(i).get(keys[j]);
                        if (keyValue == null || "".equals(keyValue)) {
                            //主表中第i条数据key对应的值为空
                            log.info("========主表中第" + i + "条数据" + keys[j] + "对应的值为空======================");
                            if (valueTwo != null) {
                                if (valueTwo.get(keys[j]) != null && !"".equals(valueTwo.get(keys[j]))) {
                                    //主表无，从表有，则为FALSE
                                    bufferedWriter.write(valueTwo.get(keys[j]) + ",无,false,");
                                    //bufferedWriter.write(key + ":false" + ",");
                                    //bufferedWriter.write("主表："+keyValue+",从表："+valueTwo.get(key)+ "\n");
                                } else {
                                    //主表无，从表无
                                    bufferedWriter.write("无,无,true,");
                                /*bufferedWriter.write(key + ":true" + ",");
                                bufferedWriter.write("主表："+keyValue+",从表："+valueTwo.get(key)+ "\n");*/
                                }
                            } else {
                                //主表无，从表无
                                bufferedWriter.write("无,无,true,");
                                //continue;
                            }
                        } else {
                            //主表中第i条数据key对应的值不为空
                            String valueMain = keyValue.toString();
                            //获取从表中第i条数据key对应的value值
                            String valueTran = "无";
                            if (valueTwo != null) {
                                if (valueTwo.get(keys[j]) != null && !"".equals(valueTwo.get(keys[j]))) {
                                    valueTran = valueTwo.get(keys[j]).toString();
                                }
                            }
                            if (valueMain.equals(valueTran)) {
                                bufferedWriter.write(valueTran + "," + valueMain + ",true,");
                            /*bufferedWriter.write(key + ":true" + ",");
                            bufferedWriter.write("主表："+valueMain+",从表："+valueTran+ "\n");*/
                            } else {
                                bufferedWriter.write(valueTran + "," + valueMain + ",false,");
                            /*bufferedWriter.write(key + ":false" + "\n");
                            bufferedWriter.write("主表："+valueMain+",从表："+valueTran+ "\n");*/
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("===============报错："+e.getMessage());
            /*try {
                bufferedWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }*/
        } finally {
            // 处理完每批数据后后将临时清空
            this.size = 0;
            gxids.clear();
        }
    }


    public void end() {
        if (count != 0){
            //当主表中总数据小于5000条时，程序直接进入end（），需要给输出文件首行添加属性名称
            handle(count ,false);// 处理最后一批不到BATCH_SIZE的数据
        }else{
            handle(count ,true);// 处理最后一批不到BATCH_SIZE的数据
        }
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("===============报错："+e.getMessage());
        }
    }
}
