/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerTrimContainerTest extends
		ModelReconcilerTest {

	private void testTrimContainer_Side(SideValue applicationState,
			SideValue userChange, SideValue newApplicationState) {
		MApplication application = createApplication();

		MTrimmedWindow window = createTrimmedWindow(application);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		trimBar.setSide(applicationState);
		window.getTrimBars().add(trimBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		trimBar.setSide(userChange);

		Object serialize = reconciler.serialize();

		application = createApplication();
		window = (MTrimmedWindow) application.getChildren().get(0);
		trimBar = window.getTrimBars().get(0);

		trimBar.setSide(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, serialize);

		assertEquals(newApplicationState, trimBar.getSide());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, trimBar.getSide());
		} else {
			// user change must override application state
			assertEquals(userChange, trimBar.getSide());
		}
	}

	public void testTrimContainer_Side_TopTopTop() {
		testTrimContainer_Side(SideValue.TOP, SideValue.TOP, SideValue.TOP);
	}

	public void testTrimContainer_Side_TopTopRight() {
		testTrimContainer_Side(SideValue.TOP, SideValue.TOP, SideValue.RIGHT);
	}

	public void testTrimContainer_Side_TopRightTop() {
		testTrimContainer_Side(SideValue.TOP, SideValue.RIGHT, SideValue.TOP);
	}

	public void testTrimContainer_Side_TopRightRight() {
		testTrimContainer_Side(SideValue.TOP, SideValue.RIGHT, SideValue.RIGHT);
	}

	public void testTrimContainer_Side_RightTopTop() {
		testTrimContainer_Side(SideValue.RIGHT, SideValue.TOP, SideValue.TOP);
	}

	public void testTrimContainer_Side_RightTopRight() {
		testTrimContainer_Side(SideValue.RIGHT, SideValue.TOP, SideValue.RIGHT);
	}

	public void testTrimContainer_Side_RightRightTop() {
		testTrimContainer_Side(SideValue.RIGHT, SideValue.RIGHT, SideValue.TOP);
	}

	public void testTrimContainer_Side_RightRightRight() {
		testTrimContainer_Side(SideValue.RIGHT, SideValue.RIGHT,
				SideValue.RIGHT);
	}

	public void testTrimContainer_Side_RightRightBottom() {
		testTrimContainer_Side(SideValue.RIGHT, SideValue.RIGHT,
				SideValue.BOTTOM);
	}

	public void testTrimContainer_Side_RightBottomRight() {
		testTrimContainer_Side(SideValue.RIGHT, SideValue.BOTTOM,
				SideValue.RIGHT);
	}

	public void testTrimContainer_Side_BottomRightRight() {
		testTrimContainer_Side(SideValue.BOTTOM, SideValue.RIGHT,
				SideValue.RIGHT);
	}

	public void testTrimContainer_Side_BottomRightBottom() {
		testTrimContainer_Side(SideValue.BOTTOM, SideValue.RIGHT,
				SideValue.BOTTOM);
	}

	public void testTrimContainer_Side_BottomBottomRight() {
		testTrimContainer_Side(SideValue.BOTTOM, SideValue.BOTTOM,
				SideValue.TOP);
	}

	public void testTrimContainer_Side_BottomBottomBottom() {
		testTrimContainer_Side(SideValue.BOTTOM, SideValue.BOTTOM,
				SideValue.BOTTOM);
	}

	public void testTrimContainer_Side_BottomBottomLeft() {
		testTrimContainer_Side(SideValue.BOTTOM, SideValue.BOTTOM,
				SideValue.LEFT);
	}

	public void testTrimContainer_Side_BottomLeftBottom() {
		testTrimContainer_Side(SideValue.BOTTOM, SideValue.LEFT,
				SideValue.BOTTOM);
	}

	public void testTrimContainer_Side_BottomLeftLeft() {
		testTrimContainer_Side(SideValue.BOTTOM, SideValue.LEFT, SideValue.LEFT);
	}

	public void testTrimContainer_Side_LeftBottomBottom() {
		testTrimContainer_Side(SideValue.LEFT, SideValue.BOTTOM,
				SideValue.BOTTOM);
	}

	public void testTrimContainer_Side_LeftBottomLeft() {
		testTrimContainer_Side(SideValue.LEFT, SideValue.BOTTOM, SideValue.LEFT);
	}

	public void testTrimContainer_Side_LeftLeftBottom() {
		testTrimContainer_Side(SideValue.LEFT, SideValue.LEFT, SideValue.BOTTOM);
	}

	public void testTrimContainer_Side_LeftLeftLeft() {
		testTrimContainer_Side(SideValue.LEFT, SideValue.LEFT, SideValue.LEFT);
	}

	public void testTrimContainer_Side_LeftLeftTop() {
		testTrimContainer_Side(SideValue.LEFT, SideValue.LEFT, SideValue.TOP);
	}

	public void testTrimContainer_Side_LeftTopLeft() {
		testTrimContainer_Side(SideValue.LEFT, SideValue.TOP, SideValue.LEFT);
	}

	public void testTrimContainer_Side_LeftTopTop() {
		testTrimContainer_Side(SideValue.LEFT, SideValue.TOP, SideValue.TOP);
	}
}
