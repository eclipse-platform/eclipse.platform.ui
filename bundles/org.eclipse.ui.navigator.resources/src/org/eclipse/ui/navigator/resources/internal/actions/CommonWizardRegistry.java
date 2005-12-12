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
package org.eclipse.ui.navigator.resources.internal.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.internal.extensions.RegistryReader;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonWizardRegistry {

	private static final CommonWizardRegistry INSTANCE = new CommonWizardRegistry();

	private static boolean isInitialized = false;

	private static final String[] NO_DESCRIPTORS = new String[0];

	public static final String WIZARD_TYPE_NEW = "new"; //$NON-NLS-1$

	private Map commonWizardDescriptors = new HashMap();

	/**
	 *  @return the singleton instance of the registry
	 */
	public static CommonWizardRegistry getInstance() {
		if (isInitialized)
			return INSTANCE;
		synchronized (INSTANCE) {
			if (!isInitialized) {
				INSTANCE.init();
				isInitialized = true;
			}
		}
		return INSTANCE;
	}


	private void init() {
		new CommonWizardRegistryReader().readRegistry();
	} 
	
	private void addCommonWizardDescriptor(CommonWizardDescriptor aDesc) {
		if (aDesc == null)
			return;
		synchronized (commonWizardDescriptors) {
			Set descriptors = (HashSet) commonWizardDescriptors.get(aDesc
					.getType());
			if (descriptors == null) {
				descriptors = new HashSet();
				commonWizardDescriptors.put(aDesc.getType(), descriptors);
			}
			if (descriptors.contains(aDesc) == false) {
				descriptors.add(aDesc);
			}
		}
	}

	/**
	 * 
	 * Returns all wizard id(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * @return the best content descriptor for the given element.
	 */
	public String[] getEnabledCommonWizardDescriptorIds(Object anElement,
			String aType) {

		Set commonDescriptors = (Set) commonWizardDescriptors.get(aType);
		if (commonDescriptors == null)
			return NO_DESCRIPTORS;
		/* Find other Common Wizard providers which enable for this object */
		List descriptorIds = new ArrayList();
		for (Iterator commonWizardDescriptorsItr = commonDescriptors.iterator(); commonWizardDescriptorsItr
				.hasNext();) {
			CommonWizardDescriptor descriptor = (CommonWizardDescriptor) commonWizardDescriptorsItr
					.next();

			if (descriptor.isEnabledFor(anElement))
				descriptorIds.add(descriptor.getWizardId());
		}
		String[] wizardIds = new String[descriptorIds.size()];
		return (String[]) descriptorIds.toArray(wizardIds); // Collections.unmodifiableList(descriptors);
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param aStructuredSelection
	 *            the element to return the best content descriptor for
	 * @return the best content descriptor for the given element.
	 */
	public String[] getEnabledCommonWizardDescriptorIds(
			IStructuredSelection aStructuredSelection, String aType) {
		Set commonDescriptors = (Set) commonWizardDescriptors.get(aType);
		if (commonDescriptors == null)
			return NO_DESCRIPTORS;
		/* Find other Common Wizard providers which enable for this object */
		List descriptorIds = new ArrayList();
		for (Iterator commonWizardDescriptorsItr = commonDescriptors.iterator(); commonWizardDescriptorsItr
				.hasNext();) {
			CommonWizardDescriptor descriptor = (CommonWizardDescriptor) commonWizardDescriptorsItr
					.next();

			if (descriptor.isEnabledFor(aStructuredSelection))
				descriptorIds.add(descriptor.getWizardId());
		}
		String[] wizardIds = new String[descriptorIds.size()];
		return (String[]) descriptorIds.toArray(wizardIds); // Collections.unmodifiableList(descriptors);
	}

	class CommonWizardRegistryReader extends RegistryReader {

		private static final String COMMON_WIZARD = "commonWizard"; //$NON-NLS-1$

		CommonWizardRegistryReader() {
			super(WorkbenchNavigatorPlugin.PLUGIN_ID, COMMON_WIZARD);
		}

 
		protected boolean readElement(IConfigurationElement anElement) {
			if (COMMON_WIZARD.equals(anElement.getName())) {
				try {
					addCommonWizardDescriptor(new CommonWizardDescriptor(
							anElement));
					return true;
				} catch (WorkbenchException e) {
					// log an error since its not safe to open a dialog here
					WorkbenchNavigatorPlugin
							.log(
									"Unable to create common wizard descriptor.", e.getStatus());//$NON-NLS-1$
				}
			}
			return false;
		}
	}
}
