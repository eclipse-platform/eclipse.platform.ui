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

import java.util.Arrays;
import java.util.Comparator;



/**
 * A patch document represent a projection of its parent document. 
 * The patch document is always in sync with its parent document
 * by utilizing the parent document as its <code>ITextStore</code>.
 * This class is for internal use only.
 * 
 * TODO: sorting Fragments on creation/joining
 *
 * @see ITextStore
 */
public final class ProjectionDocument extends AbstractDocument {
	
	final public static String FRAGMENT_CATEGORY= "__fragment_category"; //$NON-NLS-1$
	
	/** The parent document */
	private IDocument fParentDocument;
	/** The parent document as document extension */
	private IDocumentExtension fExtension;
	/** The position category defining the projection */
	private String fProjectionCategory;
	/** The document event issued by the parent document */
	private DocumentEvent fParentEvent;
	/** The document event issued and to be issued by the patch document */
	private SlaveDocumentEvent fEvent;
	/** Indicates whether the patch document initiated a parent document update or not */
	private boolean fIsUpdating= false;	
	/** The fragment updater */
	private FragmentUpdater fFragmentUpdater= new FragmentUpdater(FRAGMENT_CATEGORY);
	
	/**
	 * Creates a projection document of the given parent document.
	 *
	 * @param parentDocument the parent Document
	 * @param projectionCategory the document position category whose positions define the projection of the parent document
	 */
	public ProjectionDocument(IDocument parentDocument, String projectionCategory) {
		super();
		
		fParentDocument= parentDocument;
		if (fParentDocument instanceof IDocumentExtension) 
			fExtension= (IDocumentExtension) fParentDocument;
			
		ITextStore s= new ProjectionTextStore(this);
		ILineTracker tracker= new DefaultLineTracker();
		
		setTextStore(s);
		setLineTracker(tracker);
		
		completeInitialization();
		
		initializeProjection(projectionCategory);
		tracker.set(s.get(0, s.getLength()));
	}

	/**
	 * Initializes the projection document from the parent document based on the given
	 * projection category.
	 * @param projectionCategory the document position category whose positions define the projection of the parent document
	 */
	private void initializeProjection(String projectionCategory) {
		
		fProjectionCategory= projectionCategory;
		
		try {
			
			addPositionCategory(FRAGMENT_CATEGORY);
			addPositionUpdater(fFragmentUpdater);
			
			int offset= 0;
			Position[] patch= fParentDocument.getPositions(fProjectionCategory);
			for (int i= 0; i < patch.length; i++) {
				Position p= patch[i];
				addPosition(FRAGMENT_CATEGORY, new Fragment(offset, p.length, p));
				offset += p.length;
			}
			
		} catch (BadPositionCategoryException x) {
		} catch (BadLocationException x) {
		}
	}
	
	/**
	 * Creates a fragment from a postion of the parent document.
	 * @param parentPosition a position of the parent document
	 * @return the fragment representing the range given by the parent position
	 */
	public Fragment createFragment(Position parentPosition) {
		try {
	
			int index= fParentDocument.computeIndexInCategory(fProjectionCategory, parentPosition.offset);
			if (index <= 0)
				return new Fragment(0, parentPosition.length, parentPosition);
				
			Position[] fragments= getPositions(FRAGMENT_CATEGORY);
			Position p= fragments[index -1];
			return new Fragment(p.offset + p.length, parentPosition.length, parentPosition);
		
		} catch (BadPositionCategoryException e) {
		} catch (BadLocationException e) {
		}
		
		return null;
	}

	private int getPositionOfOffset(IDocument document, String category, int offset, int direction ) throws BadPositionCategoryException, BadLocationException{
			
		Position[] positions= document.getPositions(category);
		if (positions != null && positions.length > 0) {
			
			// test for inclusion
			int index= document.computeIndexInCategory(category, offset);
			if (index < positions.length && positions[index].includes(offset))
				return index;
			if (index > 0 && positions[index -1].includes(offset))
				return index -1;
			
			// find next accorrding to direction
			if (direction != 0) {
				if (direction > 0) {
					if (index < positions.length && positions[index].overlapsWith(offset, direction))
						return index;
				} else {
					if (index > 0 && positions[index -1].overlapsWith(offset + direction, -direction))
						return index -1;
				}
			}
		}
				
		return -1;
	}

