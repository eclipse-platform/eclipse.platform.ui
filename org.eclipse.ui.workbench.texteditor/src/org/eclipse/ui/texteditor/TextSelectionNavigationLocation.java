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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;

import org.eclipse.ui.internal.IWorkbenchConstants;


/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public class TextSelectionNavigationLocation extends NavigationLocation {
	
	
	private final static String DELETED= "deleted"; //$NON-NLS-1$
	private final static String NOT_DELETED= "not_deleted"; //$NON-NLS-1$
	
	private final static String CATEGORY= "__navigation_" + TextSelectionNavigationLocation.class.hashCode(); //$NON-NLS-1$
	private static IPositionUpdater fgPositionUpdater= new DefaultPositionUpdater(CATEGORY);
	
	private Position fPosition;
	private IDocument fDocument;
	private Position fSavedPosition;
	
	
	/**
	 * @since 2.1
	 */
	public TextSelectionNavigationLocation(ITextEditor part, boolean initialize) {
		super(part);
		
		if (initialize) {
				
			ISelection s= part.getSelectionProvider().getSelection();		
			if(s == null || s.isEmpty())
				return;
			
			ITextSelection selection= (ITextSelection) s;
			if(selection.getOffset() == 0 && selection.getLength() == 0)
				return;
			
			IDocument document= getDocument(part);
			Position position= new Position(selection.getOffset(), selection.getLength());
			if (installOnDocument(document, position)) {
				fDocument= document;
				fPosition= position;
				if (!part.isDirty())
					fSavedPosition= new Position(fPosition.offset, fPosition.length);
			}
		}
	}
	
	private IDocument getDocument(ITextEditor part) {
		IDocumentProvider provider= part.getDocumentProvider();
		return provider.getDocument(part.getEditorInput());
	}
	
	private boolean  installOnDocument(IDocument document, Position position) {
		
		if (document != null && position != null) {
			
			if (!document.containsPositionCategory(CATEGORY)) {
				document.addPositionCategory(CATEGORY);
				document.addPositionUpdater(fgPositionUpdater);
			}
				
			try {
				document.addPosition(CATEGORY, position);
				return true;
			} catch (BadLocationException e) {
			} catch (BadPositionCategoryException e) {
			}
		}
		
		return false;
	}
	
	private boolean uninstallFromDocument(IDocument document, Position position) {
		
		if (document != null && position != null) {
			try {
				
				document.removePosition(CATEGORY, position);
				
				Position[] category= document.getPositions(CATEGORY);
				if (category == null || category.length == 0) {
					document.removePositionCategory(CATEGORY);
					document.removePositionUpdater(fgPositionUpdater);
				}
				return true;
				
			} catch (BadPositionCategoryException e) {
			}
		}
		
		return false;
	}
	
	public String toString() {
		return "Selection<" + fPosition + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @since 2.1
	 */
	private boolean equalsLocationOf(ITextEditor part) {
		
		if (fPosition == null)
			return true;
			
		if (fPosition.isDeleted)
			return false;
			
		ISelectionProvider provider= part.getSite().getSelectionProvider();
		ISelection selection= provider.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection) selection;
			if (textSelection.getOffset() == fPosition.offset && textSelection.getLength() == fPosition.length) {
				String text= textSelection.getText();
				if (text != null) {
					try {
						return text.equals(fDocument.get(fPosition.offset, fPosition.length));
					} catch (BadLocationException e) {
					}
				}
			}
		}
		
		return false;
	}
	
	public void dispose() {
		uninstallFromDocument(fDocument, fPosition);
		fDocument= null;
		fPosition= null;
		fSavedPosition= null;
		super.dispose();
	}

	/**
	 * @since 2.1
	 */
	public void releaseState() {
		// deactivate
		uninstallFromDocument(fDocument, fPosition);		
		fDocument= null;
		fPosition= null;
		fSavedPosition= null;
		super.releaseState();
	}
	
	/**
	 * @since 2.1
	 */
	public boolean mergeInto(INavigationLocation location) {
		
		if (location == null)
			return false;
			
		if (getClass() != location.getClass())
			return false;
			
		if (fPosition == null || fPosition.isDeleted)
			return true;
		
		TextSelectionNavigationLocation s= (TextSelectionNavigationLocation) location;
		if (s.fPosition == null || s.fPosition.isDeleted) {
			uninstallFromDocument(fDocument, fPosition);
			s.fDocument= fDocument;
			s. fPosition= fPosition;
			s.fSavedPosition= fSavedPosition;
			return true;
		}
		
		return s.fDocument == fDocument && s.fPosition.equals(fPosition);
	}
	
	/**
	 * @since 2.1
	 */
	public void restoreLocation() {
		if (fPosition == null || fPosition.isDeleted)
			return;
			
		IEditorPart part= getEditorPart();
		if (part instanceof ITextEditor) {
			ITextEditor editor= (ITextEditor) getEditorPart();
			editor.selectAndReveal(fPosition.offset, fPosition.length);
		}
	}
	
	public void restoreState(IMemento memento) {
		
		IEditorPart part= getEditorPart();
		if (part instanceof ITextEditor) {
			
			// restore
			fDocument= getDocument((ITextEditor) part);
			
			Integer offset= memento.getInteger(IWorkbenchConstants.TAG_X);
			Integer length= memento.getInteger(IWorkbenchConstants.TAG_Y);
			String deleted= memento.getString(IWorkbenchConstants.TAG_INFO);
			
			if (offset != null && length != null) {
				Position p= new Position(offset.intValue(), length.intValue());
				if (deleted != null)
					p.isDeleted= DELETED.equals(deleted) ? true : false;
				
				// activate
				if (installOnDocument(fDocument, p)) {
					fPosition= p;
					if (!part.isDirty())
						fSavedPosition= new Position(fPosition.offset, fPosition.length);
				}
			}
		}
	}
	
	public void saveState(IMemento memento) {
		if (fSavedPosition != null) {
			memento.putInteger(IWorkbenchConstants.TAG_X, fSavedPosition.offset);
			memento.putInteger(IWorkbenchConstants.TAG_Y, fSavedPosition.length);
			memento.putString(IWorkbenchConstants.TAG_INFO, (fSavedPosition.isDeleted ? DELETED : NOT_DELETED));
		}
	}
	
	public void partSaved(IEditorPart part) {
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=25440
		if (fPosition == null || fPosition.isDeleted())
			fSavedPosition= null;
		else
			fSavedPosition= new Position(fPosition.offset, fPosition.length);
	}
	
	/**
	 * @since 2.1
	 */
	public void update() {
		IEditorPart part= getEditorPart();
		if (part instanceof ITextEditor) {
			ITextEditor textEditor= (ITextEditor) getEditorPart();
			
			if(equalsLocationOf(textEditor))
				return;
				
			ISelection s= textEditor.getSelectionProvider().getSelection();
			if(s == null || s.isEmpty())
				return;
			
			ITextSelection selection= (ITextSelection) s;
			if(selection.getOffset() == 0 && selection.getLength() == 0)
				return;
	
			fPosition.offset= selection.getOffset();
			fPosition.length= selection.getLength();
			fPosition.isDeleted= false;
								
			if (!part.isDirty())
				fSavedPosition= new Position(fPosition.offset, fPosition.length);
		}
	}
}
