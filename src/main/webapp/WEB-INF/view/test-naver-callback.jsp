<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Callback</title>
<style>
  pre{
    overflow: scroll;
  }
</style>
</head>
<body>
  <h1>콜백 페이지</h1>
  <div>${res}</div>
  <a href="/naver">go to main page</a>
  
  
  
  <h1>추가 입력 사항을 입력 해 주세요</h1>
  
  <form action="${pageContext.request.contextPath}/signUp" method="post">
  	<div>PW : </div>
  	<input type="password" name="password">
  	<div>주소 : </div>
  	<input type="text" name="address">
  	<button type="submit">가입</button>
  </form>
</body>
</html>