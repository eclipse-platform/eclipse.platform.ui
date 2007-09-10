/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkingSetElementAdapter;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A working set descriptor stores the plugin registry data for 
 * a working set page extension.
 * 
 * @since 2.0
 */
public class WorkingSetDescriptor implements IPluginContribution {
    private String id;

    private String name;

    private String icon;

    private String pageClassName;
    
    private String updaterClassName;

    private IConfigurationElement configElement;
    
    private String[] classTypes;

	private String[] adapterTypes;

    private static final String ATT_ID = "id"; //$NON-NLS-1$

    private static final String ATT_NAME = "name"; //$NON-NLS-1$

    private static final String ATT_ICON = "icon"; //$NON-NLS-1$	

    private static final String ATT_PAGE_CLASS = "pageClass"; //$NON-NLS-1$
    
    private static final String ATT_UPDATER_CLASS = "updaterClass";  //$NON-NLS-1$
    
    private static final String ATT_ELEMENT_ADAPTER_CLASS = "elementAdapterClass";  //$NON-NLS-1$

    private static final String TAG_APPLICABLE_TYPE = "applicableType"; //$NON-NLS-1$
    
    /**
     * Creates a descriptor from a configuration element.
     * 
     * @param configElement configuration element to create a descriptor from
     */
    public WorkingSetDescriptor(IConfigurationElement configElement)
            throws CoreException {
        super();
        this.configElement = configElement;
        id = configElement.getAttribute(ATT_ID);
        name = configElement.getAttribute(ATT_NAME);
        icon = configElement.getAttribute(ATT_ICON);
        pageClassName = configElement.getAttribute(ATT_PAGE_CLASS);
        updaterClassName = configElement.getAttribute(ATT_UPDATER_CLASS);

        if (name == null) {
            throw new CoreException(new Status(IStatus.ERROR,
                    WorkbenchPlugin.PI_WORKBENCH, 0,
                    "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                    null));
        }
        
        IConfigurationElement[] containsChildren = configElement
				.getChildren(TAG_APPLICABLE_TYPE);
		if (containsChildren.length > 0) {
			List byClassList = new ArrayList(containsChildren.length);
			List byAdapterList = new ArrayList(containsChildren.length);
			for (int i = 0; i < containsChildren.length; i++) {
				IConfigurationElement child = containsChildren[i];
				String className = child
						.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
				if (className != null)
					byClassList.add(className);
				if ("true".equals(child.getAttribute(IWorkbenchRegistryConstants.ATT_ADAPTABLE)))  //$NON-NLS-1$
					byAdapterList.add(className);
			}
			if (!byClassList.isEmpty()) {
				classTypes = (String[]) byClassList.toArray(new String[byClassList
						.size()]);
				Arrays.sort(classTypes);
			}
			
			if (!byAdapterList.isEmpty()) {
				adapterTypes = (String[]) byAdapterList.toArray(new String[byAdapterList
						.size()]);
				Arrays.sort(adapterTypes);
			}
		}
    }
    
    /**
     * Returns the name space that declares this working set.
     * 
     * @return the name space declaring this working set
     */
    public String getDeclaringNamespace() {
    	return configElement.getNamespace();
    }
    
    /**
	 * Return the namespace that contains the class referenced by the
	 * updaterClass. May be the bundle that declared this extension or another
	 * bundle that contains the referenced class.
	 * 
	 * @return the namespace
	 * @since 3.3
	 */
	public String getUpdaterNamespace() {
		return WorkbenchPlugin.getBundleForExecutableExtension(configElement,
				ATT_UPDATER_CLASS).getSymbolicName();
	}
    
    /**
	 * Return the namespace that contains the class referenced by the
	 * elementAdapterClass. May be the bundle that declared this extension or
	 * another bundle that contains the referenced class.
	 * 
	 * @return the namespace
	 * @since 3.3
	 */
	public String getElementAdapterNamespace() {
		return WorkbenchPlugin.getBundleForExecutableExtension(configElement,
				ATT_UPDATER_CLASS).getSymbolicName();
	}

    /**
	 * Creates a working set page from this extension descriptor.
	 * 
	 * @return a working set page created from this extension descriptor.
	 */
    public IWorkingSetPage createWorkingSetPage() {
        Object page = null;

        if (pageClassName != null) {
            try {
                page = WorkbenchPlugin.createExtension(configElement,
                        ATT_PAGE_CLASS);
            } catch (CoreException exception) {
                WorkbenchPlugin.log("Unable to create working set page: " + //$NON-NLS-1$
                        pageClassName, exception.getStatus());
            }
        }
        return (IWorkingSetPage) page;
    }

    /**
     * Returns the page's icon
     * 
     * @return the page's icon
     */
    public ImageDescriptor getIcon() {
        if (icon == null) {
			return null;
		}

        IExtension extension = configElement.getDeclaringExtension();
        String extendingPluginId = extension.getNamespace();
        return AbstractUIPlugin.imageDescriptorFromPlugin(extendingPluginId,
                icon);
    }

    /**
     * Returns the working set page id.
     * 
     * @return the working set page id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the working set page class name
     * 
     * @return the working set page class name or <code>null</code> if
     *  no page class name has been provided by the extension
     */
    public String getPageClassName() {
        return pageClassName;
    }

    /**
     * Returns the name of the working set element type the 
     * page works with.
     * 
     * @return the working set element type name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the working set updater class name
     * 
     * @return the working set updater class name or <code>null</code> if
     *  no updater class name has been provided by the extension
     */
    public String getUpdaterClassName() {
    	return updaterClassName;
    }
    
    /**
	 * Creates a working set element adapter.
	 * 
	 * @return the element adapter or <code>null</code> if no adapter has been
	 *         declared
	 */
	public IWorkingSetElementAdapter createWorkingSetElementAdapter() {
		if (!WorkbenchPlugin.hasExecutableExtension(configElement, ATT_ELEMENT_ADAPTER_CLASS))
			return null;
		IWorkingSetElementAdapter result = null;
		try {
			result = (IWorkingSetElementAdapter) WorkbenchPlugin
					.createExtension(configElement, ATT_ELEMENT_ADAPTER_CLASS);
		} catch (CoreException exception) {
			WorkbenchPlugin.log("Unable to create working set element adapter: " + //$NON-NLS-1$
					result, exception.getStatus());
		}
		return result;
	}
    
    /**
	 * Creates a working set updater.
	 * 
	 * @return the working set updater or <code>null</code> if no updater has
	 *         been declared
	 */
    public IWorkingSetUpdater createWorkingSetUpdater() {
    	if (updaterClassName == null) {
			return null;
		}
    	IWorkingSetUpdater result = null;
        try {
            result = (IWorkingSetUpdater)WorkbenchPlugin.createExtension(configElement, ATT_UPDATER_CLASS);
        } catch (CoreException exception) {
            WorkbenchPlugin.log("Unable to create working set updater: " + //$NON-NLS-1$
            	updaterClassName, exception.getStatus());
        }
        return result;   	
    }
    
    public boolean isUpdaterClassLoaded() {
    	return WorkbenchPlugin.isBundleLoadedForExecutableExtension(configElement, ATT_UPDATER_CLASS);
    }
    
    public boolean isElementAdapterClassLoaded() {
    	return WorkbenchPlugin.isBundleLoadedForExecutableExtension(configElement, ATT_ELEMENT_ADAPTER_CLASS);
    }
    
    /**
     * Returns whether working sets based on this descriptor are editable.
     * 
     * @return <code>true</code> if working sets based on this descriptor are editable; otherwise
     *  <code>false</code>
     * 
     * @since 3.1
     */
    public boolean isEditable() {
        return getPageClassName() != null;
    }

	public String getLocalId() {
		return getId();
	}

	public String getPluginId() {
		return getDeclaringNamespace();
	}
	
	/**
	 * Return the config element for this descriptor.
	 * 
	 * @return the config element
	 * @since 3.3
	 */
	public IConfigurationElement getConfigurationElement() {
		return configElement;
	}
	
	/**
	 * Return the description for this working set type.
	 * 
	 * @return the description for this type. May be an empty string.
	 * @since 3.4
	 */
	public String getDescription() {
		String description = configElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_DESCRIPTION);
		if (description == null)
			description = ""; //$NON-NLS-1$
		return description;
	}
}
