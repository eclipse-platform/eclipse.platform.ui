package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.ui.*;

/**
 * Makes help resources adaptable and persistable
 */
public class HelpResource
	implements IAdaptable, IPersistableElement {
	Object element;
	HelpResource[] children;
	HelpResource parent;

	
	/**
	 * This constructor will be called when wrapping help resources.
	 */
	public HelpResource(Object element) {
		this.element = element;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IHelpResource.class && element instanceof IHelpResource)
			return element;
		else if (
			adapter == IHelpResource[].class
				&& element instanceof IHelpResource[])
			return element;
		else if (adapter == IPersistableElement.class)
			return this;
		else
			return null;
	}

	/**
	* @see IPersistableElement
	*/
	public void saveState(IMemento memento) {
		if (asToc() != null)
			memento.putString("href", asToc().getHref());
	}

	/**
	* @see IPersistableElement.
	*/
	public String getFactoryId() {
		return "org.eclipse.help.ui.internal.workingset.HelpResourceFactory";
	}

	public IAdaptable[] getChildren() {
		if (element instanceof IHelpResource[]) {
			if (children == null) {
				IHelpResource[] resources = asArray();
				children = new HelpResource[resources.length];
				for (int i = 0; i < resources.length; i++) {
					children[i] =
						new HelpResource(resources[i]);
					//adaptableChildren[i].setParent(this);
				}
			}
			return children;
		/*
		} else if (element instanceof IToc) {
			if (children == null) {
				IToc toc = asToc();
				ITopic[] topics = toc.getTopics();
				children = new HelpResource[topics.length];
				for (int i = 0; i < topics.length; i++) {
					children[i] = new HelpResource(topics[i]);
					children[i].setParent(this);
				}
			}
			return children;
		*/
		} else {
			return new IAdaptable[0];
		}
	}

	IHelpResource[] asArray() {
		if (element instanceof IHelpResource[])
			return (IHelpResource[]) element;
		else
			return null;
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
			if (asArray() != null && res.asArray() != null && Arrays.equals(asArray(), res.asArray()))
				return true;
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
