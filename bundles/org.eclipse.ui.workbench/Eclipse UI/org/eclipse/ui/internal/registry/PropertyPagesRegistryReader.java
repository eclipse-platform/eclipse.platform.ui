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
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.dialogs.IPropertyPageContributor;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.RegistryPageContributor;

/**
 * This class loads property pages from the registry.
 */
public class PropertyPagesRegistryReader extends CategorizedPageRegistryReader {
    public static final String ATT_NAME_FILTER = "nameFilter";//$NON-NLS-1$

    public static final String ATT_FILTER_NAME = "name";//$NON-NLS-1$

    public static final String ATT_FILTER_VALUE = "value";//$NON-NLS-1$

    public static final String ATT_CLASS = "class";//$NON-NLS-1$

    private static final String TAG_PAGE = "page";//$NON-NLS-1$

    private static final String TAG_FILTER = "filter";//$NON-NLS-1$

    private static final String ATT_NAME = "name";//$NON-NLS-1$

    private static final String ATT_ID = "id";//$NON-NLS-1$

    private static final String ATT_ICON = "icon";//$NON-NLS-1$

    private static final String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$

    private static final String ATT_ADAPTABLE = "adaptable";//$NON-NLS-1$

    private static final String P_TRUE = "true";//$NON-NLS-1$

    private HashMap filterProperties;
    
    private Collection pages = new ArrayList();

    private PropertyPageContributorManager manager;
    
    class PropertyCategoryNode extends CategoryNode{
    	
    	RegistryPageContributor page;
    	
    	/**
    	 * Create a new category node on the given reader for
    	 * the property page.
    	 * @param reader
    	 * @param propertyPage
    	 */
    	PropertyCategoryNode(CategorizedPageRegistryReader reader, RegistryPageContributor propertyPage){
    		super(reader);
    		page = propertyPage;
    	}
    	/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText()
		 */
		String getLabelText() {
			return page.getPageName();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText(java.lang.Object)
		 */
		String getLabelText(Object element) {
			return ((RegistryPageContributor)element).getPageName();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getNode()
		 */
		Object getNode() {
			return page;
		}
    }

    /**
     * The constructor.
     */
    public PropertyPagesRegistryReader(PropertyPageContributorManager manager) {
        this.manager = manager;
    }

    /**
     * Parses child element and processes it 
     */
    private void processChildElement(IConfigurationElement element) {
        String tag = element.getName();
        if (tag.equals(TAG_FILTER)) {
            String key = element.getAttribute(ATT_FILTER_NAME);
            String value = element.getAttribute(ATT_FILTER_VALUE);
            if (key == null || value == null)
                return;
            if (filterProperties == null)
                filterProperties = new HashMap();
            filterProperties.put(key, value);
        }
    }

    /**
     * Reads static property page specification.
     */
    private void processPageElement(IConfigurationElement element) {
        String pluginId = element.getDeclaringExtension().getNamespace();
        String pageId = element.getAttribute(ATT_ID);
        String pageName = element.getAttribute(ATT_NAME);
        String iconName = element.getAttribute(ATT_ICON);
        String pageClassName = element.getAttribute(ATT_CLASS);
        String objectClassName = element.getAttribute(ATT_OBJECTCLASS);
        String adaptable = element.getAttribute(ATT_ADAPTABLE);
        String category = element.getAttribute(ATT_CATEGORY);

        if (pageId == null) {
            logMissingAttribute(element, ATT_ID);
            return;
        }
        if (objectClassName == null) {
            logMissingAttribute(element, ATT_OBJECTCLASS);
            return;
        }
        if (pageClassName == null) {
            logMissingAttribute(element, ATT_CLASS);
            return;
        }

        filterProperties = null;
        IConfigurationElement[] children = element.getChildren();
        for (int i = 0; i < children.length; i++) {
            processChildElement(children[i]);
        }

        IPropertyPageContributor contributor = new RegistryPageContributor(
                pluginId, pageId, pageName, iconName, category, filterProperties,
                objectClassName, P_TRUE.equalsIgnoreCase(adaptable), element);
        registerContributor(objectClassName, contributor);
        pages.add(contributor);
    }

    /**
     * Reads the next contribution element.
     * 
     * public for dynamic UI
     */
    public boolean readElement(IConfigurationElement element) {
        if (element.getName().equals(TAG_PAGE)) {
            processPageElement(element);
            readElementChildren(element);
            return true;
        }
        if (element.getName().equals(TAG_FILTER)) {
            return true;
        }

        return false;
    }

    /**
     * Creates object class instance and registers the contributor with the
     * property page manager.
     */
    private void registerContributor(String objectClassName,
            IPropertyPageContributor contributor) {
        manager.registerContributor(contributor, objectClassName);
    }

    /**
     *	Reads all occurances of propertyPages extension in the registry.
     */
    public void registerPropertyPages(IExtensionRegistry registry) {
        readRegistry(registry, PlatformUI.PLUGIN_ID,
                IWorkbenchConstants.PL_PROPERTY_PAGES);
        processNodes();
    }    
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#add(java.lang.Object, java.lang.Object)
	 */
	void add(Object parent, Object node) {
	  ((RegistryPageContributor) parent).addSubPage((RegistryPageContributor)node);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#createCategoryNode(org.eclipse.ui.internal.registry.CategorizedPageRegistryReader, java.lang.Object)
	 */
	CategoryNode createCategoryNode(CategorizedPageRegistryReader reader, Object object) {
		return new PropertyCategoryNode(reader,(RegistryPageContributor) object);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.Object, java.lang.String)
	 */
	Object findNode(Object parent, String currentToken) {
		return ((RegistryPageContributor) parent).getChild(currentToken);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.String)
	 */
	Object findNode(String id) {
		Iterator iterator = pages.iterator();
		while(iterator.hasNext()){
			RegistryPageContributor next = (RegistryPageContributor) iterator.next();
			if(next.getPageId().equals(id))
				return next;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getCategory(java.lang.Object)
	 */
	String getCategory(Object node) {
		return ((RegistryPageContributor) node).getCategory();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getFavoriteNodeId()
	 */
	String getFavoriteNodeId() {
		return null;//properties do not support favorites
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getNodes()
	 */
	Collection getNodes() {
		return pages;
	}
}