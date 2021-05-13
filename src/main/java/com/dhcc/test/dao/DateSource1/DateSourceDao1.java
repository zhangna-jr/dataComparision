package com.dhcc.test.dao.DateSource1;

import com.dhcc.test.util.GxidResultHandler;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public interface DateSourceDao1 {
    public List<HashMap> selectDate(@Param(value="sql")String sql, ResultHandler<HashMap> resultHandler);
}
