package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
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
		String href = memento.getString("href");
		if (href == null)
			return null;

		// Create the adaptable toc.
		IToc toc = HelpSystem.getTocManager().getToc(href, BootLoader.getNL());
		return new HelpResource(toc);
	}
}