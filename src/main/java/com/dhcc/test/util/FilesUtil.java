package com.dhcc.test.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FilesUtil {

    /**
     * Copyright: Copyright (c) 2020 东华软件股份公司
     * @Description: File转MultipartFile
     * @author: zhangna
     * @date: 2021/2/8 15:32
     *
     */
    public List<MultipartFile> getFiles(){
        File fileMain = new File(".","/inputfile/fileMain.txt");
        File fileTrans = new File(".","/inputfile/fileTrans.txt");
        //File fileDelete = new File(".","/inputfile/fileDelete.txt");
        boolean fileMainVali = fileMain.exists();
        boolean fileTransVali = fileTrans.exists();
        //boolean fileDeleteVali = fileDelete.exists();
        List<MultipartFile> multipartFiles = new ArrayList<>();
        if(fileMainVali && fileTransVali ){
            FileItem fileMainItem = createFileItem(fileMain);
            FileItem fileTransItem = createFileItem(fileTrans);
            //FileItem fileDeleteItem = createFileItem(fileDelete);
            MultipartFile multipartFileMain = new CommonsMultipartFile(fileMainItem);
            MultipartFile multipartFileTrans = new CommonsMultipartFile(fileTransItem);
            //MultipartFile multipartFileDelete = new CommonsMultipartFile(fileDeleteItem);
            multipartFiles.add(multipartFileMain);
            multipartFiles.add(multipartFileTrans);
            //multipartFiles.add(multipartFileDelete);
        }
        return multipartFiles;
    }

    private FileItem createFileItem(File file) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = (FileItem) factory.createItem("textField", "text/plain", true, file.getName());
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * Copyright: Copyright (c) 2020 东华软件股份公司
     * @Description: 转移文件
     * @author: zhangna
     * @date: 2021/2/8 17:24
     *
     */
    public boolean moveFile(String startPath,String endPath) throws Exception {
        try{
            File oldpaths = new File(startPath);
            File newpaths = new File(endPath);
            if (!newpaths.exists()) {
                Files.copy(oldpaths.toPath(), newpaths.toPath());
                oldpaths.delete();
                System.out.println("文件移动成功！起始路径：" + startPath);
            } else {
                newpaths.delete();
                Files.copy(oldpaths.toPath(), newpaths.toPath());
                System.out.println("文件移动成功！起始路径：" + startPath);
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        }
        return true;
    }
}
