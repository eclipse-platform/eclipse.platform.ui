package org.eclipse.e4.core.tests.services;

import org.osgi.framework.BundleContext;

import org.osgi.framework.BundleActivator;

public class TestActivator implements BundleActivator {

	public static BundleContext bundleContext;
	public TestActivator() {
	}

	public void start(BundleContext aContext) throws Exception {
		bundleContext = aContext;
	}
	public void stop(BundleContext aContext) throws Exception {
		bundleContext = null;
	}

}
