package com.file.resource.app.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.file.resource.app.IDownloadProcess;
import com.file.resource.config.MinioConfig;
import io.minio.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
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

    @Override
    public ResponseEntity downloadFile(HttpServletResponse response, String md5, String fileType,
                                       String customUuid, String resourceUuid) {

        md5 = md5 + "." + fileType;
        ResponseEntity<byte[]> responseEntity = null;
        InputStream in = null;
        OutputStream outputStream = null;
        try {

            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(md5, "UTF-8"));

            in = minioClient.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(md5).build());
            //获取输出流进行写入数据
            outputStream = response.getOutputStream();
            // 将输入流复制到输出流
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            responseEntity = new ResponseEntity<byte[]>(buffer, HttpStatus.OK);
        } catch (Exception e) {
            log.error("下载文件异常|参数：md5：{},customUuid：{},resourceUuid:{}|异常原因：{},", md5, customUuid, resourceUuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("下载流文件关闭异常|参数：md5：{},customUuid{},resourceUuid:{}|异常原因：{},", md5, customUuid, resourceUuid, e);
            }
        }
        return responseEntity;
    }

    @Override
    public void downloadAll(Integer isValid) {
        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder().bucket(minioConfig.getBucketName()).build();

        findAndSaveFile(listObjectsArgs, isValid);
    }

    private void findAndSaveFile(ListObjectsArgs listObjectsArgs, Integer isValid) {
        String LOCAL_DIR = "Y:\\Public\\课程包";
        String objectName = null;
        Item item = null;

        File folder = new File(LOCAL_DIR);
        String[] fileList = folder.list();

        // 验证
        if (isValid == 1) {
            if (fileList.length == 168) {
                log.info("恭喜，校验成功，无遗漏");
            }
            try {
                // minio桶
                Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);
                for (Result<Item> result : results) {
                    item = result.get();
                    objectName = item.objectName();
                    boolean isExist = false;
                    for (int i = 0; i < fileList.length; i++) { // 遍历返回的字符数组
                        if (objectName.equals(fileList[i])) {
                            isExist = true;
                        }
                    }
                    if (!isExist) {
                        log.error("没有此本地文件：{} ，请下载！\n", item.objectName());
                    }
                }
            } catch (Exception e) {
                log.error("校验文件异常|参数{},item:{}|异常原因：{}", objectName, item, e);
            }
        } else
            // 批量下载
            try {
                // List all objects in the bucket
                Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);
                for (Result<Item> result : results) {
                    item = result.get();
                    if (item.isDir()) {
                        System.out.println("文件夹：" + item.objectName());
                        ListObjectsArgs args = ListObjectsArgs.builder().bucket(minioConfig.getBucketName()).prefix(item.objectName()).build();
                        findAndSaveFile(args, isValid);
                    } else {
                        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(item.objectName()).build();
                        objectName = item.objectName();
                        // Create a local file with the same name as the object
                        File file = new File(LOCAL_DIR + File.separator + objectName);
                        // Create parent directories if needed
                        file.getParentFile().mkdirs();
                        // Get the object as an input stream
                        try (InputStream stream = minioClient.getObject(getObjectArgs)) {
                            // Copy the input stream to the file
                            FileUtils.copyInputStreamToFile(stream, file);
                        }
                        log.info("文件：{} 下载成功！\n", item.objectName());
                    }
                }
            } catch (Exception e) {
                log.error("遍历下载桶内文件异常|参数{},item:{}|异常原因：{}", objectName, item, e);
            }
    }


    // 测试分段下载
//@GetMapping(value = "/downloadSlice")
    public void downloadSlice(@RequestParam String filename,
                              HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        if (StringUtils.isNotBlank(filename)) {
            log.info("download:" + filename);
            String range = request.getHeader("Range");
            log.info("current request rang:" + range);
            //获取文件信息
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder().bucket("default").object(filename).build());
            System.out.println(statObjectResponse);
            //开始下载位置
            long startByte = 0;
            //结束下载位置
            long endByte = statObjectResponse.size() - 1;
            log.info("文件开始位置：{}，文件结束位置：{}，文件总长度：{}", startByte, endByte, statObjectResponse.size());

            //有range的话
            if (StringUtils.isNotBlank(range) && range.contains("bytes=") && range.contains("-")) {
                range = range.substring(range.lastIndexOf("=") + 1).trim();
                String[] ranges = range.split("-");
                try {
                    //判断range的类型
                    if (ranges.length == 1) {
                        //类型一：bytes=-2343
                        if (range.startsWith("-")) {
                            endByte = Long.parseLong(ranges[0]);
                        }
                        //类型二：bytes=2343-
                        else if (range.endsWith("-")) {
                            startByte = Long.parseLong(ranges[0]);
                        }
                    }
                    //类型三：bytes=22-2343
                    else if (ranges.length == 2) {
                        startByte = Long.parseLong(ranges[0]);
                        endByte = Long.parseLong(ranges[1]);
                    }

                } catch (NumberFormatException e) {
                    startByte = 0;
                    endByte = statObjectResponse.size() - 1;
                    log.error("Range Occur Error, Message:" + e.getLocalizedMessage());
                }
            }

            //要下载的长度
            long contentLength = endByte - startByte + 1;
            //文件类型
            String contentType = request.getServletContext().getMimeType(filename);

            //解决下载文件时文件名乱码问题
            byte[] fileNameBytes = filename.getBytes(StandardCharsets.UTF_8);
            filename = new String(fileNameBytes, 0, fileNameBytes.length, StandardCharsets.ISO_8859_1);

            //各种响应头设置
            //支持断点续传，获取部分字节内容：
            response.setHeader("Accept-Ranges", "bytes");
            //http状态码要为206：表示获取部分内容
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setContentType(contentType);
            response.setHeader("Last-Modified", statObjectResponse.lastModified().toString());
            //inline表示浏览器直接使用，attachment表示下载，fileName表示下载的文件名
            response.setHeader("Content-Disposition", "inline;filename=" + filename);
            response.setHeader("Content-Length", String.valueOf(contentLength));
            //Content-Range，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
            response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + statObjectResponse.size());
            response.setHeader("ETag", "\"".concat(statObjectResponse.etag()).concat("\""));

            try {
                GetObjectResponse stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(statObjectResponse.bucket())
                                .object(statObjectResponse.object())
                                .offset(startByte)
                                .length(contentLength)
                                .build());
                BufferedOutputStream os = new BufferedOutputStream(response.getOutputStream());
                byte[] buffer = new byte[1024];
                int len;
                while ((len = stream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
                os.close();
                response.flushBuffer();
                log.info("下载完毕");
            } catch (ClientAbortException e) {
                log.warn("用户停止下载：" + startByte + "-" + endByte);
                //捕获此异常表示拥护停止下载
            } catch (IOException e) {
                e.printStackTrace();
                log.error("用户下载IO异常，Message：{}", e.getLocalizedMessage());
            }
        }
    }
}
