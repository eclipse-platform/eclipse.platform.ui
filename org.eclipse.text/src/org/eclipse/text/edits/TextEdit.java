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
package org.eclipse.text.edits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.text.edits.TreeIterationInfo.Visitor;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * A text edit describes an elementary text manipulation operation. Edits are
 * executed by applying them to a document (e.g. an instance of <code>IDocument
 * </code>). 
 * <p>
 * Text edits form a tree. Clients can navigate the tree upwards, from child to 
 * parent, as well as downwards. Newly created edits are unparented. New edits
 * are added to the tree by calling one of the <code>add</code> methods on a parent
 * edit.
 * <p>
 * An edit tree is well formed in the following sense:
 * <ul>
 *   <li>a parent edit covers all its children</li>
 *   <li>children don't overlap</li>
 *   <li>an edit with length 0 can't have any children</li>
 * </ul>
 * Any manipulation of the tree that violates one of the above requirements results
 * in a <code>MalformedTreeException</code>.
 * <p>
 * Insert edits are represented by an edit of length 0. If more than one insert 
 * edit exists at the same offset then the edits are executed in the order in which
 * they have been added to a parent. The following code example:
 * <pre>
 *    IDocument document= new Document("org");
 * 	  MultiEdit edit= new MultiEdit();
 *    edit.add(new InsertEdit(0, "www.");
 *    edit.add(new InsertEdit(0, "eclipse.");
 *    edit.apply(document);
 * </pre> 
 * therefore results in string: "www.eclipse.org".
 * 
 * <p>
 * This class isn't intended to be subclassed outside of the edit framework. Clients 
 * are only allowed to subclass <code>MultiTextEdit</code>.
 * 
 * @see TextBufferEditor
 * 
 * @since 3.0
 */
public abstract class TextEdit {

	private static final TextEdit[] EMPTY_ARRAY= new TextEdit[0];
	
	private static final int DELETED_VALUE= -1;
	
	private int fOffset;
	private int fLength;
	
	private TextEdit fParent;
	private List fChildren;

	/**
	 * Create a new text edit. Parent is initialized to <code>
	 * null<code> and the edit doesn't have any children.
	 */
	protected TextEdit(int offset, int length) {
		Assert.isTrue(offset >= 0 && length >= 0);
		fOffset= offset;
		fLength= length;
	}
	
	/**
	 * Copy constrcutor
	 * 
	 * @param source the source to copy form
	 */
	protected TextEdit(TextEdit source) {
		fOffset= source.fOffset;
		fLength= source.fLength;
	}

	//---- Region management -----------------------------------------------

	/**
	 * Returns the range that this edit is manipulating. The returned
	 * <code>IRegion</code> contains the edit's offset and length at
	 * the point in time when this call is made. Any subsequent changes
	 * to the edit's offset and length aren't reflected in the returned
	 * region object.
	 * 
	 * @return the manipulated region
	 */
	public final IRegion getRegion() {
		return new Region(fOffset, fLength);
	}
	
	/**
	 * Returns the offset of the edit. An offset is a 0-based 
	 * character index.
	 * 
	 * @return the offset of the edit
	 */
	public final int getOffset() {
		return fOffset;
	}
	
	/**
	 * Returns the length of the edit.
	 * 
	 * @return the length of the edit
	 */
	public final int getLength() {
		return fLength;
	}
	
	/**
	 * Returns the inclusive end position of this edit. The inclusive end
	 * position denotes the last character of the region manipulated by
	 * this edit. The returned value is the result of the following
	 * calculation:
	 * <pre>
	 *   getOffset() + getLength() - 1;
	 * <pre>
	 * 
	 * @return the inclusive end position
	 */
	public final int getInclusiveEnd() {
		return fOffset + fLength - 1;
	}
	
	/**
	 * Returns the exclusive end position of this edit. The exclusive end
	 * position denotes the next character of the region manipulated by 
	 * this edit. The returned value is the result of the following
	 * calculation:
	 * <pre>
	 *   getOffset() + getLength();
	 * </pre>
	 * 
	 * @return the exclusive end position
	 */
	public final int getExclusiveEnd() {
		return fOffset + fLength;
	}
	
	/**
	 * Returns whether this edit has been deleted or not.
	 * 
	 * @return <code>true</code> if the edit has been 
	 *  deleted; otherwise <code>false</code> is returned.
	 */
	public final boolean isDeleted() {
		return fOffset == DELETED_VALUE && fLength == DELETED_VALUE;
	}
	
	/**
	 * Returns <code>true</code> if the edit covers the given edit
	 * <code>other</code>. If the length of the edit is zero <code>
	 * false</code> is returned. An insert edit can't cover any other
	 * edit, even if the other edit has the same offset and length.
	 * 
	 * @param other the other edit
	 * @return <code>true<code> if the edit covers the other edit;
	 *  otherwise <code>false</code> is returned.
	 * @see Regions#covers(IRegion, IRegion)  
	 */
	public final boolean covers(TextEdit other) {
		if (fLength == 0) {	// an insert edit can't cover anything
			return false;
		} else {
			int otherOffset= other.fOffset;
			return fOffset <= otherOffset && otherOffset + other.fLength <= fOffset + fLength;
		}		
	}

	//---- parent and children management -----------------------------
	
	/**
	 * Returns the edit's parent. The method returns <code>null</code> 
	 * if this edit hasn't been add to another edit.
	 * 
	 * @return the edit's parent
	 */
	public final TextEdit getParent() {
		return fParent;
	}
	
	/**
	 * Adds the given edit <code>child</code> to this edit.
	 * 
	 * @param child the child edit to add
	 * @exception <code>MalformedTreeException<code> is thrown if the child
	 *  edit can't be added to this edit. This is the case if the child 
	 *  overlaps with one of its siblings or if the child edit's region
	 *  isn't fully covered by this edit.
	 */
	public final void addChild(TextEdit child) throws MalformedTreeException {
		internalAdd(child);
	}
	
	/**
	 * Adds all edits in <code>edits</code> to this edit.
	 * 
	 * @param edits the text edits to add
	 * @exception <code>MalformedTreeException</code> is thrown if one of 
	 *  the given edits can't be added to this edit.
	 * 
	 * @see #addChild(TextEdit)
	 */
	public final void addChildren(TextEdit[] edits) throws MalformedTreeException {
		for (int i= 0; i < edits.length; i++) {
			internalAdd(edits[i]);
		}
	}
	
	/**
	 * Removes the edit specified by the given index from the list
	 * of children. Returns the child edit that was removed from
	 * the list of children. The parent of the returned edit is
	 * set to <code>null</code>.
	 * 
	 * @param index the index of the edit to remove
	 * @return the removed edit
	 * @exception <code>IndexOutOfBoundsException</code> if the index 
	 *  is out of range
	 */
	public final TextEdit removeChild(int index) {
		if (fChildren == null)
			throw new IndexOutOfBoundsException("Index: " + index + " Size: 0");  //$NON-NLS-1$//$NON-NLS-2$
		TextEdit result= (TextEdit)fChildren.remove(index);
		result.internalSetParent(null);
		if (fChildren.isEmpty())
			fChildren= null;
		return result;
	}
	
	/**
	 * Removes the first occurrence of the given child from the list 
	 * of children.
	 * 
	 * @param child the child to be removed
	 * @return <code>true</code> if the edit contained the given
	 *  child; otherwise <code>false</code> is returned
	 */
	public final boolean removeChild(TextEdit child) {
		Assert.isNotNull(child);
		if (fChildren == null)
			return false;
		boolean result= fChildren.remove(child);
		if (result) {
			child.internalSetParent(null);
			if (fChildren.isEmpty())
				fChildren= null;
		}
		return result;
	}
	
	/**
	 * Removes all child edits from and returns them. The parent 
	 * of the removed edits is set to <code>null</code>.
	 * 
	 * @return an array of the removed edits
	 */
	public final TextEdit[] removeChildren() {
		if (fChildren == null)
			return new TextEdit[0];
		int size= fChildren.size();
		TextEdit[] result= new TextEdit[size];
		for (int i= 0; i < size; i++) {
			result[i]= (TextEdit)fChildren.get(i);
			result[i].internalSetParent(null);
		}
		fChildren= null;
		return result;
	}
	
	/**
	 * Returns <code>true</code> if this edit has children. Otherwise
	 * <code>false</code> is returned.
	 * 
	 * @return <code>true</code> if this edit has children; otherwise
	 *  <code>false</code> is returned
	 */
	public final boolean hasChildren() {
		return fChildren != null && ! fChildren.isEmpty();
	}

	/**
	 * Returns the edit's children. If the edit doesn't have any 
	 * children an empty array is returned.
	 * 
	 * @return the edit's children
	 */
	public final TextEdit[] getChildren() {
		if (fChildren == null)
			return EMPTY_ARRAY;
		return (TextEdit[])fChildren.toArray(new TextEdit[fChildren.size()]);
	}
	
	/**
	 * Returns the text range spawned by the given array of text edits.
	 * The method requires that the given array contains at least one
	 * edit. If all edits passed are deleted the method returns <code>
	 * null</code>.
	 * 
	 * @param edits an array of edits
	 * @return the text range spawned by the given array of edits or
	 *  <code>null</code> if all edits are marked as deleted
	 */
	public static IRegion getCoverage(TextEdit[] edits) {
		Assert.isTrue(edits != null && edits.length > 0);
			
		int offset= Integer.MAX_VALUE;
		int end= Integer.MIN_VALUE;
		int deleted= 0;
		for (int i= 0; i < edits.length; i++) {
			TextEdit edit= edits[i];
			if (edit.isDeleted()) {
				deleted++;
			} else {
				offset= Math.min(offset, edit.getOffset());
				end= Math.max(end, edit.getExclusiveEnd());
			}
		}
		if (edits.length == deleted) {
			return null;
		} else {
			return new Region(offset, end - offset);
		}
	}
		
	/*
	 * Hook called before this edit gets added to the passed 
	 * parent.
	 */	
	/* package */ void aboutToBeAdded(TextEdit parent) {
	}	
	
	//---- Object methods ------------------------------------------------------

	/**
	 * The <code>Edit</code> implementation of this <code>Object</code>
	 * method uses object identity (==).
	 * 
	 * @param obj the other object
	 * @return <code>true</code> iff <code>this == obj</code>; otherwise
	 *  <code>false</code> is returned
	 * 
	 * @see Object#equals(java.lang.Object)
	 */
	public final boolean equals(Object obj) {
		return this == obj; // equivalent to Object.equals
	}
	
	/**
	 * The <code>Edit</code> implementation of this <code>Object</code>
	 * method calls uses <code>Object#hashCode()</code> to compute its
	 * hash code.
	 * 
	 * @return the object's hash code value
	 * 
	 * @see Object#hashCode()
	 */
	public final int hashCode() {
		return super.hashCode();
	}
	
	/* non Java-doc
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer= new StringBuffer("{"); //$NON-NLS-1$
		String name= getClass().getName();
		int index= name.lastIndexOf('.');
		if (index != -1) {
			 buffer.append(name.substring(index + 1));
		} else {
			buffer.append(name);
		}
		buffer.append(" } "); //$NON-NLS-1$
		if (isDeleted()) {
			buffer.append("[deleted]"); //$NON-NLS-1$
		} else {
			buffer.append("["); //$NON-NLS-1$
			buffer.append(fOffset);
			buffer.append(","); //$NON-NLS-1$
			buffer.append(fLength);
			buffer.append("]"); //$NON-NLS-1$
		}
		return buffer.toString();
	}
	
	//---- Copying -------------------------------------------------------------
	
	/**
	 * Creates a deep copy of the edit tree rooted at this
	 * edit.
	 * 
	 * @return a deep copy of the edit tree
	 * @see #doCopy() 
	 */
	public final TextEdit copy() {
		TextEditCopier copier= new TextEditCopier(this);
		return copier.perform();
	}
		
	/**
	 * Creates and returns a copy of this edit. The copy method should be 
	 * implemented in a way so that the copy can executed without causing
	 * any harm to the original edit. Implementors of this method are
	 * responsible for creating deep or shallow copies of referenced
	 * object to fullfil this requirement.
	 * <p>
	 * Implementers of this method should use the copy constructor <code>
	 * Edit#Edit(Edit source) to initialize the edit part of the copy.
	 * Implementors aren't responsible to actually copy the children or
	 * to set the right parent.
	 * <p>
	 * This method <b>should not be called</b> from outside the framework.
	 * Please use <code>copy</code> to create a copy of a edit tree.
	 * 
	 * @return a copy of this edit.
	 * @see #copy()
	 * @see #postProcessCopy(TextEditCopier)
	 * @see TextEditCopier
	 */
	protected abstract TextEdit doCopy();
	
	/**
	 * This method is called on every edit of the copied tree to do some
	 * postprocessing like connected an edit to a different edit in the tree.
	 * <p>
	 * This default implementation does nothing
	 * 
	 * @param copier the copier that manages a map between original and
	 *  copied edit.
	 * @see TextEditCopier
	 */
	protected void postProcessCopy(TextEditCopier copier) {
	}
	
	//---- Execution -------------------------------------------------------
	
	/**
	 * Returns whether the edit tree can be applied to the
	 * given document or not.
	 * 
	 * @return <code>true</code> if the edit tree can be
	 *  applied to the given document. Otherwise <code>false
	 *  </code> is returned.
	 */
	public boolean canBeApplied(IDocument document) {
		try {
			TextEditProcessor processor= new TextEditProcessor(document);
			processor.add(this);
			return processor.canPerformEdits();
		} finally {
			// unconnect from text edit processor
			fParent= null;
		}
	}
	 
	/**
	 * Applies the edit tree rooted by this edit to the given document.
	 * 
	 * @param document the document to be manipulated
	 * @exception MalformedTreeException is thrown if the tree isn't
	 *  in a valid state. This exception is thrown before any edit
	 *  is executed. So the document is still in its original state.
	 * @exception BadLocationException is thrown if one of the edits
	 *  in the tree can't be executed. The state of the document is
	 *  undefined if this exception is thrown.
	 * @see #checkIntegrity()
	 * @see #perform(IDocument)
	 */
	public final UndoMemento apply(IDocument document) throws MalformedTreeException, BadLocationException {
		try {
			TextEditProcessor processor= new TextEditProcessor(document);
			processor.add(this);
			processor.checkIntegrity();
			return processor.performEdits();
		} finally {
			// unconnect from text edit processor
			fParent= null;
		}
	}
	
	/**
	 * Checks the edit's integrity. 
	 * <p>
	 * Note that this method <b>should only be called</b> by the edit
	 * framework and not by normal clients.
	 *<p>
	 * This default implementation does nothing. Subclasses may override
	 * if needed.
	 *  
	 * @exception MalformedTreeException if the edit isn't in a valid state
	 *  and can therefore not be executed
	 */
	protected void checkIntegrity() throws MalformedTreeException {
		// does nothing
	}
	
	/**
	 * Performs the text edit.
	 * 
	 * <p>
	 * Note that this method <b>should only be called</b> by the edit framework. 
	 * 
	 * @param document the actual document to manipulate
	 * 
	 * @see #apply(IDocument)
	 */
	/* package */ abstract void perform(IDocument document) throws BadLocationException;
	
	
	//---- internal state accessors ----------------------------------------------------------
	
	/* package */ void internalSetParent(TextEdit parent) {
		if (parent != null)
			Assert.isTrue(fParent == null);
		fParent= parent;
	}
	
	/* package */ void internalSetOffset(int offset) {
		Assert.isTrue(offset >= 0);
		fOffset= offset;
	}
	
	/* package */ void internalSetLength(int length) {
		Assert.isTrue(length >= 0);
		fLength= length;
	}
			
	/* package */ List internalGetChildren() {
		return fChildren;
	}
	
	/* package */ void internalSetChildren(List children) {
		fChildren= children;
	}
	
	/* package */ void internalAdd(TextEdit child) throws MalformedTreeException {
		child.aboutToBeAdded(this);
		if (child.isDeleted())
			throw new MalformedTreeException(this, child, TextEditMessages.getString("TextEdit.deleted_edit")); //$NON-NLS-1$
		if (!covers(child))
			throw new MalformedTreeException(this, child, TextEditMessages.getString("TextEdit.range_outside")); //$NON-NLS-1$
		if (fChildren == null) {
			fChildren= new ArrayList(2);
		}
		int index= computeInsertionIndex(child);
		fChildren.add(index, child);
		child.internalSetParent(this);
	}
	
	private int computeInsertionIndex(TextEdit edit) {
		int size= fChildren.size();
		if (size == 0)
			return 0;
		int offset= edit.getOffset();
		int end= edit.getInclusiveEnd();
		for (int i= 0; i < size; i++) {
			TextEdit other= (TextEdit)fChildren.get(i);
			int childOffset= other.getOffset();
			int childEnd= other.getInclusiveEnd();
			// make sure that a duplicate insertion point at the same offet is inserted last 
			if (offset > childEnd || (offset == childOffset && edit.getLength() == 0 && other.getLength() == 0))
				continue;
			if (end < childOffset)
				return i;
			throw new MalformedTreeException(this, edit, TextEditMessages.getString("TextEdit.overlapping")); //$NON-NLS-1$
		}
		return size;
	}
		
	//---- Offset & Length updating -------------------------------------------------
	
	/**
	 * Adjusts the edits offset according to the given
	 * delta. This method doesn't update any children.
	 * 
	 * @param delta the delta of the text replace operation
	 */
	/* package */ void adjustOffset(int delta) {
		if (isDeleted())
			return;
		fOffset+= delta;
		Assert.isTrue(fOffset >= 0);
	}
	
	/**
	 * Adjusts the edits length according to the given
	 * delta. This method doesn't update any children.
	 * 
	 * @param delta the delta of the text replace operation
	 */
	/* package */ void adjustLength(int delta) {
		if (isDeleted())
			return;
		fLength+= delta;
		Assert.isTrue(fLength >= 0);
	}
	
	/** 
	 * Marks the edit as deleted. This method doesn't update
	 * any children.
	 */	
	/* package */ void markAsDeleted() {
		fOffset= DELETED_VALUE;
		fLength= DELETED_VALUE;
	}
	
	/**
	 * Updates all previously executed edits. This method doesn't 
	 * have any tree iteration info object hence it has to walk 
	 * the tree by itself. This is slower than the corresponding
	 * update method that takes a <code>TreeIterationInfo</code>
	 * object.
	 * 
	 * @param event the document event describing the change
	 */
	/* package */ final void update(DocumentEvent event) {
		checkEvent(event);
		int delta= getDelta(event);
		if (fParent != null)
			fParent.update(delta, this);
		adjustLength(this, delta);
	}
	
	private void update(int delta, TextEdit child) {
		if (fParent != null)
			fParent.update(delta, this);
		// We have children. Otherwise we wouldn't end here.
		boolean doIt= false;
		for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
			TextEdit edit= (TextEdit)iter.next();
			if (doIt) {
				adjustOffset(edit, delta);	
			} else {
				if (edit == child)
					doIt= true;
			}
		}
	}
	
	/**
	 * Updates all previously executed edits.
	 * 
	 * @param event the document event describing the change
	 * @param info a tree iteration info object describing the
	 * position of the current edit in the tree. 
	 */
	/* package */ void update(DocumentEvent event, TreeIterationInfo info) {
		checkEvent(event);
		final int delta= getDelta(event);
		Visitor visitor= new Visitor() {
			public void visit(TextEdit edit) {
				adjustOffset(edit, delta);
			}
		};
		info.accept(visitor);
		adjustLength(this, delta);
	}
	
	
	private void checkEvent(DocumentEvent event) {
		int eventOffset= event.getOffset();
		int eventLength= event.getLength();
		Assert.isTrue(fOffset <= eventOffset && eventOffset + eventLength <= fOffset + fLength);
	}

	/* package */ void markChildrenAsDeleted() {
		if (fChildren != null) {
			for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
				TextEdit edit= (TextEdit)iter.next();
				if (!edit.isDeleted()) {
					edit.markAsDeleted();
					edit.markChildrenAsDeleted();
				}
			}
		}	
	}
	
	private static int getDelta(DocumentEvent event) {
		String text= event.getText();
		return (text == null ? -event.getLength() : text.length()) - event.getLength();
	}
	
	private static void adjustOffset(TextEdit edit, int delta) {
		edit.adjustOffset(delta);
		List children= edit.internalGetChildren();
		if (children != null) {
			for (Iterator iter= children.iterator(); iter.hasNext();) {
				adjustOffset((TextEdit)iter.next(), delta);
			}
		}
	}
	
	private static void adjustLength(TextEdit edit, int delta) {
		while (edit != null) {
			edit.adjustLength(delta);
			edit= edit.getParent();
		}
	}
}

