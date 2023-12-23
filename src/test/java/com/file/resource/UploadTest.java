package com.file.resource;

import com.file.resource.application.IFileUploadProcess;
import com.file.resource.types.common.Result;
import com.file.resource.interfaces.upload.req.InitSliceDataReq;
import com.file.resource.interfaces.upload.req.UploadFileReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/30 9:54
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = FileResourceApplication.class)
public class UploadTest {
    @Resource
    private IFileUploadProcess fileUploadProcess;

    // 获取文件流的md5
    @Test
    public void getWholeFileMd5() {
        // 创建File对象
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("D:\\teach-split\\image.png");
            String md5Hex = DigestUtils.md5Hex(inputStream);
            log.info("获取文件的md5：{}", md5Hex);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO 先初始化——分片文件版本
    @Test
    public void init_slice_file() {
        InitSliceDataReq initSliceDataReq = InitSliceDataReq.builder()
                .md5("880576b5104a0ce4931c2a5c8c547000")
                .isSlice(1)
                .totalSlice(5)
                .originalFileName("test.vedio.mp4")
                .sliceFileSize(9814662.00)
                .fileSize(49073311.00)
                .build();

        Result result = fileUploadProcess.initFileData(initSliceDataReq);
        log.info("初始化结果--分片文件版本：{}", result);
    }

    // TODO 再上传文件--分片文件
    @Test
    public void slice_upload_file() {
        try {
            // 读取本地文件
            byte[] fileContent = FileUtils.readFileToByteArray(new File("D:\\teach-split\\video.mp4.4"));
            // 读取本地文件
            // 转换为MultiPartFile对象
            MultipartFile multipartFile = new MockMultipartFile("880576b5104a0ce4931c2a5c8c547000", "test.vedio.mp4",
                    "application/octet-stream", fileContent);
            UploadFileReq uploadFileReq = UploadFileReq.builder()
                    .md5("880576b5104a0ce4931c2a5c8c547000")
                    .fileSuffix("mp4")
                    .isSlice(1)
                    .totalIndex(5)
                    .sliceIndex(4)
                    .file(multipartFile).build();
            Result result = fileUploadProcess.uploadFile(uploadFileReq);
            log.info("文件上传--分片上传结果：{}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO 先初始化——完整文件版本
    @Test
    public void init_whole_file() {
        InitSliceDataReq initSliceDataReq = InitSliceDataReq.builder()
                .md5("a9500aa2091875f3d02a9b84ae1ab712")
                .isSlice(0)
                .originalFileName("image.png")
                .fileSize(1540702.00)
                .build();
        Result result = fileUploadProcess.initFileData(initSliceDataReq);
        log.info("初始化结果--完整文件版本：{}", result);
    }

    // TODO 再上传文件--完整文件
    @Test
    public void whole_upload_file() {
        try {
            // 读取本地文件
            byte[] fileContent = FileUtils.readFileToByteArray(new File("D:\\teach-split\\image.png"));
            // 读取本地文件
            // 转换为MultiPartFile对象
            MultipartFile multipartFile = new MockMultipartFile("a9500aa2091875f3d02a9b84ae1ab712", "image.png",
                    "multipart/form-data", fileContent);

            UploadFileReq uploadFileReq = UploadFileReq.builder()
                    .md5("a9500aa2091875f3d02a9b84ae1ab712")
                    .isSlice(0)
                    .fileSuffix("png")
                    .file(multipartFile).build();
            Result result = fileUploadProcess.uploadFile(uploadFileReq);
            log.info("文件上传--完整文件上传结果：{}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
