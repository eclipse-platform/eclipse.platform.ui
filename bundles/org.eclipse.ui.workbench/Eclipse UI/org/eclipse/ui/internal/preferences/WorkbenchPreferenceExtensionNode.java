/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.keywords.KeywordRegistry;

/**
 * The WorkbenchPreferenceExtensionNode is the abstract class for all property
 * and page nodes in the workbench.
 * 
 * @since 3.1
 */
public abstract class WorkbenchPreferenceExtensionNode extends PreferenceNode {

	private Collection keywordReferences;

	/**
	 * Create a new instance of the reciever.
	 * 
	 * @param id
	 */
	public WorkbenchPreferenceExtensionNode(String id) {
		super(id);
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param id
	 * @param label
	 * @param image
	 * @param className
	 */
	public WorkbenchPreferenceExtensionNode(String id, String label,
			ImageDescriptor image, String className) {
		super(id, label, image, className);
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param id
	 * @param preferencePage
	 */
	public WorkbenchPreferenceExtensionNode(String id,
			IPreferencePage preferencePage) {
		super(id, preferencePage);
	}

	/**
	 * Set the keyword references to the collection of ids if there are any.
	 * 
	 * @param keywordBindings
	 *            Collection of String representing references to keywords.
	 */
	protected void setKeywordBindings(Collection keywordBindings) {
		if (keywordBindings.size() > 0)
			keywordReferences = keywordBindings;
	}

	/**
	 * Get the ids of the keywords the receiver is bound to.
	 * 
	 * @return Collection of String or <code>null</code> if there are none.
	 */
	public Collection getKeywordReferences() {
		return keywordReferences;
	}

	/**
	 * Get the labels of all of the keywords of the receiver.
	 * 
	 * @return Collection of <code>String</code>.  Never <code>null</code>.
	 */
	public Collection getKeywordLabels() {
		if(keywordReferences == null)
			return Collections.EMPTY_LIST;
		
		// TODO: this value should be cached and the keywords extension point
		// should be monitored for changes. Doing this will require adding
		// lifecycle to this class so that listeners can be cleaned up.
		Collection keywordLabels = new ArrayList(keywordReferences.size());
		Iterator referenceIterator = keywordReferences.iterator();
		while(referenceIterator.hasNext()){
			Object label = KeywordRegistry.getInstance().getKeywordLabel(
					(String) referenceIterator.next());
			if(label != null)
				keywordLabels.add(label);
		}
		
		return keywordLabels;
	}
}
