/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;



/**
 * Adapts a {@link org.eclipse.swt.widgets.Combo} to a {@link org.eclipse.jface.contentassist.IContentAssistSubjectControl}.
 *
 * <p>
 *	Known issues:
 *  <ul>
 *		<li>https://bugs.eclipse.org/bugs/show_bug.cgi?id=50121
 *		= &gt; Combo doesn't get Arrow_up/Down keys on GTK</li>
 *
 *		<li>https://bugs.eclipse.org/bugs/show_bug.cgi?id=50123
 *		= &gt; Combo broken on MacOS X</li>
 *  </ul>
 *	</p>
 *
 * @since 3.0
 * @deprecated As of 3.2, replaced by Platform UI's field assist support
 */
public class ComboContentAssistSubjectAdapter extends AbstractControlContentAssistSubjectAdapter {

	/**
	 * The document of {@link #fCombo}'s contents.
	 */
	private class InternalDocument extends Document {
		/**
		 * Updates this document with changes in {@link #fCombo}.
		 */
		private ModifyListener fModifyListener;

		private InternalDocument() {
			super(fCombo.getText());
			fModifyListener= new ModifyListener() {
				/*
				 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
				 */
				public void modifyText(ModifyEvent e) {
					set(fCombo.getText());
				}
			};
			fCombo.addModifyListener(fModifyListener);
		}

		/*
		 * @see org.eclipse.jface.text.AbstractDocument#replace(int, int, java.lang.String)
		 */
		public void replace(int pos, int length, String text) throws BadLocationException {
			super.replace(pos, length, text);
			fCombo.removeModifyListener(fModifyListener);
			fCombo.setText(get());
			fCombo.addModifyListener(fModifyListener);
		}
	}

	/**
	 * The combo widget.
	 */
	private Combo fCombo;
	private HashMap fModifyListeners;

	/**
	 * Creates a content assist subject control adapter for the given combo.
	 *
	 * @param combo the combo to adapt
	 */
	public ComboContentAssistSubjectAdapter(Combo combo) {
		Assert.isNotNull(combo);
		fCombo= combo;
		fModifyListeners= new HashMap();
	 }

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getControl()
	 */
	public Control getControl() {
		return fCombo;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getLineHeight()
	 */
	public int getLineHeight() {
		return fCombo.getTextHeight();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getCaretOffset()
	 */
	public int getCaretOffset() {
		return fCombo.getCaretPosition();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getLocationAtOffset(int)
	 */
	public Point getLocationAtOffset(int offset) {
		String comboString= fCombo.getText();
		GC gc = new GC(fCombo);
		gc.setFont(fCombo.getFont());
		Point extent= gc.textExtent(comboString.substring(0, Math.min(offset, comboString.length())));
		int spaceWidth= gc.textExtent(" ").x; //$NON-NLS-1$
		gc.dispose();
		/*
		 * XXX: the two space widths below is a workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=44072
		 */
		int x= 2 * spaceWidth + fCombo.getClientArea().x + fCombo.getBorderWidth() + extent.x;
		return new Point(x, fCombo.getClientArea().y);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getSelectionRange()
	 */
	public Point getWidgetSelectionRange() {
		return new Point(fCombo.getSelection().x, Math.abs(fCombo.getSelection().y - fCombo.getSelection().x));
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getSelectedRange()
	 */
	public Point getSelectedRange() {
		return new Point(fCombo.getSelection().x, Math.abs(fCombo.getSelection().y - fCombo.getSelection().x));
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getDocument()
	 */
	public IDocument getDocument() {
		IDocument document= (IDocument)fCombo.getData("document"); //$NON-NLS-1$
		if (document == null) {
			document= new InternalDocument() ;
			fCombo.setData("document", document); //$NON-NLS-1$
		}
		return document;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#setSelectedRange(int, int)
	 */
	public void setSelectedRange(int i, int j) {
		fCombo.setSelection(new Point(i, i+j));
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#revealRange(int, int)
	 */
	public void revealRange(int i, int j) {
		// XXX: this should be improved
		fCombo.setSelection(new Point(i, i+j));
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#addSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public boolean addSelectionListener(final SelectionListener selectionListener) {
		fCombo.addSelectionListener(selectionListener);
		Listener listener= new Listener() {
			/*
			 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void handleEvent(Event e) {
				selectionListener.widgetSelected(new SelectionEvent(e));

			}
		};
		fCombo.addListener(SWT.Modify, listener);
		fModifyListeners.put(selectionListener, listener);
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#removeSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener selectionListener) {
		fCombo.removeSelectionListener(selectionListener);
		Object listener= fModifyListeners.get(selectionListener);
		if (listener instanceof Listener)
			fCombo.removeListener(SWT.Modify, (Listener)listener);
	}
}
