<%--
 (c) Copyright IBM Corp. 2000, 2003.
 All Rights Reserved.
--%>
<form action="searchView.jsp" method="get" accept-charset="UTF-8" target="_self">

	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td nowrap>
				<input type="text" name="searchWord" value='<%=data.getSearchWord()%>' maxlength=256 alt='<%=ServletResources.getString("SearchExpression", request)%>'>
          	  	<input type="hidden" name="maxHits" value="500" >
          	  	<input type="hidden" name="scopedSearch" value="true" >
				<input type="submit" value='<%=ServletResources.getString("GO", request)%>' alt='<%=ServletResources.getString("GO", request)%>'>
        	</td>
        </tr>
        <tr>
        	<td>
        		<%=ServletResources.getString("expression_label", request)%>
        	</td>
        </tr>
		<tr>
			<td>
				<hr>
			</td>
		</tr>
    	<tr>
  			<td>
  				<b>
				<%=ServletResources.getString("Select", request)%>
				</b>
			</td>
		</tr>
  				
<% 
TocData tocData = new TocData(application, request);
for (int toc=0; toc<tocData.getTocCount(); toc++)
{
	String label = tocData.getTocLabel(toc);
	String checked="checked=\"yes\" ";
	if( data.isSearchRequest() && !data.isTocSelected(toc) ){
		checked="";
	}
%>
  		<tr>
  			<td nowrap>
				<input type="checkbox" name='scope' value='<%=tocData.getTocHref(toc)%>' <%=checked%> alt="<%=label%>"><%=label%>
			</td>
		</tr>
<%
}		
%>
	</table>
 </form>
