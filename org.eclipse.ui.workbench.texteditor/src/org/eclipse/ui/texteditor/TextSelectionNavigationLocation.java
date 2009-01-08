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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;


/**
 * Represents the text selection context marked for the user in the navigation history.
 *
 * @since 2.1
 */
public class TextSelectionNavigationLocation extends NavigationLocation {

	// Memento tags and values
	private static final String TAG_X= "x"; //$NON-NLS-1$
	private static final String TAG_Y= "y"; //$NON-NLS-1$
	private static final String TAG_INFO= "info"; //$NON-NLS-1$
	private static final String INFO_DELETED= "deleted"; //$NON-NLS-1$
	private static final String INFO_NOT_DELETED= "not_deleted"; //$NON-NLS-1$

	private static final String CATEGORY= "__navigation_" + TextSelectionNavigationLocation.class.hashCode(); //$NON-NLS-1$
	private static final IPositionUpdater fgPositionUpdater= new DefaultPositionUpdater(CATEGORY);


	private Position fPosition;
	private IDocument fDocument;
	private Position fSavedPosition;


	/**
	 * Creates a new text selection navigation location.
	 *
	 * @param part the text editor part
	 * @param initialize a boolean indicating whether to initialize the new instance from the current selection
	 */
	public TextSelectionNavigationLocation(ITextEditor part, boolean initialize) {
		super(part);

		if (initialize) {

			ISelection s= part.getSelectionProvider().getSelection();
			if(s == null || s.isEmpty())
				return;

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
	}

	/**
	 * Returns the text editor's document.
	 *
	 * @param part the text editor
	 * @return the document of the given text editor
	 */
	private IDocument getDocument(ITextEditor part) {
		IDocumentProvider provider= part.getDocumentProvider();
		return provider.getDocument(part.getEditorInput());
	}

	/**
	 * Installs the given position on the given document.
	 *
	 * @param document the document
	 * @param position the position
	 * @return <code>true</code> if the position could be installed
	 */
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

	/**
	 * Uninstalls the given position from the given document.
	 *
	 * @param document the document
	 * @param position the position
	 * @return <code>true</code> if the position could be uninstalled
	 */
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

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "Selection<" + fPosition + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tells whether this location is equal to the current
	 * location in the given text editor.
	 *
	 * @param part the text editor
	 * @return <code>true</code> if the locations are equal
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
	 * Releases the state of this location.
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
	 * Merges the given location into this one.
	 *
	 * @param location the location to merge into this one
	 * @return <code>true<code> if merging was successful
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

		if (s.fDocument == fDocument)  {
			if (s.fPosition.overlapsWith(fPosition.offset, fPosition.length) || fPosition.offset + fPosition.length == s.fPosition.offset || s.fPosition.offset + s.fPosition.length == fPosition.offset)  {
				s.fPosition.offset= fPosition.offset;
				s.fPosition.length= fPosition.length;
				return true;
			}
		}

		return false;
	}

	/**
	 * Restores this location.
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

	/**
	 * Restores the object state from the given memento.
	 *
	 * @param memento the memento
	 */
	public void restoreState(IMemento memento) {

		IEditorPart part= getEditorPart();
		if (part instanceof ITextEditor) {

			// restore
			fDocument= getDocument((ITextEditor) part);

			Integer offset= memento.getInteger(TAG_X);
			Integer length= memento.getInteger(TAG_Y);
			String deleted= memento.getString(TAG_INFO);

			if (offset != null && length != null) {
				Position p= new Position(offset.intValue(), length.intValue());
				if (deleted != null)
					p.isDeleted= INFO_DELETED.equals(deleted) ? true : false;

				// activate
				if (installOnDocument(fDocument, p)) {
					fPosition= p;
					if (!part.isDirty())
						fSavedPosition= new Position(fPosition.offset, fPosition.length);
				}
			}
		}
	}

	/**
	 * Stores the object state into the given memento.
	 *
	 * @param memento the memento
	 */
	public void saveState(IMemento memento) {
		if (fSavedPosition != null) {
			memento.putInteger(TAG_X, fSavedPosition.offset);
			memento.putInteger(TAG_Y, fSavedPosition.length);
			memento.putString(TAG_INFO, (fSavedPosition.isDeleted ? INFO_DELETED : INFO_NOT_DELETED));
		}
	}

	/**
	 * Hook method which is called when the given editor has been saved.
	 *
	 * @param part the editor part
	 */
	public void partSaved(IEditorPart part) {
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=25440
		if (fPosition == null || fPosition.isDeleted())
			fSavedPosition= null;
		else
			fSavedPosition= new Position(fPosition.offset, fPosition.length);
	}

	/**
	 * Updates the this location.
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
