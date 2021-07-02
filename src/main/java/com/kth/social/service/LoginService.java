package com.kth.social.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kth.social.mapper.UserMapper;
import com.kth.social.vo.User;

@Service
public class LoginService {
	@Autowired UserMapper userMapper; // mapper
	
	public String checkUserId(String id) {
		return userMapper.checkUserId(id);
	}
	
	public void addUser(User user) {
		userMapper.insertUser(user);
	}
	
	public void modifyLoginState(String id, String state) {
		userMapper.updateLoginStateById(id,state);
	}
	
}
