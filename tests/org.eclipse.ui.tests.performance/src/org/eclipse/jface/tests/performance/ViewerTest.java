/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

package org.eclipse.jface.tests.performance;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.performance.UIPerformanceTestRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * The LinearViewerTest is a test that tests viewers.
 */
public abstract class ViewerTest implements BeforeEachCallback, AfterEachCallback {

	@RegisterExtension
	static UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	Shell browserShell;

	public static int ITERATIONS = 100;
	public static int MIN_ITERATIONS = 20;

	private PerformanceMeter performanceMeter;

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		Performance perf = Performance.getDefault();
		String scenarioId = this.getClass().getName() + "." + context.getDisplayName();
		performanceMeter = perf.createPerformanceMeter(scenarioId);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if (performanceMeter != null) {
			performanceMeter.dispose();
			performanceMeter = null;
		}
	}

	protected void startMeasuring() {
		if (performanceMeter != null) {
			performanceMeter.start();
		}
	}

	protected void stopMeasuring() {
		if (performanceMeter != null) {
			performanceMeter.stop();
		}
	}

	protected void commitMeasurements() {
		if (performanceMeter != null) {
			performanceMeter.commit();
		}
	}

	protected void assertPerformance() {
		if (performanceMeter != null) {
			Performance.getDefault().assertPerformance(performanceMeter);
		}
	}

	protected void openBrowser() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		browserShell = new Shell(display);
		browserShell.setSize(500, 500);
		browserShell.setLayout(new FillLayout());
		StructuredViewer viewer = createViewer(browserShell);
		viewer.setUseHashlookup(true);
		viewer.setInput(getInitialInput());
		browserShell.open();
		// processEvents();
	}

	/**
	 * Get the initial input for the receiver.
	 */
	protected Object getInitialInput() {
		return this;
	}

	/**
	 * Create the viewer we are testing.
	 */
	protected abstract StructuredViewer createViewer(Shell shell);

	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((TestElement) element).getText();
			}

		};
	}

	@AfterEach
	public final void closeBrowserShell() throws Exception {
		if(browserShell!= null){
			browserShell.close();
			browserShell = null;
		}
	}

	/**
	 * Return the number of iterations for tests that are slow on Linux
	 * @return int
	 */
	public int slowGTKIterations(){
		if(Util.isGtk())
			return ITERATIONS / 5;
		return ITERATIONS;
	}


	/**
	 * Return the number of iterations for tests that are slow on Linux
	 * @return int
	 */
	public int slowWindowsIterations(){
		if(Util.isWindows())
			return ITERATIONS / 5;
		return ITERATIONS;
	}

	public void tagAsSummary(String shortName, Dimension dimension) {
		Performance.getDefault().tagAsSummary(performanceMeter, shortName, new Dimension[] { dimension });
	}

	public void tagAsGlobalSummary(String shortName, Dimension dimension) {
		Performance.getDefault().tagAsGlobalSummary(performanceMeter, shortName, new Dimension[] { dimension });
	}

}
