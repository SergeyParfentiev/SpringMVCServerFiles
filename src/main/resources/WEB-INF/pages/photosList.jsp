<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>Prog.kiev.ua</title>
</head>
<body>
    <form action="/selectPhotos" method="POST">
        <table border="1">
            <c:forEach items="${photos}" var="photo" varStatus="status">
                <c:set var="key" value="${photo.key}"/>
                <tr valign="middle">
                    <td><input type="checkbox" name="checkboxName" value="${key}"/></td>
                    <td>${key}</td>
                    <td><img src="/photo/${key}" height="100" width="100"></td>
                </tr>
            </c:forEach>
        </table>
        <input type="submit" name="choose" value="Delete selected">
        <input type="submit" name="choose" value="In Zip">
    </form>

    <input type="submit" value="Back" onclick="window.location='/';"/>

</body>
</html>