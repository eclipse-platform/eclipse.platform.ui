/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

class StatusLineContributionItem extends ContributionItem {

	final static int DEFAULT_CHAR_WIDTH = 40; 
	
	private int charWidth = DEFAULT_CHAR_WIDTH;
	private CLabel label;
	private String text = ""; //$NON-NLS-1$
	private int widthHint = -1;

	StatusLineContributionItem(String id) {
		super(id);
	}

	StatusLineContributionItem(String id, int charWidth) {
		super(id);
		this.charWidth = charWidth;
	}

	String getText() {
		return text;
	}

	void setText(String text)
		throws IllegalArgumentException {
		if (text == null)
			throw new IllegalArgumentException();

		this.text = text;
		
		if (label != null && !label.isDisposed())
			label.setText(this.text);
		
		if (this.text.length() == 0) {
			if (isVisible()) {
				setVisible(false);
				getParent().update(true);
			}
		} else {
			if (!isVisible()) {
				setVisible(true);
				getParent().update(true);	
			}
		}
	}

	public void fill(Composite parent) {	
		label = new CLabel(parent, SWT.SHADOW_IN);
		StatusLineLayoutData statusLineLayoutData = new StatusLineLayoutData();
		
		if (widthHint < 0) {
			GC gc = new GC(parent);
			gc.setFont(parent.getFont());
			widthHint = gc.getFontMetrics().getAverageCharWidth() * charWidth;
			gc.dispose();
		}

		statusLineLayoutData.widthHint = widthHint;
		label.setLayoutData(statusLineLayoutData);
		label.setText(text);
	}
}
