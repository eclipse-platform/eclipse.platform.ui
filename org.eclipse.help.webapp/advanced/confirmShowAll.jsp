<%--
 Copyright (c) 2000, 2004 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<jsp:include page="confirm.jsp">
	<jsp:param name="title"		value="<%=ServletResources.getString("confirmShowAllTitle", request)%>"/>
	<jsp:param name="header"	value="<%=ServletResources.getString("confirmShowAllQuestion", request)%>"/>
	<jsp:param name="message"	value="<%=ServletResources.getString("confirmShowAllExplanation", request)%>"/>
	<jsp:param name="dontaskagain"	value="true"/>
	<jsp:param name="dontaskagainCallback"	value="<%="dontAskAgain()"%>"/>
	<jsp:param name="confirmCallback"	value="<%="showAll()"%>"/>
	<jsp:param name="initialFocus"	value="ok"/>
</jsp:include>