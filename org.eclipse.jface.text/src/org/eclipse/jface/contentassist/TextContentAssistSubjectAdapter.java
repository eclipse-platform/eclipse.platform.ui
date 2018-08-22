/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


/**
 * Adapts a {@link org.eclipse.swt.widgets.Text} to an {@link org.eclipse.jface.contentassist.IContentAssistSubjectControl}.
 *
 * @see org.eclipse.swt.widgets.Text
 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl
 * @since 3.0
 * @deprecated As of 3.2, replaced by Platform UI's field assist support
 */
@Deprecated
public class TextContentAssistSubjectAdapter extends AbstractControlContentAssistSubjectAdapter {

	/**
	 * The document backing this adapter's text widget.
	 */
	private class InternalDocument extends Document {

		/**
		 * Updates this document with changes in this adapter's text widget.
		 */
		private ModifyListener fModifyListener;

		private InternalDocument() {
			super(fText.getText());
			fModifyListener= new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					set(fText.getText());
				}
			};
			fText.addModifyListener(fModifyListener);
		}

		@Override
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
	private HashMap<SelectionListener, Listener> fModifyListeners= new HashMap<>();

	/**
	 * Creates a content assist subject control adapter for the given text widget.
	 *
	 * @param text the text widget to adapt
	 */
	public TextContentAssistSubjectAdapter(Text text) {
		Assert.isNotNull(text);
		fText= text;
	}

	@Override
	public Control getControl() {
		return fText;
	}

	@Override
	public int getLineHeight() {
		return fText.getLineHeight();
	}

	@Override
	public int getCaretOffset() {
		return fText.getCaretPosition();
	}

	@Override
	public Point getLocationAtOffset(int offset) {
		Point caretLocation= fText.getCaretLocation();
		/*
		 * XXX: workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=52520
		 */
		caretLocation.y += 2;
		return caretLocation;
	}

	@Override
	public Point getWidgetSelectionRange() {
		return new Point(fText.getSelection().x, Math.abs(fText.getSelection().y - fText.getSelection().x));
	}

	@Override
	public Point getSelectedRange() {
		return new Point(fText.getSelection().x, Math.abs(fText.getSelection().y - fText.getSelection().x));
	}

	@Override
	public IDocument getDocument() {
		IDocument document= (IDocument)fText.getData("document"); //$NON-NLS-1$
		if (document == null) {
			document= new InternalDocument() ;
			fText.setData("document", document); //$NON-NLS-1$
		}
		return document;
	}

	@Override
	public void setSelectedRange(int i, int j) {
		fText.setSelection(new Point(i, i+j));
	}

	@Override
	public void revealRange(int i, int j) {
		// XXX: this should be improved
		fText.setSelection(new Point(i, i+j));
	}

	@Override
	public boolean addSelectionListener(final SelectionListener selectionListener) {
		fText.addSelectionListener(selectionListener);
		Listener listener= new Listener() {
			@Override
			public void handleEvent(Event e) {
				selectionListener.widgetSelected(new SelectionEvent(e));

			}
		};
		fText.addListener(SWT.Modify, listener);
		fModifyListeners.put(selectionListener, listener);
		return true;
	}

	@Override
	public void removeSelectionListener(SelectionListener selectionListener) {
		fText.removeSelectionListener(selectionListener);
		Listener listener= fModifyListeners.get(selectionListener);
		if (listener != null)
			fText.removeListener(SWT.Modify, listener);
	}
}
