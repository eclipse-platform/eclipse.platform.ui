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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.RegistryPageContributor;

/**
 * This class loads property pages from the registry.
 */
public class PropertyPagesRegistryReader extends RegistryReader {
    public static final String ATT_NAME_FILTER = "nameFilter";//$NON-NLS-1$

    public static final String ATT_FILTER_NAME = "name";//$NON-NLS-1$

    public static final String ATT_FILTER_VALUE = "value";//$NON-NLS-1$

    public static final String ATT_CLASS = "class";//$NON-NLS-1$

    private static final String TAG_PAGE = "page";//$NON-NLS-1$

    public static final String TAG_FILTER = "filter";//$NON-NLS-1$

    public static final String ATT_NAME = "name";//$NON-NLS-1$

    private static final String ATT_ID = "id";//$NON-NLS-1$

    public static final String ATT_ICON = "icon";//$NON-NLS-1$

    public static final String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$

    public static final String ATT_ADAPTABLE = "adaptable";//$NON-NLS-1$

    private static final String P_TRUE = "true";//$NON-NLS-1$

    private PropertyPageContributorManager manager;

    /**
     * The constructor.
     */
    public PropertyPagesRegistryReader(PropertyPageContributorManager manager) {
        this.manager = manager;
    }

    /**
     * Reads static property page specification.
     */
    private void processPageElement(IConfigurationElement element) {
    	String pageId = element.getAttribute(ATT_ID);
        String pageClassName = element.getAttribute(ATT_CLASS);
        String objectClassName = element.getAttribute(ATT_OBJECTCLASS);

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

        RegistryPageContributor contributor = new RegistryPageContributor(
                pageId, element);
        registerContributor(contributor, objectClassName);
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
     * @param objectClassName
     */
    private void registerContributor(RegistryPageContributor contributor, String objectClassName) {
        manager.registerContributor(contributor, objectClassName);
    }

    /**
     *	Reads all occurances of propertyPages extension in the registry.
     */
    public void registerPropertyPages(IExtensionRegistry registry) {
        readRegistry(registry, PlatformUI.PLUGIN_ID,
                IWorkbenchConstants.PL_PROPERTY_PAGES);
    }
}