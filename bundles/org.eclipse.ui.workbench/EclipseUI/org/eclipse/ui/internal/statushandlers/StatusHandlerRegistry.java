/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.statushandlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The registry of status handlers extensions.
 *
 * @since 3.3
 */
public class StatusHandlerRegistry implements IExtensionChangeHandler {

	private static final String STATUSHANDLERS_POINT_NAME = "statusHandlers"; //$NON-NLS-1$

	private static final String TAG_STATUSHANDLER = "statusHandler"; //$NON-NLS-1$

	private static final String TAG_STATUSHANDLER_PRODUCTBINDING = "statusHandlerProductBinding"; //$NON-NLS-1$

	private static final String STATUSHANDLER_ARG = "-statushandler"; //$NON-NLS-1$

	private ArrayList<StatusHandlerDescriptor> statusHandlerDescriptors = new ArrayList<>();

	private ArrayList<StatusHandlerProductBindingDescriptor> productBindingDescriptors = new ArrayList<>();

	private StatusHandlerDescriptorsMap statusHandlerDescriptorsMap;

	private StatusHandlerDescriptor defaultHandlerDescriptor;

	private static StatusHandlerRegistry instance;

	/**
	 * Creates an instance of the class.
	 */
	private StatusHandlerRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		IExtensionPoint handlersPoint = Platform.getExtensionRegistry().getExtensionPoint(WorkbenchPlugin.PI_WORKBENCH,
				STATUSHANDLERS_POINT_NAME);
		IExtension[] extensions = handlersPoint.getExtensions();

		statusHandlerDescriptorsMap = new StatusHandlerDescriptorsMap();

		// initial population
		for (IExtension extension : extensions) {
			addExtension(tracker, extension);
		}

		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(handlersPoint));

		// registers on products ext. point to, needed
		// for changing the default handler if product is changed
		IExtensionPoint productsPoint = Platform.getExtensionRegistry().getExtensionPoint(Platform.PI_RUNTIME,
				Platform.PT_PRODUCT);

		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(productsPoint));
	}

	/**
	 * Returns StatusHandlerRegistry singleton instance.
	 *
	 * @return StatusHandlerRegistry instance
	 */
	public static StatusHandlerRegistry getDefault() {
		if (instance == null) {
			instance = new StatusHandlerRegistry();
		}
		return instance;
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] configElements = extension.getConfigurationElements();
		for (IConfigurationElement configElement : configElements) {
			if (configElement.getName().equals(TAG_STATUSHANDLER)) {
				StatusHandlerDescriptor descriptor = new StatusHandlerDescriptor(configElement);
				tracker.registerObject(extension, descriptor, IExtensionTracker.REF_STRONG);
				statusHandlerDescriptors.add(descriptor);
			} else if (configElement.getName().equals(TAG_STATUSHANDLER_PRODUCTBINDING)) {
				StatusHandlerProductBindingDescriptor descriptor = new StatusHandlerProductBindingDescriptor(
						configElement);
				tracker.registerObject(extension, descriptor, IExtensionTracker.REF_STRONG);
				productBindingDescriptors.add(descriptor);
			}
		}
		buildHandlersStructure();
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof StatusHandlerDescriptor) {
				statusHandlerDescriptors.remove(object);
			} else if (object instanceof StatusHandlerProductBindingDescriptor) {
				productBindingDescriptors.remove(object);
			}
		}
		buildHandlersStructure();
	}

	/**
	 * Returns the default product handler descriptor, or null if the product is not
	 * defined or there is no product binding
	 *
	 * @return the default handler
	 */
	public StatusHandlerDescriptor getDefaultHandlerDescriptor() {
		return defaultHandlerDescriptor;
	}

	/**
	 * Returns a list of handler descriptors which should be used for statuses with
	 * given plugin id.
	 *
	 * @return list of handler descriptors
	 */
	public List getHandlerDescriptors(String pluginId) {
		return statusHandlerDescriptorsMap.getHandlerDescriptors(pluginId);
	}

	/**
	 * Returns status handler descriptor for given id.
	 *
	 * @param statusHandlerId the id to get for
	 * @return the status handler descriptor
	 */
	public StatusHandlerDescriptor getHandlerDescriptor(String statusHandlerId) {
		StatusHandlerDescriptor descriptor = null;
		for (Iterator<StatusHandlerDescriptor> it = statusHandlerDescriptors.iterator(); it.hasNext();) {
			descriptor = it.next();
			if (descriptor.getId().equals(statusHandlerId)) {
				return descriptor;
			}
		}

		if (defaultHandlerDescriptor != null && defaultHandlerDescriptor.getId().equals(statusHandlerId)) {
			return defaultHandlerDescriptor;
		}

		return null;
	}

	/**
	 * Disposes the registry.
	 */
	public void dispose() {
		PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
	}

	/**
	 * It is possible since Eclipse 3.5 to configure custom status handling using
	 * the -statushandler parameter.
	 *
	 * @return the id of the statushandler
	 * @since 3.5
	 */
	private String resolveUserStatusHandlerId() {
		String[] parameters = Platform.getCommandLineArgs();

		for (int i = 0; i < parameters.length - 1; i++) {
			if (STATUSHANDLER_ARG.equals(parameters[i])) {
				return parameters[i + 1];
			}
		}
		return null;
	}

	/**
	 * Sets the default product handler descriptor if product exists and binding is
	 * defined and creates handler descriptors tree due to the prefix policy.
	 */
	private void buildHandlersStructure() {
		statusHandlerDescriptorsMap.clear();
		defaultHandlerDescriptor = null;

		String productId = Platform.getProduct() != null ? Platform.getProduct().getId() : null;

		List<StatusHandlerDescriptor> allHandlers = new ArrayList<>();

		String defaultHandlerId = resolveUserStatusHandlerId();

		if (defaultHandlerId == null) {
			// we look for product related statushandler if it was not passed as
			// an argument to Eclipse
			for (Iterator<StatusHandlerProductBindingDescriptor> it = productBindingDescriptors.iterator(); it
					.hasNext();) {
				StatusHandlerProductBindingDescriptor descriptor = (it.next());

				if (descriptor.getProductId().equals(productId)) {
					defaultHandlerId = descriptor.getHandlerId();
				}
			}
		}

		for (Iterator<StatusHandlerDescriptor> it = statusHandlerDescriptors.iterator(); it.hasNext();) {
			StatusHandlerDescriptor descriptor = (it.next());

			allHandlers.add(descriptor);
		}

		StatusHandlerDescriptor handlerDescriptor = null;

		for (Iterator<StatusHandlerDescriptor> it = allHandlers.iterator(); it.hasNext();) {
			handlerDescriptor = it.next();

			if (handlerDescriptor.getId().equals(defaultHandlerId)) {
				defaultHandlerDescriptor = handlerDescriptor;
			} else {
				statusHandlerDescriptorsMap.addHandlerDescriptor(handlerDescriptor);
			}
		}
	}
}
