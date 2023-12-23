package com.file.resource.interfaces.download;

import com.file.resource.application.IDownloadProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/30 18:15
 */
@Slf4j
@RestController
public class DownloadController {


    @Resource
    private IDownloadProcess downloadProcess;

    // http://localhost:8082/download?filename=a9500aa2091875f3d02a9b84ae1ab712.mp4&range=bytes=0-52428800
    // 分段下载的化，支持断点下载，暂停下载，断网恢复下载等。
    // 我测试就采取这种方式RequestParam，大家真实场景可以放到header里 @RequestHeader(name = "Range", required = false) String range,
    @GetMapping("/download")
    public ResponseEntity downloadFile(@RequestParam String filename,
                                       @RequestParam(required = false) String range,
                                       HttpServletRequest request, HttpServletResponse response) {
        try {
            return downloadProcess.downloadFile(filename, range, request, response);
        } catch (Exception e) {
            log.error("下载异常|参数：{}，{}|{}", filename, range, e);
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
