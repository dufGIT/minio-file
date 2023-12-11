package com.file.resource.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.file.resource.infrastructure.po.TResourceFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author df
 * @Description: TODO
 * @Date 2023/11/29 14:21
 */
@Mapper
public interface TResourceMapper extends BaseMapper<TResourceFile> {
}
