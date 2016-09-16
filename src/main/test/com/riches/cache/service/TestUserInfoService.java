package com.riches.cache.service;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)  //使用junit4进行测试
@ContextConfiguration({"classpath:spring.xml"}) //加载配置文件

public class TestUserInfoService {

	@Resource
	private UserInfoService userInfoService;
	
	//@Test
	public void testSetUserName(){
		userInfoService.setUserName("number-1");
	}
	
	@Test
	public void testGetUserName(){
		String name = userInfoService.getUserName("number-1");
		System.out.println(name);
	}
}
