package org.eclipse.help.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import org.eclipse.help.*;
import org.w3c.dom.Element;

/**
 * Makes help resources adaptable and persistable
 */
public class AdaptableTopic extends AdaptableHelpResource {

	/**
	 * This constructor will be called when wrapping help resources.
	 */
	AdaptableTopic(ITopic element) {
		super(element);
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == ITopic.class)
			return element;
		else
			return super.getAdapter(adapter);
	}

	public AdaptableHelpResource[] getChildren() {
		return new AdaptableHelpResource[0];
	}

	/**
	 * @see org.eclipse.help.ITopic#getSubtopics()
	 */
	public ITopic[] getSubtopics() {
		return ((ITopic)element).getSubtopics();
	}

	public void saveState(Element element) {
		AdaptableToc toc = (AdaptableToc)getParent();
		toc.saveState(element);
		AdaptableHelpResource[] topics = toc.getChildren();
		for (int i=0; i<topics.length; i++)
			if (topics[i] == this)		
				element.setAttribute("topic", String.valueOf(i));
	}
}
