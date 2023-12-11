package com.file.resource.common;

/**
 * @Author df
 * @Description: 通用常量信息以及一些枚举信息
 * @Date 2023/10/16 11:34
 */
public class Constants {

    // 结果反馈码
    public enum ResponseCode {
        SUCCESS("0000", "成功"),
        UN_ERROR("0001", "未知失败,请联系管理员！"),

        VALIDATE_BAD_PARAM("0002", "请求参数缺少"),
        VALIDATE_BAD_PARAM_TYPE("0003", "请求参数类型错误！"),
        INDEX_DUP("0004", "主键冲突"),


        // 文件上传--------------------------------------
        UPLOAD_INDEX_UNMATCHED("U001", "上传的索引分片不一致！"),
        UPLOAD_FILE_FAILED("U002", "文件上传失败！"),
        UPLOAD_BUCKET_FAILED("U003", "创建文件桶失败！"),
        UPLOAD_INIT_SLICE_FAILED("U004", "初始化文件切片缓存失败！"),
        UPLOAD_UPDATEDB_FAILED("U005", "文件入库更新失败！"),
        UPLOAD_COMPOSE_FAILED("U006", "文件合并失败！");


        private String code;
        private String msg;

        ResponseCode(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

    }


}
