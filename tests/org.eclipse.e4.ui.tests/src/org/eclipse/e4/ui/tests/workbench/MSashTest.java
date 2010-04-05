/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * This test validates the UI Model <-> SWT Widget interactions specifically
 * applicable to the <code>MSashForm</code> model element.
 * <p>
 * The rules being tested are:
 * <ol>
 * <li>The SWT SashForm's weight count matches the number of modeled children</li>
 * <li>The MSashForm's weights are equal if not specified in the model</li>
 * <li>Changing the SWT SashForm's weights (a simulated sash 'drag') cause the
 * MSashForm to update to match*</li>
 * <li>Changing the MSashForm's weights cause the SWT SashForm to update its
 * weights to match*</li>
 * </ol>
 * </p>
 * <Strong>* NOTE:</strong> The SWT SashForm reports its weights based on the
 * actual bounds of the widgets it's hosting. This means that setting the
 * model's weights to 75,25 does <b>not</b> result in the SashForm's weights
 * being 75,25. This means that the tests have to perform 'fuzzy' comparisons,
 * ensuring only that the resulting widget bounds and the SashForm's weights
 * match the MSashForm's weights <b>ratios</b> to some tolerance. Even the
 * simple example of setting the weights to 50,50 may not end up with the child
 * controls not being equally sized because the available area may not be
 * equally divisible amongst its children.
 * 
 */
public class MSashTest extends TestCase {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}

		if (appContext instanceof IDisposable) {
			((IDisposable) appContext).dispose();
		}
	}

	public void testSashWeights() {
		MWindow window = createSashWithNViews(2);

		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Widget topWidget = (Widget) window.getWidget();
		((Shell) topWidget).layout(true);
		MPartSashContainer sash = (MPartSashContainer) window.getChildren()
				.get(0);
		assertTrue("Should be an MPartSashContainer",
				sash instanceof MPartSashContainer);

		MPart part0 = (MPart) sash.getChildren().get(0);
		MPart part1 = (MPart) sash.getChildren().get(1);

		int cdVal0 = -1;
		try {
			cdVal0 = Integer.parseInt(part0.getContainerData());
		} catch (NumberFormatException e) {
		}
		assertTrue("Part0 data is not an integer", cdVal0 != -1);

		int cdVal1 = -1;
		try {
			cdVal1 = Integer.parseInt(part1.getContainerData());
		} catch (NumberFormatException e) {
		}
		assertTrue("Part1 data is not an integer", cdVal1 != -1);

		assertTrue("Values should be equal", cdVal0 == cdVal1);

		testWeights(sash, 50, 50);
	}

	private void testWeights(MPartSashContainer sf, double w1, double w2) {
		double baseRatio = w1 / w2;

		MPart part0 = (MPart) sf.getChildren().get(0);
		MPart part1 = (MPart) sf.getChildren().get(1);

		int cdVal0 = -1;
		try {
			cdVal0 = Integer.parseInt(part0.getContainerData());
		} catch (NumberFormatException e) {
		}
		assertTrue("Part0 data is not an integer", cdVal0 != -1);

		int cdVal1 = -1;
		try {
			cdVal1 = Integer.parseInt(part1.getContainerData());
		} catch (NumberFormatException e) {
		}
		assertTrue("Part1 data is not an integer", cdVal1 != -1);

		// test the model
		checkRatio("MSashForm", cdVal0, cdVal1, baseRatio);

		// test the SashForm
		SashForm sfw = (SashForm) sf.getWidget();
		int[] sfwWghts = sfw.getWeights();
		checkRatio("SWT SashForm", sfwWghts[0], sfwWghts[1], baseRatio);

		// Test the controls (assume vertical for now)
		Composite c1 = (Composite) sfw.getChildren()[0];
		Composite c2 = (Composite) sfw.getChildren()[1];
		checkRatio("Control Bounds", c1.getSize().y, c2.getSize().y, baseRatio);
	}

	private void checkRatio(String label, int num, int div, double baseRatio) {
		double ratio = (double) num / (double) div;

		double TOLERANCE = 0.1;
		boolean withinTolerance = Math.abs(ratio - baseRatio) < TOLERANCE;
		assertTrue("Ratio mismatch on" + label + "weights", withinTolerance);
	}

	private MWindow createSashWithNViews(int n) {
		final MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		window.setHeight(300);
		window.setWidth(401);
		window.setLabel("MyWindow");
		MPartSashContainer sash = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sash);

		for (int i = 0; i < n; i++) {
			MPart contributedPart = MApplicationFactory.eINSTANCE.createPart();
			contributedPart.setLabel("Sample View" + i);
			contributedPart
					.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
			sash.getChildren().add(contributedPart);
		}

		return window;
	}
}
