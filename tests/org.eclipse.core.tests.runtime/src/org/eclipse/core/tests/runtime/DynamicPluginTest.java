/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;

public class DynamicPluginTest extends RuntimeTest {
	private static final String PLUGIN_TESTING_ROOT = "Plugin_Testing";

	public DynamicPluginTest(String name) {
		super(name);
	}

	/**
	 * Allows test cases to wait for event notification so they can make assertions on the event.  
	 */
	protected static class TestRegistryChangeListener implements IRegistryChangeListener {
		private IRegistryChangeEvent event;
		private String xpNamespace;
		private String xpId;
		private String extNamespace;
		private String extId;

		/**
		 * Creates a new listener. The parameters allow filtering of events based on extension point/extension's 
		 * namespaces/ids.
		 * 
		 * @param xpNamespace extension point namespace. If <code>null</code>, xpId must also be null
		 * @param xpId extension point simple id.  May be <code>null</code>
		 * @param extNamespace extension namespace. If <code>null</code>, extId must also be null
		 * @param extIdextension id. May be <code>null</code>
		 */
		public TestRegistryChangeListener(String xpNamespace, String xpId, String extNamespace, String extId) {
			this.xpNamespace = xpNamespace;
			this.xpId = xpId;
			this.extNamespace = extNamespace;
			this.extId = extId;
		}

		/**
		 * @see IRegistryChangeListener#registryChanged
		 */
		public synchronized void registryChanged(IRegistryChangeEvent newEvent) {
			if (this.event != null)
				return;
			if (xpNamespace != null) {
				if (extNamespace != null) {
					if (newEvent.getExtensionDelta(xpNamespace, xpId, extNamespace + '.' + extId) == null)
						return;
				} else if (newEvent.getExtensionDeltas(xpNamespace, xpId).length == 0)
					return;
			}
			this.event = newEvent;
			notify();
		}

		/**
		 * Returns the first event that is received, blocking for at most <code>timeout</code> milliseconds.
		 * Returns <code>null</code> if a event was not received for the time allowed.
		 * 
		 * @param timeout the maximum time to wait in milliseconds. If zero, this method will 
		 * block until an event is received 
		 * @return the first event received, or <code>null</code> if none was received
		 */
		public synchronized IRegistryChangeEvent getEvent(long timeout) {
			IRegistryChangeEvent result = event;
			if (event != null) {
				event = null;
				return result;
			}
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				// who cares?
			}
			result = event;
			event = null;
			return result;
		}
	}

	protected void registerListener(TestRegistryChangeListener listener, String namespace) {
		InternalPlatform.getDefault().getRegistry().addRegistryChangeListener(listener, namespace);
	}

	protected void unregisterListener(TestRegistryChangeListener listener) {
		InternalPlatform.getDefault().getRegistry().removeRegistryChangeListener(listener);
	}

	public Bundle installBundle(String location) throws BundleException, MalformedURLException, IOException {
		URL entry = InternalPlatform.getDefault().getBundle(PI_RUNTIME_TESTS).getEntry(PLUGIN_TESTING_ROOT + '/' + location);
		return InternalPlatform.getDefault().getBundleContext().installBundle(Platform.asLocalURL(entry).toExternalForm());
	}

	/**
	 * Do PackageAdmin.refreshPackages() in a synchronous way.  After installing
	 * all the requested bundles we need to do a refresh and want to ensure that 
	 * everything is done before returning.
	 * @param bundles
	 */
	public void refreshPackages(Bundle[] bundles) {
		if (bundles.length == 0)
			return;
		BundleContext context =  InternalPlatform.getDefault().getBundleContext();
		ServiceReference packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
		PackageAdmin packageAdmin = null;
		if (packageAdminRef != null) {
			packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
			if (packageAdmin == null)
				return;
		}
		// TODO this is such a hack it is silly.  There are still cases for race conditions etc
		// but this should allow for some progress...
		// (patch from John A.)
		final boolean[] flag = new boolean[] {false};
		FrameworkListener listener = new FrameworkListener() {
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
					synchronized (flag) {
						flag[0] = true;
						flag.notifyAll();
					}
			}
		};
		context.addFrameworkListener(listener);
		packageAdmin.refreshPackages(bundles);
		synchronized (flag) {
			while (!flag[0]) {
				try {
					flag.wait();
				} catch (InterruptedException e) {
					// who cares....
				}
			}
		}
		context.removeFrameworkListener(listener);
		context.ungetService(packageAdminRef);
	}

	public Bundle[] getBundles(String symbolicName, String version) {
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		ServiceReference packageAdminReference = context.getServiceReference(PackageAdmin.class.getName());
		if (packageAdminReference == null)
			throw new IllegalStateException("No package admin service found");
		PackageAdmin packageAdmin = (PackageAdmin) context.getService(packageAdminReference);
		Bundle[] result = packageAdmin.getBundles(symbolicName, version, null);
		context.ungetService(packageAdminReference);
		return result;
	}
}