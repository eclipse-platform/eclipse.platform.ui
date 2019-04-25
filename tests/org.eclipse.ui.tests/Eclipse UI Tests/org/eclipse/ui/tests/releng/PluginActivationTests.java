/*******************************************************************************
 * Copyright (c) 2017 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.releng;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Tests to ensure the correct bundles are activated. There are two reasons why
 * a bundle is activated:
 *
 * - a plugin contributes OSGi services - a plugin contains an activator
 *
 * Additional plug-ins with activators should be avoided as they can slow down
 * the startup of the Eclipse IDE Additional plug-ins provided OSGi services is
 * desired as the OSGi framework initializes these services asynchronously
 *
 * @since 3.1
 */

public class PluginActivationTests {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	private static String[] NOT_ACTIVE_BUNDLES = new String[] {
			"org.apache.xerces",
			"com.jcraft.jsch",
			"javax.servlet",
			"javax.servlet.jsp",
			"org.apache.ant",
			"org.apache.commons.logging",
			"org.apache.lucene",
			"org.eclipse.ant.core",
			"org.eclipse.ant.ui",
			"org.eclipse.core.commands",
			"org.eclipse.core.filesystem.win32.x86",
			"org.eclipse.core.resources.compatibility",
			"org.eclipse.core.resources.win32",
			"org.eclipse.core.runtime.compatibility.registry",
			"org.eclipse.equinox.http.jetty",
			"org.eclipse.equinox.http.registry",
			"org.eclipse.equinox.http.servlet",
			"org.eclipse.equinox.jsp.jasper",
			"org.eclipse.equinox.jsp.jasper.registry",
			"org.eclipse.help.base",
			"org.eclipse.help.ui",
			"org.eclipse.help.webapp",
			"org.eclipse.jface.databinding",
			"org.eclipse.jface.text",
			"org.eclipse.osgi.services",
			"org.eclipse.platform.doc.isv",
			"org.eclipse.platform.doc.user",
			"org.eclipse.sdk",
			"org.eclipse.sdk.tests",
			"org.eclipse.search",
			"org.eclipse.swt",
			"org.eclipse.swt.win32.win32.x86",
			"org.eclipse.test.performance",
			"org.eclipse.test.performance.ui",
			"org.eclipse.test.performance.win32",
			"org.eclipse.text",
			"org.eclipse.text.tests",
			"org.eclipse.ui.cheatsheets",
			"org.eclipse.ui.console",
			"org.eclipse.ui.editors.tests",
			"org.eclipse.ui.views.properties.tabbed",
			"org.eclipse.ui.win32",
			"org.eclipse.ui.workbench.compatibility",
			"org.eclipse.update.ui",
			"org.junit",
			"org.eclipse.core.databinding.beans",
			"org.eclipse.equinox.launcher",
			"org.eclipse.equinox.launcher.win32.win32.x86",
			"org.eclipse.help.appserver",
			"org.eclipse.jdt.apt.pluggable.core",
			"org.eclipse.jsch.ui",
			"org.eclipse.osgi.util",
			"org.eclipse.platform",
			"org.eclipse.rcp",
			"org.eclipse.ui.browser"
		};

	private static String[] ACTIVE_BUNDLES = new String[] {
			"org.eclipse.osgi",
			"org.eclipse.equinox.simpleconfigurator",
			"com.ibm.icu",
			"org.apache.felix.gogo.command",
			"org.apache.felix.gogo.runtime",
			"org.apache.felix.gogo.shell",
			"org.apache.felix.scr",
			"org.eclipse.compare",
			"org.eclipse.compare.core",
			"org.eclipse.core.contenttype",
			"org.eclipse.core.expressions",
			"org.eclipse.core.filebuffers",
			"org.eclipse.core.filesystem",
			"org.eclipse.core.jobs",
			"org.eclipse.core.net",
			"org.eclipse.core.resources",
			"org.eclipse.core.runtime",
			"org.eclipse.debug.core",
			"org.eclipse.e4.core.contexts",
			"org.eclipse.e4.core.di",
			"org.eclipse.e4.core.di.extensions",
			"org.eclipse.e4.core.di.extensions.supplier",
			"org.eclipse.e4.core.services",
			"org.eclipse.e4.demo.contacts",
			"org.eclipse.e4.ui.bindings",
			"org.eclipse.e4.ui.css.swt",
			"org.eclipse.e4.ui.css.swt.theme",
			"org.eclipse.e4.ui.di",
			"org.eclipse.e4.ui.model.workbench",
			"org.eclipse.e4.ui.progress",
			"org.eclipse.e4.ui.services",
			"org.eclipse.e4.ui.workbench",
			"org.eclipse.e4.ui.workbench.swt",
			"org.eclipse.emf.common",
			"org.eclipse.emf.ecore",
			"org.eclipse.emf.ecore.xmi",
			"org.eclipse.equinox.app",
			"org.eclipse.equinox.common",
			"org.eclipse.ui.cheatsheets",
			"org.eclipse.equinox.console",
			"org.eclipse.equinox.ds",
			"org.eclipse.equinox.event",
			"org.eclipse.equinox.preferences",
			"org.eclipse.equinox.registry",
			"org.eclipse.equinox.security",
			"org.eclipse.help",
			"org.eclipse.jgit",
			"org.eclipse.jsch.core",
			"org.eclipse.ui",
			"org.eclipse.ui.editors",
			"org.eclipse.ui.examples.contributions",
			"org.eclipse.ui.ide",
			"org.eclipse.ui.ide.application",
			"org.eclipse.ui.intro",
			"org.eclipse.ui.intro.universal",
			"org.eclipse.ui.monitoring",
			"org.eclipse.ui.navigator",
			"org.eclipse.ui.navigator.resources",
			"org.eclipse.ui.net",
			"org.eclipse.ui.tests",
			"org.eclipse.ui.tests.harness",
			"org.eclipse.ui.themes",
			"org.eclipse.ui.views.log",
			"org.eclipse.ui.workbench",
			"org.eclipse.ui.workbench.texteditor",
			"org.hamcrest.core"
		};

