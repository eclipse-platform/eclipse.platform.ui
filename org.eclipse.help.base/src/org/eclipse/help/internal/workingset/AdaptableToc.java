/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.workingset;

import org.eclipse.help.*;
import org.w3c.dom.*;

/**
 * Makes help resources adaptable and persistable
 */
public class AdaptableToc extends AdaptableHelpResource {

	protected AdaptableTopic[] children;

	/**
	 * This constructor will be called when wrapping help resources.
	 */
	AdaptableToc(IToc element) {
		super(element);
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IToc.class)
			return element;
		else
			return super.getAdapter(adapter);
	}

	public AdaptableHelpResource[] getChildren() {
		if (children == null) {
			ITopic[] topics = ((IToc) element).getTopics();
			children = new AdaptableTopic[topics.length];
			for (int i = 0; i < topics.length; i++) {
				children[i] = new AdaptableTopic(topics[i]);
				children[i].setParent(this);
			}
		}
		return children;
	}

	/**
	 * @see org.eclipse.help.IToc#getTopic(java.lang.String)
	 */
	public ITopic getTopic(String href) {
		return ((IToc) element).getTopic(href);
	}

	/**
	 * @see org.eclipse.help.IToc#getTopics()
	 */
	public ITopic[] getTopics() {
		return ((IToc) element).getTopics();
	}

	public void saveState(Element element) {
		element.setAttribute("toc", getHref()); //$NON-NLS-1$
	}
}
