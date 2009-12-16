/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.resource.JFaceColors;

/**
 * Contribution item for the status line.
 * @since 2.0
 */
public class StatusLineContributionItem extends ContributionItem implements IStatusField, IStatusFieldExtension {

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
	}

	/**
	 * Left and right margin used in CLabel.
	 * @since 2.1
	 */
	private static final int INDENT= 3;
	/**
	 * Default number of characters that should fit into the item.
	 * @since 3.0
	 */
	static final int DEFAULT_WIDTH_IN_CHARS= 14;
	/**
	 * Pre-computed label width hint.
	 * @since 2.1
	 */
	private int fFixedWidth= -1;
	/**
	 * Pre-computed label height hint.
	 * @since 3.0
	 */
	private int fFixedHeight= -1;
	/** The text */
	private String fText;
	/** The image */
	private Image fImage;
	/**
	 * The error text.
	 * @since 3.0
	 */
	private String fErrorText;
	/**
	 * The error image.
	 * @since 3.0
	 */
	private Image fErrorImage;
	/**
	 * The tool tip text.
	 * @since 3.0
	 */
	private String fToolTipText;
	/**
	 * Number of characters that should fit into the item.
	 * @since 3.0
	 */
	private int fWidthInChars;
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
		this(id, true, DEFAULT_WIDTH_IN_CHARS);
	}

	/**
	 * Creates a new item with the given attributes.
	 *
	 * @param id the item's id
	 * @param visible the visibility of this item
	 * @param widthInChars the width in characters
	 * @since 3.0
	 */
	public StatusLineContributionItem(String id, boolean visible, int widthInChars) {
		super(id);
		setVisible(visible);
		fWidthInChars= widthInChars;
	}

	/*
	 * @see IStatusField#setText(String)
	 */
	public void setText(String text) {
		fText= text;
		updateMessageLabel();
	}

	/*
	 * @see IStatusField#setImage(Image)
	 */
	public void setImage(Image image) {
		fImage= image;
		updateMessageLabel();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IStatusFieldExtension#setErrorText(java.lang.String)
	 * @since 3.0
	 */
	public void setErrorText(String text) {
		fErrorText= text;
		updateMessageLabel();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IStatusFieldExtension#setErrorImage(org.eclipse.swt.graphics.Image)
	 * @since 3.0
	 */
	public void setErrorImage(Image image) {
		fErrorImage= image;
		updateMessageLabel();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IStatusFieldExtension#setToolTipText(java.lang.String)
	 * @since 3.0
	 */
	public void setToolTipText(String string) {
		fToolTipText= string;
		updateMessageLabel();
	}

	/*
	 * @see IContributionItem#fill(Composite)
	 */
	public void fill(Composite parent) {

		Label sep= new Label(parent, SWT.SEPARATOR);
		fLabel= new CLabel(parent, SWT.SHADOW_NONE);

		fLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				fMouseListener= null;
			}
		});
		if (fActionHandler != null) {
			fMouseListener= new Listener();
			fLabel.addMouseListener(fMouseListener);
		}

		StatusLineLayoutData data= new StatusLineLayoutData();
		data.widthHint= getWidthHint(parent);
		fLabel.setLayoutData(data);

		data= new StatusLineLayoutData();
		data.heightHint= getHeightHint(parent);
		sep.setLayoutData(data);

		updateMessageLabel();
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
			fFixedWidth= gc.getFontMetrics().getAverageCharWidth() * fWidthInChars;
			fFixedWidth += INDENT * 2;
			gc.dispose();
		}
		return fFixedWidth;
	}

	/**
	 * Returns the height hint for this label.
	 *
	 * @param control the root control of this label
	 * @return the height hint for this label
	 * @since 3.0
	 */
	private int getHeightHint(Composite control) {
		if (fFixedHeight < 0) {
			GC gc= new GC(control);
			gc.setFont(control.getFont());
			fFixedHeight= gc.getFontMetrics().getHeight();
			gc.dispose();
		}
		return fFixedHeight;
	}

	/**
	 * Updates the message label widget.
	 *
	 * @since 3.0
	 */
	private void updateMessageLabel() {
		if (fLabel != null && !fLabel.isDisposed()) {
			Display display= fLabel.getDisplay();
			if ((fErrorText != null && fErrorText.length() > 0) || fErrorImage != null) {
				String escapedErrorText= escape(fErrorText);
				fLabel.setForeground(JFaceColors.getErrorText(display));
				fLabel.setText(escapedErrorText);
				fLabel.setImage(fErrorImage);
				if (fToolTipText != null)
					fLabel.setToolTipText(escape(fToolTipText));
				else if (fErrorText.length() > fWidthInChars)
					fLabel.setToolTipText(escapedErrorText);
				else
					fLabel.setToolTipText(null);
				
			} else {
				String escapedText= escape(fText);
				fLabel.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
				fLabel.setText(escapedText);
				fLabel.setImage(fImage);
				if (fToolTipText != null)
					fLabel.setToolTipText(escape(fToolTipText));
				else if (fText != null && fText.length() > fWidthInChars)
					fLabel.setToolTipText(escapedText);
				else
					fLabel.setToolTipText(null);
			}
		}
	}

	/**
	 * Escapes '&' with '&' in the given text.
	 *
	 * @param text the text to escape, can be <code>null</code>
	 * @return the escaped string or <code>null</code> if text was <code>null</code>
	 * @since 3.4
	 */
	private String escape(String text) {
		if (text == null)
			return text;
		return LegacyActionTools.escapeMnemonics(text);
	}

}

