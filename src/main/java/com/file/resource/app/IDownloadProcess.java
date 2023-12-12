package com.file.resource.app;

import io.minio.errors.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @Author df
 * @Description: 下载流程
 * @Date 2023/12/1 11:09
 */
public interface IDownloadProcess {

    ResponseEntity downloadFile(String filename, String range, HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException, Exception;

}
