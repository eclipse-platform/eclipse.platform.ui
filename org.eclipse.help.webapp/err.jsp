<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title> Error </title>
<body>

	<%@ page isErrorPage="true" %>
	
	There was an error in your action:
	<p>
	<%= exception.toString() %>
	</P>
	
</body>
</html>

