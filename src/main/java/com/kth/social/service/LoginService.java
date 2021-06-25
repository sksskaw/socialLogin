package com.kth.social.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kth.social.mapper.UserMapper;

@Service
public class LoginService {
	@Autowired UserMapper user; // mapper
	
	public String checkEmail(String email) {
		return user.checkEmail(email);
	}
	
	public void addUser(Map<String,Object> map) {
		user.insertUser(map);
	}
	
	public String Login(Map<String,Object> map) {
		return user.selectUser(map);
	}
}
