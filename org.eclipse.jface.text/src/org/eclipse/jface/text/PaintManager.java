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
 * Manages the painters attached to a source viewer.
 */
public final class PaintManager implements KeyListener, MouseListener, ISelectionChangedListener, ITextListener, ITextInputListener {		
					

	static class PaintPositionUpdater extends DefaultPositionUpdater {
		
		/**
		 * Creates the position updater.
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
	};

	static class PositionManager implements IPaintPositionManager {
		
		private IDocument fDocument;
		private IPositionUpdater fPositionUpdater;
		private String fCategory;
		
		public PositionManager() {
			fCategory= getClass().getName() + hashCode();
			fPositionUpdater= new PaintPositionUpdater(fCategory);
		}

		public void install(IDocument document) {
			fDocument= document;
			fDocument.addPositionCategory(fCategory);
			fDocument.addPositionUpdater(fPositionUpdater);
		}
		
		public void dispose() {
			uninstall(fDocument);
		}
		
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
	};
	
	
	private List fPainters= new ArrayList(2);
	private PositionManager fManager;
	private ITextViewer fTextViewer;
	
	
	public PaintManager(ITextViewer textViewer) {
		fTextViewer= textViewer;
	}
	
	public void addPainter(IPainter painter) {
		if (!fPainters.contains(painter)) {
			fPainters.add(painter);
			if (fPainters.size() == 1)
				install();
			painter.setPositionManager(fManager);
			painter.paint(IPainter.INTERNAL);
		}
	}
	
	public void removePainter(IPainter painter) {
		if (fPainters.remove(painter))
			painter.setPositionManager(null);
		if (fPainters.size() == 0)
			dispose();
	}
	
	private void install() {
		
		fManager= new PositionManager();
		if (fTextViewer.getDocument() != null)
			fManager.install(fTextViewer.getDocument());
		
		fTextViewer.addTextInputListener(this);
		
		ISelectionProvider provider= fTextViewer.getSelectionProvider();
		provider.addSelectionChangedListener(this);
		
		fTextViewer.addTextListener(this);
		
		StyledText text= fTextViewer.getTextWidget();
		text.addKeyListener(this);
		text.addMouseListener(this);
	}
	
	public void dispose() {
		
		if (fManager != null) {
			fManager.dispose();
			fManager= null;
		}
		
		for (Iterator e = fPainters.iterator(); e.hasNext();)
			((IPainter) e.next()).dispose();	
		fPainters.clear();
		
		fTextViewer.removeTextInputListener(this);
		
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
			for (Iterator e = fPainters.iterator(); e.hasNext();)
				((IPainter) e.next()).deactivate(false);				
			fManager.uninstall(oldInput);
		}
	}
	
	/*
	 * @see ITextInputListener#inputDocumentChanged(IDocument, IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput != null) {
			fManager.install(newInput);
			paint(IPainter.TEXT_CHANGE);
		}
	}
}

