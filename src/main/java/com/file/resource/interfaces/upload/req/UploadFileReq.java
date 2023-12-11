package com.file.resource.interfaces.upload.req;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/27 15:15
 */
@Data
@Builder
public class UploadFileReq {
    // 整个文件的md5
    @NotBlank(message = "md5不能为空")
    private String md5;
    // 切片索引-从0开始
    private Integer sliceIndex;
    // 总切片索引
    private Integer totalIndex;
    // 文件，如果是分片就是每个块文件
    @NotNull(message = "文件不能为空！")
    private MultipartFile file;
    // 是否分片-0：否，1：是
    @NotNull(message = "是否分片不能为空！")
    private Integer isSlice;
   // 文件后缀
    private String fileSuffix;


}
