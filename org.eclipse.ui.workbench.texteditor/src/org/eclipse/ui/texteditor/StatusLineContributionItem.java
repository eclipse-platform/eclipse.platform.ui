/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;

/**
 * Contribution item for the status line.
 * @since 2.0
 */
public class StatusLineContributionItem extends ContributionItem implements IStatusField {
	/** Left and right margin used in CLabel */
	private static final int INDENT = 3; 
	/** Precomputed label width hint */
	private int fFixedWidth = -1;

	/** The label text */
	private String fText;
	/** The label image */
	private Image fImage;
	/** The status line label widget */
	private CLabel fLabel;
	
	/**
	 * Creates a new item with the given id.
	 * 
	 * @param id the item's id
	 */
	public StatusLineContributionItem(String id) {
		super(id);
	}
	
	/*
	 * @see IStatusField#setText
	 */
	public void setText(String text) {
		fText= text;
		if (fLabel != null && !fLabel.isDisposed()) {
			fLabel.setText(fText);
		}		
	}
	
	/*
	 * @see IStatusField#setImage(Image)
	 */
	public void setImage(Image image) {
		fImage= image;
		if (fLabel != null && !fLabel.isDisposed()) {
			fLabel.setImage(fImage);
		}
	}
	
	/*
	 * @see IContributionItem#fill(Composite)
	 */
	public void fill(Composite parent) {
		fLabel= new CLabel(parent, SWT.SHADOW_IN);
		StatusLineLayoutData data = new StatusLineLayoutData();
		if (fFixedWidth < 0) {
			GC gc = new GC(parent);
			gc.setFont(parent.getFont());
			fFixedWidth = gc.getFontMetrics().getAverageCharWidth() * 14;
			fFixedWidth += INDENT * 2;
			gc.dispose();
		}
		data.widthHint = fFixedWidth;
		fLabel.setLayoutData(data);
		
		if (fText != null)
			fLabel.setText(fText);
	}
}

