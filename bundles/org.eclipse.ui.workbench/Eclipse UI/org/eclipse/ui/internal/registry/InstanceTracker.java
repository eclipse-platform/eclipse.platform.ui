/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * This will be defined in core, I've put it into ui for now to keep it all together.
 * EXPERIMENTAL
 * @since 3.1
 */
public class InstanceTracker {
	private IConfigurationElement configElt; //A reference to the configuration element
	private Object instance; //The instance created by calling createExecutableExtension
	private Runnable onDispose; //The code being run when the configuration element is being disposed.

	//Need to deal with the synchronization issues
	final BundleListener listener = new BundleListener() {
		public void bundleChanged(BundleEvent event) {
			if (configElt == null)
				return;
			
			if (event.getType() != BundleEvent.UNINSTALLED && event.getType() != BundleEvent.UNRESOLVED && event.getType() != BundleEvent.UPDATED)
					return;
			
			if(! configElt.getDeclaringExtension().getNamespace().equals(event.getBundle().getSymbolicName())) 
				return;

			try {
				onDispose.run();
			} catch (Exception e) {
				System.out.println(e); //TODO log
			} finally {
			    close();
			}
		}

	};

	public InstanceTracker(IConfigurationElement config, Runnable onDispose) {
		configElt = config;
		this.onDispose = onDispose;
	}

	public Object getExecutableExtension(String property) throws CoreException {
		if (instance != null)
			return instance;

		if (configElt == null)
			return null;

		instance = configElt.createExecutableExtension(property);

		if (instance != null && onDispose != null)
			track();

		return instance;
	}

	private void track() {
		InternalPlatform.getDefault().getBundleContext().addBundleListener(listener);
	}

	public void close() {
		InternalPlatform.getDefault().getBundleContext().removeBundleListener(listener);
		instance = null;
		configElt = null;
		onDispose = null;
	}

	public IConfigurationElement getConfigurationElement() {
		return configElt;
	}

	/**
	 * 
	 */
	public void releaseExecutableExtension() {
		instance = null;
	}
}
