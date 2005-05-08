/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.*;

public class CheatSheetPage extends Page {
	// Colors
	private Color introColor;
	private Color activeColor;

	private Color alternateColor;

	private CheatSheet cheatSheet;

	private ArrayList viewItemList;

	private CheatSheetViewer viewer;

	public CheatSheetPage(CheatSheet cheatSheet, ArrayList viewItemList,
			CheatSheetViewer cheatSheetViewer) {
		super();
		this.cheatSheet = cheatSheet;
		this.viewItemList = viewItemList;
		this.viewer = cheatSheetViewer;
	}

	public void createPart(Composite parent) {
		CheatSheetStopWatch.startStopWatch("CheatSheetPage.createPart()"); //$NON-NLS-1$		
		super.createPart(parent);
		CheatSheetStopWatch
				.printLapTime(
						"CheatSheetPage.createPart()", "Time in CheatSheetPage.createInfoArea() after super.createInfoArea(): "); //$NON-NLS-1$ //$NON-NLS-2$
		IntroItem intro = new IntroItem(this, cheatSheet.getIntroItem(),
				introColor, viewer);
		CheatSheetStopWatch
				.printLapTime(
						"CheatSheetPage.createPart()", "Time in CheatSheetPage.createInfoArea() after new IntroItem(): "); //$NON-NLS-1$ //$NON-NLS-2$

		intro.setBold(true);
		viewItemList.add(intro);

		CheatSheetStopWatch
				.printLapTime(
						"CheatSheetPage.createInfoArea()", "Time in CheatSheetPage.createPart() before add loop: "); //$NON-NLS-1$ //$NON-NLS-2$
		// Get the content info from the parser. This makes up all items except
		// the intro item.
		ArrayList items = cheatSheet.getItems();
		for (int i = 0; i < items.size(); i++) {
			Color color = (i % 2) == 0 ? backgroundColor : alternateColor;

			CoreItem coreItem = new CoreItem(this,
					(org.eclipse.ui.internal.cheatsheets.data.Item) items
							.get(i), color, viewer);
			viewItemList.add(coreItem);
		}
		CheatSheetStopWatch
				.printLapTime(
						"CheatSheetPage.createInfoArea()", "Time in CheatSheetPage.createPart(): "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates the cheatsheet's title areawhich will consists of a title and
	 * image.
	 * 
	 * @param parent
	 *            the SWT parent for the title area composite
	 */
	protected String getTitle() {
		if (cheatSheet != null && cheatSheet.getTitle() != null)
			return cheatSheet.getTitle();
		return ICheatSheetResource.EMPTY_STRING;
	}

	public void dispose() {
		super.dispose();

		if (alternateColor != null)
			alternateColor.dispose();

		if (activeColor != null)
			activeColor.dispose();
		
		if (introColor != null)
			introColor.dispose();
		alternateColor = null;
		activeColor = null;
		introColor = null;
	}

	protected void init(Display display) {
		super.init(display);
		computeColors(display);
	}

	private void computeColors(Display display) {
		RGB rgb;
		RGB white = new RGB(255, 255, 255);
		RGB black = new RGB(0, 0, 0);

		if (toolkit.getColors().isWhiteBackground()) {
			rgb = toolkit.getColors().getSystemColor(SWT.COLOR_LIST_SELECTION);
			// active color is selection + 80% white
			rgb = FormColors.blend(rgb, white, 20);
			// test for bounds
			if (FormColors.testTwoPrimaryColors(rgb, 245, 256)) {
				// too bright - tone down 20%
				rgb = FormColors.blend(rgb, black, 80);
			} else if (FormColors.testAnyPrimaryColor(rgb, 170, 191)) {
				// too dark - brighten 15%
				rgb = FormColors.blend(rgb, white, 85);
			} else if (FormColors.testAnyPrimaryColor(rgb, 190, 215)) {
				// too dark - brighten 10%
				rgb = FormColors.blend(rgb, white, 90);
			}
			// final check - if gray
			if (Math.abs(rgb.blue-rgb.green) <5 && 
					Math.abs(rgb.blue-rgb.red)<5 && 
					Math.abs(rgb.green-rgb.red)<5) {
				// blend with blue
				rgb = FormColors.blend(rgb, new RGB(100, 100, 255), 90);
			}
			activeColor = new Color(display, rgb);

			// alternate color is widget background + 40% white
			rgb = toolkit.getColors().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND);
			rgb = FormColors.blend(rgb, white, 60);
			// test for bounds
			if (FormColors.testTwoPrimaryColors(rgb, 209, 220)) {
				// too dark - add 30% white
				rgb = FormColors.blend(rgb, white, 70);
			} else if (FormColors.testTwoPrimaryColors(rgb, 209, 230)) {
				// too dark - add 20% white
				rgb = FormColors.blend(rgb, white, 80);
			}
			alternateColor = new Color(display, rgb);
		} else {
			// colored background
			rgb = toolkit.getColors().getSystemColor(SWT.COLOR_LIST_SELECTION);
			// active color is selection + 70% white
			rgb = FormColors.blend(rgb, white, 30);
			// If these values are in the range of 201 to 220, then decrease
			// white by 20%
			if (FormColors.testTwoPrimaryColors(rgb, 200, 221))
				rgb = FormColors.blend(rgb, black, 80);
			// If these values are in the range of 221 to 240, then decrease
			// white by 40%
			else if (FormColors.testTwoPrimaryColors(rgb, 222, 241))
				rgb = FormColors.blend(rgb, black, 60);
			// If these values are in the range of 241 to 255, then decrease
			// white by 70%
			else if (FormColors.testTwoPrimaryColors(rgb, 240, 256))
				rgb = FormColors.blend(rgb, black, 30);
			activeColor = new Color(display, rgb);

			// alternate color is widget background + 40% white
			rgb = toolkit.getColors().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND);
			rgb = FormColors.blend(rgb, white, 60);
			// If these values are in the range of 201 to 215, then decrease
			// white by 10%
			if (FormColors.testTwoPrimaryColors(rgb, 200, 216))
				rgb = FormColors.blend(rgb, black, 90);
			// If these values are in the range of 216 to 220, then decrease
			// white by 20%
			else if (FormColors.testTwoPrimaryColors(rgb, 215, 221))
				rgb = FormColors.blend(rgb, black, 80);
			// If these values are in the range of 221 to 230, then decrease
			// white by 40%
			else if (FormColors.testTwoPrimaryColors(rgb, 220, 231))
				rgb = FormColors.blend(rgb, black, 60);
			// If these values are in the range of 231 to 255, then decrease
			// white by 60%
			else if (FormColors.testTwoPrimaryColors(rgb, 230, 256))
				rgb = FormColors.blend(rgb, black, 40);
			alternateColor = new Color(display, rgb);
		}
		rgb = activeColor.getRGB();
		rgb = FormColors.blend(rgb, white, 40);
		introColor = new Color(display, rgb);
	}

	public void initialized() {
		for (Iterator iter = viewItemList.iterator(); iter.hasNext();) {
			ViewItem item = (ViewItem) iter.next();
			item.initialized();
		}
	}

	public Color getActiveColor() {
		return activeColor;
	}

	public Color getAlternateColor() {
		return alternateColor;
	}

	public ScrolledForm getForm() {
		return form;
	}

	public FormToolkit getToolkit() {
		return toolkit;
	}
}
