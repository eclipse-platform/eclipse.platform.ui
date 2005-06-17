/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;


/**
 * A delete line target.
 * @since 2.1
 */
class DeleteLineTarget {

	/**
	 * A clipboard which concatenates subsequent delete line actions.
	 */
	private static class DeleteLineClipboard implements MouseListener, ModifyListener, ISelectionChangedListener, ITextListener, FocusListener {

		/** The text viewer. */
		private final ITextViewer fViewer;
		/*
		 * This is a hack to stop a string of deletions when the user moves
		 * the caret. This kludge is necessary since:
		 * 1) Moving the caret does not fire a selection event
		 * 2) There is no support in StyledText for a CaretListener
		 * 3) The AcceleratorScope and KeybindingService classes are internal
		 *
		 * This kludge works by comparing the offset of the caret to the offset
		 * recorded the last time the action was run. If they differ, we do not
		 * continue the session.
		 *
		 * @see #saveState
		 * @see #checkState
		 */
		/** The last known offset of the caret */
		private int fIndex= -1;
		/** The clip board. */
		private Clipboard fClipboard;
		/** A string buffer. */
		private final StringBuffer fBuffer= new StringBuffer();
		/** The delete flag indicates if a deletion is in progress. */
		private boolean fDeleting;

		/**
		 * Creates the clipboard.
		 *
		 * @param viewer the text viewer
		 */
		public DeleteLineClipboard(ITextViewer viewer) {
			Assert.isNotNull(viewer);
			fViewer= viewer;
		}

		/**
		 * Returns the text viewer.
		 *
		 * @return the text viewer
		 */
		public ITextViewer getViewer() {
			return fViewer;
		}

		/**
		 * Saves the current state, to be compared later using
		 * <code>checkState</code>.
		 */
		private void saveState() {
			fIndex= fViewer.getTextWidget().getCaretOffset();
		}

		/**
		 * Checks that the state has not changed since it was saved.
		 *
		 * @return returns <code>true</code> if the current state is the same as
		 * when it was last saved.
		 */
		private boolean hasSameState() {
			return fIndex == fViewer.getTextWidget().getCaretOffset();
		}

		/**
		 * Checks the state of the clipboard.
		 */
		public void checkState() {

			if (fClipboard == null) {
				StyledText text= fViewer.getTextWidget();
				if (text == null)
					return;

				fViewer.getSelectionProvider().addSelectionChangedListener(this);
				text.addFocusListener(this);
				text.addMouseListener(this);
				text.addModifyListener(this);

				fClipboard= new Clipboard(text.getDisplay());
				fBuffer.setLength(0);

			} else if (!hasSameState()) {
				fBuffer.setLength(0);
			}
		}

		/**
		 * Appends the given string to this clipboard.
		 *
		 * @param deltaString the string to append
		 */
		public void append(String deltaString) {
			fBuffer.append(deltaString);
			String string= fBuffer.toString();
			Transfer[] dataTypes= new Transfer[] { TextTransfer.getInstance() };
			Object[] data= new Object[] { string };
			fClipboard.setContents(data, dataTypes);
		}

		/**
		 * Uninstalls this action.
		 */
		private void uninstall() {

			if (fClipboard == null)
				return;

			StyledText text= fViewer.getTextWidget();
			if (text == null)
				return;

			fViewer.getSelectionProvider().removeSelectionChangedListener(this);
			text.removeFocusListener(this);
			text.removeMouseListener(this);
			text.removeModifyListener(this);

			fClipboard.dispose();
			fClipboard= null;
		}

		/**
		 * Mark whether a deletion is in progress.
		 *
		 * @param deleting <code>true</code> if a deletion is in progress
		 */
		public void setDeleting(boolean deleting) {
			fDeleting= deleting;
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			uninstall();
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
			uninstall();
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
			uninstall();
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			uninstall();
		}

		/*
		 * @see org.eclipse.swt.events.FocusListener#focusGained(FocusEvent)
		 */
		public void focusGained(FocusEvent e) {
			uninstall();
		}

		/*
		 * @see org.eclipse.swt.events.FocusListener#focusLost(FocusEvent)
		 */
		public void focusLost(FocusEvent e) {
			uninstall();
		}

		/*
		 * @see org.eclipse.jface.text.ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {
			uninstall();
		}

		/*
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			if (!fDeleting)
				uninstall();
		}
	}

	/**
	 * The clipboard manager.
	 */
	private final DeleteLineClipboard fClipboard;

	/**
	 * Creates a new target.
	 *
	 * @param viewer the viewer that the new target operates on
	 */
	public DeleteLineTarget(ITextViewer viewer) {
		fClipboard= new DeleteLineClipboard(viewer);
	}

	/**
	 * Returns the document's delete region specified by position and type.
	 *
	 * @param document	the document
	 * @param position	the position
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 * @return the document's delete region
	 * @throws BadLocationException
	 */
	private static IRegion getDeleteRegion(IDocument document, int position, int type) throws BadLocationException {

		int line= document.getLineOfOffset(position);
		int offset= 0;
		int length= 0;

		switch  (type) {
		case DeleteLineAction.WHOLE:
			offset= document.getLineOffset(line);
			length= document.getLineLength(line);
			break;

		case DeleteLineAction.TO_BEGINNING:
			offset= document.getLineOffset(line);
			length= position - offset;
			break;

		case DeleteLineAction.TO_END:
			offset= position;

			IRegion lineRegion= document.getLineInformation(line);
			int end= lineRegion.getOffset() + lineRegion.getLength();

			if (position == end) {
				String lineDelimiter= document.getLineDelimiter(line);
				length= lineDelimiter == null ? 0 : lineDelimiter.length();

			} else {
				length= end - offset;
			}
			break;

		default:
			throw new IllegalArgumentException();
		}

		return new Region(offset, length);
	}

	/**
	 * Deletes the specified fraction of the line of the given offset.
	 *
	 * @param document the document
	 * @param position the offset
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 * @param copyToClipboard <code>true</code> if the deleted line should be copied to the clipboard
	 * @throws BadLocationException if position is not valid in the given document
	 */
	public void deleteLine(IDocument document, int position, int type, boolean copyToClipboard) throws BadLocationException {

		IRegion deleteRegion= getDeleteRegion(document, position, type);
		int offset= deleteRegion.getOffset();
		int length= deleteRegion.getLength();

		if (length == 0)
			return;

		if (copyToClipboard) {

			fClipboard.checkState();
			try {
				fClipboard.append(document.get(offset, length));
			} catch (SWTError e) {
				if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
					throw e;
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59459
				// don't delete if copy to clipboard fails, rather log & abort

				// log
				Status status= new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, e.code, EditorMessages.Editor_error_clipboard_copy_failed_message, e);
				TextEditorPlugin.getDefault().getLog().log(status);

				fClipboard.uninstall();
				return; // don't delete
			}

			fClipboard.setDeleting(true);
			document.replace(offset, length, null);
			fClipboard.setDeleting(false);

			fClipboard.saveState();

		} else {
			document.replace(offset, length, null);
		}
	}
}
