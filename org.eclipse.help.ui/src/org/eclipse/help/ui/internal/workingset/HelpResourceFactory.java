package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.ui.*;

/**
 * Makes help resources adaptable
 */
public class HelpResourceFactory implements IElementFactory {

	/**
	 * This constructor will be called while restoring a persisted state.
	 * @see java.lang.Object#Object()
	 */
	public HelpResourceFactory() {
	}

	/**
	* @see IPersistableElement.
	*/
	public String getFactoryId() {
		return "org.eclipse.help.ui.internal.workingset.HelpResourceFactory";
	}

	/**
	 * @see IElementFactory
	 */
	public IAdaptable createElement(IMemento memento) {

		// Get the href
		String href = memento.getString("toc");
		if (href == null)
			return null;

		String child_pos = memento.getString("topic");
		int pos = -1;
		if (child_pos != null) {
			try {
				pos = Integer.parseInt(child_pos);
			} catch (Exception e) {
			}
		}

		AdaptableHelpResource toc = HelpSystem.getWorkingSetManager().getAdaptableToc(href);

		if (toc == null)
			return null;
			
		if (pos == -1) {
			// Create the adaptable toc.
			return toc;
		} else {
			// Create the adaptable topic
			AdaptableTopic[] topics = (AdaptableTopic[])toc.getChildren();
			if (pos <0 || topics.length <= pos)
				return null;
			else
				return topics[pos]; 
		}
	}
}