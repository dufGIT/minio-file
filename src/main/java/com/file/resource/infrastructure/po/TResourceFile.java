package com.file.resource.infrastructure.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/29 13:49
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TResourceFile   implements Serializable {
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id ;
    /** 标识列 */
    private String uuid ;
    /** 文件原始名称 */
    private String originalFileName ;
    /** 分片的大小 */
    private Double sliceFileSize;
    /** 文件大小 */
    private Double fileSize ;
    /** 文件存储minio地址 */
    private String minioPath ;
    /** 总分片;0代表没有分片是完整的文件 */
    private Integer totalSlice ;
    /** 文件minioMd5 */
    private String minioMd5 ;
    /** minio桶名称 */
    private String minioBucket ;
    /** 是否上传;0：未上传，1：已上传 */
    private Integer isUploaded ;
    /** 创建时间 */
    private Date createdTime ;
    /** 更新时间 */
    private Date updatedTime ;
    /** 更新人 */
    private String updatedBy ;
    /** 创建人 */
    private String createdBy ;
}
