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
  <h1>로그인 성공</h1>
  <h1>콜백 페이지</h1>
  <div>${res}</div>
  <a href="/naver/invalidate">로그아웃 (Invalidate Session)</a>
</body>
</html>