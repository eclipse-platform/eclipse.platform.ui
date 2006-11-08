/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Phil Loats (IBM Corp.) - fix to use only foundation APIs
 *******************************************************************************/
package org.eclipse.help.internal.context;

import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.Node;
import org.eclipse.help.internal.NodeAdapter;
import org.eclipse.help.internal.Topic;

/*
 * Adapts a "context" Node as an IContext. All methods operate on the
 * underlying adapted Node.
 */
public class Context extends NodeAdapter implements IContext {

	public static final String NAME = "context"; //$NON-NLS-1$
	public static final String ELEMENT_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	
	/*
	 * Constructs a new context adapter for an empty context node.
	 */
	public Context() {
		super();
		setNodeName(NAME);
	}

	/*
	 * Constructs a new context adapter for the given context node.
	 */
	public Context(Node node) {
		super(node);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IContext#getRelatedTopics()
	 */
	public IHelpResource[] getRelatedTopics() {
		return (Topic[])getChildren(Topic.NAME, Topic.class);
	}
	
	/*
	 * Returns the Context's unique id. 
	 */
	public String getId() {
		return getAttribute(ATTRIBUTE_ID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IContext#getText()
	 */
	public String getText() {
		Node[] children = getChildNodes();
		if (children.length > 0 && ELEMENT_DESCRIPTION.equals(children[0].getNodeName())) {
			Node description = children[0];
			Node[] descriptionChildren = description.getChildNodes();
			if (descriptionChildren.length > 0) {
				return descriptionChildren[0].getValue();
			}
		}
		return null;
	}

	/*
	 * Sets the Context's unique id.
	 */
	public void setId(String id) {
		setAttribute(ATTRIBUTE_ID, id);
	}

	/*
	 * Sets the Context's description text.
	 */
	public void setText(String text) {
		Node[] children = getChildNodes();
		if (children.length > 0 && ELEMENT_DESCRIPTION.equals(children[0].getNodeName())) {
			Node description = children[0];
			Node[] descriptionChildren = description.getChildNodes();
			for (int i=0;i<descriptionChildren.length;++i) {
				description.removeChild(descriptionChildren[i]);
			}
			Node textNode = new Node();
			textNode.setNodeValue(text);
			description.appendChild(textNode);
		}
	}
}
