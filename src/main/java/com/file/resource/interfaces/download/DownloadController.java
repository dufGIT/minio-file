package com.file.resource.interfaces.download;

import com.file.resource.app.IDownloadProcess;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/30 18:15
 */
@RestController
public class DownloadController {


    @Resource
    private IDownloadProcess downloadProcess;


    @GetMapping("/download/{resourceUuid}/{customUuid}/{fileType}/{md5}")
    public ResponseEntity downloadFile(HttpServletResponse response,
                                       @PathVariable("resourceUuid") String resourceUuid,
                                       @PathVariable("customUuid") String customUuid,
                                       @PathVariable("fileType") String fileType,
                                       @PathVariable("md5") String md5) {
        return downloadProcess.downloadFile(response, md5, fileType, customUuid, resourceUuid);
    }

    // isValid 1:是，0:否
    @GetMapping("/downloadAll")
    public void downloadAll(Integer isValid) {
        downloadProcess.downloadAll(isValid);
    }


}
