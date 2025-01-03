package com.taiwei.reggie.controller;

import com.taiwei.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * upload download
 */
@Slf4j
@RestController
@RequestMapping("/common")

public class CommonController {

    @Value("${reggie.image_path}")
    private String basePath;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info(file.toString());
        String oriFilename = file.getOriginalFilename();
        String suffix = oriFilename.substring(oriFilename.lastIndexOf('.'));
        String fileName = oriFilename.substring(0, oriFilename.lastIndexOf('.'))+"_"+UUID.randomUUID().toString() + suffix;

        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        try {
            //file.transferTo(new File("/tmp/reggie_images/{}",file.getName()));
            file.transferTo(new File(basePath+fileName));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));

            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            int len =0;
            byte[] bytes =new byte[1024];

            while((len = fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes, 0,len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
