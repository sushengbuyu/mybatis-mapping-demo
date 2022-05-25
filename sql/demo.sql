CREATE TABLE IF NOT EXISTS `sys_role`
(
    `id`      bigint      NOT NULL,
    `user_id` bigint      NOT NULL,
    `name`    varchar(50) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


INSERT INTO `sys_role` (`id`, `user_id`, `name`)
VALUES (11, 1, 'r1'),
       (12, 1, 'r2'),
       (13, 2, 'r3');

CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`        bigint      NOT NULL COMMENT '主键',
    `username`  varchar(20) NOT NULL DEFAULT '' COMMENT '登录名',
    `password`  varchar(50) NOT NULL DEFAULT '' COMMENT '密码',
    `nick_name` varchar(20) NOT NULL DEFAULT '' COMMENT '昵称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统用户';

INSERT INTO `sys_user` (`id`, `username`, `password`, `nick_name`)
VALUES (1, 'a1', '123456', 'aa11'),
       (2, 'a2', '123456', 'aa22');

CREATE TABLE IF NOT EXISTS `sys_permission`
(
    `id`   bigint      NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL DEFAULT '0' COMMENT '名称',
    `type` int         NOT NULL DEFAULT '0' COMMENT '类型',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统权限';


INSERT INTO `sys_permission` (`id`, `name`, `type`)
VALUES (1, 'add', 0),
       (2, 'query', 0),
       (3, 'del', 0);

CREATE TABLE IF NOT EXISTS `sys_role_permission`
(
    `role_id` bigint NOT NULL,
    `perm_id` bigint NOT NULL,
    UNIQUE KEY `role_id_perm_id` (`role_id`, `perm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色权限关联表';


INSERT INTO `sys_role_permission` (`role_id`, `perm_id`)
VALUES (11, 1),
       (11, 2),
       (12, 3);

