/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.preference.*;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ScopePreferenceManager extends PreferenceManager implements IHelpUIConstants {
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
	    public void createPage() {
	    	IPreferencePage page = desc.createRootPage();
	    	setPage(page);
	    	page.setTitle(desc.getLabel());
	    	page.setImageDescriptor(desc.getImageDescriptor());
	    	page.setDescription(desc.getDescription());
	    }
	}
	/**
	 * 
	 */
	public ScopePreferenceManager(ArrayList engineDescriptors) {
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
