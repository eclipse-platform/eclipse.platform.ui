/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

package org.eclipse.help.servlet;

import java.io.IOException;

import javax.servlet.*;
/**
 * Sets character encoding of request to UTF8.
 */
public class SetUtf8Filter implements Filter {

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain)
		throws IOException, ServletException {
		request.setCharacterEncoding("UTF8");
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}
}