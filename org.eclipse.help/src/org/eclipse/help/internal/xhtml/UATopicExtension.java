/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class UATopicExtension extends AbstractUAElement {

	protected static final String TAG_TOPIC_EXTENSION = "topicExtension"; //$NON-NLS-1$

	protected static final String ATT_PATH = "path"; //$NON-NLS-1$
	private static final String ATT_CONTENT = "content"; //$NON-NLS-1$

	private String path;
	private String content;
	private Element element;

	UATopicExtension(Element element, Bundle bundle) {
		super(element, bundle);
		path = getAttribute(element, ATT_PATH);
		content = getAttribute(element, ATT_CONTENT);
		content = BundleUtil.getResolvedResourceLocation(content, bundle, false);
		this.element = element;
	}

	/**
	 * @return Returns the content.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}


	public Document getDocument() {

		UAContentParser parser = new UAContentParser(content);
		Document dom = parser.getDocument();
		return dom;
	}


	public Element getElement() {
		return element;
	}


}
