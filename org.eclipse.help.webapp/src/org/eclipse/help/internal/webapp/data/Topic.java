/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

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
