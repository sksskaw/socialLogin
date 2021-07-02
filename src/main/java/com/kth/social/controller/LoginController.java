package com.kth.social.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kth.social.service.LoginService;
import com.kth.social.vo.User;

@Controller
@Slf4j
public class LoginController {

	@Autowired LoginService loginService;
	
	private String CLIENT_ID  = "CmICvXSP5Iz8y_NK2GpT"; // 애플리케이션 클라이언트 아이디값";
	private String CLI_SECRET = "j0NkiKorZu"; 			// 애플리케이션 클라이언트 시크릿값";

	/**
	 * 로그인 화면이 있는 페이지 컨트롤
	 * 
	 * @param session
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UnknownHostException
	 */
	@RequestMapping("/naver")
	public String testNaver(HttpSession session, Model model)
			throws UnsupportedEncodingException, UnknownHostException {
		
		log.debug("--------------------- testNaver session currentUser: " + session.getAttribute("currentUser"));
		
		String redirectURI = URLEncoder.encode("http://localhost/auth/naver/callback", "UTF-8");
		
		// CSRF 방지를 위한 상태 토큰 생성 코드
		SecureRandom random = new SecureRandom();
		String state = new BigInteger(130, random).toString();
		
		String apiURL = "https://nid.naver.com/oauth2.0/authorize?response_type=code";
		apiURL += String.format("&client_id=%s&redirect_uri=%s&state=%s", CLIENT_ID, redirectURI, state);
		
		/* 네아로 요청 apiURL
		 * https://nid.naver.com/oauth2.0/authorize?response_type=code&
		 * client_id={클라이언트 아이디}&
		 * redirect_uri={개발자 센터에 등록한 콜백 URL(URL 인코딩)}&
		 * state={상태토큰}
		 */		
		
		session.setAttribute("state", state);
		model.addAttribute("apiURL", apiURL); // 네이버 로그인 페이지 URL
		return "test-naver";
	}

	/**
	 * 콜백 페이지 컨트롤러
	 * 
	 * @param session
	 * @param request
	 * @param model
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@RequestMapping("/auth/naver/callback")
	public String naverCallback1(HttpSession session, HttpServletRequest request, Model model)
			throws IOException, ParseException {
		
		/*
		 * state: 콜백으로 전달받은 상태 토큰. 애플리케이션이 생성한 상태 토큰과 일치해야 합니다. 
		 * code: 콜백으로 전달받은 인증코드(authentication code). 접근 토큰(access token) 발급에 사용합니다.
		 */
		
		String code = request.getParameter("code"); 
		String state = request.getParameter("state");
		
		// CSRF 방지를 위한 상태 토큰 검증 검증
		// 세션 또는 별도의 저장 공간에 저장된 상태 토큰과 콜백으로 전달받은 state 파라미터의 값이 일치해야 함
		
		String storedState = (String)session.getAttribute("state");
		
		if( !state.equals( storedState ) ) {
		    return "401"; //401 unauthorized 페이지 리턴
		}
		
		
		
		// 접근 토큰 요청
		/*
		 * client_id: 	  애플리케이션 등록 후 발급받은 클라이언트 아이디 
		 * client_secret: 애플리케이션 등록 후 발급받은 클라이언트 시크릿 
		 * grant_type:    인증 타입에 대한 구분값. authorization_code로 값이 고정돼 있습니다. 
		 * state: 		  애플리케이션이 생성한 상태 토큰 
		 * code: 		  콜백으로 전달받은 인증 코드
		 */
		
		String redirectURI = URLEncoder.encode("http://localhost:8080/naver/callback", "UTF-8");
		String apiURL;
		apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&";
		apiURL += "client_id=" + CLIENT_ID;
		apiURL += "&client_secret=" + CLI_SECRET;
		apiURL += "&redirect_uri=" + redirectURI;
		apiURL += "&code=" + code;
		apiURL += "&state=" + state;

		String res = requestToServer(apiURL);
		
		// Json 데이터 파싱
		if (res != null && !res.equals("")) {
			Map<String, Object> parsedJson = new JSONParser(res).parseObject();
			session.setAttribute("currentUser", res);
			session.setAttribute("currentAT", parsedJson.get("access_token"));
			session.setAttribute("currentRT", parsedJson.get("refresh_token"));
		} else {
			model.addAttribute("res", "Login failed!");
		}

		log.debug("--------------------- naverCallback1 setSession currentUser: " + session.getAttribute("currentUser"));
		log.debug("--------------------- naverCallback1 setSession currentAT: " + session.getAttribute("currentAT"));
		log.debug("--------------------- naverCallback1 setSession currentRT: " + session.getAttribute("currentRT"));
		
		// 프로필 정보 가져오기
		String userInfo = getProfileFromNaver((String)session.getAttribute("currentAT"));
		
