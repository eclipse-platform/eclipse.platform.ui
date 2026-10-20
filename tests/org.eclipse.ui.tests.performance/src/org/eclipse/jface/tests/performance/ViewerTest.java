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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.performance.UIPerformanceTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Base class for the JFace viewer performance tests. Times the measured
 * sections and reports their distribution once the test is done.
 */
public abstract class ViewerTest {

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	Shell browserShell;

	public static int ITERATIONS = 100;
	public static int MIN_ITERATIONS = 20;

	private final List<Long> timings = new ArrayList<>();

	private String scenario;

	private long measuringSince;

	@BeforeEach
	public void startScenario(TestInfo testInfo) {
		scenario = getClass().getSimpleName() + "." + testInfo.getDisplayName();
		timings.clear();
	}

	protected void startMeasuring() {
		measuringSince = System.nanoTime();
	}

	protected void stopMeasuring() {
		timings.add(System.nanoTime() - measuringSince);
	}

	/**
	 * Reports what has been measured so far under the given suffix and drops the
	 * measurements, so that a test with several phases can report them separately.
	 */
	protected void reportTimings(String suffix) {
		UIPerformanceTestUtil.reportTimings(suffix.isEmpty() ? scenario : scenario + " " + suffix, timings);
		timings.clear();
	}

	protected void reportTimings() {
		reportTimings("");
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

}
