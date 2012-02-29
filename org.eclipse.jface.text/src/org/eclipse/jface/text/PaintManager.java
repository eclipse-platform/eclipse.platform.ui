/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;


/**
 * Manages the {@link org.eclipse.jface.text.IPainter} object registered with an
 * {@link org.eclipse.jface.text.ITextViewer}.
 * <p>
 * Clients usually instantiate and configure objects of this type.</p>
 *
 * @since 2.1
 */
public final class PaintManager implements KeyListener, MouseListener, ISelectionChangedListener, ITextListener, ITextInputListener {

	/**
	 * Position updater used by the position manager. This position updater differs from the default position
	 * updater in that it extends a position when an insertion happens at the position's offset and right behind
	 * the position.
	 */
	static class PaintPositionUpdater extends DefaultPositionUpdater {

		/**
		 * Creates the position updater for the given category.
		 *
		 * @param category the position category
		 */
		protected PaintPositionUpdater(String category) {
			super(category);
		}

		/**
		 * If an insertion happens at a position's offset, the
		 * position is extended rather than shifted. Also, if something is added
		 * right behind the end of the position, the position is extended rather
		 * than kept stable.
		 */
		protected void adaptToInsert() {

			int myStart= fPosition.offset;
			int myEnd=   fPosition.offset + fPosition.length;
			myEnd= Math.max(myStart, myEnd);

			int yoursStart= fOffset;
			int yoursEnd=   fOffset + fReplaceLength;// - 1;
			yoursEnd= Math.max(yoursStart, yoursEnd);

			if (myEnd < yoursStart)
				return;

			if (myStart <= yoursStart)
				fPosition.length += fReplaceLength;
			else
				fPosition.offset += fReplaceLength;
		}
	}

	/**
	 * The paint position manager used by this paint manager. The paint position
	 * manager is installed on a single document and control the creation/disposed
	 * and updating of a position category that will be used for managing positions.
	 */
	static class PositionManager implements IPaintPositionManager {

//		/** The document this position manager works on */
		private IDocument fDocument;
		/** The position updater used for the managing position category */
		private IPositionUpdater fPositionUpdater;
		/** The managing position category */
		private String fCategory;

		/**
		 * Creates a new position manager. Initializes the managing
		 * position category using its class name and its hash value.
		 */
		public PositionManager() {
			fCategory= getClass().getName() + hashCode();
			fPositionUpdater= new PaintPositionUpdater(fCategory);
		}

		/**
		 * Installs this position manager in the given document. The position manager stays
		 * active until <code>uninstall</code> or <code>dispose</code>
		 * is called.
		 *
		 * @param document the document to be installed on
		 */
		public void install(IDocument document) {
			fDocument= document;
			fDocument.addPositionCategory(fCategory);
			fDocument.addPositionUpdater(fPositionUpdater);
		}

		/**
		 * Disposes this position manager. The position manager is automatically
		 * removed from the document it has previously been installed
		 * on.
		 */
		public void dispose() {
			uninstall(fDocument);
		}

		/**
		 * Uninstalls this position manager form the given document. If the position
		 * manager has no been installed on this document, this method is without effect.
		 *
		 * @param document the document form which to uninstall
		 */
		public void uninstall(IDocument document) {
			if (document == fDocument && document != null) {
				try {
					fDocument.removePositionUpdater(fPositionUpdater);
					fDocument.removePositionCategory(fCategory);
				} catch (BadPositionCategoryException x) {
					// should not happen
				}
				fDocument= null;
			}
		}

		/*
		 * @see IPositionManager#addManagedPosition(Position)
		 */
		public void managePosition(Position position) {
			try {
				fDocument.addPosition(fCategory, position);
			} catch (BadPositionCategoryException x) {
				// should not happen
			} catch (BadLocationException x) {
				// should not happen
			}
		}

		/*
		 * @see IPositionManager#removeManagedPosition(Position)
		 */
		public void unmanagePosition(Position position) {
			try {
				fDocument.removePosition(fCategory, position);
			} catch (BadPositionCategoryException x) {
				// should not happen
			}
		}
	}


	/** The painters managed by this paint manager. */
	private List fPainters= new ArrayList(2);
	/** The position manager used by this paint manager */
	private PositionManager fManager;
	/** The associated text viewer */
	private ITextViewer fTextViewer;

	/**
	 * Creates a new paint manager for the given text viewer.
	 *
	 * @param textViewer the text viewer associated to this newly created paint manager
	 */
	public PaintManager(ITextViewer textViewer) {
		fTextViewer= textViewer;
	}


