package com.file.resource.application;

import com.file.resource.types.common.Result;
import com.file.resource.interfaces.upload.req.InitSliceDataReq;
import com.file.resource.interfaces.upload.req.UploadFileReq;

/**
 * @Author df
 * @Description: 文件上传流程
 * @Date 2023/11/28 10:40
 */
public interface IFileUploadProcess {
    /**
     * 文件上传
     * 1.完整文件直接上传，上传完更新库的文件信息(resourceFile)
     * 2.分片上传需要判断是否全部都上传完，全部上传完则合并数据,
     * 合并后删除原来的分片数据，并更新库的文件信息（resourceFile）。
     *
     * @initSliceDataReq 初始化分片需要的参数
     */
    Result uploadFile(UploadFileReq uploadFileReq);

    /**
     * 初始化切片操作
     * 1.没有桶创建桶
     * 2.如果是分片缓存，后续分片上传需要
     * 3.先插入一条资源附件表
     *
     * @initSliceDataReq 初始化分片需要的参数
     */
    Result initFileData(InitSliceDataReq initSliceDataReq);
}
