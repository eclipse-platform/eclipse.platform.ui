/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.search.ISearchScope;
import org.eclipse.help.ui.*;
import org.eclipse.help.ui.RootScopePage;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
		if (icon!=null)
			image = HelpUIResources.getImage(icon);
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
	public RootScopePage createRootPage() {
		try {
			Object obj = config.createExecutableExtension(IHelpUIConstants.ATT_PAGE_CLASS);
			if (obj instanceof RootScopePage) {
				RootScopePage page = (RootScopePage)obj;
				page.setEngineIdentifier(getId());
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
					Object obj = config.createExecutableExtension(eclass);
					if (obj instanceof ISearchEngine) {
						engine = (ISearchEngine)obj;
					}
				}
				catch (CoreException e) {
					// Handle this
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
					Object obj = config.createExecutableExtension(fclass);
					if (obj instanceof ISearchScopeFactory) {
						factory = (ISearchScopeFactory)obj;
					}
				}
				catch (CoreException e) {
					// Handle this
				}
			}
		}
		if (factory!=null)
			return factory.createSearchScope(store);
		else
			return null;
	}
}