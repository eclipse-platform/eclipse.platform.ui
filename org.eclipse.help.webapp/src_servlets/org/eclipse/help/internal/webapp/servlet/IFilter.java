/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;

import javax.servlet.http.*;

/**
 * Filter for filtering out content of help documents
 * delivered to the client
 */
public interface IFilter {
	/**
	 * Filters OutputStream out
	 * @param req HTTPServletRequest for resource being filtered;
	 *  filter's logic might differ depending on the request
	 * @param out original OutputStream
	 * @return filtered OutputStream
	 */
	OutputStream filter(HttpServletRequest req, OutputStream out);
}
