/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

	private HashMap<String, WorkingSetDescriptor> workingSetDescriptors = new HashMap<>();

	public WorkingSetRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));

	}

	/**
	 *
	 * @since 3.3
	 */
	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_WORKINGSETS);
	}

	/**
	 * Adds a working set descriptor.
	 *
	 * @param descriptor working set descriptor to add. Must not exist in the
	 *                   registry yet.
	 */
	public void addWorkingSetDescriptor(WorkingSetDescriptor descriptor) {
		Assert.isTrue(!workingSetDescriptors.containsValue(descriptor), "working set descriptor already registered"); //$NON-NLS-1$
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerObject(descriptor.getConfigurationElement().getDeclaringExtension(), descriptor,
				IExtensionTracker.REF_WEAK);
		workingSetDescriptors.put(descriptor.getId(), descriptor);
	}

	/**
	 * Returns the default, resource based, working set page
	 *
	 * @return the default working set page.
	 */
	public IWorkingSetPage getDefaultWorkingSetPage() {
		// @issue this will return the IDE resource working set page... not good for
		// generic workbench
		WorkingSetDescriptor descriptor = workingSetDescriptors.get(DEFAULT_PAGE_ID);

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
		return workingSetDescriptors.get(pageId);
	}

	/**
	 * Returns an array of all working set descriptors.
	 *
	 * @return an array of all working set descriptors.
	 */
	public WorkingSetDescriptor[] getWorkingSetDescriptors() {
		return workingSetDescriptors.values().toArray(new WorkingSetDescriptor[workingSetDescriptors.size()]);
	}

	/**
	 * Returns an array of all working set descriptors having a page class attribute
	 *
	 * @return an array of all working set descriptors having a page class attribute
	 */
	public WorkingSetDescriptor[] getNewPageWorkingSetDescriptors() {
		Collection<WorkingSetDescriptor> descriptors = workingSetDescriptors.values();
		List<WorkingSetDescriptor> result = new ArrayList<>(descriptors.size());
		for (Iterator<WorkingSetDescriptor> iter = descriptors.iterator(); iter.hasNext();) {
			WorkingSetDescriptor descriptor = iter.next();
			if (descriptor.getPageClassName() != null) {
				result.add(descriptor);
			}
		}
		return result.toArray(new WorkingSetDescriptor[result.size()]);
	}

	/**
	 * Returns <code>true</code> if there is a working set descriptor with a page
	 * class attribute. Otherwise <code>false</code> is returned.
	 *
	 * @return whether a descriptor with a page class attribute exists
	 */
	public boolean hasNewPageWorkingSetDescriptor() {
		Collection<WorkingSetDescriptor> descriptors = workingSetDescriptors.values();
		for (Iterator<WorkingSetDescriptor> iter = descriptors.iterator(); iter.hasNext();) {
			WorkingSetDescriptor descriptor = iter.next();
			if (descriptor.getPageClassName() != null) {
				return true;
			}
		}
		return false;
	}

	public List<WorkingSetDescriptor> getUpdaterDescriptorsForNamespace(String namespace) {
		if (namespace == null) { // fix for Bug 84225
			return Collections.emptyList();
		}
		List<WorkingSetDescriptor> result = new ArrayList<>();
		for (WorkingSetDescriptor descriptor : workingSetDescriptors.values()) {
			if (namespace.equals(descriptor.getUpdaterNamespace())) {
				result.add(descriptor);
			}
		}
		return result;
	}

	public WorkingSetDescriptor[] getElementAdapterDescriptorsForNamespace(String namespace) {
		if (namespace == null) // fix for Bug 84225
			return new WorkingSetDescriptor[0];
		Collection<WorkingSetDescriptor> descriptors = workingSetDescriptors.values();
		List<WorkingSetDescriptor> result = new ArrayList<>();
		for (Iterator<WorkingSetDescriptor> iter = descriptors.iterator(); iter.hasNext();) {
			WorkingSetDescriptor descriptor = iter.next();
			if (namespace.equals(descriptor.getDeclaringNamespace())) {
				result.add(descriptor);
			}
		}
		return result.toArray(new WorkingSetDescriptor[result.size()]);
	}

	/**
	 * Returns the working set page with the given id.
	 *
	 * @param pageId working set page id
	 * @return the working set page with the given id.
	 */
	public IWorkingSetPage getWorkingSetPage(String pageId) {
		WorkingSetDescriptor descriptor = workingSetDescriptors.get(pageId);

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

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		WorkingSetRegistryReader reader = new WorkingSetRegistryReader(this);
		for (IConfigurationElement element : extension.getConfigurationElements()) {
			reader.readElement(element);
		}
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof WorkingSetDescriptor) {
				WorkingSetDescriptor desc = (WorkingSetDescriptor) object;
				workingSetDescriptors.remove(desc.getId());
			}
		}
	}
}
