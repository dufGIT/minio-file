package com.file.resource.interfaces.upload;

import com.file.resource.application.IFileUploadProcess;
import com.file.resource.types.common.Result;
import com.file.resource.domain.resource.TResourceService;
import com.file.resource.interfaces.upload.req.InitSliceDataReq;
import com.file.resource.interfaces.upload.req.UploadFileReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;


/**
 * @Author df
 * @Description:
 * @Date 2023/11/28 10:10
 */
@Validated
@RestController
public class FileUploadController {

    @Resource
    private IFileUploadProcess fileUploadProcess;
    @Resource
    private TResourceService resourceFileService;


    /**
     * 文件上传
     */
    @PostMapping("/upload/file")
    public Result uploadFile(@Valid @ModelAttribute UploadFileReq uploadFileReq) {
        return fileUploadProcess.uploadFile(uploadFileReq);
    }

    /**
     * 检查是否上传过文件
     *
     * @return true：已上传过,false：没有上传过
     */
    @GetMapping("/isExist/file")
    public Result checkIsUploadFile(@NotBlank(message = "文件md5不能为空！")
                                            String md5) {
        try {
            Boolean isExistFile = resourceFileService.checkIsExistFile(md5);
            return Result.success(isExistFile);
        } catch (Exception e) {
            return Result.failed();
        }
    }

    /**
     * 文件初始化
     * 1.创建桶
     * 2.将分片索引放入到redis中
     * 3.将分片以及对应基本信息先存储到资源附件中(resourceFile)
     */
    @PostMapping("/init/fileData")
    public Result initFileData(@RequestBody @Valid InitSliceDataReq initSliceDataReq) {
        return fileUploadProcess.initFileData(initSliceDataReq);
    }
}
