/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.core.tests.internal.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.harness.CoreTest;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.*;

public class FileLocatorTest extends CoreTest {

	private final static String searchLocation = "$nl$/intro/messages.properties";

	private final static String nl = "aa_BB"; // make sure we have a stable NL value

	private final static String mostSpecificPath = "/nl/aa/BB/intro/messages.properties";
	private final static String lessSpecificPath = "/nl/aa/intro/messages.properties";
	private final static String nonSpecificPath = "/intro/messages.properties";

	public FileLocatorTest(String name) {
		super(name);
	}

	public void testFileLocatorFind() throws IOException, BundleException {
		Bundle bundle = BundleTestingHelper.installBundle("Plugin", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "fileLocator/testFileLocator");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle});
		Bundle fragment = BundleTestingHelper.installBundle("Fragment", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "fileLocator/testFileLocator.nl");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {fragment});

		IPath path = new Path(searchLocation);
		Map<String, String> map = new HashMap<>(1);
		map.put("$nl$", nl);

		URL oneSolution = FileLocator.find(bundle, path, map);
		assertNotNull(oneSolution);
		assertTrue(oneSolution.getPath().equals(mostSpecificPath));
		assertBundleURL(oneSolution);

		URL[] solutions = FileLocator.findEntries(bundle, path, map);

		// expected:
		// Bundle/nl/aa/BB/intro/messages.properties,
		// Fragment/nl/aa/BB/intro/messages.properties,
		// Bundle/nl/aa/intro/messages.properties,
		// Fragment/nl/aa/intro/messages.properties,
		// Bundle/121/intro/messages.properties

		assertTrue(solutions.length == 5);

		assertTrue(solutions[0].getPath().equals(mostSpecificPath));
		assertBundleURL(solutions[0]);
		assertTrue(solutions[1].getPath().equals(mostSpecificPath));
		assertFragmentURL(solutions[1]);

		assertTrue(solutions[2].getPath().equals(lessSpecificPath));
		assertBundleURL(solutions[2]);
		assertTrue(solutions[3].getPath().equals(lessSpecificPath));
		assertFragmentURL(solutions[3]);

		assertTrue(solutions[4].getPath().equals(nonSpecificPath));
		assertBundleURL(solutions[4]);

		// remove the first bundle
		fragment.uninstall();
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {fragment});
		bundle.uninstall();
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle});
	}

	public void testFileLocatorGetBundleFile01() throws BundleException, IOException {
		// test for bug 198447
		// install the bundle via reference
		BundleContext context = RuntimeTestsPlugin.getContext();
		URL url = context.getBundle().getEntry(RuntimeTestsPlugin.TEST_FILES_ROOT + "fileLocator/testFileLocatorGetRootFile");
		Bundle bundle = context.installBundle("reference:" + FileLocator.toFileURL(url).toExternalForm());
		BundleTestingHelper.refreshPackages(context, new Bundle[] {bundle});

		File file1 = FileLocator.getBundleFile(bundle);
		assertNotNull(file1);

		URL fileURL = FileLocator.toFileURL(context.getBundle().getEntry(RuntimeTestsPlugin.TEST_FILES_ROOT + "fileLocator/testFileLocatorGetRootFile"));
		assertTrue(new File(fileURL.getFile()).equals(file1));

		// remove the bundle
		bundle.uninstall();
		BundleTestingHelper.refreshPackages(context, new Bundle[] {bundle});
	}

	public void testFileLocatorGetBundleFile02() throws BundleException, IOException {
		// install the bundle via reference
		BundleContext context = RuntimeTestsPlugin.getContext();
		URL url = context.getBundle().getEntry(RuntimeTestsPlugin.TEST_FILES_ROOT + "fileLocator/testFileLocatorGetRootFile.jar");
		Bundle bundle = context.installBundle("reference:" + FileLocator.toFileURL(url).toExternalForm());
		BundleTestingHelper.refreshPackages(context, new Bundle[] {bundle});

		File file1 = FileLocator.getBundleFile(bundle);
		assertNotNull(file1);

		URL fileURL = FileLocator.toFileURL(context.getBundle().getEntry(RuntimeTestsPlugin.TEST_FILES_ROOT + "fileLocator/testFileLocatorGetRootFile.jar"));
		assertTrue(new File(fileURL.getFile()).equals(file1));

		URL manifest = bundle.getEntry("META-INF/MANIFEST.MF");
		manifest = FileLocator.resolve(manifest);
		assertEquals("Expection jar protocol: " + manifest.toExternalForm(), "jar", manifest.getProtocol());

		String manifestExternal = manifest.toExternalForm();
		int index = manifestExternal.lastIndexOf('!');
		assertTrue("No ! found", index >= 0);
		String fileExternal = manifestExternal.substring(4, index);
		try {
			URL fileExternalURL = new URL(fileExternal);
			new File(fileExternalURL.toURI());
		} catch (Exception e) {
			fail("Unexpected exception.", e);
		}

		// remove the bundle
		bundle.uninstall();
		BundleTestingHelper.refreshPackages(context, new Bundle[] {bundle});
	}

	private Bundle getHostBundle(URL url) {
		String host = url.getHost();
		int dot = host.indexOf('.');
		Long hostId = Long.decode(dot < 0 ? host : host.substring(0, dot));
		assertNotNull(hostId);
		return RuntimeTestsPlugin.getContext().getBundle(hostId.longValue());
	}

	private void assertBundleURL(URL url) {
		Bundle hostBundle = getHostBundle(url);
		assertNotNull(hostBundle);
		assertTrue(hostBundle.getSymbolicName().equals("fileLocatorTest"));
	}

	private void assertFragmentURL(URL url) {
		Bundle hostBundle = getHostBundle(url);
		assertNotNull(hostBundle);
		assertTrue(hostBundle.getSymbolicName().equals("fileLocatorTest.nl"));
	}

	public static Test suite() {
		TestSuite sameSession = new TestSuite(FileLocatorTest.class.getName());
		sameSession.addTest(new FileLocatorTest("testFileLocatorFind"));
		sameSession.addTest(new FileLocatorTest("testFileLocatorGetBundleFile01"));
		sameSession.addTest(new FileLocatorTest("testFileLocatorGetBundleFile02"));
		return sameSession;
	}
}
