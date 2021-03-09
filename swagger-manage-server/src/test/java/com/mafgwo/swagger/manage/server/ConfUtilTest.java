package com.mafgwo.swagger.manage.server;

import com.mafgwo.swagger.manage.server.common.ConfUtil;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

/**
 * @author tanghc
 */
public class ConfUtilTest {

    @Test
    public void testSet() {
        ConfUtil.setValue("group1", "name", "jim");
        ConfUtil.setValue("group2", "name", "2");
        ConfUtil.setValue("group1", "name", "tom");
    }

    @Test
    public void testGet() {
        String value = ConfUtil.getValue("group1", "name1");
        Assert.isTrue(value == null, "必须为null");
        String name = ConfUtil.getValue("group1", "name");
        Assert.notNull(name, "name不能为null");
    }

}
