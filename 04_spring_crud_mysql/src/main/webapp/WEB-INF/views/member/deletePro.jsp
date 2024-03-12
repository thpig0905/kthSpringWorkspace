<%--
  Created by IntelliJ IDEA.
  User: gimminhyeog
  Date: 3/12/24
  Time: 4:02 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <c:if test="${check eq 1}">
        <script>
            alert("탈퇴 성공");
            location.href = "${cp}/member/loginForm"
        </script>
    </c:if>

    <c:if test="${check ne 1}">
        <script>
            alert("탈퇴 실패");
            location.href = "${cp}/member/userMenu"
        </script>
    </c:if>
</body>
</html>