	/**
	 * Returns the position which is used to manage a parent
	 * document range represented in this projection document and that
	 * includes or is close to the given parent document offset. The distance
	 * is computed based on the given direction hint.
	 * 
	 * @param offsetInParent the parent document offset
	 * @param direction the direction hint used for computing the distance
	 * @return position the parent document position including or near to the parent document offset
	 */
	private Position getParentDocumentPositionOfOffset(int offsetInParent, int direction ) {
		try {
			
			int index= getPositionOfOffset(fParentDocument, fProjectionCategory, offsetInParent, direction);
			if (index > -1) {
				Position[] positions= fParentDocument.getPositions(fProjectionCategory);
				return positions[index];
			}
			
		} catch (BadPositionCategoryException x) {
		} catch (BadLocationException x) {
		}
		
		return null;
	}
		
	/**
	 * Returns the offset in the projection document corresponding to the
	 * given parent document offset.
	 * 
	 * @param offsetInParent the parent document offset
	 * @return the projection document offset  corresponding to the given parent document offset
	 */
	private int toProjectionDocumentOffset(int offsetInParent, int direction) {
		
		Position p= getParentDocumentPositionOfOffset(offsetInParent, direction);
		if (p == null)
			return -1;
					
		int relative= offsetInParent - p.offset;
		
		if (direction > 0) {
			if (relative < 0)
				relative= 0;
		} else if (direction < 0) {
			if (relative >= p.length)
				relative= p.length -1;
		}
		
		Fragment f= findCorrespondingFragment(p);
		return f.offset + relative;
	}
	
	/**
	 * Creates a position describing the projection document range corresponding to
	 * the given parent document range.
	 * 
	 * @param offsetInParent the parent document offset
	 * @param lengthInParent the parent document lengh
	 * @return position describing the projection document range corresponding to the given parent document range
	 */
	public Position computeProjectionDocumentPosition(int offsetInParent, int lengthInParent) {
		
		Position p= getParentDocumentCoverage();
		if (p != null) {
			
			if (p.overlapsWith(offsetInParent, lengthInParent)) {
			
				int o1= toProjectionDocumentOffset(offsetInParent, lengthInParent);
				if (o1 == -1)
					return null;
					
				if (lengthInParent == 0)
					return new Position(o1, 0);
					
				int o2= toProjectionDocumentOffset(offsetInParent + lengthInParent -1, 1 - lengthInParent);
				if (o2 == -1)
					return null;
					
				return new Position(o1, o2 - o1 + 1);
			
			} else if (p.getOffset() + p.getLength() == offsetInParent + lengthInParent) {
				
				Position[] fragments= getFragmentation();
				if (fragments != null && fragments.length > 0) {
					Position last= fragments[fragments.length -1];
					return new Position(last.getOffset() + last.getLength());
				}
			}
		}
		
		return null;
	}
	
	public int toParentDocumentOffset(int offset) throws BadLocationException {
		Fragment fragment= getFragmentOfOffset(offset);
		
		if (fragment == null) {
			
//			if (offset == 0)
//				return 0;
//			throw new BadLocationException();

			Position[] fragmentation= getFragmentation();
			if (fragmentation != null && fragmentation.length > 0) {
				Fragment last= (Fragment) fragmentation[fragmentation.length -1];
				if (last.offset + last.length == offset) {
					Position origin= last.getOrigin();
					return origin.offset + origin.length;
				}
			}
			
			throw new BadLocationException();
		}

		int relative= offset - fragment.offset;
		return fragment.getOrigin().offset + relative;
	}
	
	public IRegion computeParentDocumentRegion(int offset, int length) throws BadLocationException {
		
		if (length == 0) {
			if (offset == 0 && length == getLength())
				return new Region(0, fParentDocument.getLength());
			return new Region(toParentDocumentOffset(offset), 0);
		}
		
		int o1= toParentDocumentOffset(offset);
		int o2= toParentDocumentOffset(offset + length -1);
		return new Region(o1, o2 - o1 + 1);
	}
	
	/**
	 * Removes all fragments and thus empties this projection document.
	 */
	public void removeAllFragments() {
		Position[] projection= getProjection();
		if (projection == null)
			return;
			
		for (int i= 0; i < projection.length; i++) {
			try {
				removeFragment(projection[i]);
			} catch (BadLocationException e) {
			}
		}
	}
	
