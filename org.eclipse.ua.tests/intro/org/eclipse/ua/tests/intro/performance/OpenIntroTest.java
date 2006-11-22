/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;

public class OpenIntroTest extends PerformanceTestCase {

	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(OpenIntroTest.class);
	}

	public void testOpenIntro() throws Exception {
		tagAsSummary("Open welcome", Dimension.ELAPSED_PROCESS);

		// warm-up
		for (int i=0;i<3;++i) {
			closeIntro();
			openIntro();
		}
		
		// run the tests
		for (int i=0;i<20;++i) {
			closeIntro();
			startMeasuring();
			openIntro();
			stopMeasuring();
		}
		
		commitMeasurements();
		assertPerformance();
	}
	
	public static void closeIntro() throws Exception {
		IIntroManager manager = PlatformUI.getWorkbench().getIntroManager();
		IIntroPart part = manager.getIntro();
		if (part != null) {
			manager.closeIntro(part);
		}
		flush();
	}

	private static void openIntro() throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IIntroManager manager = workbench.getIntroManager();
		manager.showIntro(workbench.getActiveWorkbenchWindow(), false);
		flush();
	}
	
	private static void flush() {
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
		}
	}
}
