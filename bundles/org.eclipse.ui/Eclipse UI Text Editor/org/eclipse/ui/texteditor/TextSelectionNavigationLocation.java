/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

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
import org.eclipse.ui.NavigationLocation;
import org.eclipse.ui.internal.IWorkbenchConstants;


/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public class TextSelectionNavigationLocation extends NavigationLocation {
	
	
	private final static String DELETED= "deleted";
	private final static String NOT_DELETED= "not_deleted";
	
	private final static String CATEGORY= "__navigation_" + TextSelectionNavigationLocation.class.hashCode();
	private static IPositionUpdater fgPositionUpdater= new DefaultPositionUpdater(CATEGORY);
	
	private Position fPosition;
	private IDocument fDocument;
	private Position fSavedPosition;
	
	
	public TextSelectionNavigationLocation(ITextEditor part) {
		
		ISelection s= part.getSelectionProvider().getSelection();
		ITextSelection selection= (ITextSelection) s;
		IDocument document= getDocument(part);
		
		Position position= new Position(selection.getOffset(), selection.getLength());
		if (installOnDocument(document, position)) {
			fDocument= document;
			fPosition= position;
			
			if (!part.isDirty())
				fSavedPosition= new Position(fPosition.offset, fPosition.length);
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
		return "Selection<" + fPosition + ">";
	}
	
	public boolean differsFromCurrentLocation(IEditorPart part) {
		
		if (fPosition == null)
			return false;
			
		if (fPosition.isDeleted)
			return true;
			
		ISelectionProvider provider= part.getSite().getSelectionProvider();
		ISelection selection= provider.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection) selection;
			if (textSelection.getOffset() == fPosition.offset && textSelection.getLength() == fPosition.length) {
				String text= textSelection.getText();
				if (text != null) {
					try {
						return !text.equals(fDocument.get(fPosition.offset, fPosition.length));
					} catch (BadLocationException e) {
					}
				}
			}
		}
		return true;
	}
	
	public void dispose() {
		uninstallFromDocument(fDocument, fPosition);
		clearState();
	}

	private void clearState() {
		fDocument= null;
		fPosition= null;
		fSavedPosition= null;
	}
	
	public boolean mergeInto(NavigationLocation location) {
		
		if (location == null)
			return false;
			
		if (getClass() != location.getClass())
			return false;
			
		TextSelectionNavigationLocation s= (TextSelectionNavigationLocation) location;
		
		if (fPosition == null || fPosition.isDeleted)
			return true;
		
		if (s.fPosition == null || s.fPosition.isDeleted) {
			uninstallFromDocument(fDocument, fPosition);
			s.fDocument= fDocument;
			s. fPosition= fPosition;
			s.fSavedPosition= fSavedPosition;
			return true;
		}
		
		return s.fDocument == fDocument && s.fPosition.equals(fPosition);
	}
	
	public void restoreLocation(IEditorPart part) {
		if (fPosition == null || fPosition.isDeleted)
			return;
			
		if (part instanceof ITextEditor) {
			ITextEditor editor= (ITextEditor) part;
			editor.selectAndReveal(fPosition.offset, fPosition.length);
		}
	}
	
	public void restoreAndActivate(IEditorPart part, IMemento memento) {
		
		clearState();
		
		if (part instanceof ITextEditor) {
			
			// restore
			fDocument= getDocument((ITextEditor) part);
			
			Integer offset= memento.getInteger(IWorkbenchConstants.TAG_X);
			Integer length= memento.getInteger(IWorkbenchConstants.TAG_Y);
			String deleted= memento.getString(IWorkbenchConstants.TAG_INFO);
			
			if (offset != null && length != null) {
				fPosition= new Position(offset.intValue(), length.intValue());
				if (deleted != null)
					fPosition.isDeleted= DELETED.equals(deleted) ? true : false;
			}
			
			// activate
			if (installOnDocument(fDocument, fPosition) && !part.isDirty())
				fSavedPosition= new Position(fPosition.offset, fPosition.length);
		}
	}
	
	public void saveAndDeactivate(IEditorPart part, IMemento memento) {
		
		// save
		if (fSavedPosition != null) {
			memento.putInteger(IWorkbenchConstants.TAG_X, fSavedPosition.offset);
			memento.putInteger(IWorkbenchConstants.TAG_Y, fSavedPosition.length);
			memento.putString(IWorkbenchConstants.TAG_INFO, (fSavedPosition.isDeleted ? DELETED : NOT_DELETED));
		}
		
		// deactivate
		uninstallFromDocument(fDocument, fPosition);
		clearState();
	}
	
	public void partSaved(IEditorPart part) {
		
		if (fPosition == null)
			return;
		
		if (fSavedPosition == null)
			fSavedPosition= new Position(0, 0);
			
		fSavedPosition.offset= fPosition.offset;
		fSavedPosition.length= fPosition.length;
		fSavedPosition.isDeleted= fPosition.isDeleted;
	}
}
