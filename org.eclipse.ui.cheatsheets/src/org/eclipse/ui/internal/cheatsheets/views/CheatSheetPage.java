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

import java.util.ArrayList;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
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

		WorkbenchHelp.setHelp(cheatSheetComposite, IHelpContextIds.WELCOME_EDITOR);

//		checkDynamicModel();
	}

	/**
	 * Creates the main composite area of the view.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created info area composite
	 */
	protected void createInfoArea(Composite parent) {
		super.createInfoArea(parent);
	
		IntroItem myintro = new IntroItem(toolkit, form, cheatSheet.getIntroItem(), activeColor, viewer);
	
		myintro.setItemColor(myintro.alternateColor);
		myintro.setBold(true);
		viewItemList.add(myintro);
			
		//Get the content info from the parser.  This makes up all items except the intro item.
		ArrayList items = cheatSheet.getItems();
		for (int i = 0; i < items.size(); i++) {
			Color color = (i%2) == 0 ? backgroundColor : alternateColor;

			CoreItem coreItem = new CoreItem(toolkit, form, (org.eclipse.ui.internal.cheatsheets.data.Item)items.get(i), color, viewer);
			viewItemList.add(coreItem);
		}
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
			return ""; //$NON-NLS-1$
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
}
