package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.HashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * Stores working set descriptors for working set extensions.
 */
public class WorkingSetRegistry {
	// used in Workbench plugin.xml for default workingSet extension
	private static final String DEFAULT_PAGE_ID = "org.eclipse.ui.resourceWorkingSetPage";
	
	private HashMap workingSetDescriptors = new HashMap();

	/**
	 * Adds a working set descriptor.
	 * 
	 * @param descriptor working set descriptor to add. Must not 
	 * 	exist in the registry yet.
	 */
	public void addWorkingSetDescriptor(WorkingSetDescriptor descriptor) {
		Assert.isTrue(!workingSetDescriptors.containsValue(descriptor), "working set descriptor already registered"); //$NON-NLS-1$
		workingSetDescriptors.put(descriptor.getId(), descriptor);
	}
	/**
	 * Returns the default, resource based, working set page
	 * 
	 * @return the default working set page.
	 */
	public IWorkingSetPage getDefaultWorkingSetPage() {
		WorkingSetDescriptor descriptor = (WorkingSetDescriptor) workingSetDescriptors.get(DEFAULT_PAGE_ID);

		if (descriptor != null) {
			return descriptor.createWorkingSetPage();
		}
		return null;
	}
	/**
	 * Returns an array of all working set descriptors.
	 * 
	 * @return an array of all working set descriptors.
	 */
	public WorkingSetDescriptor[] getWorkingSetDescriptors() {
		return (WorkingSetDescriptor[]) workingSetDescriptors.values().toArray(new WorkingSetDescriptor[workingSetDescriptors.size()]);
	}
	/**
	 * Returns the working set page with the given id.
	 * 
	 * @param pageId working set page id
	 * @return the working set page with the given id.
	 */
	public IWorkingSetPage getWorkingSetPage(String pageId) {
		WorkingSetDescriptor descriptor = (WorkingSetDescriptor) workingSetDescriptors.get(pageId);
		
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
		reader.readWorkingSets(Platform.getPluginRegistry(), this);
	}	
}