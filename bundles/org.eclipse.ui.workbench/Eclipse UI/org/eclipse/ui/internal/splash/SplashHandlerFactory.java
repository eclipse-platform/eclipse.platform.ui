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

package org.eclipse.ui.internal.splash;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.splash.AbstractSplashHandler;

/**
 * Simple non-caching access to the splashHandler extension point.
 * 
 * @since 3.3
 */
public final class SplashHandlerFactory {

	/**
	 * Find the splash handler for the given product or <code>null</code> if
	 * it cannot be found.
	 * 
	 * @param product
	 *            the product
	 * @return the splash or <code>null</code>
	 */
	public static AbstractSplashHandler findSplashHandlerFor(IProduct product) {
		if (product == null)
			return null;

		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_SPLASH_HANDLERS);

		if (point == null)
			return null;

		IExtension[] extensions = point.getExtensions();
		Map idToSplash = new HashMap(); // String->ConfigurationElement
		String[] targetId = new String[1];
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] children = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < children.length; j++) {
				AbstractSplashHandler handler = processElement(children[j],
						idToSplash, targetId, product);
				if (handler != null)
					return handler;

			}
		}
		return null;
	}

	/**
	 * Process a given element.
	 * 
	 * @param configurationElement
	 *            the element to process
	 * @param idToSplash
	 *            the map of current splash elements
	 * @param targetId
	 *            the target id if known
	 * @param product
	 *            the product to search for
	 * @return a splash matching the target id from this element or
	 *         <code>null</code>
	 */
	private static AbstractSplashHandler processElement(
			IConfigurationElement configurationElement, Map idToSplash,
			String[] targetId, IProduct product) {
		String type = configurationElement.getName();
		if (IWorkbenchRegistryConstants.TAG_SPLASH_HANDLER.equals(type)) {
			String id = configurationElement
					.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
			if (id == null)
				return null;

			// we know the target and this element is it
			if (targetId[0] != null && id.equals(targetId[0])) {
				return create(configurationElement);
			}
			// store for later examination
			idToSplash.put(id, configurationElement);

		} else if (IWorkbenchRegistryConstants.TAG_SPLASH_HANDLER_PRODUCT_BINDING
				.equals(type)) {
			String productId = configurationElement
					.getAttribute(IWorkbenchRegistryConstants.ATT_PRODUCTID);
			if (product.getId().equals(productId) && targetId[0] == null) { // we
				// found the target ID
				targetId[0] = configurationElement
						.getAttribute(IWorkbenchRegistryConstants.ATT_SPLASH_ID);
				// check all currently located splashes
				IConfigurationElement splashElement = (IConfigurationElement) idToSplash
						.get(targetId[0]);
				if (splashElement != null)
					return create(splashElement);
			}
		}

		return null;
	}

	/**
	 * Create the splash implementation.
	 * 
	 * @param splashElement
	 *            the element to create from
	 * @return the element or <code>null</code> if it couldn't be created
	 */
	private static AbstractSplashHandler create(
			final IConfigurationElement splashElement) {
		final AbstractSplashHandler[] handler = new AbstractSplashHandler[1];
		SafeRunner.run(new SafeRunnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() throws Exception {
				handler[0] = (AbstractSplashHandler) WorkbenchPlugin
						.createExtension(splashElement,
								IWorkbenchRegistryConstants.ATT_CLASS);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.util.SafeRunnable#handleException(java.lang.Throwable)
			 */
			public void handleException(Throwable e) {
				WorkbenchPlugin
						.log("Problem creating splash implementation", e); //$NON-NLS-1$
			}
		});

		return handler[0];
	}
}
