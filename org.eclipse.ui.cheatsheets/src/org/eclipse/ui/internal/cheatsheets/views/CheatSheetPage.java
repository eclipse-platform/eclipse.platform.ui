/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.util.*;
import java.util.ArrayList;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.data.*;

public class CheatSheetPage extends Page {
	// Colors
	// Active color's RGB value
	protected final RGB activeRGB = new RGB(232, 242, 254);
	protected Color activeColor;
	// Alternating color's RGB value
	protected final RGB alternateRGB = new RGB(244, 244, 244);
	protected Color alternateColor;

	private CheatSheet cheatSheet;
	private ArrayList viewItemList;
	private CheatSheetViewer viewer;

	public CheatSheetPage(CheatSheet cheatSheet, ArrayList viewItemList, CheatSheetViewer cheatSheetViewer) {
		super();
		this.cheatSheet = cheatSheet;
		this.viewItemList = viewItemList;
		this.viewer = cheatSheetViewer;
	}

	public void createPart(Composite parent) {
		super.createPart(parent);
	}

	/**
	 * Creates the main composite area of the view.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created info area composite
	 */
	protected void createInfoArea(Composite parent) {
		CheatSheetStopWatch.startStopWatch("CheatSheetPage.createInfoArea()"); //$NON-NLS-1$
		super.createInfoArea(parent);
		CheatSheetStopWatch.printLapTime("CheatSheetPage.createInfoArea()", "Time in CheatSheetPage.createInfoArea() after super.createInfoArea(): "); //$NON-NLS-1$ //$NON-NLS-2$
	
		IntroItem intro = new IntroItem(toolkit, form, cheatSheet.getIntroItem(), activeColor, viewer);
		CheatSheetStopWatch.printLapTime("CheatSheetPage.createInfoArea()", "Time in CheatSheetPage.createInfoArea() after new IntroItem(): "); //$NON-NLS-1$ //$NON-NLS-2$
	
		intro.setItemColor(intro.alternateColor);
		intro.setBold(true);
		viewItemList.add(intro);
			
		CheatSheetStopWatch.printLapTime("CheatSheetPage.createInfoArea()", "Time in CheatSheetPage.createInfoArea() before add loop: "); //$NON-NLS-1$ //$NON-NLS-2$
		//Get the content info from the parser.  This makes up all items except the intro item.
		ArrayList items = cheatSheet.getItems();
		for (int i = 0; i < items.size(); i++) {
			Color color = (i%2) == 0 ? backgroundColor : alternateColor;

			CoreItem coreItem = new CoreItem(toolkit, form, (org.eclipse.ui.internal.cheatsheets.data.Item)items.get(i), color, viewer);
			viewItemList.add(coreItem);
		}
		CheatSheetStopWatch.printLapTime("CheatSheetPage.createInfoArea()", "Time in CheatSheetPage.createInfoArea(): "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates the cheatsheet's title areawhich will consists
	 * of a title and image.
	 *
	 * @param parent the SWT parent for the title area composite
	 */
	protected String getTitle() {
		if(cheatSheet != null & cheatSheet.getTitle() != null)
			return cheatSheet.getTitle();
		else
			return ICheatSheetResource.EMPTY_STRING;
	}

	public void dispose() {
		super.dispose();

		if (alternateColor != null)
			alternateColor.dispose();

		if (activeColor != null)
			activeColor.dispose();
	}

	protected void init(Display display) {
		super.init(display);

		activeColor = new Color(display, activeRGB);
		alternateColor = new Color(display, alternateRGB);
	}
	
	public void initialized() {
		for (Iterator iter = viewItemList.iterator(); iter.hasNext();) {
			ViewItem item = (ViewItem) iter.next();
			item.initialized();
		}
	}
}
