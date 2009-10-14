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

import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

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
public class MSashTest extends RenderingTestCase {
	public void testSashWeights() {
		MWindow window = createSashWithNViews(2);

		topWidget = createModel(window);
		((Shell) topWidget).layout(true);
		MPartSashContainer sash = (MPartSashContainer) window.getChildren()
				.get(0);
		assertTrue("Should be an MPartSashContainer",
				sash instanceof MPartSashContainer);

		MPartSashContainer sf = (MPartSashContainer) sash;
		EList wgts = sf.getWeights();
		assertTrue("weight list muct not be null", wgts != null);
		assertTrue("expected count 2, was " + wgts.size(), wgts.size() == 2);

		Integer first = (Integer) wgts.get(0);
		Integer second = (Integer) wgts.get(1);
		assertTrue("Values should be equal", first.intValue() == second
				.intValue());

		MPart v1 = (MPart) sf.getChildren().get(0);
		Composite c1 = (Composite) v1.getWidget();
		Rectangle r1 = c1.getBounds();
		MPart v2 = (MPart) sf.getChildren().get(1);
		Composite c2 = (Composite) v2.getWidget();
		Rectangle r2 = c2.getBounds();
		assertTrue("SWT controls should be equal:", r1.width == r2.width);

		SashForm sfw = (SashForm) sf.getWidget();
		int[] w = { 75, 25 };
		sfw.setWeights(w);
		sfw.layout(true);
		testWeights(sf, 75, 25);

		sf.getWeights().clear();
		sf.getWeights().add(new Integer(40));
		sf.getWeights().add(new Integer(60));
		testWeights(sf, 40, 60);
	}

	private void testWeights(MPartSashContainer sf, double w1, double w2) {
		double baseRatio = w1 / w2;

		// test the model
		Integer sf1 = (Integer) sf.getWeights().get(0);
		Integer sf2 = (Integer) sf.getWeights().get(1);
		checkRatio("MSashForm", sf1, sf2, baseRatio);

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
		window.setName("MyWindow");
		MPartSashContainer sash = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sash);

		for (int i = 0; i < n; i++) {
			MPart contributedPart = MApplicationFactory.eINSTANCE.createPart();
			contributedPart.setName("Sample View" + i);
			contributedPart
					.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
			sash.getChildren().add(contributedPart);
		}

		return window;
	}
}
