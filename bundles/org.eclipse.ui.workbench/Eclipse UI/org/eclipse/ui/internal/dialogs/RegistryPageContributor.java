/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.CategorizedPageRegistryReader;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This property page contributor is created from page entry
 * in the registry. Since instances of this class are created
 * by the workbench, there is no danger of premature loading
 * of plugins.
 */

public class RegistryPageContributor implements IPropertyPageContributor, IAdaptable {
    private String pageId;
    /**
     * The list of subpages (immediate children) of this node (element type:
     * <code>RegistryPageContributor</code>).
     */
    private Collection subPages = new ArrayList();

    private boolean adaptable = false;

    private IConfigurationElement pageElement;

    private SoftReference filterProperties;

    /**
     * PropertyPageContributor constructor.
     * 
     * @param pageId the id
     * @param element the element
     */
    public RegistryPageContributor(String pageId, IConfigurationElement element) {
        this.pageId = pageId;
        this.pageElement = element;
        adaptable = Boolean.valueOf(pageElement.getAttribute(PropertyPagesRegistryReader.ATT_ADAPTABLE)).booleanValue();
    }

	 /* (non-Javadoc)
     * @see org.eclipse.ui.internal.dialogs.IPropertyPageContributor#contributePropertyPages(org.eclipse.ui.internal.dialogs.PropertyPageManager, java.lang.Object)
     */
    public boolean contributePropertyPages(PropertyPageManager mng,
            Object element) {
        PropertyPageNode node = new PropertyPageNode(this, element);
        
        if(getCategory() == null){
        	mng.addToRoot(node);
        	return true;
        }
        if(!mng.addToDeep(getCategory(),node))
        	mng.addToRoot(node);
    
        return true;
    }

    /**
     * Creates the page based on the information in the configuration element.
     * 
     * @param element the adaptable element
     * @return the property page
     * @throws CoreException thrown if there is a problem creating the apge
     */
    public IWorkbenchPropertyPage createPage(Object element)
            throws CoreException {
        IWorkbenchPropertyPage ppage = null;
        ppage = (IWorkbenchPropertyPage) WorkbenchPlugin.createExtension(
                pageElement, IWorkbenchRegistryConstants.ATT_CLASS);

        ppage.setTitle(getPageName());
        
        Object adapted = element;
        if(adaptable) {
        	adapted = LegacyResourceSupport.getAdapter(element, getObjectClass());
        	if(adapted == null) {
            	String message = "Error adapting selection to " + getObjectClass() +  //$NON-NLS-1$
            			". Property page " + pageId + " is being ignored"; //$NON-NLS-1$ //$NON-NLS-2$            	
            	throw new CoreException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
                        IStatus.ERROR,message, null));
           }
        }
        
        if(adapted instanceof IAdaptable)
        	ppage.setElement((IAdaptable) adapted);
        else
        	ppage.setElement(new AdaptableForwarder(adapted));

        return ppage;
    }

    /**
     * Returns page icon as defined in the registry.
     * 
     * @return the page icon
     */
    public ImageDescriptor getPageIcon() {
    	String iconName = pageElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
        if (iconName == null)
            return null;
        return AbstractUIPlugin.imageDescriptorFromPlugin(pageElement
                .getNamespaceIdentifier(), iconName);
    }

    /**
     * Returns page ID as defined in the registry.
     * 
     * @return the page id
     */

    public String getPageId() {
        return pageId;
    }

    /**
     * Returns page name as defined in the registry.
     * 
     * @return the page name
     */
    public String getPageName() {
        return pageElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
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
        if (getFilterProperties() == null)
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

        if (filter != null) 
            return testCustom(object, filter);

        return true;
    }

    /**
     * Returns whether the object passes a custom key value filter
     * implemented by a matcher.
     */
    private boolean testCustom(Object object, IActionFilter filter) {
        Map filterProperties = getFilterProperties();
		
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
    
	/**
	 * Return the object class name
	 * 
	 * @return the object class name
	 */
	public String getObjectClass() {
		return pageElement.getAttribute(PropertyPagesRegistryReader.ATT_OBJECTCLASS);
	}
	
    /**
	 * Get the id of the category.
	 * @return String
	 * @since 3.1
	 */
	public String getCategory() {
		return pageElement.getAttribute(CategorizedPageRegistryReader.ATT_CATEGORY);
	}
	
	/**
	 * Return the children of the receiver.
	 * @return Collection
	 */
	public Collection getSubPages() {
		return subPages;
	}
	
	/**
	 * Add child to the list of children.
	 * @param child
	 */
	public void addSubPage(RegistryPageContributor child){
		subPages.add(child);
	}

    private Map getFilterProperties () {
    	if (filterProperties == null || filterProperties.get() == null) {
    		Map map = new HashMap();
    		filterProperties = new SoftReference(map);
	        IConfigurationElement[] children = pageElement.getChildren();
	        for (int i = 0; i < children.length; i++) {
	            processChildElement(map, children[i]);
	        }
    	}
	    return (Map) filterProperties.get();
    }
    
	/**
	 * Get the child with the given id.
	 * 
	 * @param id
	 * @return RegistryPageContributor
	 */
	public Object getChild(String id) {
		Iterator iterator = subPages.iterator();
		while(iterator.hasNext()){
			RegistryPageContributor next = (RegistryPageContributor) iterator.next();
			if(next.getPageId().equals(id))
				return next;
		}
		return null;
	}
	
    /**
     * Parses child element and processes it.
     * 
     * @since 3.1
     */
    private void processChildElement(Map map, IConfigurationElement element) {
        String tag = element.getName();
        if (tag.equals(PropertyPagesRegistryReader.TAG_FILTER)) {
            String key = element.getAttribute(PropertyPagesRegistryReader.ATT_FILTER_NAME);
            String value = element.getAttribute(PropertyPagesRegistryReader.ATT_FILTER_VALUE);
            if (key == null || value == null)
                return;
            map.put(key, value);
        }
    }
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 * @since 3.1
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IConfigurationElement.class)) {
			return getConfigurationElement();
		}
		return null;
	}

	/**
	 * @return the configuration element
	 * @since 3.1
	 */
	IConfigurationElement getConfigurationElement() {
		return pageElement;
	}
}
