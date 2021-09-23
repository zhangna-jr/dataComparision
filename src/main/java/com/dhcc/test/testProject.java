package com.dhcc.test;


import com.dhcc.test.service.DateComparamService;
import com.dhcc.test.util.TimerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class testProject {

    @Autowired
    DateComparamService dateComparamService;

    /**
     * Copyright: Copyright (c) 2020 东华软件股份公司
     * @Description: 将定时任务注入容器中
     * @author: zhangna
     * @date: 2021/2/8 17:12
     *
     */
    @Bean(name = { "timerUtils" }, initMethod = "creatTimer")
     public TimerUtil timerUtils() {
        return new TimerUtil(dateComparamService);//dateComparamService传入进去，解决空指针的错误
    }


    public static void main(String[] args) {
        SpringApplication.run(testProject.class,args);
    }




}
