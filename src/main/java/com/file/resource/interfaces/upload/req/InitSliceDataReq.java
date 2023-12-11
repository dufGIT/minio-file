package com.file.resource.interfaces.upload.req;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/29 9:43
 */
@Data
@Builder
public class InitSliceDataReq {
    // 整个文件的md5
    @NotBlank(message = "md5不能为空")
    private String md5;
    // 总分片个数
    private Integer totalSlice;
    // 是否分片 0：不分片 1:分片
    @NotNull(message = "是否分片不能为空")
    private Integer isSlice;
    // 源文件名称
    @NotBlank(message = "文件名称不能为空")
    private String originalFileName;
    // 文件大小
    @NotNull(message = "文件size不能为空")
    private Double fileSize;
    // 分片的文件大小
    private Double sliceFileSize;
}