	/**
	 * Add a new fragment of the parent document to this projection document.
	 * 
	 * @param offsetInParent offset of the parent document range
	 * @param lengthInParent length of the parent document range
	 * @return returns the position representing the parent document range in this projection document
	 * @throws BadLocationException
	 */
	public void addFragment(int offsetInParent, int lengthInParent) throws BadLocationException {
		
		if (lengthInParent == 0)
			return;
		
		try {
			
			ProjectionPosition p= new ProjectionPosition(this, offsetInParent, lengthInParent);
			fParentDocument.addPosition(fProjectionCategory, p);
			
			Fragment fragment= createFragment(p);
			p.setFragment(fragment);
			fireDocumentProjectionChanged(new DocumentEvent(this, fragment.offset, 0, fParentDocument.get(offsetInParent, lengthInParent)));
			addPosition(FRAGMENT_CATEGORY, fragment);
			

			getTracker().set(getStore().get(0, getStore().getLength()));
			
		} catch (BadPositionCategoryException x) {
		}
		
	}
	
	public void joinFragments() {
		try {
			while (joinTwoFragments()) {}
		} catch (BadPositionCategoryException x) {
		}
	}
	
	private boolean joinTwoFragments() throws BadPositionCategoryException {
		Position[] projection= getProjection();
		if (projection != null && projection.length > 0) {
			Position previous= projection[0];
			for (int i= 1; i < projection.length; i++) {
				Position current= projection[i];
				if (previous.offset + previous.length == current.offset) {
					join(previous, current);
					return true;
				}
				previous= current;
			}
		}
		return false;
	}
	
	private void join(Position p1, Position p2) throws BadPositionCategoryException {
		// remove p2
		Fragment fragment= findCorrespondingFragment(p2);
		removePosition(FRAGMENT_CATEGORY, fragment);
		fParentDocument.removePosition(fProjectionCategory, p2);
		// extend p1 by length of p2
		fragment= findCorrespondingFragment(p1);
		fragment.length += p2.length;
		p1.length += p2.length;
	}
	
	/**
	 * Removes the given parent document range from this projection document.
	 * 
	 * @param parentPosition the position representing the parent document range
	 * @throws BadLocationException
	 */
	public void removeFragment(Position parentPosition) throws BadLocationException {
		try {
			
			Fragment fragment= findCorrespondingFragment(parentPosition);
			if (fragment != null) {
				removePosition(FRAGMENT_CATEGORY, fragment);
				fParentDocument.removePosition(fProjectionCategory, parentPosition);
				fireDocumentProjectionChanged(new DocumentEvent(this, fragment.offset, fragment.length, null));
				getTracker().set(getStore().get(0, getStore().getLength()));
			}
			
		} catch (BadPositionCategoryException x) {
		}
	}
	
	public Position[] getAffectedFragments(int offsetInParent, int lengthInParent) {
		
		Position p= computeProjectionDocumentPosition(offsetInParent, lengthInParent);
		if (p == null)
			return null;
			
		Fragment[] f= getFragmentsOfRange(p.offset, p.length);
		if (f == null)
			return null;
				
		Position[] result= new Position[f.length];
		for (int i= 0; i < f.length; i++)
			result[i]= f[i].getOrigin();
		return result;
	}

	/**
	 * Finds the fragment that represents the given parent document range in this projection document.
	 * 
	 * @param parentPosition the parent document range
	 * @return the fragment representing the given parent document range
	 */
	private Fragment findCorrespondingFragment(Position parentPosition) {
		try {
			Position[] fragments= getPositions(FRAGMENT_CATEGORY);
			for (int i= 0; i < fragments.length; i++) {
				Fragment f= (Fragment) fragments[i];
				if (parentPosition.equals(f.getOrigin()))
					return f;
			}
		} catch (BadPositionCategoryException x) {
		}
		
		return null;
	}
	
	/**
	 * Returns the fragment that contains the given offset.
	 * 
	 * @param offset the offset
	 * @return the fragment that contains the given offset
	 */
	protected Fragment getFragmentOfOffset(int offset) throws BadLocationException {
		try {
			int index= getPositionOfOffset(this, FRAGMENT_CATEGORY, offset, 0);
			if (index > -1) {
				Position[] fragments= getPositions(FRAGMENT_CATEGORY);
				return (Fragment) fragments[index];
			}	
		} catch (BadPositionCategoryException x) {
		}
		return null;
	}

