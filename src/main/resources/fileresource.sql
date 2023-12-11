

CREATE DATABASE `fileresource`; /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */;

CREATE TABLE `t_resource_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uuid` varchar(32) NOT NULL COMMENT '标识列',
  `original_file_name` varchar(90) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件原始名称',
  `slice_file_size` double NOT NULL COMMENT '分片的大小(单位kb)',
  `file_size` double DEFAULT NULL COMMENT '总文件大小(单位kb)',
  `minio_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件存储minio地址',
  `total_slice` int(11) DEFAULT NULL COMMENT '总分片;0代表没有分片是完整的文件',
  `minio_md5` varchar(90) NOT NULL COMMENT '文件miniomd5',
  `minio_bucket` varchar(22) NOT NULL COMMENT 'minio桶名称',
  `is_uploaded` int(1) DEFAULT NULL COMMENT '是否上传;0：未上传，1：已上传',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人'
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=249 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件表';
