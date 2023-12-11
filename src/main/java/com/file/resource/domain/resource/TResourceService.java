package com.file.resource.domain.resource;

import com.file.resource.infrastructure.po.TResourceFile;

/**
 * @Author df
 * @Description: TODO
 * @Date 2023/11/29 14:33
 */
public interface TResourceService {
    /**
     * 新增ResourceFile数据
     *
     * @param resourceFile 实例对象
     * @return 实例对象
     */
    Boolean insertFile(TResourceFile resourceFile);

    Boolean checkIsExistFile(String md5);

    /**
     * 更新数据库文件为已上传
     *
     * @param md5          文件的md5
     * @return 实例对象
     */
    Boolean updateFileUploadState(
            String md5);

}
