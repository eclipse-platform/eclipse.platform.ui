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

package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.StatusLineLayoutData;

/**
 * Contribution item for the status line.
 * @since 2.0
 */
public class StatusLineContributionItem extends ContributionItem implements IStatusField {
	
	/**
	 * Internal mouse listener to track double clicking the status line item.
	 * @since 3.0
	 */
	private class Listener extends MouseAdapter {
		/*
		 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			if (fActionHandler != null && fActionHandler.isEnabled())
				fActionHandler.run();
		}
	};
	
	/**
	 * Left and right margin used in CLabel.
	 * @since 2.1
	 */
	private static final int INDENT= 3;
	/** 
	 * Number of characters that should fit into the item.
	 * @since 2.1
	 */
	private static final int LENGTH= 14;
	
	/**
	 * Precomputed label width hint.
	 * @since 2.1
	 */
	private int fFixedWidth= -1;
	/** The label text */
	private String fText;
	/** The label image */
	private Image fImage;
	/** The status line label widget */
	private CLabel fLabel;
	/** 
	 * The action handler.
	 * @since 3.0
	 */
	private IAction fActionHandler;
	/** 
	 * The mouse listener 
	 * @since 3.0
	 */
	private MouseListener fMouseListener;
	
	
	/**
	 * Creates a new item with the given id.
	 * 
	 * @param id the item's id
	 */
	public StatusLineContributionItem(String id) {
		super(id);
	}
	
	/*
	 * @see IStatusField#setText(String)
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
		fLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				fMouseListener= null;
			}
		});
		if (fActionHandler != null) {
			fMouseListener= new Listener();
			fLabel.addMouseListener(fMouseListener);
		}
		
		StatusLineLayoutData data = new StatusLineLayoutData();
		data.widthHint= getWidthHint(parent);
		fLabel.setLayoutData(data);
		
		if (fText != null)
			fLabel.setText(fText);
	}
	
	public void setActionHandler(IAction actionHandler) {
		if (fActionHandler != null && actionHandler == null && fMouseListener != null) {
			if (!fLabel.isDisposed())
				fLabel.removeMouseListener(fMouseListener);
			fMouseListener= null;
		}
		
		fActionHandler= actionHandler;
		
		if (fLabel != null && !fLabel.isDisposed() && fMouseListener == null && fActionHandler != null) {
			fMouseListener= new Listener();
			fLabel.addMouseListener(fMouseListener);
		}
	}
	
	/**
	 * Returns the width hint for this label.
	 * 
	 * @param control the root control of this label
	 * @return the width hint for this label
	 * @since 2.1
	 */
	private int getWidthHint(Composite control) {
		if (fFixedWidth < 0) {
			GC gc= new GC(control);
			gc.setFont(control.getFont());
			fFixedWidth= gc.getFontMetrics().getAverageCharWidth() * LENGTH;
			fFixedWidth += INDENT * 2;
			gc.dispose();
		}
		return fFixedWidth;
	}
}

