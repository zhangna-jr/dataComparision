package com.dhcc.test.dao.DateSource2;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public interface DateSourceDao2 {
    //List<HashMap> selectDate(@Param(value="sql")String sql);
    int createNewTableAndInsertData(@Param(value="sql")String sql);
    int createNewTablesAndInsertData(@Param(value="sql")String sql,@Param(value="tableName")String tableName);
    int truncateTable();
    int dropTable();
    int truncateMidTable(@Param(value="midTable")String midTable);
    int dropMidTable(@Param(value="midTable")String midTable);
    HashMap selectByDateAndTranceNo(@Param(value="txDate") String txDate, @Param(value="tranceNo")String tranceNo);
    List<HashMap> selectAll();
    int createIndex();
    int createIndexs(@Param(value="indexName")String indexName,@Param(value="tableName")String tableName);
    int selectMiddleTable();
    HashMap selectMidByDateAndTranceNo(@Param(value="tableName") String tableName,@Param(value="txDate") String txDate, @Param(value="tranceNo")String tranceNo);
    List<String> selectAllMiddleTable();
}