	/**
	 * Returns the minimal consecutive list of fragments that completely covers the given range.
	 * 
	 * @param offset the offset of the range
	 * @param length the length of the range
	 * @return the minimal consecutive list of fragments convering the given range
	 */
	protected Fragment[] getFragmentsOfRange(int offset, int length) {
		
		try {
			
			int start= getPositionOfOffset(this, FRAGMENT_CATEGORY, offset, length);
			int end= getPositionOfOffset(this, FRAGMENT_CATEGORY, offset + length -1, 1 - length);
					
			if (start > -1 && end > -1) {
				
				Position[]  positions= getPositions(FRAGMENT_CATEGORY);
				
				if (start == end)
					return new Fragment[] { (Fragment) positions[start] };
					
				Fragment[] result= new Fragment[end - start + 1];
				for (int i= start; i <= end; i++)
					result[i - start]= (Fragment) positions[i];
				sortFragments(result);
				return result;
			}
			
		} catch (BadPositionCategoryException e) {
		} catch (BadLocationException e) {
		}
				
		return new Fragment[0];
	}
	
	/**
	 * Sorts a list of fragments based on the sequence of their origins in the parent document.
	 * @param result
	 */
	private void sortFragments(Object[] result) {
		
		Comparator comparator= new Comparator() {
			
			public int compare(Object o1, Object o2) {
				Fragment f1= (Fragment) o1;
				Fragment f2= (Fragment) o2;
				return f1.getOrigin().getOffset() - f2.getOrigin().getOffset();
			}

			public boolean equals(Object obj) {
				return false;
			}
		};
		
		Arrays.sort(result, comparator);
	}
	
	/**
	 * Returns the minimal range of the parent document that covers all fragments.
	 * 
	 * @return a position describing the minimal parent document range covering all fragments
	 */
	public Position getParentDocumentCoverage() {
		Position[] projection= getProjection();
		if (projection != null && projection.length > 0) {
			Position first=projection[0];
			Position last= projection[projection.length -1];
			return  new Position(first.offset, last.offset - first.offset + last.length);
		}
		return new Position(0, 0);
	}
	
	private void fireDocumentProjectionChanged(DocumentEvent event) {
		fFragmentUpdater.enableShiftMode(true);
		try {
			updatePositions(event);
		} finally {
			fFragmentUpdater.enableShiftMode(false);
		}
	}
	
	/**
	 * Returns parent document
	 *
	 * @return the parent document
	 */
	public IDocument getParentDocument() {
		return fParentDocument;
	}
	
	/**
	 * Returns the ranges of the parent document covered by this patch document.
	 *
	 * @return the child document's parent document range
	 */
	public Position[] getProjection() {
		try {
			return fParentDocument.getPositions(fProjectionCategory);
		} catch (BadPositionCategoryException x) {
		}
		return null;
	}
	
	public Position[] getFragmentation() {
		try {
			
			Position[] fragmentation= getPositions(FRAGMENT_CATEGORY);
			sortFragments(fragmentation);
			return fragmentation;
		
		} catch (BadPositionCategoryException x) {
		}
		return null;
	}
	
	/**
	 * Transforms a document event of the parent document into a patch document
	 * based document event.
	 *
	 * @param e the parent document event
	 * @return the child document event
	 */
	private SlaveDocumentEvent normalize(DocumentEvent e) {
		
		Position c= computeProjectionDocumentPosition(e.getOffset(), e.getLength());
		
		if (c != null) {
			if (c.length == 0) {
				int insertLength= e.getText() == null ? 0 : e.getText().length();
				if (insertLength == 0)
					return null;
			}
			return new SlaveDocumentEvent(this, c.offset, c.length, e.getText(), e);
		}
		
		return null;
	}
	
	/**
	 * When called this patch document is informed about a forthcoming change
	 * of its parent document. This child document checks whether the parent
	 * document change affects it and if so informs all document listeners.
	 *
	 * @param event the parent document event
	 */
	public void parentDocumentAboutToBeChanged(DocumentEvent event) {
		fParentEvent= event;
		fEvent= normalize(event);
		if (fEvent != null)
			delayedFireDocumentAboutToBeChanged();
	}
	
