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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.KeywordRegistryReader;

/**
 * The WorkbenchPreferenceExtensionNode is the abstract class for all property
 * and page nodes in the workbench.
 * 
 */
public abstract class WorkbenchPreferenceExtensionNode extends PreferenceNode {

	private static Map keywords;
	
	private Collection keywordReferences;

	private Collection keywordLabels;
	
	static{
	     KeywordRegistryReader keywordReader = new KeywordRegistryReader();
         keywordReader
         	.readRegistry(Platform.getExtensionRegistry(), PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_KEYWORDS);
        setKeywords(keywordReader.getKeywords());
	}

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
	 * Get the mapping of keyword ids to human readable Strings.
	 * @return Map
	 */
	public static Map getKeywords() {
		return keywords;
	}
	/**
	 * Set the mapping of keyword ids to human readable Strings.
	 * @param keywordMappings 
	 */
	public static void setKeywords(Map keywordMappings) {
		keywords = keywordMappings;
	}

	/**
	 * Get the labels of all of the keywords of the receiver.
	 * @return Collection of String or <code>null</code> if there
	 * are no keyword references.
	 */
	public Collection getKeywordLabels() {
		if(keywordReferences == null)
			return null;
		if(keywordLabels == null){
			keywordLabels = new ArrayList(0);
			Iterator referenceIterator = keywordReferences.iterator();
			while(referenceIterator.hasNext()){
				Object label = keywords.get(referenceIterator.next());
				if(label != null)
					keywordLabels.add(label);
			}
		}
		return keywordLabels;
	}

}