	/**
	 * Adds the given painter to the list of painters managed by this paint manager.
	 * If the painter is already registered with this paint manager, this method is
	 * without effect.
	 *
	 * @param painter the painter to be added
	 */
	public void addPainter(IPainter painter) {
		if (!fPainters.contains(painter)) {
			fPainters.add(painter);
			if (fPainters.size() == 1)
				install();
			painter.setPositionManager(fManager);
			painter.paint(IPainter.INTERNAL);
		}
	}

	/**
	 * Removes the given painter from the list of painters managed by this
	 * paint manager. If the painter has not previously been added to this
	 * paint manager, this method is without effect.
	 *
	 * @param painter the painter to be removed
	 */
	public void removePainter(IPainter painter) {
		if (fPainters.remove(painter)) {
			painter.deactivate(true);
			painter.setPositionManager(null);
		}
		if (fPainters.size() == 0)
			dispose();
	}

	/**
	 * Installs/activates this paint manager. Is called as soon as the
	 * first painter is to be managed by this paint manager.
	 */
	private void install() {

		fManager= new PositionManager();
		if (fTextViewer.getDocument() != null) {
			fManager.install(fTextViewer.getDocument());
			addListeners();
		}

		fTextViewer.addTextInputListener(this);

	}

	/**
	 * Installs our listener set on the text viewer and the text widget,
	 * respectively.
	 */
	private void addListeners() {
		ISelectionProvider provider= fTextViewer.getSelectionProvider();
		provider.addSelectionChangedListener(this);

		fTextViewer.addTextListener(this);

		StyledText text= fTextViewer.getTextWidget();
		text.addKeyListener(this);
		text.addMouseListener(this);
	}

	/**
	 * Disposes this paint manager. The paint manager uninstalls itself
	 * and clears all registered painters. This method is also called when the
	 * last painter is removed from the list of managed painters.
	 */
	public void dispose() {

		if (fManager != null) {
			fManager.dispose();
			fManager= null;
		}

		for (Iterator e = fPainters.iterator(); e.hasNext();)
			((IPainter) e.next()).dispose();
		fPainters.clear();

		fTextViewer.removeTextInputListener(this);

		removeListeners();
	}

	/**
	 * Removes our set of listeners from the text viewer and widget,
	 * respectively.
	 */
	private void removeListeners() {
		ISelectionProvider provider= fTextViewer.getSelectionProvider();
		if (provider != null)
			provider.removeSelectionChangedListener(this);

		fTextViewer.removeTextListener(this);

		StyledText text= fTextViewer.getTextWidget();
		if (text != null && !text.isDisposed()) {
			text.removeKeyListener(this);
			text.removeMouseListener(this);
		}
	}

	/**
	 * Triggers all registered painters for the given reason.
	 *
	 * @param reason the reason
	 * @see IPainter
	 */
	private void paint(int reason) {
		for (Iterator e = fPainters.iterator(); e.hasNext();)
			((IPainter) e.next()).paint(reason);
	}

	/*
	 * @see KeyListener#keyPressed(KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		paint(IPainter.KEY_STROKE);
	}

	/*
	 * @see KeyListener#keyReleased(KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * @see MouseListener#mouseDoubleClick(MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
	}

	/*
	 * @see MouseListener#mouseDown(MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		paint(IPainter.MOUSE_BUTTON);
	}

	/*
	 * @see MouseListener#mouseUp(MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
	}

	/*
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		paint(IPainter.SELECTION);
	}

	/*
	 * @see ITextListener#textChanged(TextEvent)
	 */
	public void textChanged(TextEvent event) {

		if (!event.getViewerRedrawState())
			return;

		Control control= fTextViewer.getTextWidget();
		if (control != null) {
			control.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (fTextViewer != null)
						paint(IPainter.TEXT_CHANGE);
				}
			});
		}
	}

	/*
	 * @see ITextInputListener#inputDocumentAboutToBeChanged(IDocument, IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput != null) {
			for (Iterator e= fPainters.iterator(); e.hasNext();)
				((IPainter)e.next()).deactivate(false);
			fManager.uninstall(oldInput);
			removeListeners();
		}
	}

	/*
	 * @see ITextInputListener#inputDocumentChanged(IDocument, IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput != null && newInput != fManager.fDocument) {
			fManager.install(newInput);
			paint(IPainter.TEXT_CHANGE);
			addListeners();
		}
	}
}

