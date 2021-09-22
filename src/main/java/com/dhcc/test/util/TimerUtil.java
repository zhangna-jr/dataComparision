package com.dhcc.test.util;

import com.dhcc.test.service.DateComparamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class TimerUtil {

    @Autowired
    public DateComparamService dateComparamService;

    public TimerUtil(DateComparamService ct){
        dateComparamService = ct;
    }

    public void creatTimer(){
        Timer timer = new Timer();
        int i = 0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Date date = new Date(this.scheduledExecutionTime());
                //System.out.println("本次执行该线程的时间为：" + date);
                FilesUtil filesUtil = new FilesUtil();
                List<MultipartFile> multipartFiles = filesUtil.getFiles();
                if (multipartFiles.size() > 0) {
                    //dateComparamService.DateComparam(multipartFiles.get(0), multipartFiles.get(1));
                    dateComparamService.ThreadDateComparam(multipartFiles.get(0), multipartFiles.get(1));
                }else{
                    log.info("----------inputfile文件夹中未找到文件-----");
                }
                // 程序启动之后等待5000毫秒开始run()方法，每隔180000毫秒执行一次。
            }
            //},5000,3000);
        },5000,180000);
    }

}
