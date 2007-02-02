/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import org.eclipse.help.ITopic;
import org.w3c.dom.Element;

public class Topic extends UAElement implements ITopic {

	public static final String NAME = "topic"; //$NON-NLS-1$
	public static final String ATTRIBUTE_HREF = "href"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LABEL = "label"; //$NON-NLS-1$
	
	public Topic() {
		super(NAME);
	}
	
	public Topic(ITopic src) {
		super(NAME, src);
		setHref(src.getHref());
		setLabel(src.getLabel());
		appendChildren(src.getChildren());
	}
	
	public Topic(Element src) {
		super(src);
	}

	public String getHref() {
		return getAttribute(ATTRIBUTE_HREF);
	}
	
	public String getLabel() {
		return getAttribute(ATTRIBUTE_LABEL);
	}
	
	public ITopic[] getSubtopics() {
		return (ITopic[])getChildren(ITopic.class);
	}
	
	public void setHref(String href) {
		setAttribute(ATTRIBUTE_HREF, href);
	}
	
	public void setLabel(String label) {
		setAttribute(ATTRIBUTE_LABEL, label);
	}
}
