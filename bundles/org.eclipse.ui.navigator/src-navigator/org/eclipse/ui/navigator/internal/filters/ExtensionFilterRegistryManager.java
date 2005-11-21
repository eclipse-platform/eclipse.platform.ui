/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jan 14, 2004
 * 
 * To change the template for this generated file go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
package org.eclipse.ui.navigator.internal.filters;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class ExtensionFilterRegistryManager {

	private static final ExtensionFilterRegistryManager INSTANCE = new ExtensionFilterRegistryManager();

	private final Map extensionFilterRegistries = new HashMap();


	private ExtensionFilterRegistryManager() {
	}

	public static ExtensionFilterRegistryManager getInstance() {
		return INSTANCE;
	}

	public ExtensionFilterViewerRegistry getViewerRegistry(String viewerId) {

		/* The simple case is that the registry has been loaded and is ready to go */
		ExtensionFilterViewerRegistry registry = (ExtensionFilterViewerRegistry) getExtensionFilterRegistries().get(viewerId);
		if (registry != null)
			return registry;
		/* Otherwise the registry hasn't been loaded yet, so acquire a lock */
		synchronized (getExtensionFilterRegistries()) {
			/*
			 * Ensure that another thread wasn't in the process of initializing the registry for the
			 * given viewerid
			 */
			registry = (ExtensionFilterViewerRegistry) getExtensionFilterRegistries().get(viewerId);
			/* If the registry is still null, then we create, read, and add to the registry map */
			if (registry == null) {
				try {
					registry = new ExtensionFilterViewerRegistry(viewerId);
					getExtensionFilterRegistries().put(viewerId, registry);
				} catch (RuntimeException e) {
					e.printStackTrace();
				} catch (NoClassDefFoundError ncdfe) {
					ncdfe.printStackTrace();
				}
			}

		}
		return registry;
	}

	public void disposeViewerRegistry(String viewerId) {
		synchronized (getExtensionFilterRegistries()) {
			getExtensionFilterRegistries().remove(viewerId);
		}
	}

	/**
	 * @return Returns the extensionFilterRegistries.
	 */
	protected Map getExtensionFilterRegistries() {
		return extensionFilterRegistries;
	}



}
