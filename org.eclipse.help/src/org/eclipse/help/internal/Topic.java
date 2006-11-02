/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.help.Node;

/*
 * Adapts a "topic" Node as an ITopic. All methods operate on the
 * underlying adapted Node.
 */
public class Topic extends NodeAdapter implements ITopic {

	public static final String NAME = "topic"; //$NON-NLS-1$
	public static final String ATTRIBUTE_HREF = "href"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LABEL = "label"; //$NON-NLS-1$
	
	/*
	 * Constructs a new topic adapter for an empty topic node.
	 */
	public Topic() {
		super();
		setName(NAME);
	}

	/*
	 * Constructs a new topic adapter for the given topic node.
	 */
	public Topic(Node node) {
		super(node);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IHelpResource#getHref()
	 */
	public String getHref() {
		return node.getAttribute(ATTRIBUTE_HREF);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IHelpResource#getLabel()
	 */
	public String getLabel() {
		return node.getAttribute(ATTRIBUTE_LABEL);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.ITopic#getSubtopics()
	 */
	public ITopic[] getSubtopics() {
		return (Topic[])getChildren(NAME, Topic.class);
	}
	
	/*
	 * Sets the topic's href.
	 */
	public void setHref(String href) {
		node.setAttribute(ATTRIBUTE_HREF, href);
	}
	
	/*
	 * Sets the topic's label.
	 */
	public void setLabel(String label) {
		node.setAttribute(ATTRIBUTE_LABEL, label);
	}
}