	/**
	 * When called this child document is informed about a change of its parent document.
	 * If this child document is affected it informs all of its document listeners.
	 *
	 * @param event the parent document event
	 */
	public void parentDocumentChanged(DocumentEvent event) {
		if ( !fIsUpdating && event == fParentEvent && fEvent != null) {
			try {
				getTracker().replace(fEvent.getOffset(), fEvent.getLength(), fEvent.getText());
				fireDocumentChanged(fEvent);
			} catch (BadLocationException x) {
				Assert.isLegal(false);
			}
		}
	}
	
	/*
	 * @see AbstractDocument#fireDocumentAboutToBeChanged
	 */
	protected void fireDocumentAboutToBeChanged(DocumentEvent event) {
		// delay it until there is a notification from the parent document
		// otherwise there it is expensive to construct the parent document information
	}
	
	/**
	 * Fires the child document event as about-to-be-changed event to all registed listeners.
	 */
	private void delayedFireDocumentAboutToBeChanged() {
		super.fireDocumentAboutToBeChanged(fEvent);
	}
	
	/**
	 * Ignores the given event and sends the similar child document event instead.
	 *
	 * @param event the event to be ignored
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		super.fireDocumentChanged(fEvent);
	}
	
	/*
	 * @see IDocument#replace(int, int, String)
	 * @since 2.0
	 */
	public void replace(int offset, int length, String text) throws BadLocationException {
		try {
			fIsUpdating= true;
			if (fExtension != null)
				fExtension.stopPostNotificationProcessing();
				
			super.replace(offset, length, text);
			
		} finally {
			fIsUpdating= false;
			if (fExtension != null)
				fExtension.resumePostNotificationProcessing();
		}
	}
	
	/*
	 * @see IDocument#set(String)
	 * @since 2.0
	 */
	public void set(String text) {
		try {
			fIsUpdating= true;
			if (fExtension != null)
				fExtension.stopPostNotificationProcessing();
				
			super.set(text);
		
		} finally {
			fIsUpdating= false;
			if (fExtension != null)
				fExtension.resumePostNotificationProcessing();
		}
	}
	
	/*
	 * @see IDocumentExtension#registerPostNotificationReplace(IDocumentListener, IDocumentExtension.IReplace)
	 * @since 2.0
	 */
	public void registerPostNotificationReplace(IDocumentListener owner, IDocumentExtension.IReplace replace) {
		if (!fIsUpdating)
			throw new UnsupportedOperationException();
		super.registerPostNotificationReplace(owner, replace);
	}


	/**
	 * Convenience method for hiding the specified region of the document.
	 * 
	 * @param document
	 * @param offsetInParent
	 * @param lengthInParent
	 */
	public void hide(int offsetInParent, int lengthInParent) {
		
		IDocument parent= getParentDocument();
		Position[] effected= getAffectedFragments(offsetInParent, lengthInParent);
		
		try {
			
			if (effected == null) {
				// populate new document with two new fragments, the left and the right of the hidden region
				int end= offsetInParent + lengthInParent;
				addFragment(0, offsetInParent);
				addFragment(end, parent.getLength() - end);
			} else if (effected.length == 1) {
				// the only affected fragment must be splitted into two
				Position fragment= effected[0];
				removeFragment(fragment);
				addFragment(fragment.offset, offsetInParent - fragment.offset);
				int secondOffset= offsetInParent + lengthInParent;
				addFragment(secondOffset, fragment.offset + fragment.length - secondOffset);
			} else {
				// first expand and than collapse
				internalShow(offsetInParent, lengthInParent, effected);
				hide(offsetInParent, lengthInParent);
			}
	
			joinFragments();			
		
		} catch (BadLocationException x) {
		}
	}

	public void show(int offsetInParent, int lengthInParent) {
		
		Position[] effected= getAffectedFragments(offsetInParent, lengthInParent);
		if (effected == null || effected.length == 0) {
			try {
				addFragment(offsetInParent, lengthInParent);
				joinFragments();
			} catch (BadLocationException x) {
			}
			return;
		}
		
		internalShow(offsetInParent, lengthInParent, effected);
		joinFragments();

	}

	private void internalShow(int offsetInParent, int lengthInParent, Position[] effected) {
		try {
			
			int size= effected.length;
			for (int i= 0; i < size; i++)
				removeFragment(effected[i]);
				
			int offset= Math.min(offsetInParent, effected[0].offset);
			int end= Math.max(offsetInParent + lengthInParent, effected[size -1].offset + effected[size -1].length);
			addFragment(offset, end - offset);
		
		} catch (BadLocationException x) {
		}
	}
}