		// 프로필 정보 Json 데이터 파싱
		Map<String, Object> parsedJson = new JSONParser(userInfo).parseObject();
		log.debug("--------------------- response : "+parsedJson.get("response"));
		
		Map<String, Object> response = ((Map<String, Object>) parsedJson.get("response"));
		
		String id = ((String)response.get("id"));
		session.setAttribute("id", id);
		
		// 최초 로그인한 사람이면 DB에 회원 프로필 정보 등록
		String checkUserId = loginService.checkUserId(id);
		User user = new User();
		
		if(checkUserId == null) {
			user.setId(id);
			user.setNickname((String)response.get("nickname"));
			user.setAge((String)response.get("age"));
			user.setGender((String)response.get("gender"));
			user.setEmail((String)response.get("email"));
			user.setMobile((String)response.get("mobile"));
			user.setName((String)response.get("name"));
			user.setBirthday((String)response.get("birthday"));
			user.setBirthyear((String)response.get("birthyear"));
			
			loginService.addUser(user);
		}
		
		loginService.modifyLoginState((String)session.getAttribute("id"), "접속중"); // 접속 상태 변경
		return "test-naver-callback";
	}

	/**
	 * 토큰 갱신 요청 페이지 컨트롤러
	 * 
	 * @param session
	 * @param request
	 * @param model
	 * @param refreshToken
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@GetMapping("/naver/refreshToken")
	public String refreshToken(HttpSession session, HttpServletRequest request, Model model, String refreshToken)
			throws IOException, ParseException {
		String apiURL;
		apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=refresh_token&";
		apiURL += "client_id=" + CLIENT_ID;
		apiURL += "&client_secret=" + CLI_SECRET;
		apiURL += "&refresh_token=" + refreshToken;
		System.out.println("apiURL=" + apiURL);
		String res = requestToServer(apiURL);
		model.addAttribute("res", res);
		session.invalidate();
		return "test-naver-callback";
	}

	/**
	 * 토큰 삭제 컨트롤러
	 * 
	 * @param session
	 * @param request
	 * @param model
	 * @param accessToken
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/naver/deleteToken")
	public String deleteToken(HttpSession session, HttpServletRequest request, Model model, String accessToken)
			throws IOException {
		String apiURL;
		apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=delete&";
		apiURL += "client_id=" + CLIENT_ID;
		apiURL += "&client_secret=" + CLI_SECRET;
		apiURL += "&access_token=" + accessToken;
		apiURL += "&service_provider=NAVER";
		System.out.println("apiURL=" + apiURL);
		String res = requestToServer(apiURL);
		model.addAttribute("res", res);
		session.invalidate();
		return "test-naver-callback";
	}

	/**
	 * 액세스 토큰으로 네이버에서 프로필 받기
	 * 
	 * @param accessToken
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping("/naver/getProfile")
	public String getProfileFromNaver(String accessToken) throws IOException {
		// 네이버 로그인 접근 토큰;
		String apiURL = "https://openapi.naver.com/v1/nid/me";
		String headerStr = "Bearer " + accessToken; // Bearer 다음에 공백 추가
		String res = requestToServer(apiURL, headerStr);
		log.debug("--------------------- getProfileFromNaver res: " + res); // res 여기에 네이버에서 준 회원 정보,,, 번호라든지...
		return res;
	}

	/**
	 * 세션 무효화(로그아웃)
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping("/naver/invalidate")
	public String invalidateSession(HttpSession session) {
		
		// 회원 DB의 상태값 변경
		loginService.modifyLoginState((String)session.getAttribute("id"), "미접속");
		
		session.invalidate();
		return "redirect:/naver";
	}

	/**
	 * 서버 통신 메소드
	 * 
	 * @param apiURL
	 * @return
	 * @throws IOException
	 */
	private String requestToServer(String apiURL) throws IOException {
		return requestToServer(apiURL, "");
	}

	/**
	 * 서버 통신 메소드
	 * 
	 * @param apiURL
	 * @param headerStr
	 * @return
	 * @throws IOException
	 */
	private String requestToServer(String apiURL, String headerStr) throws IOException {
		URL url = new URL(apiURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		System.out.println("header Str: " + headerStr);
		
		if (headerStr != null && !headerStr.equals("")) {
			con.setRequestProperty("Authorization", headerStr);
		}
		
		int responseCode = con.getResponseCode();
		BufferedReader br;
		System.out.println("responseCode=" + responseCode);
		
		if (responseCode == 200) { // 정상 호출
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else { // 에러 발생
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		
		String inputLine;
		StringBuffer res = new StringBuffer();
		
		while ((inputLine = br.readLine()) != null) {
			res.append(inputLine);
		}
		br.close();
		
		if (responseCode == 200) {
			return res.toString();
		} else {
			return null;
		}
	}
	
}
