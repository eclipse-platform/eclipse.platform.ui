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
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.preference.*;

public class ScopePreferenceManager extends PreferenceManager implements IHelpUIConstants {
	private ScopeSet set;	
	class SubpagePreferenceNode extends PreferenceNode {
		private IConfigurationElement config;
	
		public SubpagePreferenceNode(String id, String label,
				IConfigurationElement config) {
			super(id, label, null, null);
			this.config = config;
		}
	    public void createPage() {
	    	try {
	    		Object obj = config.createExecutableExtension(IHelpUIConstants.ATT_CLASS);
	    		IPreferencePage page = (IPreferencePage)obj;
	    		setPage(page);
	    		page.setTitle(getLabelText());
	    	}
	    	catch (CoreException e) {
	    		// TODO handle this
	    	}
	    }
	}
	class EnginePreferenceNode extends PreferenceNode {
		private EngineDescriptor desc;
		
		public EnginePreferenceNode(EngineDescriptor desc) {
			super(desc.getId(), desc.getLabel(), desc.getImageDescriptor(), null);
			this.desc = desc;
		}
		public EngineDescriptor getDescriptor() {
			return desc;
		}
	    public void createPage() {
	    	IPreferencePage page = desc.createRootPage(set.getName());
	    	setPage(page);
	    	page.setTitle(desc.getLabel());
	    	page.setImageDescriptor(desc.getImageDescriptor());
	    	page.setDescription(desc.getDescription());
	    }
	}
	/**
	 * 
	 */
	public ScopePreferenceManager(ArrayList engineDescriptors, ScopeSet set) {
		this.set = set;		
		load(engineDescriptors);
	}

	private void load(ArrayList descriptors) {
		for (int i=0; i<descriptors.size(); i++) {
			EngineDescriptor desc = (EngineDescriptor)descriptors.get(i);
			addNode(desc);
			IConfigurationElement [] pages = desc.getPages();
			for (int j=0; j<pages.length; j++) {
				String category = pages[i].getAttribute(ATT_CATEGORY);
				addNode(category, pages[i].getAttribute(ATT_ID), 
						pages[i].getAttribute(ATT_LABEL), 
						pages[i]);
			}
		}
	}
	private IPreferenceNode addNode(EngineDescriptor desc) {
		PreferenceNode node = new EnginePreferenceNode(desc);
		addToRoot(node);
		return node;
	}
	private IPreferenceNode addNode(String category, String id, String name, IConfigurationElement config) {
		IPreferenceNode parent = find(category);
		PreferenceNode node = new SubpagePreferenceNode(id, name, config);
		if (parent!=null)
			parent.add(node);
		return node;
	}	
}