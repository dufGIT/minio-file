package com.file.resource.domain.resource.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Author df
 * @Description:
 * @Date 2023/12/1 18:14
 */
@Data
public class TResourceCustomAuthDTO {
    private Long id;
    /**
     * 权限结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date authEndTime;
    /**
     * 权限开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date authStartTime;
    /**
     * 接收方名称
     */
    private String didName;

    /**
     * 接收方标识（joy的机构id）
     */
    private String did;

    /**
     * 客户uuid
     */
    private String customUuid;
}
