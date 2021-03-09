
CREATE DATABASE IF NOT EXISTS `swaggeradmin` DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
USE `swaggeradmin`;




DROP TABLE IF EXISTS `system_config`;
DROP TABLE IF EXISTS `swagger_info`;
DROP TABLE IF EXISTS `project_info`;


CREATE TABLE `project_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL COMMENT '项目名称',
  `host` varchar(128) NOT NULL DEFAULT '' COMMENT '访问地址，如：http://localhost:8080',
  `gateway_host` varchar(128) DEFAULT NULL COMMENT '网关地址',
  `gateway_rule` varchar(128) DEFAULT NULL COMMENT '网关替换规则',
  `is_import` int(11) NOT NULL DEFAULT 0 COMMENT '是否导入的项目，1：是，0：否',
  `basic_auth_username` varchar(128) DEFAULT NULL COMMENT 'basic username',
  `basic_auth_password` varchar(128) DEFAULT NULL COMMENT 'basic password',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目信息';



CREATE TABLE `swagger_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL DEFAULT '0' COMMENT 'project_info.id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '分组名称',
  `url` varchar(128) NOT NULL DEFAULT '' COMMENT '文档地址',
  `doc_content` longtext NOT NULL COMMENT '文档内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='swagger信息';


CREATE TABLE `system_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT '配置类型，1：全局header',
  `config_key` varchar(128) NOT NULL DEFAULT '' COMMENT '配置key',
  `config_value` varchar(128) NOT NULL DEFAULT '' COMMENT '配置值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统配置';
