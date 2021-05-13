package com.dhcc.test.controller;

import com.dhcc.test.service.DateComparamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@Slf4j
public class DateComparamController {

    @Autowired
    private DateComparamService dateComparamService;


    @GetMapping(value = "/DateComparam")
    //@PostMapping(value = "/DateComparam")
    public boolean DateComparam(@RequestParam(value = "fileMain", required = false) MultipartFile fileMain,
                                @RequestParam(value = "fileTrans", required = false) MultipartFile fileTrans) {
        boolean value = dateComparamService.DateComparam(fileMain, fileTrans);
        return value;
    }
}
