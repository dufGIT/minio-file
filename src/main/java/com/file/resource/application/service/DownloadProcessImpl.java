package com.file.resource.application.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.file.resource.application.IDownloadProcess;
import com.file.resource.app.MinioConfig;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @Author df
 * @Description:
 * @Date 2023/12/1 11:09
 */
@Slf4j
@Service
public class DownloadProcessImpl implements IDownloadProcess {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioConfig minioConfig;

    // 完整文件与分片文件下载
    @Override
    public ResponseEntity downloadFile(String filename, String range, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ResponseEntity<byte[]> responseEntity = null;
        BufferedOutputStream os = null;
        GetObjectResponse stream = null;
        if (StringUtils.isNotBlank(filename)) {
            log.info("要下载的文件:{}", filename);
            //String range = request.getHeader("Range");
            log.info("current request rang:{}", range);
            // 获取桶里文件信息
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filename)
                            .build());
            //开始下载位置
            long startByte = 0;
            //结束下载位置
            long endByte = statObjectResponse.size() - 1;
            log.info("文件开始位置：{}，文件结束位置：{}，文件总长度：{}", startByte, endByte, statObjectResponse.size());

            // 有range的话,需要根据前端下载长度进行下载，也就是分段下载
            // 例如：range=bytes=0-52428800
            if (StringUtils.isNotBlank(range) && range.contains("bytes=") && range.contains("-")) {
                range = range.substring(range.lastIndexOf("=") + 1).trim();
                String[] ranges = range.split("-");
                //判断range的类型
                if (ranges.length == 1) {
                    //类型一：bytes=-2343
                    if (range.startsWith("-")) endByte = Long.parseLong(ranges[0]);

                    //类型二：bytes=2343-
                    if (range.endsWith("-")) startByte = Long.parseLong(ranges[0]);

                }
                //类型三：bytes=22-2343
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }
            }

            //要下载的长度
            long contentLength = endByte - startByte + 1;
            //文件类型
            String contentType = request.getServletContext().getMimeType(filename);

            //解决下载文件时文件名乱码问题
            byte[] fileNameBytes = filename.getBytes(StandardCharsets.UTF_8);
            filename = new String(fileNameBytes, 0, fileNameBytes.length, StandardCharsets.ISO_8859_1);

            //各种响应头设置---------------------------------------------------------------------------------------------
            //支持断点续传，获取部分字节内容：
            response.setHeader("Accept-Ranges", "bytes");
            //http状态码要为206：表示获取部分内容,SC_PARTIAL_CONTENT,部分浏览器不支持，所以改成SC_OK
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(contentType);
            response.setHeader("Last-Modified", statObjectResponse.lastModified().toString());
            //inline表示浏览器直接使用，attachment表示下载，fileName表示下载的文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            response.setHeader("Content-Length", String.valueOf(contentLength));
            //Content-Range，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
            response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + statObjectResponse.size());
            response.setHeader("ETag", "\"".concat(statObjectResponse.etag()).concat("\""));
            response.setContentType("application/octect-stream;charset=UTF-8");


            try {
                // 获取文件流
                stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(statObjectResponse.bucket())
                                .object(statObjectResponse.object())
                                .offset(startByte)
                                .length(contentLength)
                                .build());
                os = new BufferedOutputStream(response.getOutputStream());
                // 将读取的文件写入到OutputStream
                byte[] buffer = new byte[1024];
                long bytesWritten = 0;
                int bytesRead = -1;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    if (bytesWritten + bytesRead > contentLength) {
                        os.write(buffer, 0, (int) (contentLength - bytesWritten));
                        break;
                    } else {
                        os.write(buffer, 0, bytesRead);
                        bytesWritten += bytesRead;
                    }
                }
                os.flush();
                response.flushBuffer();
                log.info("下载完毕");
                // 返回对应http状态
                responseEntity = new ResponseEntity<byte[]>(buffer, HttpStatus.OK);
            } finally {
                if (os != null) os.close();
                if (stream != null) stream.close();
            }
        }
        return responseEntity;
    }
}
