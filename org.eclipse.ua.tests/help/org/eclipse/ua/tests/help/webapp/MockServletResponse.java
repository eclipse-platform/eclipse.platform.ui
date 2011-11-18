/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Class used for tests which require an HttpServletRequest parameter
 */

public class MockServletResponse implements HttpServletResponse {
	
	private List<Cookie> cookies = new ArrayList<Cookie>();
	private String illegalCharactersFound = "";

	public String getCharacterEncoding() {
		return null;
	}

	public String getContentType() {
		return null;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	public PrintWriter getWriter() throws IOException {
		return null;
	}

	public void setCharacterEncoding(String charset) {
		
	}

	public void setContentLength(int len) {
	}

	public void setContentType(String type) {
		
	}

	public void setBufferSize(int size) {
		
	}

	public int getBufferSize() {
		return 0;
	}

	public void flushBuffer() throws IOException {
		
	}

	public void resetBuffer() {
		
	}

	public boolean isCommitted() {
		return false;
	}

	public void reset() {
		
	}

	public void setLocale(Locale loc) {
		
	}

	public Locale getLocale() {
		return null;
	}

	public void addCookie(Cookie cookie) {
		checkForIllegalCharacters(cookie.getValue());
		// Replace if it already exists, otherwise set
		for (int i = 0; i < cookies.size(); i++) {
			Cookie nextCookie = cookies.get(i);
			if (nextCookie.getName().equals(cookie.getName())) {
				cookies.remove(i);
				cookies.add(cookie);
				return;
			}
		}
		cookies.add(cookie);
	}
	
	private void checkForIllegalCharacters(String value) {
		// Check for illegal characters 
		final String illegalChars = "()<>@,;:\\\"/[]?={} \t";
		for (int i = 0; i < illegalChars.length(); i++) {
			char ch = illegalChars.charAt(i);
			if (value.indexOf(ch) >= 0 && illegalCharactersFound.indexOf(ch) < 0) {
				illegalCharactersFound = illegalCharactersFound + ch;
			}
		}
	}

	public Cookie[] getCookies() {
		return cookies.toArray(new Cookie[cookies.size()]);
	}

	public boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void sendRedirect(String location) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}

	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}

	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	public void addHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	public void setIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}

	public void addIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}

	public void setStatus(int sc) {
		// TODO Auto-generated method stub
		
	}

	public void setStatus(int sc, String sm) {
		// TODO Auto-generated method stub
		
	}
	
	public String getIllegalCharatersFound() {
		return illegalCharactersFound;
	}

	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
