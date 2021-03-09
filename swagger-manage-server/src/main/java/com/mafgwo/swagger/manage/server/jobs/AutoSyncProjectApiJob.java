package com.mafgwo.swagger.manage.server.jobs;

import com.mafgwo.swagger.manage.server.entity.ProjectInfo;
import com.mafgwo.swagger.manage.server.mapper.ProjectInfoMapper;
import com.mafgwo.swagger.manage.server.service.DocService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自动同步接口文档定时任务
 *
 * @author chenxiaoqi
 * @since 2021/02/22
 */
@Component
public class AutoSyncProjectApiJob {

    private static Logger log = LoggerFactory.getLogger(AutoSyncProjectApiJob.class);

    @Autowired
    private DocService docService;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Value("${swagger.refresh:false}")
    private Boolean refresh;

    @Scheduled(fixedRate = 3600000)
    public void syncProjectApi() {
        if (refresh == null || !refresh) {
            return;
        }
        List<ProjectInfo> projectInfos = projectInfoMapper.listAll();
        for (ProjectInfo projectInfo : projectInfos) {
            try {
                docService.syncProject(projectInfo);
                log.info("成功同步api文档：{}", projectInfo.getName());
            } catch (Exception e) {
                log.error("同步项目api异常");
            }
        }
    }

}
