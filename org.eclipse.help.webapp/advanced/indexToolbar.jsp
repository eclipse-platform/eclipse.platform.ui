<%--
 Copyright (c) 2005, 2010 Intel Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     Intel Corporation - initial API and implementation
     IBM Corporation   - add filter button
--%>
<%@ include file="header.jsp"%>


<jsp:include page="toolbar.jsp">
	<jsp:param name="script" value="navActions.js"/>
	<jsp:param name="view" value="index"/>
	
	<jsp:param name="name"     value="show_all"/>
	<jsp:param name="tooltip"  value='show_all'/>
	<jsp:param name="image"    value="show_all.gif"/>
	<jsp:param name="action"   value="toggleShowAll"/>
	<jsp:param name="param"    value=""/>
	<jsp:param name="state"    value="<%=(new ActivitiesData(application, request, response)).getButtonState()%>"/>
	
</jsp:include>