	@Before
	public void setUpTest() {
		/*
		 * Since https://bugs.eclipse.org/484795 in EGit 4.2,
		 * org.eclipse.egit.ui/plugin.xml contributes: <extension
		 * point="org.eclipse.ui.services"> <sourceProvider
		 * provider="org.eclipse.egit.ui.internal.selection.RepositorySourceProvider">
		 * ... This activates the EGit UI bundle very early. Because of that, EGit's
		 * org.eclipse.egit.ui.team.MergeTool command's handler class is loaded, which
		 * in turn activates the org.eclipse.compare bundle on startup.
		 *
		 * org.eclipse.pde.ui also contributes a sourceProvider, that's why we don't
		 * test for it...
		 *
		 * Workaround is to remove org.eclipse.compare if EGit is present:
		 */
		if (Platform.getBundle("org.eclipse.egit.ui") != null) {
			addLoadedPlugIns("org.eclipse.compare");
		}

		// enforce to show certain views to trigger plug-in activation
		// are used
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			window.getActivePage().showView("org.eclipse.ui.navigator.ProjectExplorer");
			window.getActivePage().showView("org.eclipse.ui.views.PropertySheet");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If a test suite uses this test and has other tests that cause plug-ins to be
	 * loaded then those need to be indicated here.
	 *
	 * @param loadedPlugins
	 *            plug-ins that are additionally loaded by the caller
	 * @since 3.5
	 */
	public static void addLoadedPlugIns(String... loadedPlugins) {
		Assert.isLegal(loadedPlugins != null);
		List<String> l = new ArrayList<>(Arrays.asList(NOT_ACTIVE_BUNDLES));
		l.removeAll(Arrays.asList(loadedPlugins));
		NOT_ACTIVE_BUNDLES = l.toArray(new String[0]);
	}

	/**
	 * For debugging purposes
	 *
	 * @param active
	 *            defines if method should print the active or the not not active
	 */
	public void printPluginStatus(boolean active) {
		Bundle bundle = FrameworkUtil.getBundle(PluginActivationTests.class);
		Bundle[] bundles = bundle.getBundleContext().getBundles();
		System.out.println("Started printPluginStatus\n. Active status: " + active);
		for (Bundle b : bundles) {
			if (!active) {
				if (b.getState() != Bundle.ACTIVE) {
					System.out.println(b.getSymbolicName());
				}
			} else {
				if (b.getState() == Bundle.ACTIVE) {
					System.out.println(b.getSymbolicName());
				}

			}

		}
		System.out.println("Finished printPluginStatus\n");
	}

	/**
	 * Only plug-ins which provides OSGi services should be active.
	 *
	 * Also plug-ins with an activator must be activated but additional activators
	 * should be avoided as the slow down the startup of Eclipse
	 *
	 */

	@Test
	@Ignore("See Bug 516743")
	public void pluginsWithoutOSGiServiceOrActivatorShouldNotActive() {
		StringBuilder buf = new StringBuilder();
		for (String element : NOT_ACTIVE_BUNDLES) {
			Bundle bundle = Platform.getBundle(element);
			if (bundle == null) {
				// // log bundles that cannot be found:
				// buf.append("- not found: ");
				// buf.append(NOT_ACTIVE_BUNDLES[i]);
				// buf.append('\n');
			} else if (bundle.getState() == Bundle.ACTIVE) {
				buf.append("- ");
				buf.append(element);
				buf.append('\n');
			}
		}
		// add some debug output
		if (buf.length() > 0) {
			printPluginStatus(true);
		}
		assertTrue("Unexpected bundles in status active:\n" + buf, buf.length() == 0);
	}

	/**
	 * As the number of active plug-ins with an activator slow down the start of
	 * Eclipse, we should avoid increasing them.
	 *
	 * If a bundle provides OSGi services (without an activator), this is a desired
	 * situation. If the test is failing due to such a situation, add the
	 * corresponding bundle to this list.
	 *
	 * Equinox also (seems to activates plug-ins which set the singleton:=true. Not
	 * sure if that is a bug or desired
	 *
	 * Also plug-ins with an activator must be activated but additional activators
	 * should be avoided as the slow down the startup of Eclipse
	 *
	 */

	@Test
	@Ignore("See Bug 516743")
	public void activePluginsShouldNotIncrease() {
		printPluginStatus(true);
		StringBuilder buf = new StringBuilder();
		for (String element : ACTIVE_BUNDLES) {
			Bundle bundle = Platform.getBundle(element);
			if (bundle == null) {
				// // log bundles that cannot be found:
				// buf.append("- not found: ");
				// buf.append(NOT_ACTIVE_BUNDLES[i]);
				// buf.append('\n');
			} else if (bundle.getState() != Bundle.ACTIVE) {
				buf.append("- ");
				buf.append(element);
				buf.append('\n');
			}
		}
		// add some debug output
		if (buf.length() > 0) {
			printPluginStatus(true);
		}
		assertTrue("Bundles not active which used to be active:\n" + buf, buf.length() == 0);
	}
}
