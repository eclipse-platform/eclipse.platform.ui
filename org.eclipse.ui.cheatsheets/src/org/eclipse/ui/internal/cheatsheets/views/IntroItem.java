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

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.Item;

public class IntroItem extends ViewItem {
	private ImageHyperlink startButton;

	/**
	 * Constructor for IntroItem.
	 * @param parent
	 * @param contentItem
	 */
	public IntroItem(FormToolkit toolkit, ScrolledForm form, Item contentItem, Color itemColor, CheatSheetViewer viewer) {
		super(toolkit, form, contentItem, itemColor, viewer);
	}


	/*package*/ void setStartImage() {
		startButton.setImage(CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_START));
		startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.START_CHEATSHEET_TOOLTIP));
//		startButton.setFAccessibleName(startButton.getToolTipText());
	}

	/*package*/ void setRestartImage() {
		startButton.setImage(CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_RESTART));
		startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.RESTART_CHEATSHEET_TOOLTIP));
//		startButton.setFAccessibleName(startButton.getToolTipText());
	}

	/**
	 * @see org.eclipse.ui.internal.cheatsheets.data.ViewItem#handleButtons(Composite)
	 */
	/*package*/ void handleButtons() {
		buttonComposite = toolkit.createComposite(bodyWrapperComposite);
		GridLayout buttonlayout = new GridLayout(4, false);
		buttonlayout.marginHeight = 2;
		buttonlayout.marginWidth = 2;
		buttonlayout.verticalSpacing = 2;

		TableWrapData buttonData = new TableWrapData(TableWrapData.FILL);

		buttonComposite.setLayout(buttonlayout);
		buttonComposite.setLayoutData(buttonData);
		buttonComposite.setBackground(itemColor);
		Label filllabel = toolkit.createLabel(buttonComposite, null);
		filllabel.setBackground(itemColor);
		GridData filldata = new GridData();
		filldata.widthHint = 16;
		filllabel.setLayoutData(filldata);

		startButton = createButton(buttonComposite, CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_START), this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.START_CHEATSHEET_TOOLTIP));
		toolkit.adapt(startButton, true, true);
		startButton.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				viewer.advanceIntroItem();
			}
		});
	}

}
