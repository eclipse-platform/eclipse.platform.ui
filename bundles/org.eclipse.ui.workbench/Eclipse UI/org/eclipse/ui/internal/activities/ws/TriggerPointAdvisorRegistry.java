/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal.activities.ws;

import java.util.ArrayList;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * @since 3.1
 */
public class TriggerPointAdvisorRegistry {

	private static TriggerPointAdvisorRegistry instance;

	private TriggerPointAdvisorRegistry() {
	}

	/**
	 * Return the instance of this registry.
	 *
	 * @return the instance of this registry
	 */
	public static TriggerPointAdvisorRegistry getInstance() {
		if (instance == null) {
			instance = new TriggerPointAdvisorRegistry();
		}

		return instance;
	}

	/**
	 * Return the trigger point advisors.
	 *
	 * @return the advisors
	 */
	public TriggerPointAdvisorDescriptor[] getAdvisors() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACTIVITYSUPPORT);
		if (point == null) {
			return new TriggerPointAdvisorDescriptor[0];
		}

		IExtension[] extensions = point.getExtensions();
		extensions = RegistryReader.orderExtensions(extensions);

		ArrayList<TriggerPointAdvisorDescriptor> list = new ArrayList<>(extensions.length);
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(IWorkbenchRegistryConstants.TAG_TRIGGERPOINTADVISOR)) {
					try {
						TriggerPointAdvisorDescriptor descriptor = new TriggerPointAdvisorDescriptor(element);
						list.add(descriptor);
					} catch (IllegalArgumentException e) {
						// log an error since its not safe to open a dialog here
						WorkbenchPlugin.log("invalid trigger point advisor extension", //$NON-NLS-1$
								StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e));
					}
				}
			}
		}

		return list.toArray(new TriggerPointAdvisorDescriptor[list.size()]);
	}

	/**
	 * Return the trigger point advisor bound to a given product.
	 *
	 * @param productId the product id
	 * @return the advisor
	 */
	public TriggerPointAdvisorDescriptor getAdvisorForProduct(String productId) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACTIVITYSUPPORT);
		if (point == null) {
			return null;
		}

		IExtension[] extensions = point.getExtensions();
		extensions = RegistryReader.orderExtensions(extensions);

		String targetIntroId = getAdvisorForProduct(productId, extensions);
		if (targetIntroId == null) {
			return null;
		}

		TriggerPointAdvisorDescriptor[] advisors = getAdvisors();
		for (TriggerPointAdvisorDescriptor advisor : advisors) {
			if (advisor.getId().equals(targetIntroId)) {
				return advisor;
			}
		}

		return null;
	}

	/**
	 * @return the advisor id
	 */
	private String getAdvisorForProduct(String targetProductId, IExtension[] extensions) {
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(IWorkbenchRegistryConstants.TAG_ADVISORPRODUCTBINDING)) {
					String advisorId = element.getAttribute(IWorkbenchRegistryConstants.ATT_ADVISORID);
					String productId = element.getAttribute(IWorkbenchRegistryConstants.ATT_PRODUCTID);

					if (advisorId == null || productId == null) {
						IStatus status = new Status(IStatus.ERROR,
								element.getDeclaringExtension().getContributor().getName(), IStatus.ERROR,
								"triggerPointAdvisorId and productId must be defined.", new IllegalArgumentException()); //$NON-NLS-1$
						WorkbenchPlugin.log("Invalid trigger point advisor binding", status); //$NON-NLS-1$
						continue;
					}

					if (targetProductId.equals(productId)) {
						return advisorId;
					}
				}
			}
		}
		return null;
	}

}
