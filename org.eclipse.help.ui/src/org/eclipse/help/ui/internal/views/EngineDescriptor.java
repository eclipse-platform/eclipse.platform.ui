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

import org.eclipse.core.runtime.*;
import org.eclipse.help.search.*;
import org.eclipse.help.ui.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Descriptor for a federated search engine participant.
 */
public class EngineDescriptor {
	private IConfigurationElement config;
	private Image image;
	private ISearchEngine engine;
	private ISearchScopeFactory factory;
	/**
	 * 
	 */
	public EngineDescriptor(IConfigurationElement config) {
		this.config = config;
	}
	public IConfigurationElement getConfig() {
		return config;
	}
	public IConfigurationElement [] getPages() {
		return config.getChildren("subpage");
	}
	
	public String getLabel() {
		return config.getAttribute(IHelpUIConstants.ATT_LABEL);		
	}
	public String getId() {
		return config.getAttribute(IHelpUIConstants.ATT_ID);
	}
	public boolean isEnabled() {
		String enabled = config.getAttribute(IHelpUIConstants.ATT_ENABLED);
		if (enabled!=null)
			return enabled.equals("true");
		return false;
	}
	public Image getIconImage() {
		if (image!=null)
			return image;
		String icon = config.getAttribute(IHelpUIConstants.ATT_ICON);
		if (icon!=null) {
			String bundleId = config.getNamespace();
			HelpUIResources.getImageDescriptor(bundleId, icon);
			return HelpUIResources.getImage(icon);
		}
		else
			image = HelpUIResources.getImage(IHelpUIConstants.IMAGE_HELP_SEARCH);
		return image;
	}
	public String getDescription() {
		String desc = null;
		IConfigurationElement [] children = config.getChildren(IHelpUIConstants.EL_DESC);
		if (children.length==1) 
			desc = children[0].getValue();
		return desc;
	}
	public ImageDescriptor getImageDescriptor() {
		ImageDescriptor desc=null;
		String icon = config.getAttribute(IHelpUIConstants.ATT_ICON);
		if (icon!=null)
			desc = HelpUIResources.getImageDescriptor(icon);
		else
			desc = HelpUIResources.getImageDescriptor(IHelpUIConstants.IMAGE_HELP_SEARCH);
		return desc;
	}
	public RootScopePage createRootPage(String scopeSetName) {
		try {
			Object obj = config.createExecutableExtension(IHelpUIConstants.ATT_PAGE_CLASS);
			if (obj instanceof RootScopePage) {
				RootScopePage page = (RootScopePage)obj;
				page.init(getId(), scopeSetName);
				return page;
			}
			else
				return null;
		}
		catch (CoreException e) {
			return null;
		}
	}
	public ISearchEngine getEngine() {
		if (engine==null) {
			String eclass = config.getAttribute(IHelpUIConstants.ATT_CLASS);
			if (eclass!=null) {
				try {
					Object obj = config.createExecutableExtension(IHelpUIConstants.ATT_CLASS);
					if (obj instanceof ISearchEngine) {
						engine = (ISearchEngine)obj;
					}
				}
				catch (CoreException e) {
					HelpUIPlugin.logWarning("Engine " + eclass + " cannot be instantiated");
				}
			}
		}
		return engine;
	}
	
	public ISearchScope createSearchScope(IPreferenceStore store) {
		if (factory==null) {
			String fclass = config.getAttribute(IHelpUIConstants.ATT_SCOPE_FACTORY);
			if (fclass!=null) {
				try {
					Object obj = config.createExecutableExtension(IHelpUIConstants.ATT_SCOPE_FACTORY);
					if (obj instanceof ISearchScopeFactory) {
						factory = (ISearchScopeFactory)obj;
					}
				}
				catch (CoreException e) {
                    HelpUIPlugin.logWarning("Scope factory " + fclass + " cannot be instantiated");
				}
			}
		}
		if (factory!=null)
			return factory.createSearchScope(store);
		else
			return null;
	}
}