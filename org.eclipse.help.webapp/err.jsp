<html>

<head>
	<title> Error </title>
      	<link rel="stylesheet" TYPE="text/css" HREF="help.css" TITLE="nav"/>
<body>

	<%@ page isErrorPage="true" %>
	
	There was an error in your action:
	<p>
	<%= exception.toString() %>
	</P>
	
</body>
</html>

