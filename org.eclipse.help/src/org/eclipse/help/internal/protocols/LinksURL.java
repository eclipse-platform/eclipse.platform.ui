/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.*;
import org.eclipse.help.internal.context.LinksResult;
/**
 * URL to the links server.
 */
public class LinksURL extends HelpURL {
	public final static String LINKS = "links";
	
	/**
	 * LinksURL constructor.
	 * @param url java.lang.String
	 */
	public LinksURL(String url) {
		super(url, "");
		int index = url.indexOf("?");
		if (index > -1) {
			if (url.length() > index + 1) {
				String query = url.substring(index + 1);
				this.query = new StringBuffer(query);
				parseQuery(query);
			}
			super.url = url.substring(0, index);
		}
	}
	/** Returns the path prefix that identifies the URL. */
	public static String getPrefix() {
		return LINKS;
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		try {
			String contextId = (String) arguments.get("contextId");
			LinksResult result = new LinksResult(contextId);
			InputStream is = new ByteArrayInputStream(result.toString().getBytes("UTF8"));
			if (is != null) {
				contentSize = is.available();
			}
			return is;
		} catch (Exception e) {
			return new ByteArrayInputStream(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<toc label=\"Links\"/>"
					.getBytes());
		}
	}
}