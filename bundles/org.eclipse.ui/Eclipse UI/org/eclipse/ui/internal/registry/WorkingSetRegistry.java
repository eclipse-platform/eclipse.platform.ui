package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetDialog;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Stores working set descriptors for working set extensions.
 */
public class WorkingSetRegistry {
	// used in Workbench plugin.xml for default workingSet extension
	private static final String DEFAULT_DIALOG_ELEMENT_CLASS = "org.eclipse.core.resources.IResource";
	
	private HashMap workingSetDescriptors = new HashMap();

	/**
	 * Adds a working set descriptor.
	 * 
	 * @param descriptor working set descriptor to add. Must not 
	 * 	exist in the registry yet.
	 */
	public void addWorkingSetDescriptor(WorkingSetDescriptor descriptor) {
		Assert.isTrue(!workingSetDescriptors.containsValue(descriptor), "working set descriptor already registered"); //$NON-NLS-1$
		workingSetDescriptors.put(descriptor.getElementClassName(), descriptor);
	}
	/**
	 * Returns the default, resource based, working set dialog
	 * 
	 * @return the default working set dialog.
	 */
	public IWorkingSetDialog getDefaultWorkingSetDialog() {
		WorkingSetDescriptor descriptor = (WorkingSetDescriptor) workingSetDescriptors.get(DEFAULT_DIALOG_ELEMENT_CLASS);

		if (descriptor != null) {
			return descriptor.createWorkingSetDialog();
		}
		return null;
	}
	/**
	 * Returns a working set dialog that works with the elements in 
	 * the given working set.
	 * 
	 * @param workingSet working set to return a dialog for.
	 * @return a working set dialog that works with the elements in 
	 * 	the given working set.
	 */
	public IWorkingSetDialog getWorkingSetDialog(IWorkingSet workingSet) {
		IAdaptable[] elements = workingSet.getElements();
		
		if (elements.length == 0) {
			return getDefaultWorkingSetDialog();
		}
		Iterator iterator = workingSetDescriptors.keySet().iterator();			
		WorkingSetDescriptor descriptor = null;
		while (iterator.hasNext()) {
			String elementClassName = (String) iterator.next();
			Class elementClass = null;
			
			try {
				 elementClass = Class.forName(elementClassName);
			} catch (ClassNotFoundException e) {
				WorkbenchPlugin.log("Unable to create working set dialog for element type " + elementClassName);	//$NON-NLS-1$
			}				
			if (elementClass != null && elementClass.isInstance(elements[0])) {
				descriptor = (WorkingSetDescriptor) workingSetDescriptors.get(elementClassName);
				break;
			}
		}
		if (descriptor != null) {
			return descriptor.createWorkingSetDialog();
		}
		return null;
	}
	/**
	 * Loads the working set registry.
	 */
	public void load() {
		WorkingSetRegistryReader reader = new WorkingSetRegistryReader();
		reader.readWorkingSets(Platform.getPluginRegistry(), this);
	}	
}