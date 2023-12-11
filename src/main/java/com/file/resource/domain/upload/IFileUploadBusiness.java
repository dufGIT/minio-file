package com.file.resource.domain.upload;

import com.file.resource.common.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/28 10:32
 */
public interface IFileUploadBusiness {

    /**
     * 完整文件上传
     */
    Boolean uploadFile(String objectName, MultipartFile file);

    /**
     * 分片文件上传
     * 1.去缓存取出对应索引信息，判断此次给的索引信息是否相同，不相同则告知
     * 2.上传分片文件以后则删除缓存数据
     * 3.全部分片上传完后data数据返回-1
     *
     * @param objectName  可以当文件名这里用md5
     * @param totalPieces 总分片数量
     * @param sliceIndex  当前传过来的分片索引
     * @param file        文件
     */
    Result uploadSliceFile(String objectName, Integer totalPieces, Integer sliceIndex, MultipartFile file);

    /**
     * 分片文件合并
     *
     * @param objectName  可以当文件名这里用md5
     * @param totalPieces 总分片数量
     */
    Boolean composeFile(String objectName,String fullObjectName, Integer totalPieces);

    /**
     * 删除minio分片文件
     *
     * @param objectName  可以当文件名这里用md5
     * @param totalPieces 总分片数量
     * @return Boolean ture成功，false是失败
     */
    Boolean delSliceFile(String objectName, Integer totalPieces);

    /**
     * 初始化时创建桶
     *
     * @return Boolean ture成功，false是失败
     */
    Boolean createFileBucket();

    /**
     * 初始化分片入库
     *
     * @return Boolean ture成功，false是失败
     */
    Boolean sliceInRedis(String md5, Integer totalSlice);

}
