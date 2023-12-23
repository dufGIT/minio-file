package com.file.resource.application.service;

import com.file.resource.types.common.Constants;
import com.file.resource.types.common.Result;
import com.file.resource.application.IFileUploadProcess;
import com.file.resource.app.MinioConfig;
import com.file.resource.domain.resource.TResourceService;
import com.file.resource.domain.upload.IFileUploadBusiness;
import com.file.resource.infrastructure.po.TResourceFile;
import com.file.resource.interfaces.upload.req.InitSliceDataReq;
import com.file.resource.interfaces.upload.req.UploadFileReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/28 10:40
 */
@Slf4j
@Service
public class FileUploadProcessImpl implements IFileUploadProcess {

    @Resource
    private IFileUploadBusiness fileUploadBusiness;
    @Resource
    private TResourceService resourceFileService;
    @Resource
    private MinioConfig minioConfig;


    @Override
    public Result uploadFile(UploadFileReq uploadFileReq) {
        //--------------------文件-------------------------------
        MultipartFile file = uploadFileReq.getFile();
        //--------------------资源-------------------------------
        String md5 = uploadFileReq.getMd5();
        Integer totalIndex = uploadFileReq.getTotalIndex();
        Integer sliceIndex = uploadFileReq.getSliceIndex();

        String fullMd5Name = md5 + "." + uploadFileReq.getFileSuffix();

        // 文件上传-不分片
        if (uploadFileReq.getIsSlice() == 0) {
            log.debug("完整文件上传处理-------------");
            Boolean isWholeUpload = fileUploadBusiness.uploadFile(fullMd5Name, file);
            if (!isWholeUpload) {
                // 上传失败
                return Result.failed(Constants.ResponseCode.UPLOAD_FILE_FAILED.getCode(),
                        Constants.ResponseCode.UPLOAD_FILE_FAILED.getMsg());
            }
            // 更新已上传状态信息到库里
            Boolean isUpdateSucc = resourceFileService.updateFileUploadState(md5);
            if (!isUpdateSucc) return Result.failed(Constants.ResponseCode.UPLOAD_UPDATEDB_FAILED.getCode(),
                    Constants.ResponseCode.UPLOAD_UPDATEDB_FAILED.getMsg());
            return Result.success();
        }

        // 文件上传--分片
        if (uploadFileReq.getIsSlice() == 1) {
            log.debug("分片文件上传处理-------------");
            // 分片上传
            Result sliceResult = fileUploadBusiness.uploadSliceFile(md5, totalIndex, sliceIndex, file);
            if (sliceResult != null) {
                String code = sliceResult.getCode();
                String sliceIndexStr = String.valueOf(sliceResult.getData());
                // 完成所有的分片上传
                if (code.equals(Constants.ResponseCode.SUCCESS.getCode())
                        && "-1".equals(sliceIndexStr)) {
                    log.debug("已完成分片文件上传处理-------------");
                    // 合并文件
                    Boolean composeSuccess = fileUploadBusiness.composeFile(md5,fullMd5Name, totalIndex);

                    // 文件合并失败提示
                    if (!composeSuccess) {
                        return Result.failed(Constants.ResponseCode.UPLOAD_COMPOSE_FAILED.getCode(),
                                Constants.ResponseCode.UPLOAD_COMPOSE_FAILED.getMsg());
                    }

                    // 更新已上传状态信息到库里
                    Boolean isUpdateSucc = resourceFileService.updateFileUploadState(md5);
                    if (!isUpdateSucc) return Result.failed(Constants.ResponseCode.UPLOAD_UPDATEDB_FAILED.getCode(),
                            Constants.ResponseCode.UPLOAD_UPDATEDB_FAILED.getMsg());

                    // TODO 是否开一个线程单独处理它
                    // 删除minio所有的分片文件
                    fileUploadBusiness.delSliceFile(md5, totalIndex);
                }
            }
            return sliceResult;
        }
        return Result.success();

    }

    /**
     * 初始化文件信息操作
     */
    @Override
    public Result initFileData(InitSliceDataReq initSliceDataReq) {
        Integer isSlice = initSliceDataReq.getIsSlice();
        // 判断是否有minio桶，没有先创建桶
        Boolean isCreateBucket = fileUploadBusiness.createFileBucket();
        if (!isCreateBucket) return Result.failed(Constants.ResponseCode.UPLOAD_BUCKET_FAILED.getCode(),
                Constants.ResponseCode.UPLOAD_BUCKET_FAILED.getMsg());

        // 初始化切片放入缓存
        if (isSlice == 1) {
            Boolean isInRedis = fileUploadBusiness.sliceInRedis(initSliceDataReq.getMd5(), initSliceDataReq.getTotalSlice());
            if (!isInRedis) return Result.failed(Constants.ResponseCode.UPLOAD_INIT_SLICE_FAILED.getCode(),
                    Constants.ResponseCode.UPLOAD_INIT_SLICE_FAILED.getMsg());
        }

        // 文件名称
        String fileName = initSliceDataReq.getOriginalFileName();
        // 是否存在文件后缀
        Integer isExistStr = fileName.lastIndexOf(".");
        // 获取文件后缀
        String fileType = isExistStr != -1 ? fileName.substring(isExistStr + 1) : "";
        // 判断是否已存库
        Boolean isExist = resourceFileService.checkIsExistFile(initSliceDataReq.getMd5());
        if (!isExist) {
            // 上传前先存储库
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            TResourceFile resourceFile = TResourceFile.builder()
                    .isUploaded(0)
                    .uuid(uuid)
                    .minioMd5(initSliceDataReq.getMd5())
                    .minioBucket(minioConfig.getBucketName())
                    .totalSlice(isSlice == 0 ? 0 : initSliceDataReq.getTotalSlice())
                    .originalFileName(initSliceDataReq.getOriginalFileName())
                    .sliceFileSize(isSlice == 0 ? 0 : initSliceDataReq.getSliceFileSize())
                    .fileSize(initSliceDataReq.getFileSize())
                    .createdTime(new Date()).build();
            Boolean isFileInfoDB = resourceFileService.insertFile(resourceFile);
            if (!isFileInfoDB) return Result.failed();
        }
        log.info("初始化成功！|initFileData|参数：initSliceDataReq:{}", initSliceDataReq);
        return Result.success();
    }
}
