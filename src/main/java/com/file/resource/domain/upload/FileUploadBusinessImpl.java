package com.file.resource.domain.upload;


import com.file.resource.types.common.Constants;
import com.file.resource.types.common.Result;
import com.file.resource.app.MinioConfig;
import com.file.resource.infrastructure.RedisDao;
import com.file.resource.util.MinioUtils;
import io.minio.ComposeSource;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/28 10:33
 */
@Slf4j
@Service
public class FileUploadBusinessImpl implements IFileUploadBusiness {


    @Resource
    private MinioUtils minioUtils;

    @Resource
    private RedisDao redisDao;

    @Resource
    private MinioConfig minioConfig;

    /**
     * 完整文件上传
     */
    @Override
    public Boolean uploadFile(String objectName, MultipartFile file) {
        //  文件上传
        return minioUtils.uploadFile(minioConfig.getBucketName(), objectName, null, file);
    }

    /**
     * 把文件块上传到分片桶
     *
     * @return Integer 要上传的分片数，-1是上传完成
     */
    @Override
    public Result uploadSliceFile(String objectName, Integer totalPieces, Integer sliceIndex, MultipartFile file) {
        // 分片上传完毕标志
        Integer isUploaded = -1;
        // 无需合并标志
        Integer isCompose = -2;

        Object sliceIndexRedis = redisDao.indexValue(objectName, 0);
        String sliceIndexStr = String.valueOf(sliceIndexRedis);
        if (!"".equals(sliceIndexRedis) && null != sliceIndexRedis) {
            Integer sliceIndexRedisInt = Integer.parseInt(sliceIndexStr);
            // 索引不一致，要求一致
            if (sliceIndexRedisInt != sliceIndex) {
                return Result.failed(Constants.ResponseCode.UPLOAD_INDEX_UNMATCHED.getCode(),
                        Constants.ResponseCode.UPLOAD_INDEX_UNMATCHED.getMsg(), sliceIndexRedisInt);
            }
        } else {
            log.debug("缓存中无此分片信息，可能已经全部上传完毕，或没有初始化分片");
            // 无分片，可能上传完了，所以不需要合并
            return Result.success(isCompose);
        }
        // 上传分片文件
        Boolean isUpload = minioUtils.uploadFile(minioConfig.getBucketNameSlice(), objectName, sliceIndex, file);
        if (isUpload) {

            // 上传完成删除缓存中的分片
            // TODO 删除判断
            boolean isRemove = redisDao.remove(objectName, sliceIndexStr, 1);
            if (sliceIndex < totalPieces - 1) {
                return Result.success(sliceIndex + 1);
            }
            return Result.success(isUploaded);
        }
        // 上传失败
        return Result.failed(Constants.ResponseCode.UPLOAD_FILE_FAILED.getCode(),
                Constants.ResponseCode.UPLOAD_FILE_FAILED.getMsg(), sliceIndex);
    }

    /**
     * 分片合并
     *
     * @return Boolean ture成功，false是失败
     */
    @Override
    public Boolean composeFile(String objectName,String fullObjectName, Integer totalPieces) {
        // 完成上传从缓存目录合并迁移到正式目录
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(totalPieces)
                .map(i -> ComposeSource.builder()
                        .bucket(minioConfig.getBucketNameSlice())
                        .object(objectName.concat("/").concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        log.debug("文件合并|composeFile|参数objectName:{},fullObjectName:{},totalPieces:{}", objectName,fullObjectName, totalPieces);
        // 合并操作
        Boolean isCompose = minioUtils.composeFile(minioConfig.getBucketName(), fullObjectName, sourceObjectList);
        return isCompose;
    }


    /**
     * 删除minio分片文件
     *
     * @return Boolean ture成功，false是失败
     */
    @Override
    public Boolean delSliceFile(String objectName, Integer totalPieces) {
        // 删除所有的临时分片文件
        List<DeleteObject> delObjects = Stream.iterate(0, i -> ++i)
                .limit(totalPieces)
                .map(i -> new DeleteObject(objectName.concat("/").concat(Integer.toString(i))))
                .collect(Collectors.toList());
        Boolean isDel = minioUtils.removeFiles(minioConfig.getBucketNameSlice(), delObjects);
        return isDel;
    }

    /**
     * 初始化时创建桶
     *
     * @return Boolean ture成功，false是失败
     */
    @Override
    public Boolean createFileBucket() {
        boolean sliceBucket = minioUtils.createBucket(minioConfig.getBucketNameSlice());
        boolean fileBucket = minioUtils.createBucket(minioConfig.getBucketName());
        if (sliceBucket && fileBucket) {
            return true;
        }
        return false;
    }


    @Override
    public Boolean sliceInRedis(String md5, Integer totalSlice) {
        if (!redisDao.hasKey(md5)) {
            for (int i = 0; i < totalSlice; i++) {
                redisDao.rpush(md5, String.valueOf(i));
            }
        }
        return true;
    }

}
