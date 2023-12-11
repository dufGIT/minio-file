package com.file.resource.domain.resource.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.file.resource.domain.resource.TResourceService;
import com.file.resource.infrastructure.mapper.TResourceMapper;
import com.file.resource.infrastructure.po.TResourceFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author df
 * @Description: 资源附件表
 * @Date 2023/11/29 14:34
 */
@Slf4j
@Service
public class TResourceServiceImpl implements TResourceService {
    @Resource
    private TResourceMapper tResourceFileMapper;

    @Override
    public Boolean insertFile(TResourceFile resourceFile) {
        try {
            resourceFile.setCreatedTime(new Date());
            tResourceFileMapper.insert(resourceFile);
            return true;
        } catch (Exception e) {
            log.error("保存tResourceFile异常|参数:{}|{}", resourceFile, e);
            return false;
        }
    }

    @Override
    public Boolean checkIsExistFile(String md5) {
        QueryWrapper<TResourceFile> wrapper = new QueryWrapper<>();
        wrapper.eq("minio_md5", md5);
        Long existNum = tResourceFileMapper.selectCount(wrapper);
        if (existNum > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean updateFileUploadState(String md5) {
        try {
            // 设置需要更改的参数
            UpdateWrapper<TResourceFile> wrapper = new UpdateWrapper<>();
            wrapper.eq("minio_md5", md5)
                    .set("is_uploaded", 1);
            log.debug("更新已上传文件状态到DB|updateFileUploadState|参数:resourceUuid:{}，md5:{}", md5);
            tResourceFileMapper.update(new TResourceFile(), wrapper);
            return true;
        } catch (Exception e) {
            log.error("更新tResourceFile异常|参数:{},{}|{}", md5, e);
            return false;
        }
    }

}
