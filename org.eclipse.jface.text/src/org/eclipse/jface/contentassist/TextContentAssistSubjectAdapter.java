/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.contentassist;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * Adapts a <code>Text</code> to an <code>IContentAssistSubjectControl</code>.
 * 
 * @see org.eclipse.swt.widgets.Text
 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl
 * @since 3.0
 */
public class TextContentAssistSubjectAdapter extends AbstractControlContentAssistSubjectAdapter {

	/**
	 * The <code>Document</code> of <code>fCombo</code>'s contents.
	 */
	private class InternalDocument extends Document {
	
		/**
		 * Updates this <code>Document</code> with changes in <code>fText</code>.
		 */
		private ModifyListener fModifyListener;
		
		private InternalDocument() {
			super(fText.getText());
			fModifyListener= new ModifyListener() {
				/*
				 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
				 */
				public void modifyText(ModifyEvent e) {
					set(fText.getText());
				}
			};
			fText.addModifyListener(fModifyListener);
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractDocument#replace(int, int, java.lang.String)
		 */
		public void replace(int pos, int length, String text) throws BadLocationException {
			super.replace(pos, length, text);
			fText.removeModifyListener(fModifyListener);
			fText.setText(get());
			fText.addModifyListener(fModifyListener);
		}
	}

	/** The text. */
	private Text fText;
	/** The modify listeners. */
	private HashMap fModifyListeners;
	
	/**
	 * Creates a content assist subject control adapter for the given text.
	 * 
	 * @param text the text to adapt
	 */
	public TextContentAssistSubjectAdapter(Text text) {
		Assert.isNotNull(text);
		fText= text;
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return fText;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLineHeight() {
		return fText.getLineHeight();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCaretOffset() {
		return fText.getCaretPosition();
	}

	/**
	 * {@inheritDoc}
	 */
	public Point getLocationAtOffset(int offset) {
		Point caretLocation= fText.getCaretLocation();
		/*
		 * FIXME workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=52520
		 */
		caretLocation.y += 2;
		return caretLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	public Point getWidgetSelectionRange() {
		return new Point(fText.getSelection().x, Math.abs(fText.getSelection().y - fText.getSelection().x));
	}

	/**
	 * {@inheritDoc}
	 */
	public Point getSelectedRange() {
		return new Point(fText.getSelection().x, Math.abs(fText.getSelection().y - fText.getSelection().x));
	}

	/**
	 * {@inheritDoc}
	 */
	public IDocument getDocument() {
		IDocument document= (IDocument)fText.getData("document"); //$NON-NLS-1$
		if (document == null) {
			document= new InternalDocument() ;
			fText.setData("document", document); //$NON-NLS-1$
		}
		return document;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSelectedRange(int i, int j) {
		fText.setSelection(new Point(i, i+j));
	}

	/**
	 * {@inheritDoc}
	 */
	public void revealRange(int i, int j) {
		// XXX: this should be improved
		fText.setSelection(new Point(i, i+j));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addSelectionListener(final SelectionListener selectionListener) {
		fText.addSelectionListener(selectionListener);
		Listener listener= new Listener() {
			/**
			 * {@inheritDoc}
			 */
			public void handleEvent(Event e) {
				selectionListener.widgetSelected(new SelectionEvent(e));
	
			}
		};
		fText.addListener(SWT.Modify, listener); 
		fModifyListeners.put(selectionListener, listener);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeSelectionListener(SelectionListener selectionListener) {
		fText.removeSelectionListener(selectionListener);
		Object listener= fModifyListeners.get(selectionListener);
		if (listener instanceof Listener)
			fText.removeListener(SWT.Modify, (Listener)listener);
	}
}
