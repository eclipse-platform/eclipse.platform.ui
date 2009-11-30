/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.workingset;

import org.eclipse.help.ITopic;
import org.w3c.dom.Element;

/**
 * Makes help resources adaptable and persistable
 */
public class AdaptableSelectedTopic extends AdaptableHelpResource {

	/**
	 * This constructor will be called when wrapping help resources.
	 */
	public AdaptableSelectedTopic(ITopic element) {
		super(element);
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == ITopic.class)
			return element;
		return super.getAdapter(adapter);
	}

	public AdaptableHelpResource[] getChildren() {
		return new AdaptableHelpResource[0];
	}

	/**
	 * When href is exactly the href of the selected topic, then return the selected topic
	 * Otherwise, return null
	 * 
	 * @param href
	 *            The topic's href value.
	 */
	public ITopic getTopic(String href) {
		if (href == null)
			return null;
		if (href.equals(getHref())) {
			return (ITopic)element;
		}
		return null;
	}

	public void saveState(Element element) {

	}
}
