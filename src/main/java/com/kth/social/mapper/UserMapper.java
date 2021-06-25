package com.kth.social.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
	String checkEmail(String email);
	void insertUser(Map<String,Object> map);
	String selectUser(Map<String,Object> map);
}
