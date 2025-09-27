/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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

package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;

import org.eclipse.text.undo.DocumentUndoEvent;
import org.eclipse.text.undo.DocumentUndoManager;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoListener;
import org.eclipse.text.undo.IDocumentUndoManager;

import org.eclipse.jface.dialogs.MessageDialog;


/**
 * Implementation of {@link org.eclipse.jface.text.IUndoManager} using the shared
 * document undo manager.
 * <p>
 * It registers with the connected text viewer as text input listener, and obtains
 * its undo manager from the current document.  It also monitors mouse and keyboard
 * activities in order to partition the stream of text changes into undo-able
 * edit commands.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @see ITextViewer
 * @see ITextInputListener
 * @see IDocumentUndoManager
 * @see MouseListener
 * @see KeyListener
 * @see DocumentUndoManager
 *
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TextViewerUndoManager implements IUndoManager, IUndoManagerExtension {


	/**
	 * Internal listener to mouse and key events.
	 */
	private class KeyAndMouseListener implements MouseListener, KeyListener {

		/*
		 * @see MouseListener#mouseDoubleClick
		 */
		@Override
		public void mouseDoubleClick(MouseEvent e) {
		}

		/*
		 * If the right mouse button is pressed, the current editing command is closed
		 * @see MouseListener#mouseDown
		 */
		@Override
		public void mouseDown(MouseEvent e) {
			if (e.button == 1)
				if (isConnected())
					fDocumentUndoManager.commit();
		}

		/*
		 * @see MouseListener#mouseUp
		 */
		@Override
		public void mouseUp(MouseEvent e) {
		}

		/*
		 * @see KeyListener#keyPressed
		 */
		@Override
		public void keyReleased(KeyEvent e) {
		}

		/*
		 * On cursor keys, the current editing command is closed
		 * @see KeyListener#keyPressed
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
					if (isConnected()) {
						fDocumentUndoManager.commit();
					}
					break;
			}
		}
	}


	/**
	 * Internal text input listener.
	 */
	private class TextInputListener implements ITextInputListener {

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			disconnectDocumentUndoManager();
		}

		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			connectDocumentUndoManager(newInput);
		}
	}


	/**
	 * Internal document undo listener.
	 */
	private class DocumentUndoListener implements IDocumentUndoListener {

		@Override
		public void documentUndoNotification(DocumentUndoEvent event ){
			if (!isConnected()) return;

			int eventType= event.getEventType();
			if (((eventType & DocumentUndoEvent.ABOUT_TO_UNDO) != 0) || ((eventType & DocumentUndoEvent.ABOUT_TO_REDO) != 0))  {
				if (event.isCompound()) {
					ITextViewerExtension extension= null;
					if (fTextViewer instanceof ITextViewerExtension)
						extension= (ITextViewerExtension) fTextViewer;

					if (extension != null)
						extension.setRedraw(false);
				}
				fTextViewer.getTextWidget().getDisplay().syncExec(() -> {
					if (fTextViewer instanceof TextViewer)
						((TextViewer) fTextViewer).ignoreAutoEditStrategies(true);
				});

			} else if (((eventType & DocumentUndoEvent.UNDONE) != 0) || ((eventType & DocumentUndoEvent.REDONE) != 0))  {
				fTextViewer.getTextWidget().getDisplay().syncExec(() -> {
					if (fTextViewer instanceof TextViewer)
						((TextViewer) fTextViewer).ignoreAutoEditStrategies(false);
				});
				if (event.isCompound()) {
					ITextViewerExtension extension= null;
					if (fTextViewer instanceof ITextViewerExtension)
						extension= (ITextViewerExtension) fTextViewer;

					if (extension != null)
						extension.setRedraw(true);
				}

				// Reveal the change if this manager's viewer has the focus.
				if (fTextViewer != null) {
					StyledText widget= fTextViewer.getTextWidget();
					if (widget != null && !widget.isDisposed() && (widget.isFocusControl()) && (widget.getSelectionRanges().length <= 2))// || fTextViewer.getTextWidget() == control))
						selectAndReveal(event.getOffset(), event.getText() == null ? 0 : event.getText().length());
				}
			}
		}

	}

	/** The internal key and mouse event listener */
	private KeyAndMouseListener fKeyAndMouseListener;
	/** The internal text input listener */
	private TextInputListener fTextInputListener;


	/** The text viewer the undo manager is connected to */
	private ITextViewer fTextViewer;

	/** The undo level */
	private int fUndoLevel;

	/** The document undo manager that is active. */
	private IDocumentUndoManager fDocumentUndoManager;

	/** The document that is active. */
	private IDocument fDocument;

	/** The document undo listener */
	private IDocumentUndoListener fDocumentUndoListener;

	/**
	 * Creates a new undo manager who remembers the specified number of edit commands.
	 *
	 * @param undoLevel the length of this manager's history
	 */
	public TextViewerUndoManager(int undoLevel) {
		fUndoLevel= undoLevel;
	}

	/**
	 * Returns whether this undo manager is connected to a text viewer.
	 *
	 * @return <code>true</code> if connected, <code>false</code> otherwise
	 */
	private boolean isConnected() {
		return fTextViewer != null && fDocumentUndoManager != null;
	}

	/*
	 * @see IUndoManager#beginCompoundChange
	 */
	@Override
	public void beginCompoundChange() {
		if (isConnected()) {
			fDocumentUndoManager.beginCompoundChange();
		}
	}


	/*
	 * @see IUndoManager#endCompoundChange
	 */
	@Override
	public void endCompoundChange() {
		if (isConnected()) {
			fDocumentUndoManager.endCompoundChange();
		}
	}

	/**
	 * Registers all necessary listeners with the text viewer.
	 */
	private void addListeners() {
		StyledText text= fTextViewer.getTextWidget();
		if (text != null) {
			fKeyAndMouseListener= new KeyAndMouseListener();
			text.addMouseListener(fKeyAndMouseListener);
			text.addKeyListener(fKeyAndMouseListener);
			fTextInputListener= new TextInputListener();
			fTextViewer.addTextInputListener(fTextInputListener);
		}
	}

	/**
	 * Unregister all previously installed listeners from the text viewer.
	 */
	private void removeListeners() {
		StyledText text= fTextViewer.getTextWidget();
		if (text != null) {
			if (fKeyAndMouseListener != null) {
				text.removeMouseListener(fKeyAndMouseListener);
				text.removeKeyListener(fKeyAndMouseListener);
				fKeyAndMouseListener= null;
			}
			if (fTextInputListener != null) {
				fTextViewer.removeTextInputListener(fTextInputListener);
				fTextInputListener= null;
			}
		}
	}

	/**
	 * Shows the given exception in an error dialog.
	 *
	 * @param title the dialog title
	 * @param ex the exception
	 */
	private void openErrorDialog(final String title, final Exception ex) {
		Shell shell= null;
		if (isConnected()) {
			StyledText st= fTextViewer.getTextWidget();
			if (st != null && !st.isDisposed())
				shell= st.getShell();
		}
		if (Display.getCurrent() != null)
			MessageDialog.openError(shell, title, ex.getLocalizedMessage());
		else {
			Display display;
			final Shell finalShell= shell;
			if (finalShell != null)
				display= finalShell.getDisplay();
			else
				display= Display.getDefault();
			display.syncExec(() -> MessageDialog.openError(finalShell, title, ex.getLocalizedMessage()));
		}
	}

	@Override
	public void setMaximalUndoLevel(int undoLevel) {
		fUndoLevel= Math.max(0, undoLevel);
		if (isConnected()) {
			fDocumentUndoManager.setMaximalUndoLevel(fUndoLevel);
		}
	}

	@Override
	public void connect(ITextViewer textViewer) {
		if (fTextViewer == null && textViewer != null) {
			fTextViewer= textViewer;
			addListeners();
		}
		IDocument doc= fTextViewer.getDocument();
		connectDocumentUndoManager(doc);
	}

	@Override
	public void disconnect() {
		if (fTextViewer != null) {
			removeListeners();
			fTextViewer= null;
		}
		disconnectDocumentUndoManager();
	}

	@Override
	public void reset() {
		if (isConnected())
			fDocumentUndoManager.reset();

	}

	@Override
	public boolean redoable() {
		if (isConnected())
			return fDocumentUndoManager.redoable();
		return false;
	}

	@Override
	public boolean undoable() {
		if (isConnected())
			return fDocumentUndoManager.undoable();
		return false;
	}

	@Override
	public void redo() {
		if (isConnected()) {
			try {
				fDocumentUndoManager.redo();
			} catch (ExecutionException ex) {
				openErrorDialog(JFaceTextMessages.getString("DefaultUndoManager.error.redoFailed.title"), ex); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void undo() {
		if (isConnected()) {
			try {
				fDocumentUndoManager.undo();
			} catch (ExecutionException ex) {
				openErrorDialog(JFaceTextMessages.getString("DefaultUndoManager.error.undoFailed.title"), ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Selects and reveals the specified range.
	 *
	 * @param offset the offset of the range
	 * @param length the length of the range
	 */
	private void selectAndReveal(int offset, int length) {
		if (fTextViewer instanceof ITextViewerExtension5 extension) {
			extension.exposeModelRange(new Region(offset, length));
		} else if (!fTextViewer.overlapsWithVisibleRegion(offset, length))
			fTextViewer.resetVisibleRegion();

		fTextViewer.setSelectedRange(offset, length);
		fTextViewer.revealRange(offset, length);
	}

	@Override
	public IUndoContext getUndoContext() {
		if (isConnected()) {
			return fDocumentUndoManager.getUndoContext();
		}
		return null;
	}

	private void connectDocumentUndoManager(IDocument document) {
		disconnectDocumentUndoManager();
		if (document != null) {
			fDocument= document;
			DocumentUndoManagerRegistry.connect(fDocument);
			fDocumentUndoManager= DocumentUndoManagerRegistry.getDocumentUndoManager(fDocument);
			fDocumentUndoManager.connect(this);
			setMaximalUndoLevel(fUndoLevel);
			fDocumentUndoListener= new DocumentUndoListener();
			fDocumentUndoManager.addDocumentUndoListener(fDocumentUndoListener);
		}
	}

	private void disconnectDocumentUndoManager() {
		if (fDocumentUndoManager != null) {
			fDocumentUndoManager.disconnect(this);
			DocumentUndoManagerRegistry.disconnect(fDocument);
			fDocumentUndoManager.removeDocumentUndoListener(fDocumentUndoListener);
			fDocumentUndoListener= null;
			fDocumentUndoManager= null;
		}
	}
}
