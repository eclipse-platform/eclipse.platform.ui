package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;


/**
 * Contribution item for the status line.
 */
public class StatusLineContributionItem extends ContributionItem implements IStatusField {
	
	
	static class StatusLineLabel extends CLabel {
		
		private static int INDENT= 3; // left and right margin used in CLabel
		
		private Point fFixedSize;
		
		public StatusLineLabel(Composite parent, int style) {
			super(parent, style);
			
			GC gc= new GC(parent);
			gc.setFont(parent.getFont());
			Point extent= gc.textExtent("MMMMMMMMM"); //$NON-NLS-1$
			gc.dispose();
			
			fFixedSize= new Point(extent.x + INDENT * 2, 10);
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			return fFixedSize;
		}
	};
	
	private String fText;
	private Image fImage;
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

