package org.eclipse.core.tests.internal.registry;

import java.io.IOException;
import junit.framework.TestCase;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class StaleObjects extends TestCase {
	private class HandleCatcher implements IRegistryChangeListener {
		private IExtension extensionFromTheListener;

		public HandleCatcher() {
			Platform.getExtensionRegistry().addRegistryChangeListener(this);
		}

		public void registryChanged(IRegistryChangeEvent event) {
			boolean gotException = false;
			try {
				extensionFromTheListener = event.getExtensionDeltas()[0].getExtension();
				extensionFromTheListener.getSimpleIdentifier();
			} catch (InvalidRegistryObjectException e) {
				gotException = true;
			}
			assertEquals(false, gotException);
		}

		public IExtension getAcquiredHandle() {
			return extensionFromTheListener;
		}
	}

	public synchronized void testA() throws IOException, BundleException {
		HandleCatcher listener = new HandleCatcher();
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testStale1");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});

		IExtension willBeStale = Platform.getExtensionRegistry().getExtension("testStale.ext1");

		//Test that handles obtained from deltas are working.

		//Test that handles obtained from an addition deltas are working even after the delta is done being broadcasted.
		boolean gotException = false;
		try {
			IExtension result = null;
			while ((result = listener.getAcquiredHandle()) == null) {
				try {
					wait(200);
				} catch (InterruptedException e) {
					//ignore.
				}
			}
			result.getSimpleIdentifier();
		} catch (InvalidRegistryObjectException e) {
			gotException = true;
		}
		assertEquals(false, gotException);

		//Add a listener capturing a handle removal. Inside the handle catcher the handle is valid
		HandleCatcher listener2 = new HandleCatcher();
		try {
			wait(500); //Wait for the listeners to be done
		} catch (InterruptedException e) {
			//ignore.
		}

		bundle01.uninstall();
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});

		//Outside of the event notification the handle from a removed object should be invalid
		gotException = false;
		try { 
			while (listener2.getAcquiredHandle() == null) {
				try {
					wait(200);
				} catch (InterruptedException e) {
					//ignore.
				}
			}
			listener2.getAcquiredHandle().getSimpleIdentifier();
		} catch (InvalidRegistryObjectException e) {
			gotException = true;
		}
		assertEquals(true, gotException);

		//Check that the initial handles are stale as well
		gotException = false;
		try {
			willBeStale.getSimpleIdentifier();
		} catch (InvalidRegistryObjectException e) {
			gotException = true;
		}
		assertEquals(true, gotException);
	}

	public void testStaleConfigurationElement() throws IOException, BundleException {
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testStale2");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});

		IConfigurationElement ce = Platform.getExtensionRegistry().getExtension("testStale2.ext1").getConfigurationElements()[0];
		assertNotNull(ce);

		bundle01.uninstall();
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});

		boolean gotException = false;
		try {
			ce.createExecutableExtension("name");
		} catch (CoreException c) {
			gotException = true;
		}
		assertEquals(true, gotException);
	}
}
