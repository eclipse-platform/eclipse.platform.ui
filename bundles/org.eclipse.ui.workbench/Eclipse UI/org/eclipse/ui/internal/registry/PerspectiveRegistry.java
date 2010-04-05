/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.e4.core.di.annotations.PostConstruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Perspective registry.
 */
public class PerspectiveRegistry implements IPerspectiveRegistry, IExtensionChangeHandler {

	private IPropertyChangeListener preferenceListener;

	@Inject
	private IExtensionRegistry extensionRegistry;

	private Map<String, IPerspectiveDescriptor> descriptors = new HashMap<String, IPerspectiveDescriptor>();

	@PostConstruct
	void postConstruct() {
		IExtensionPoint point = extensionRegistry.getExtensionPoint("org.eclipse.ui.perspectives"); //$NON-NLS-1$
		for (IConfigurationElement element : point.getConfigurationElements()) {
			String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
			String label = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);

			descriptors.put(id, new PerspectiveDescriptor(id, label, element));
		}
	}

	/**
	 * Construct a new registry.
	 */
	public PerspectiveRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerHandler(this, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveRegistry#clonePerspective(java.lang.String,
	 * java.lang.String, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public IPerspectiveDescriptor clonePerspective(String id, String label,
			IPerspectiveDescriptor desc) throws IllegalArgumentException {
		// FIXME: compat clonePerspective
		E4Util.unsupported("clonePerspective"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveRegistry#deletePerspective(org.eclipse.ui.
	 * IPerspectiveDescriptor)
	 */
	public void deletePerspective(IPerspectiveDescriptor persp) {
		// FIXME: compat deletePerspective
		E4Util.unsupported("deletePerspective"); //$NON-NLS-1$
	}


	/**
	 * Deletes a list of perspectives
	 * 
	 * @param perspToDelete
	 */
	public void deletePerspectives(ArrayList perspToDelete) {
		for (int i = 0; i < perspToDelete.size(); i++) {
			deletePerspective((IPerspectiveDescriptor) perspToDelete.get(i));
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveRegistry#findPerspectiveWithId(java.lang.String
	 * )
	 */
	public IPerspectiveDescriptor findPerspectiveWithId(String perspectiveId) {
		return descriptors.get(perspectiveId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveRegistry#findPerspectiveWithLabel(java.lang
	 * .String)
	 */
	public IPerspectiveDescriptor findPerspectiveWithLabel(String label) {
		for (IPerspectiveDescriptor descriptor : descriptors.values()) {
			if (descriptor.getLabel().equals(label)) {
				return descriptor;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveRegistry#getDefaultPerspective()
	 */
	public String getDefaultPerspective() {
		return PrefUtil.getAPIPreferenceStore().getString(
				IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveRegistry#getPerspectives()
	 */
	public IPerspectiveDescriptor[] getPerspectives() {
		return descriptors.values().toArray(new IPerspectiveDescriptor[descriptors.size()]);
	}

	/**
	 * @see IPerspectiveRegistry#setDefaultPerspective(String)
	 */
	public void setDefaultPerspective(String id) {
		IPerspectiveDescriptor desc = findPerspectiveWithId(id);
		if (desc != null) {
			PrefUtil.getAPIPreferenceStore().setValue(
					IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, id);
		}
	}

	/**
	 * Return <code>true</code> if a label is valid. This checks only the given
	 * label in isolation. It does not check whether the given label is used by
	 * any existing perspectives.
	 * 
	 * @param label
	 *            the label to test
	 * @return whether the label is valid
	 */
	public boolean validateLabel(String label) {
		label = label.trim();
		if (label.length() <= 0) {
			return false;
		}
		return true;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveRegistry#revertPerspective(org.eclipse.ui.
	 * IPerspectiveDescriptor)
	 */
	public void revertPerspective(IPerspectiveDescriptor perspToRevert) {
		// FIXME: compat revertPerspective
		E4Util.unsupported("revertPerspective"); //$NON-NLS-1$
	}

	/**
	 * Dispose the receiver.
	 */
	public void dispose() {
		PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
		WorkbenchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(
				preferenceListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#
	 * removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
	 */
	public void removeExtension(IExtension source, Object[] objects) {
		// TODO compat: what do we do about disappearing extensions
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension
	 * (org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker,
	 * org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension addedExtension) {
		// TODO compat: what do we do about appeaering extensions
	}
}
