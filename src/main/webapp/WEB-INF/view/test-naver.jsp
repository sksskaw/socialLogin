<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>네이버 아이디로 로그인</title>
<style>
  pre{
    overflow: scroll;
  }
</style>
</head>
<body>
	<c:if test="${currentUser == null}">
	  <div>
	  	
	  	
	    <h3>네이버 계정으로 로그인</h3>
	    <a href="${apiURL}"><img height="50" src="http://static.nid.naver.com/oauth/small_g_in.PNG"/></a>
	    
	    <form action="${pageContext.request.contextPath}/login" method="post">
	  		<div>주소 : </div>
	  		<input type="text" name="email">
	  		<div>PW : </div>
	  		<input type="password" name="password">
	  		<button type="submit">로그인</button>
	    </form>
	  </div>
	</c:if>

  <c:if test="${currentUser != null}">
	  <div>
	    <h3>이 부분은 로그인한 사용자한테만 보임</h3>
	    <h3>서비스를 이용할 수 있는 페이지</h3>
	    <a href="/naver/invalidate">로그아웃 (Invalidate Session)</a>
	  </div>
  </c:if>

  
</body>
</html>