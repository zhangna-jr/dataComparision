package com.dhcc.test.service;

import org.springframework.web.multipart.MultipartFile;

public interface DateComparamService {
    public boolean DateComparam(MultipartFile fileOne,MultipartFile fileTwo);
    public boolean ThreadDateComparam(MultipartFile fileOne,MultipartFile fileTwo);

}
