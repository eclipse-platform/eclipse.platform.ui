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
	public DynamicPluginTest(String name) {
		super(name);
	}
	/**
	 * Allows test cases to wait for event notification so they can make assertions on the event.  
	 */
	public static class RegistryChangeListener implements IRegistryChangeListener {
		private IRegistryChangeEvent event;
		private String xpNamespace;
		private String xpId;
		private String extNamespace;
		private String extId;
		public RegistryChangeListener(String xpNamespace, String xpId, String extNamespace, String extId) {
			this.xpNamespace = xpNamespace;
			this.xpId = xpId;
			this.extNamespace = extNamespace;
			this.extId = extId;
		}
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
		public synchronized IRegistryChangeEvent waitFor(long timeout) {
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
	
	protected void installRegistryListener(RegistryChangeListener listener, String namespace) {
		InternalPlatform.getDefault().getRegistry().addRegistryChangeListener(listener, namespace);
	}	
	public void installBundle(String location) throws BundleException, MalformedURLException, IOException {
		URL entry = InternalPlatform.getDefault().getBundle(PI_RUNTIME_TESTS).getEntry("Plugin_Testing/" + location);
		Bundle installed = InternalPlatform.getDefault().getBundleContext().installBundle(Platform.asLocalURL(entry).toExternalForm());
		refreshPackages(InternalPlatform.getDefault().getBundleContext(), new Bundle[] {installed});
	}
	/**
	 * Do PackageAdmin.refreshPackages() in a synchronous way.  After installing
	 * all the requested bundles we need to do a refresh and want to ensure that 
	 * everything is done before returning.
	 * @param bundles
	 */
	private void refreshPackages(BundleContext context, Bundle[] bundles) {
		if (bundles.length == 0)
			return;
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
				}
			}
		}
		context.removeFrameworkListener(listener);
		context.ungetService(packageAdminRef);
	}
	
}