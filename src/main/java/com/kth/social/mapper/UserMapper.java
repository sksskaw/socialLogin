package com.kth.social.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kth.social.vo.User;

@Mapper
public interface UserMapper {
	String checkUserId(String id);
	void insertUser(User user);
	void updateLoginStateById(String id, String state);
}
