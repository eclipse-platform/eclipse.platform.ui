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
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This property page contributor is created from page entry
 * in the registry. Since instances of this class are created
 * by the workbench, there is no danger of premature loading
 * of plugins.
 */

public class RegistryPageContributor implements IPropertyPageContributor {
    private String pluginId;

    private String pageName;

    private String iconName;

    private String pageId;

    private boolean adaptable = false;

    private IConfigurationElement pageElement;

    private HashMap filterProperties;

	private String objectClassName;

    /**
     * PropertyPageContributor constructor.
     */
    public RegistryPageContributor(String pluginId, String pageId,
            String pageName, String iconName, HashMap filterProperties,
            String objectClassName, boolean adaptable,
            IConfigurationElement pageElement) {
        this.pluginId = pluginId;
        this.pageId = pageId;
        this.pageName = pageName;
        this.iconName = iconName;
        this.filterProperties = filterProperties;
        this.pageElement = pageElement;
        this.adaptable = adaptable;
        this.objectClassName = objectClassName;
    }

    /**
     * Implements the interface by creating property page specified with
     * the configuration element.
     */
    public boolean contributePropertyPages(PropertyPageManager mng,
            IAdaptable element) {
        PropertyPageNode node = new PropertyPageNode(this, element);
        mng.addToRoot(node);
        return true;
    }

    /**
     * Creates the page based on the information in the configuration element.
     */
    public IWorkbenchPropertyPage createPage(IAdaptable element)
            throws CoreException {
        IWorkbenchPropertyPage ppage = null;
        ppage = (IWorkbenchPropertyPage) WorkbenchPlugin.createExtension(
                pageElement, PropertyPagesRegistryReader.ATT_CLASS);

        ppage.setTitle(pageName);
        
        Object adapted = element;
        if(adaptable) {
        	adapted = LegacyResourceSupport.getAdapter(element, objectClassName);
        	if(adapted == null) {
            	String message = "Error adapting selection to " + objectClassName +  //$NON-NLS-1$
            			". Property page " + pageId + " is being ignored"; //$NON-NLS-1$ //$NON-NLS-2$            	
            	throw new CoreException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
                        IStatus.ERROR,message, null));
           }
        }
        
        ppage.setElement((IAdaptable)adapted);

        return ppage;
    }

    /**
     * Returns page icon as defined in the registry.
     */
    public ImageDescriptor getPageIcon() {
        if (iconName == null)
            return null;
        IExtension extension = pageElement.getDeclaringExtension();
        return AbstractUIPlugin.imageDescriptorFromPlugin(extension
                .getNamespace(), iconName);
    }

    /**
     * Returns page ID as defined in the registry.
     */

    public String getPageId() {
        return pageId;
    }

    /**
     * Returns plugin ID as defined in the registry.
     */

    public String getPluginId() {
        return pluginId;
    }

    /**
     * Returns page name as defined in the registry.
     */

    public String getPageName() {
        return pageName;
    }

    /**
     * Return true if name filter is not defined in the registry for this page,
     * or if name of the selected object matches the name filter.
     */
    public boolean isApplicableTo(Object object) {
        // Test name filter
        String nameFilter = pageElement
                .getAttribute(PropertyPagesRegistryReader.ATT_NAME_FILTER);
        if (nameFilter != null) {
            String objectName = object.toString();
            if (object instanceof IAdaptable) {
                IWorkbenchAdapter adapter = (IWorkbenchAdapter) ((IAdaptable) object)
                        .getAdapter(IWorkbenchAdapter.class);
                if (adapter != null) {
                    String elementName = adapter.getLabel(object);
                    if (elementName != null) {
                        objectName = elementName;
                    }
                }
            }
            if (!SelectionEnabler.verifyNameMatch(objectName, nameFilter))
                return false;
        }

        // Test custom filter	
        if (filterProperties == null)
            return true;
        IActionFilter filter = null;

        // Do the free IResource adapting
		Object adaptedObject = LegacyResourceSupport.getAdaptedResource(object);
		if(adaptedObject != null) {
			object = adaptedObject;
		}

        if (object instanceof IActionFilter) {
            filter = (IActionFilter) object;
        } else if (object instanceof IAdaptable) {
            filter = (IActionFilter) ((IAdaptable) object)
                    .getAdapter(IActionFilter.class);
        }

        if (filter != null) {
            return testCustom(object, filter);
        } else {
            return true;
        }
    }

    /**
     * Returns whether the object passes a custom key value filter
     * implemented by a matcher.
     */
    private boolean testCustom(Object object, IActionFilter filter) {
        if (filterProperties == null)
            return false;
        Iterator iter = filterProperties.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = (String) filterProperties.get(key);
            if (!filter.testAttribute(object, key, value))
                return false;
        }
        return true;
    }

    /*
     * @see IObjectContributor#canAdapt()
     */
    public boolean canAdapt() {
        return adaptable;
    }
    
	public String getObjectClass() {
		return objectClassName;
	}
}