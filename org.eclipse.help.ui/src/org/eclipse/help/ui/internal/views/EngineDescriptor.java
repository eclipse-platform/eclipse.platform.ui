/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.*;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.help.search.*;
import org.eclipse.help.ui.RootScopePage;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Descriptor for a federated search engine participant.
 */
public class EngineDescriptor {
	public static final String P_MASTER = "__enabled__";
	private ISearchEngine engine;
	private IConfigurationElement config;
	private EngineTypeDescriptor etdesc;
	private Hashtable parameters;
	/**
	 * 
	 */
	public EngineDescriptor(IConfigurationElement config) {
		this.config = config;
	}
	public void setEngineType(EngineTypeDescriptor etdesc) {
		this.etdesc = etdesc;
	}
	public IConfigurationElement getConfig() {
		return config;
	}
	
	public String getLabel() {
		String label = config.getAttribute(IHelpUIConstants.ATT_LABEL);
		if (label==null)
			label = etdesc.getLabel();
		return label;
	}
	public String getId() {
		return config.getAttribute(IHelpUIConstants.ATT_ID);
	}
	
	public String getEngineId() {
		return config.getAttribute(IHelpUIConstants.ATT_ENGINE_TYPE_ID);
	}

	public boolean isEnabled() {
		String enabled = config.getAttribute(IHelpUIConstants.ATT_ENABLED);
		if (enabled!=null)
			return enabled.equals("true"); //$NON-NLS-1$
		return false;
	}
	public boolean isRemovable() {
		String removable = config.getAttribute(IHelpUIConstants.ATT_REMOVABLE);
		if (removable!=null)
			return removable.equals("true"); //$NON-NLS-1$
		return false;
		
	}
	
	public Image getIconImage() {
		return etdesc.getIconImage();
	}
	
	public String getDescription() {
		String desc = null;
		IConfigurationElement [] children = config.getChildren(IHelpUIConstants.EL_DESC);
		if (children.length==1)
			desc = children[0].getValue();
		if (desc==null)
			return etdesc.getDescription();
		return desc;
	}	
	
	public IConfigurationElement [] getPages() {
		return etdesc.getPages();
	}
	
	public ImageDescriptor getImageDescriptor() {
		return etdesc.getImageDescriptor();
	}
	
	public RootScopePage createRootPage(String scopeSetName) {
		RootScopePage page = etdesc.createRootPage(scopeSetName);
		if (page!=null) {
			Dictionary parameters = getParameters();
			page.init(getId(), scopeSetName, parameters);
		}
		return page;
	}

	public Dictionary getParameters() {
		if (parameters!=null) return parameters;
		parameters = new Hashtable();
		parameters.put(P_MASTER, isEnabled()?Boolean.TRUE:Boolean.FALSE);
		IConfigurationElement[] params = config.getChildren("param");
		for (int i=0; i<params.length; i++) {
			IConfigurationElement param = params[i];
			String name = param.getAttribute(IHelpUIConstants.ATT_NAME);
			String value = param.getAttribute(IHelpUIConstants.ATT_VALUE);
			if (name!=null && value!=null)
				parameters.put(name, value);
		}
		return parameters;
	}
	
	public ISearchEngine getEngine() {
		if (engine==null) {
			engine = etdesc.createEngine();
		}
		return engine;
	}
	
	public ISearchScope createSearchScope(IPreferenceStore store) {
		return etdesc.createSearchScope(store, getId(), getParameters());
	}
}