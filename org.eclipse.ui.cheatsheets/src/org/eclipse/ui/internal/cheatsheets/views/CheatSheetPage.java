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
import org.eclipse.ui.internal.cheatsheets.data.Item;

public class CheatSheetPage extends Page {
	//Colors
	private Color darkGrey;
	private Color lightGrey;
	private final RGB darkGreyRGB = new RGB(160, 192, 208);
	private final RGB HIGHLIGHT_RGB = new RGB(230, 230, 230);

	private CheatSheetDomParser parser;
	private ArrayList viewItemList;
	private CheatSheetView cheatSheetView;

	public CheatSheetPage(CheatSheetDomParser parser, ArrayList viewItemList, CheatSheetView cheatSheetView) {
		super();
		this.parser = parser;
		this.viewItemList = viewItemList;
		this.cheatSheetView = cheatSheetView;
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
	
		IntroItem myintro = new IntroItem(form, form.getBody(), parser.getIntroItem(), darkGrey, cheatSheetView);
	
		myintro.setItemColor(myintro.lightGrey);
		myintro.boldTitle();
		viewItemList.add(myintro);
			
		//Get the content info from the parser.  This makes up all items except the intro item.
		ArrayList items = parser.getItems();

		int switcher = 0;
	
		for (int i = 0; i < items.size(); i++) {
			if (switcher == 0) {
				if (items.get(i) instanceof ItemWithSubItems) {
					CoreItem ciws = new CoreItem(form, form.getBody(), (ItemWithSubItems) items.get(i), backgroundColor, cheatSheetView);
					viewItemList.add(ciws);
				} else {
					CoreItem mycore = new CoreItem(form, form.getBody(), (Item) items.get(i), backgroundColor, cheatSheetView);
					viewItemList.add(mycore);
				}
				switcher = 1;
			} else {
				if (items.get(i) instanceof ItemWithSubItems) {
					CoreItem ciws = new CoreItem(form, form.getBody(), (ItemWithSubItems) items.get(i), lightGrey, cheatSheetView);
					viewItemList.add(ciws);
				} else {
					CoreItem mycore = new CoreItem(form, form.getBody(), (Item) items.get(i), lightGrey, cheatSheetView);
					viewItemList.add(mycore);
				}
				switcher = 0;
			}
		}
	}

	/**
	 * Creates the cheatsheet's title areawhich will consists
	 * of a title and image.
	 *
	 * @param parent the SWT parent for the title area composite
	 */
	protected String getTitle() {
		if(parser != null & parser.getTitle() != null)
			return parser.getTitle();
		else
			return "";//$NON-NLS-1$
	}

	public void dispose() {
		super.dispose();

		if (lightGrey != null)
			lightGrey.dispose();

		if (darkGrey != null)
			darkGrey.dispose();
	}

	protected void init(Display display) {
		super.init(display);

		lightGrey = new Color(display, HIGHLIGHT_RGB);
		darkGrey = new Color(display, darkGreyRGB);
	}
}
