package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.help.*;
import org.eclipse.help.internal.workingset.AdaptableHelpResource;
import org.eclipse.ui.*;

/**
 * Makes help resources adaptable and persistable
 */
public class HelpResource
	implements IAdaptable, IPersistableElement {
		
	AdaptableHelpResource element;
	HelpResource parent;

	
	/**
	 * This constructor will be called when wrapping help resources.
	 */
	public HelpResource(AdaptableHelpResource element) {
		this.element = element;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IPersistableElement.class)
			return this;
		else 
			return element.getAdapter(adapter);
	}

	/**
	* @see IPersistableElement
	*/
	public void saveState(IMemento memento) {
		if (asToc() != null)
			memento.putString("toc", asToc().getHref());
		else if (asTopic() != null) {
			ITopic topic = asTopic();
			parent.saveState(memento);
			// get the index of this topic
			IAdaptable[] topics = parent.getChildren();
			for (int i=0; i<topics.length; i++)
				if (topics[i] == this) {
					memento.putString("topic", String.valueOf(i));
					return;
				}
		}
	}

	/**
	* @see IPersistableElement.
	*/
	public String getFactoryId() {
		return "org.eclipse.help.ui.internal.workingset.HelpResourceFactory";
	}

	public IAdaptable[] getChildren() {
		return element.getChildren();
	}

	IHelpResource asResource() {
		if (element instanceof IHelpResource)
			return (IHelpResource) element;
		else
			return null;
	}

	IToc asToc() {
		if (element instanceof IToc)
			return (IToc) element;
		else
			return null;
	}

	ITopic asTopic() {
		if (element instanceof ITopic)
			return (ITopic) element;
		else
			return null;
	}

	private void setParent(HelpResource parent) {
		this.parent = (HelpResource) parent;
	}

	HelpResource getParent() {
		return this.parent;
	}
	
	/**
	 * Tests the receiver and the object for equality
	 * 
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same.
	 * 	false otherwise
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof HelpResource) {
			HelpResource res = (HelpResource) object;
			if (asToc() != null && res.asToc() != null && asToc() == res.asToc())
				return true;
			//if (asArray() != null && res.asArray() != null && Arrays.equals(asArray(), res.asArray()))
			//	return true;
		}
		return false;
	}
	
		/**
	 * Returns the hash code.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		if (element == null)
			return -1;
		else
			return element.hashCode();
	}
}
