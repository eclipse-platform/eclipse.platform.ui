<%--
 Copyright (c) 2000, 2011 IBM Corporation and others.

 This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License 2.0
 which accompanies this distribution, and is available at
 https://www.eclipse.org/legal/epl-2.0/

 SPDX-License-Identifier: EPL-2.0
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ page import="org.eclipse.help.internal.webapp.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.Scanner" %>
<%@ page import="java.net.URL" %>
<%
	request.setCharacterEncoding("UTF-8");
	ServerState.webappStarted(application,request, response);	
	// Read the scope parameter
	RequestScope.setScopeFromRequest(request, response);
	LayoutData data = new LayoutData(application,request, response);

	if(data.isBot()){
		TocData tData = new TocData(application,request, response);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="<%=ServletResources.getString("locale", request)%>">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=data.getWindowTitle()%></title>
</head>
<body>
<% tData.generateLinks(out); %>
</body>
</html>	
<%
	} else {

		// Experimental UI: see bug 501718
		String experimentalUi = System.getProperty("org.eclipse.help.webapp.experimental.ui");
		if (request.getParameter("legacy") == null && experimentalUi != null) {
			try {
				// In a JSP forwarding to non JSP resources does not work
				// (page is shown, but "java.lang.IllegalStateException: STREAM" is thrown)
				// so read from URL instead:
				URL baseUrl = new URL(request.getRequestURL().toString());
				URL forwardUrl = new URL(baseUrl, experimentalUi);
				// Same-origin policy
				if (!baseUrl.getProtocol().equals(forwardUrl.getProtocol())
					|| !baseUrl.getHost().equals(forwardUrl.getHost())
					|| baseUrl.getPort() != forwardUrl.getPort()) throw new Exception();
				// Read it as InputStream and convert it to a String
				// (by using a Scanner with a delimiter that cannot be found: \A - start of input)
				Scanner scanAll = new Scanner(forwardUrl.openStream()).useDelimiter("\\A");
				response.getWriter().write(scanAll.hasNext() ? scanAll.next() : "");
			} catch (Exception e) {
				// Experimental UI resource not found, so fall back to legacy UI
				request.getRequestDispatcher("/advanced/index.jsp" + data.getQuery()).forward(request, response);
			}

		// legacy UI
		} else {
			request.getRequestDispatcher("/advanced/index.jsp" + data.getQuery()).forward(request, response);
		}

	}
%>
