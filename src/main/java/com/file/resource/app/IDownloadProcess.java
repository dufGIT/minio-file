package com.file.resource.app;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author df
 * @Description: 下载流程
 * @Date 2023/12/1 11:09
 */
public interface IDownloadProcess {

    ResponseEntity downloadFile(HttpServletResponse response, String md5,
                                String fileType,
                                String customUuid,
                                String resourceUuid);


    void downloadAll(Integer isValid);
}
