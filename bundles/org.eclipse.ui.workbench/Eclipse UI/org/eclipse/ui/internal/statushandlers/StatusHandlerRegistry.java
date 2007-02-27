/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 */
public class StatusHandlerRegistry implements IExtensionChangeHandler {

	private static final String STATUSHANDLERS_POINT_NAME = "statusHandlers"; //$NON-NLS-1$

	private static final String TAG_STATUSHANDLER = "statusHandler"; //$NON-NLS-1$

	private static final String TAG_STATUSHANDLER_PRODUCTBINDING = "statusHandlerProductBinding"; //$NON-NLS-1$

	private ArrayList statusHandlerDescriptors = new ArrayList();

	private ArrayList productBindingDescriptors = new ArrayList();

	private StatusHandlerDescriptorsMap statusHandlerDescriptorsMap;

	private StatusHandlerDescriptor defaultHandlerDescriptor;

	private static StatusHandlerRegistry instance;

	/**
	 * Creates an instance of the class.
	 */
	private StatusHandlerRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench()
				.getExtensionTracker();
		IExtensionPoint handlersPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(WorkbenchPlugin.PI_WORKBENCH,
						STATUSHANDLERS_POINT_NAME);
		IExtension[] extensions = handlersPoint.getExtensions();

		statusHandlerDescriptorsMap = new StatusHandlerDescriptorsMap();

		// initial population
		for (int i = 0; i < extensions.length; i++) {
			addExtension(tracker, extensions[i]);
		}

		tracker.registerHandler(this, ExtensionTracker
				.createExtensionPointFilter(handlersPoint));

		// registers on products ext. point to, needed
		// for changing the default handler if product is changed
		IExtensionPoint productsPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(Platform.PI_RUNTIME, Platform.PT_PRODUCT);

		tracker.registerHandler(this, ExtensionTracker
				.createExtensionPointFilter(productsPoint));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] configElements = extension
				.getConfigurationElements();
		for (int j = 0; j < configElements.length; j++) {
			if (configElements[j].getName().equals(TAG_STATUSHANDLER)) {
				StatusHandlerDescriptor descriptor = new StatusHandlerDescriptor(
						configElements[j]);
				tracker.registerObject(extension, descriptor,
						IExtensionTracker.REF_STRONG);
				statusHandlerDescriptors.add(descriptor);
			} else if (configElements[j].getName().equals(
					TAG_STATUSHANDLER_PRODUCTBINDING)) {
				StatusHandlerProductBindingDescriptor descriptor = new StatusHandlerProductBindingDescriptor(
						configElements[j]);
				tracker.registerObject(extension, descriptor,
						IExtensionTracker.REF_STRONG);
				productBindingDescriptors.add(descriptor);
			}
		}
		buildHandlersStructure();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof StatusHandlerDescriptor) {
				statusHandlerDescriptors.remove(objects[i]);
			} else if (objects[i] instanceof StatusHandlerProductBindingDescriptor) {
				productBindingDescriptors.remove(objects[i]);
			}
		}
		buildHandlersStructure();
	}

	/**
	 * Returns the default product handler descriptor, or null if the product is
	 * not defined or there is no product binding
	 * 
	 * @return the default handler
	 */
	public StatusHandlerDescriptor getDefaultHandlerDescriptor() {
		return defaultHandlerDescriptor;
	}

	/**
	 * Returns a list of handler descriptors which should be used for statuses
	 * with given plugin id.
	 * 
	 * @param pluginId
	 * @return list of handler descriptors
	 */
	public List getHandlerDescriptors(String pluginId) {
		return statusHandlerDescriptorsMap.getHandlerDescriptors(pluginId);
	}

	/**
	 * Returns status handler descriptor for given id.
	 * 
	 * @param statusHandlerId
	 *            the id to get for
	 * @return the status handler descriptor
	 */
	public StatusHandlerDescriptor getHandlerDescriptor(String statusHandlerId) {
		StatusHandlerDescriptor descriptor = null;
		for (Iterator it = statusHandlerDescriptors.iterator(); it.hasNext();) {
			descriptor = (StatusHandlerDescriptor) it.next();
			if (descriptor.getId().equals(statusHandlerId)) {
				return descriptor;
			}
		}

		if (defaultHandlerDescriptor != null
				&& defaultHandlerDescriptor.getId().equals(statusHandlerId)) {
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
	 * Sets the default product handler descriptor if product exists and binding
	 * is defined and creates handler descriptors tree due to the prefix policy.
	 */
	private void buildHandlersStructure() {
		statusHandlerDescriptorsMap.clear();
		defaultHandlerDescriptor = null;

		String productId = Platform.getProduct() != null ? Platform
				.getProduct().getId() : null;

		List allHandlers = new ArrayList();

		String defaultHandlerId = null;

		for (Iterator it = productBindingDescriptors.iterator(); it.hasNext();) {
			StatusHandlerProductBindingDescriptor descriptor = ((StatusHandlerProductBindingDescriptor) it
					.next());

			if (descriptor.getProductId().equals(productId)) {
				defaultHandlerId = descriptor.getHandlerId();
			}
		}

		for (Iterator it = statusHandlerDescriptors.iterator(); it.hasNext();) {
			StatusHandlerDescriptor descriptor = ((StatusHandlerDescriptor) it
					.next());

			allHandlers.add(descriptor);
		}

		StatusHandlerDescriptor handlerDescriptor = null;

		for (Iterator it = allHandlers.iterator(); it.hasNext();) {
			handlerDescriptor = (StatusHandlerDescriptor) it.next();

			if (handlerDescriptor.getId().equals(defaultHandlerId)) {
				defaultHandlerDescriptor = handlerDescriptor;
			} else {
				statusHandlerDescriptorsMap
						.addHandlerDescriptor(handlerDescriptor);
			}
		}
	}
}
