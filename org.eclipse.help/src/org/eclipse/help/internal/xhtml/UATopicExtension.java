/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class UATopicExtension extends AbstractUAElement {

	protected static final String TAG_TOPIC_EXTENSION = "topicExtension"; //$NON-NLS-1$

	protected static final String ATT_PATH = "path"; //$NON-NLS-1$
	private static final String ATT_CONTENT = "content"; //$NON-NLS-1$
	
	private static final Element[] EMPTY_ELEMENT_ARRAY = new Element[0];

	private String path;
	private String contentFile;
	private String contentId;
	private Element element;

	UATopicExtension(Element element, Bundle bundle) {
		super(element, bundle);
		path = getAttribute(element, ATT_PATH);
		extractFileAndId(getAttribute(element, ATT_CONTENT), bundle);
		contentFile = BundleUtil.getResolvedResourceLocation(contentFile, bundle, false);
		this.element = element;
	}

	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the elements loaded from the content attribute. This is the content
	 * that should be inserted for the extension. If it is a file, all child elements
	 * of body are returned. If it is a file with an id, only the element with the id
	 * is returned.
	 * 
	 * @return the elements to be inserted
	 */
	public Element[] getElements() {
		UAContentParser parser = new UAContentParser(contentFile);
		Document dom = parser.getDocument();
		if (dom != null) {
			if (contentId != null) {
				// id specified, only get that element
				return new Element[] { dom.getElementById(contentId) };
			}
			else {
				// no id specified, use the whole body
				Element extensionBody = DOMUtil.getBodyElement(dom);
				return DOMUtil.getElementsByTagName(extensionBody, "*"); //$NON-NLS-1$
			}
		}
		return EMPTY_ELEMENT_ARRAY;
	}

	public Element getElement() {
		return element;
	}

	/**
	 * Extracts the file and id parts of the content attribute. This attribute has two modes -
	 * if you specify a file, it will include the body of that file (minus the body element itself).
	 * If you append an id after the file, only the element with that id will be included. However
	 * we need to know which mode we're in.
	 * 
	 * @param content the content attribute value
	 * @param bundle the bundle that contributed this extension
	 */
	private void extractFileAndId(String content, Bundle bundle) {
		// look for the file first
		IPath resourcePath = new Path(content);
		if (Platform.find(bundle, resourcePath) != null) {
			// found it, it's a file with no id
			contentFile = content;
		}
		else {
			// didn't find the file, assume the last segment is an id
			int lastSlashIndex = content.lastIndexOf('/');
			if (lastSlashIndex != -1) {
				contentFile = content.substring(0, lastSlashIndex);
				contentId = content.substring(lastSlashIndex + 1);
			}
			else {
				// there was no slash, it must be a file
				contentFile = content;
			}
		}
	}
}
