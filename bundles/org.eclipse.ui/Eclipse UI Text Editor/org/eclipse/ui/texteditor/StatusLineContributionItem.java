/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;


/**
 * Contribution item for the status line.
 * @since 2.0
 */
public class StatusLineContributionItem extends ContributionItem implements IStatusField {
	
	/**
	 * Specific label for the status line.
	 */
	static class StatusLineLabel extends CLabel {
		
		/** Left and right margin used in CLabel */
		private static int INDENT= 3; 
		/** Precomputed label size */
		private Point fFixedSize;
		
		/**
		 * Creates a new status line label.
		 * @param parent parent control
		 * @param style the swt style bits
		 */
		public StatusLineLabel(Composite parent, int style) {
			super(parent, style);
			
			GC gc= new GC(parent);
			gc.setFont(parent.getFont());
			Point extent= gc.textExtent("MMMMMMMMM"); //$NON-NLS-1$
			gc.dispose();
			
			fFixedSize= new Point(extent.x + INDENT * 2, 10);
		}
		
		/*
		 * @see Control#computeSize(int, int, boolean)
		 */
		public Point computeSize(int wHint, int hHint, boolean changed) {
			return fFixedSize;
		}
	};
	
	/** The label text */
	private String fText;
	/** The label image */
	private Image fImage;
	/** The status line label widget */
	private StatusLineLabel fLabel;
	
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
		fLabel= new StatusLineLabel(parent, SWT.SHADOW_IN);
		fLabel.setData(this);
		
		if (fText != null)
			fLabel.setText(fText);
	}
}

