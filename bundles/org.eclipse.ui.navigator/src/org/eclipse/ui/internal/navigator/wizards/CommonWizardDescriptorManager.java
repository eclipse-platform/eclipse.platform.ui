/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentRegistryReader;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 */
public class CommonWizardDescriptorManager {

	private static final CommonWizardDescriptorManager INSTANCE = new CommonWizardDescriptorManager();

	private static boolean isInitialized = false;

	private static final String[] NO_DESCRIPTOR_IDS = new String[0];

	private static final CommonWizardDescriptor[] NO_DESCRIPTORS = new CommonWizardDescriptor[0];
	
	/**
	 * Find wizards of type 'new'.
	 */
	public static final String WIZARD_TYPE_NEW = "new"; //$NON-NLS-1$

	private Map commonWizardDescriptors = new HashMap();

	/**
	 * @return the singleton instance of the registry
	 */
	public static CommonWizardDescriptorManager getInstance() {
		if (isInitialized) {
			return INSTANCE;
		}
		synchronized (INSTANCE) {
			if (!isInitialized) {
				INSTANCE.init();
				isInitialized = true;
			}
		}
		return INSTANCE;
	}

	private void init() {
		new CommonWizardRegistry().readRegistry();
	}

	private void addCommonWizardDescriptor(CommonWizardDescriptor aDesc) {
		if (aDesc == null) {
			return;
		} else if(aDesc.getWizardId() == null) {
			NavigatorPlugin.logError(0, "A null wizardId was supplied for a commonWizard in " + aDesc.getNamespace(), null); //$NON-NLS-1$
		}
		synchronized (commonWizardDescriptors) {
			Set descriptors = (Set) commonWizardDescriptors.get(aDesc
					.getType());
			if (descriptors == null) { 
				commonWizardDescriptors.put(aDesc.getType(), descriptors = new HashSet());
			}
			if (!descriptors.contains(aDesc)) {
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
	 * @param aType
	 *            The type of wizards to locate (e.g. 'new', 'import', or
	 *            'export' etc).
	 * @param aContentService 
	 * 			 The content service to use when deciding visibility.   
	 * @return The set of commonWizard ids for the given element
	 */
	public String[] getEnabledCommonWizardDescriptorIds(Object anElement,
			String aType, INavigatorContentService aContentService) {

		Set commonDescriptors = (Set) commonWizardDescriptors.get(aType);
		if (commonDescriptors == null) {
			return NO_DESCRIPTOR_IDS;
		}
		/* Find other Common Wizard providers which enable for this object */
		List descriptorIds = new ArrayList();
		for (Iterator commonWizardDescriptorsItr = commonDescriptors.iterator(); commonWizardDescriptorsItr
				.hasNext();) {
			CommonWizardDescriptor descriptor = (CommonWizardDescriptor) commonWizardDescriptorsItr
					.next();

			if (isVisible(aContentService, descriptor)
					&& descriptor.isEnabledFor(anElement)) {
				descriptorIds.add(descriptor.getWizardId());
			}
		}
		String[] wizardIds = new String[descriptorIds.size()];
		return (String[]) descriptorIds.toArray(wizardIds); 
	}
	

	/**
	 * 
	 * Returns all wizard descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * @param aType
	 *            The type of wizards to locate (e.g. 'new', 'import', or
	 *            'export' etc).
	 * @param aContentService 
	 * 			 The content service to use when deciding visibility.   
	 * @return The set of commonWizard descriptors for the element
	 */
	public CommonWizardDescriptor[] getEnabledCommonWizardDescriptors(Object anElement,
			String aType, INavigatorContentService aContentService) {

		Set commonDescriptors = (Set) commonWizardDescriptors.get(aType);
		if (commonDescriptors == null) {
			return NO_DESCRIPTORS;
		}
		/* Find other Common Wizard providers which enable for this object */
		List descriptors = new ArrayList();
		for (Iterator commonWizardDescriptorsItr = commonDescriptors.iterator(); commonWizardDescriptorsItr
				.hasNext();) {
			CommonWizardDescriptor descriptor = (CommonWizardDescriptor) commonWizardDescriptorsItr
					.next();

			if (isVisible(aContentService, descriptor)
					&& descriptor.isEnabledFor(anElement)) {
				descriptors.add(descriptor);
			}
		}
		CommonWizardDescriptor[] enabledDescriptors = new CommonWizardDescriptor[descriptors.size()];
		return (CommonWizardDescriptor[]) descriptors.toArray(enabledDescriptors);  
	}

	/**
	 * @param aContentService
	 * @param descriptor
	 * @return True if the descriptor is visible to the given content service.
	 */
	private boolean isVisible(INavigatorContentService aContentService, CommonWizardDescriptor descriptor) {
		return !WorkbenchActivityHelper.filterItem(descriptor) && 
					(aContentService == null || 
							(descriptor.getId() == null || 
									( aContentService.isVisible(descriptor.getId()) && 
											aContentService.isActive(descriptor.getId())
									)
							)
					);
	}
  
	private class CommonWizardRegistry extends NavigatorContentRegistryReader {

		protected boolean readElement(final IConfigurationElement anElement) {
			final boolean[] retValue = new boolean[1];

			if (TAG_COMMON_WIZARD.equals(anElement.getName())) {
				SafeRunner.run(new NavigatorSafeRunnable(anElement) {
					public void run() throws Exception {
						addCommonWizardDescriptor(new CommonWizardDescriptor(anElement));
						retValue[0] = true;
					}
				});
				return retValue[0];
			}
			if (TAG_NAVIGATOR_CONTENT.equals(anElement.getName())) {
				IConfigurationElement[] commonWizards = anElement.getChildren(TAG_COMMON_WIZARD);
				final String contentExtensionId = anElement.getAttribute(ATT_ID);
				// In case there are none
				retValue[0] = true;
				for (int i = 0; i < commonWizards.length; i++) {
					final IConfigurationElement element = commonWizards[i];
					// Assume it did not work
					retValue[0] = false;
					SafeRunner.run(new NavigatorSafeRunnable(element) {
						public void run() throws Exception {
							addCommonWizardDescriptor(new CommonWizardDescriptor(element,
									contentExtensionId));
							retValue[0] = true;
						}
					});
					if (!retValue[0])
						break;
				}
				return retValue[0];
			}
			return super.readElement(anElement);
		}
	}

}
