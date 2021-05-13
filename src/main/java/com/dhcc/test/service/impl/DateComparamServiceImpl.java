package com.dhcc.test.service.impl;

import com.dhcc.test.dao.DateSource1.DateSourceDao1;
import com.dhcc.test.dao.DateSource2.DateSourceDao2;
import com.dhcc.test.service.DateComparamService;
import com.dhcc.test.util.FilesUtil;
import com.dhcc.test.util.GxidResultHandler;
import com.dhcc.test.util.SqlUser;
import com.dhcc.test.util.ThreadGxidResultHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class DateComparamServiceImpl implements DateComparamService {

    @Autowired
    private DateSourceDao1 dateSourceDao1;
    @Autowired
    private DateSourceDao2 dateSourceDao2;
    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    Calendar cal = Calendar.getInstance();
    Date date = cal.getTime();
    String dateTime = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS").format(date);
    String filename = dateTime.replace(":", "").replace("/", "") ;

    @Override
    public boolean DateComparam(MultipartFile fileOne, MultipartFile fileTwo) {
        //long a = System.currentTimeMillis();
        int count = dateSourceDao2.selectMiddleTable();
        //long b = System.currentTimeMillis();
        //System.out.println("查询中间表用了："+(b-a)+"毫秒");
        //System.out.println("=====count:"+count);
        if(count>0){
            dateSourceDao2.truncateTable();
            dateSourceDao2.dropTable();
        }
        BufferedReader brOne = null;
        BufferedReader brTwo = null;
        //BufferedWriter bufferedWriter = null;

        boolean value = false;
        try {
            brOne = new BufferedReader(new InputStreamReader(fileOne.getInputStream()));
            //bufferedWriter = new BufferedWriter(new FileWriter("outputfile/" + filename+ ".txt"));
            //获取fileOne文件中的sql
            String sqlOne = "";
            //获取fileTwo文件中的sql
            String sqlTwo = "";
            String dateOne;
            while ((dateOne = brOne.readLine()) != null) {
                if (dateOne.length() > 0) {
                    //dateOne = dateOne+" ";
                    sqlOne += dateOne + " ";
                }
            }
            brTwo = new BufferedReader(new InputStreamReader(fileTwo.getInputStream()));
            String dateTwo;
            while ((dateTwo = brTwo.readLine()) != null) {
                if (dateTwo.length() > 0) {
                    //dateTwo = dateTwo+" ";
                    sqlTwo += dateTwo + " ";
                }
            }
            FilesUtil filesUtil = new FilesUtil();
            //转移inputfile/fileMain.txt文件到history文件夹下
            filesUtil.moveFile("inputfile/fileMain.txt", "history/fileMain-" + filename + ".txt");
            //转移inputfile/fileTrans.txt文件到history文件夹下
            filesUtil.moveFile("inputfile/fileTrans.txt", "history/fileTrans-" + filename + ".txt");

            //创建中间表
            dateSourceDao2.createNewTableAndInsertData(sqlTwo);
            //创建联合唯一索引
            dateSourceDao2.createIndex();


            //根据fileMain中sql查询数据
            GxidResultHandler gxidResultHandler = new GxidResultHandler(dateSourceDao2);
            HashMap sql = new HashMap();
            sql.put("sql",sqlOne);
            sqlSessionTemplate.select("com.dhcc.test.dao.DateSource1.DateSourceDao1.selectDate",sql,gxidResultHandler);
            gxidResultHandler.end();
            //根据fileTrans文件sql建表(middleTable)并将sql中查询的数据登记进表中
            /*try{
                dateSourceDao2.createNewTableAndInsertData(sqlTwo);
                //创建联合唯一索引
                dateSourceDao2.createIndex();
                for (int i = 0; i < valueOnes.size(); i++) {
                    //获取第i条数据的所有属性名称
                    Set keys = valueOnes.get(i).keySet();
                    String txDate = valueOnes.get(i).get("TXDATE").toString();
                    String tranceNo = valueOnes.get(i).get("TRANCENO").toString();
                    HashMap valueTwo = dateSourceDao2.selectByDateAndTranceNo(txDate, tranceNo);
                    if (valueTwo != null) {
                        bufferedWriter.write("-------第" + i + "条数据对比，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                    } else {
                        //该条数据主表中有从表中无
                        bufferedWriter.write("-------第" + i + "条数据对比,该条数据主表有从表无，txdate:" + txDate + ",tranceNo:" + tranceNo + "-----" + "\n");
                    }
                    for (Object key : keys) {
                        //根据属性名称获取属性值
                        Object keyValue = valueOnes.get(i).get(key);
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
                value = true;
            }catch(Exception e){
                e.printStackTrace();
                //清除表数据
                //dateSourceDao2.truncateTable();
                //删除表
                //dateSourceDao2.dropTable();
                return false;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                brOne.close();
                brTwo.close();
                //bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    @Override
    public boolean ThreadDateComparam(MultipartFile fileOne, MultipartFile fileTwo) {
        try{
            List<String> midTables = dateSourceDao2.selectAllMiddleTable();
            if (midTables.size()>0){
                for (String midTable:midTables) {
                    dateSourceDao2.truncateMidTable(midTable);
                    dateSourceDao2.dropMidTable(midTable);
                }
            }
            BufferedReader brOne = null;
            BufferedReader brTwo = null;
            try {
                brOne = new BufferedReader(new InputStreamReader(fileOne.getInputStream()));
                //获取fileOne文件中的sql
                List<String> sqlOneList = new ArrayList<>();
                //获取fileTwo文件中的sql
                List<String> sqlTwoList = new ArrayList<>();
                String sqlOne = "";
                String sqlTwo = "";
                String dateOne;
                while ((dateOne = brOne.readLine()) != null) {
                    if (dateOne.length() > 0 ) {
                        //String substring = dateOne.substring(0, dateOne.indexOf(";"));
                        sqlOne += dateOne + " ";
                        //sqlOneList.add(dateOne);
                    }
                }
                brTwo = new BufferedReader(new InputStreamReader(fileTwo.getInputStream()));
                String dateTwo;
                while ((dateTwo = brTwo.readLine()) != null) {
                    if (dateTwo.length() > 0) {
                        sqlTwo += dateTwo + " ";
                        //sqlTwoList.add(dateTwo);
                    }
                }
                sqlOneList = Arrays.asList(sqlOne.split(";"));
                sqlTwoList = Arrays.asList(sqlTwo.split(";"));
                FilesUtil filesUtil = new FilesUtil();
                //转移inputfile/fileMain.txt文件到history文件夹下
                filesUtil.moveFile("inputfile/fileMain.txt", "history/fileMain-" + filename + ".txt");
                //转移inputfile/fileTrans.txt文件到history文件夹下
                filesUtil.moveFile("inputfile/fileTrans.txt", "history/fileTrans-" + filename + ".txt");
                if (sqlOneList.size() != sqlTwoList.size()){
                    System.out.println("==================主表与从表sql条数不对等====================");
                    return false;
                }

                //创建中间表
                for (int i=0;i<sqlTwoList.size()-1;i++) {
                    final List<String> finalSqlTwoList = sqlTwoList;
                    final List<String> finalSqlOneList = sqlOneList;
                    final int finalI1 = i;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            log.info("============="+Thread.currentThread().getName()+"开始了=========="+"finalI1:"+finalI1);
                            String tableName = "MIDDLETABLE"+ finalI1;
                            //long a = System.currentTimeMillis();
                            //创建中间表
                            dateSourceDao2.createNewTablesAndInsertData(finalSqlTwoList.get(finalI1),tableName);
                            //long b = System.currentTimeMillis();
                            //System.out.println("=============创建中间表用了："+(b-a)+"毫秒================");
                            //创建联合唯一索引
                            String indexName = "MIDDLETABLE_CON"+ finalI1;
                            dateSourceDao2.createIndexs(indexName,tableName);
                            //long c = System.currentTimeMillis();
                            //System.out.println("=============创建索引用了："+(c-b)+"毫秒================");
                            String mainName = splitStringDate(finalSqlOneList.get(finalI1).toLowerCase(), "from", "where");
                            String transName = splitStringDate(finalSqlTwoList.get(finalI1).toLowerCase(), "from", "where");
                            String outPutFileName = mainName+"-"+transName;
                            ThreadGxidResultHandler gxidResultHandler = new ThreadGxidResultHandler(dateSourceDao2,finalI1,outPutFileName);
                            HashMap sql = new HashMap();
                            sql.put("sql",finalSqlOneList.get(finalI1));
                            sqlSessionTemplate.select("com.dhcc.test.dao.DateSource1.DateSourceDao1.selectDate",sql,gxidResultHandler);
                            gxidResultHandler.end();
                            System.out.println("============="+Thread.currentThread().getName()+"结束==========");

                        }
                    },"线程"+i).start();
                }

            }  catch (Exception e) {
                e.printStackTrace();
                log.info(e.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();
            log.info(e.getMessage());
        }
        return false;
    }

    private String splitStringDate(String originDate,String startDate,String endDate){
        int strStartIndex = originDate.indexOf(startDate);
        int strEndIndex = originDate.indexOf(endDate);
        if (strStartIndex < 0) {
            System.out.println("字符串 :---->" + originDate + "<---- 中不存在 " + startDate + ", 无法截取目标字符串");
            return "false";
        }
        if (strEndIndex < 0) {
            String str1=originDate.substring(0, strStartIndex);
            String[] split=originDate.substring(str1.length(), originDate.length()).split("\\s+");
            return split[1];
        }
        String[] split = originDate.substring(strStartIndex, strEndIndex).split("\\s+");
        if (split.length>0){
            return  split[1];
        }
        return "false";

    }

}
