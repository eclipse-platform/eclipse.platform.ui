/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * Stores working set descriptors for working set extensions.
 */
public class WorkingSetRegistry implements IExtensionChangeHandler {
    // used in Workbench plugin.xml for default workingSet extension
    // @issue this is an IDE specific working set page!
    private static final String DEFAULT_PAGE_ID = "org.eclipse.ui.resourceWorkingSetPage"; //$NON-NLS-1$

    private HashMap/*<String, WorkingSetDescriptor>*/ workingSetDescriptors = new HashMap();

    /**
	 * 
	 */
	public WorkingSetRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench()
				.getExtensionTracker();
		tracker.registerHandler(this, ExtensionTracker
				.createExtensionPointFilter(getExtensionPointFilter()));

	}

	/**
	 * 
	 * @return
	 * @since 3.3
	 */
	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(
				PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_WORKINGSETS);
	}
	
    /**
     * Adds a working set descriptor.
     * 
     * @param descriptor working set descriptor to add. Must not 
     * 	exist in the registry yet.
     */
    public void addWorkingSetDescriptor(WorkingSetDescriptor descriptor) {
		Assert.isTrue(!workingSetDescriptors.containsValue(descriptor),
				"working set descriptor already registered"); //$NON-NLS-1$
		IExtensionTracker tracker = PlatformUI.getWorkbench()
				.getExtensionTracker();
		tracker.registerObject(descriptor.getConfigurationElement()
				.getDeclaringExtension(), descriptor,
				IExtensionTracker.REF_WEAK);
		workingSetDescriptors.put(descriptor.getId(), descriptor);
	}

    /**
	 * Returns the default, resource based, working set page
	 * 
	 * @return the default working set page.
	 */
    public IWorkingSetPage getDefaultWorkingSetPage() {
        // @issue this will return the IDE resource working set page... not good for generic workbench
        WorkingSetDescriptor descriptor = (WorkingSetDescriptor) workingSetDescriptors
                .get(DEFAULT_PAGE_ID);

        if (descriptor != null) {
            return descriptor.createWorkingSetPage();
        }
        return null;
    }

    /**
     * Returns the working set descriptor with the given id.
     * 
     * @param pageId working set page id
     * @return the working set descriptor with the given id.
     */
    public WorkingSetDescriptor getWorkingSetDescriptor(String pageId) {
        return (WorkingSetDescriptor) workingSetDescriptors.get(pageId);
    }

    /**
     * Returns an array of all working set descriptors.
     * 
     * @return an array of all working set descriptors.
     */
    public WorkingSetDescriptor[] getWorkingSetDescriptors() {
        return (WorkingSetDescriptor[]) workingSetDescriptors.values().toArray(
                new WorkingSetDescriptor[workingSetDescriptors.size()]);
    }
    
    /**
     * Returns an array of all working set descriptors having
     * a page class attribute
     * 
     * @return an array of all working set descriptors having a 
     * page class attribute
     */
    public WorkingSetDescriptor[] getNewPageWorkingSetDescriptors() {
    	Collection descriptors= workingSetDescriptors.values();
        List result= new ArrayList(descriptors.size());
        for (Iterator iter= descriptors.iterator(); iter.hasNext();) {
			WorkingSetDescriptor descriptor= (WorkingSetDescriptor)iter.next();
			if (descriptor.getPageClassName() != null) {
				result.add(descriptor);
			}
		}
        return (WorkingSetDescriptor[])result.toArray(new WorkingSetDescriptor[result.size()]);
    }
    
    /**
     * Returns <code>true</code> if there is a working set descriptor with
     * a page class attribute. Otherwise <code>false</code> is returned.
     * 
     * @return whether a descriptor with a page class attribute exists
     */
    public boolean hasNewPageWorkingSetDescriptor() {
    	Collection descriptors= workingSetDescriptors.values();
        for (Iterator iter= descriptors.iterator(); iter.hasNext();) {
			WorkingSetDescriptor descriptor= (WorkingSetDescriptor)iter.next();
			if (descriptor.getPageClassName() != null) {
				return true;
			}
		}
    	return false;
    }
    
    public WorkingSetDescriptor[] getUpdaterDescriptorsForNamespace(
			String namespace) {
    	if (namespace == null) // fix for Bug 84225
    		return new WorkingSetDescriptor[0];
		Collection descriptors = workingSetDescriptors.values();
		List result = new ArrayList();
		for (Iterator iter = descriptors.iterator(); iter.hasNext();) {
			WorkingSetDescriptor descriptor = (WorkingSetDescriptor) iter
					.next();
			if (namespace.equals(descriptor.getUpdaterNamespace())) {
				result.add(descriptor);
			}
		}
		return (WorkingSetDescriptor[]) result
				.toArray(new WorkingSetDescriptor[result.size()]);
	}
    
    public WorkingSetDescriptor[] getElementAdapterDescriptorsForNamespace(
			String namespace) {
    	if (namespace == null) // fix for Bug 84225
    		return new WorkingSetDescriptor[0];
		Collection descriptors = workingSetDescriptors.values();
		List result = new ArrayList();
		for (Iterator iter = descriptors.iterator(); iter.hasNext();) {
			WorkingSetDescriptor descriptor = (WorkingSetDescriptor) iter
					.next();
			if (namespace.equals(descriptor.getDeclaringNamespace())) {
				result.add(descriptor);
			}
		}
		return (WorkingSetDescriptor[]) result
				.toArray(new WorkingSetDescriptor[result.size()]);
	}

    /**
     * Returns the working set page with the given id.
     * 
     * @param pageId working set page id
     * @return the working set page with the given id.
     */
    public IWorkingSetPage getWorkingSetPage(String pageId) {
        WorkingSetDescriptor descriptor = (WorkingSetDescriptor) workingSetDescriptors
                .get(pageId);

        if (descriptor == null) {
            return null;
        }
        return descriptor.createWorkingSetPage();
    }

    /**
     * Loads the working set registry.
     */
    public void load() {
        WorkingSetRegistryReader reader = new WorkingSetRegistryReader();
        reader.readWorkingSets(Platform.getExtensionRegistry(), this);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		WorkingSetRegistryReader reader = new WorkingSetRegistryReader(this);
		IConfigurationElement[] elements = extension.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
			reader.readElement(elements[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof WorkingSetDescriptor) {
                WorkingSetDescriptor desc = (WorkingSetDescriptor) objects[i];
                workingSetDescriptors.remove(desc.getId());
            }
        }
	}
}
