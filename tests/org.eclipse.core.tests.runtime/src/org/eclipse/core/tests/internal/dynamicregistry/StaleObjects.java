package org.eclipse.core.tests.internal.dynamicregistry;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.internal.registry.InvalidHandleException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.eclipse.core.tests.runtime.TestRegistryChangeListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class StaleObjects extends TestCase {
	public void testA() throws IOException, BundleException {
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testStale1");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
		IExtensionPoint xpt = Platform.getExtensionRegistry().getExtensionPoint("testStale.xptB2");
		IExtension willBeStale = Platform.getExtensionRegistry().getExtension("testStale.ext1");
		TestRegistryChangeListener listener = new TestRegistryChangeListener("testStale", "xptB2", null, null);
		listener.register();
		
		assertNotNull(willBeStale);
		bundle01.uninstall();
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01} );
		IRegistryChangeEvent event = listener.getEvent(5000);
		
		IExtensionDelta[] deltas = event.getExtensionDeltas();
		System.out.println(deltas[0].getExtension().getSimpleIdentifier());
		
		boolean gotException = false;
		try {
			willBeStale.getSimpleIdentifier();
		} catch(InvalidHandleException e) {
			gotException = true;
		}
		assertEquals(gotException, true);
	}
}
