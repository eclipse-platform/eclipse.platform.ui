/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.presentations;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;
import org.eclipse.ui.tests.performance.UIPerformancePlugin;

public class PresentationPerformanceTest extends BasicPerformanceTest {

	protected Shell theShell;

	protected Image img;

	protected Image img2;

	protected static final int NAME = 0;

	protected static final int TITLE = 1;

	protected static final int DIRTY = 2;

	protected static final int DESCRIPTION = 3;

	protected static final int TOOLTIP = 4;

	protected static final int IMAGE = 5;

	protected static final int TOOLBAR = 6;

	public PresentationPerformanceTest(String testName) {
		super(testName);
	}

	protected void doSetUp() throws Exception {
		super.doSetUp();
		theShell = new Shell(Display.getCurrent(), SWT.NONE);
		theShell.setBounds(0, 0, 1024, 768);
		theShell.setVisible(true);
		img = UIPerformancePlugin.getImageDescriptor(
				"icons/anything.gif").createImage();
		img2 = UIPerformancePlugin.getImageDescriptor("icons/view.gif")
				.createImage();
	}

	protected void doTearDown() throws Exception {
		theShell.dispose();
		theShell = null;
		img.dispose();
		img2.dispose();
		super.doTearDown();
	}

	protected PresentationTestbed createPresentation(
			AbstractPresentationFactory factory, int type, int numParts) {
		TestPresentablePart selection = null;
		PresentationTestbed testBed = new PresentationTestbed(theShell,
				factory, type);
		for (int partCount = 0; partCount < numParts; partCount++) {
			TestPresentablePart part = new TestPresentablePart(theShell, img);
			part.setName("Some part");
			part.setContentDescription("Description");
			part.setTitle("Some title");
			part.setDirty(partCount % 2 == 0);
			part.setTooltip("This is a tooltip");
			testBed.add(part);
			selection = part;
		}

		testBed.setSelection(selection);

		Control ctrl = testBed.getControl();
		ctrl.setBounds(theShell.getClientArea());
		return testBed;
	}

	protected void twiddleProperty(int property, TestPresentablePart part) {
		switch (property) {
		case NAME: {
			String originalName = part.getName();
			part.setName("Some new name");
			processEvents();
			part.setName(originalName);
			processEvents();
			break;
		}
		case TITLE: {
			String originalTitle = part.getTitle();
			part.setTitle("Some new title");
			processEvents();
			part.setTitle(originalTitle);
			processEvents();
			break;
		}
		case DIRTY: {
			boolean originalDirty = part.isDirty();
			part.setDirty(!originalDirty);
			processEvents();
			part.setDirty(originalDirty);
			processEvents();
			break;
		}
		case DESCRIPTION: {
			String originalDescription = part.getTitleStatus();
			part.setContentDescription("Some new description");
			processEvents();
			part.setContentDescription(originalDescription);
			processEvents();
			break;
		}
		case TOOLTIP: {
			String originalTooltip = part.getTitleToolTip();
			part.setTooltip("Some new tooltip");
			processEvents();
			part.setTooltip(originalTooltip);
			processEvents();
			break;
		}
		case IMAGE: {
			Image originalImage = part.getTitleImage();

			part.setImage(img2);
			processEvents();
			part.setImage(originalImage);
			processEvents();
			break;
		}
		case TOOLBAR: {
			ToolItem newItem = part.addToToolbar(img2);

			processEvents();
			part.removeFromToolbar(newItem);
			processEvents();
			break;
		}
		}
	}

}
