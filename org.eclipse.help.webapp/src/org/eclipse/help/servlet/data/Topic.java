package org.eclipse.help.servlet.data;

import org.eclipse.help.servlet.UrlUtil;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
public class Topic {
	private String label;
	private String href;
	
	public Topic(String label, String href) {
		this.label = label;
		this.href = href;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getHref() {
		return UrlUtil.getHelpURL(href);
	}

}
