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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * A move source edit denotes the source of a move operation. Move
 * source edits are only valid inside an edit tree if they have a
 * corresponding traget edit. Furthermore the corresponding target 
 * edit can't be a direct or indirect child of the source edit. 
 * Violating one of two requirements will result in a <code>
 * MalformedTreeException</code> when executing the edit tree.
 * <p>
 * A move source edit can manange an optional source modifier. A
 * source modifier can provide a set of replace edits which will
 * to applied to the source before it gets inserted at the target
 * position.
 * 
 * @see org.eclipse.text.edits.MoveTargetEdit
 * @see org.eclipse.text.edits.CopySourceEdit
 *  
 * @since 3.0 
 */
public final class MoveSourceEdit extends AbstractTransferEdit {

	/* package */ int fCounter;
	private MoveTargetEdit fTarget;
	private ISourceModifier fModifier;
	
	private String fContent;
	private int fContentOffset;
	private List fContentChildren;
	
	/**
	 * Constructs a new move source edit.
	 * 
	 * @param offset the edit's offset
	 * @param length the edit's length
	 */
	public MoveSourceEdit(int offset, int length) {
		super(offset, length);
	}

	/**
	 * Constructs a new copy source edit.
	 * 
	 * @param offset the edit's offset
	 * @param length the edit's length
	 * @param target the edit's target
	 */
	public MoveSourceEdit(int offset, int length, MoveTargetEdit target) {
		this(offset, length);
		setTargetEdit(target);
	}

	/*
	 * Copy constructor
	 */
	private MoveSourceEdit(MoveSourceEdit other) {
		super(other);
		if (other.fModifier != null)
			fModifier= other.fModifier.copy();
	}
	
	/**
	 * Returns the associated traget edit or <code>null</code>
	 * if no target edit is associated yet.
	 * 
	 * @return the target edit or <code>null</code>
	 */
	public MoveTargetEdit getTargetEdit() {
		return fTarget;
	}
	
	/**
	 * Sets the target edit.
	 * 
	 * @param edit the new target edit.
	 * 
	 * @exception MalformedTreeException is thrown if the target edit
	 *  is a direct or indirect child of the source edit
	 */
	public void setTargetEdit(MoveTargetEdit edit) {
		fTarget= edit;
		fTarget.setSourceEdit(this);
	}
	
	/**
	 * Returns the current source modifier or <code>null</code>
	 * if no source modifier is set.
	 * 
	 * @return the source modifier
	 */
	public ISourceModifier getSourceModifier() {
		return fModifier;
	}
	
	/**
	 * Sets the optional source modifier.
	 * 
	 * @param modifier the source modifier or <code>null</code>
	 *  if no source modification is need. 
	 */
	public void setSourceModifier(ISourceModifier modifier) {
		fModifier= modifier;
	}
	
	/* non Java-doc
	 * @see TextEdit#doCopy
	 */	
	protected TextEdit doCopy() {
		return new MoveSourceEdit(this);
	}

	/* non Java-doc
	 * @see TextEdit#postProcessCopy
	 */	
	protected void postProcessCopy(TextEditCopier copier) {
		if (fTarget != null) {
			((MoveSourceEdit)copier.getCopy(this)).setTargetEdit((MoveTargetEdit)copier.getCopy(fTarget));
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#checkIntegrity
	 */	
	protected void checkIntegrity() throws MalformedTreeException {
		if (fTarget == null)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("MoveSourceEdit.no_target")); //$NON-NLS-1$
		if (fTarget.getSourceEdit() != this)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("MoveSourceEdit.different_source"));  //$NON-NLS-1$
	}
	
	/* non Java-doc
	 * @see TextEdit#perform
	 */	
	/* package */ void perform(IDocument document) throws BadLocationException {
		fCounter++;
		switch(fCounter) {
			// Position of move source > position of move target.
			// Hence MoveTarget does the actual move. Move Source
			// only deletes the content.
			case 1:
				fContent= getContent(document);
				fContentOffset= getOffset();
				fContentChildren= internalGetChildren();
				fMode= DELETE;
				document.replace(getOffset(), getLength(), ""); //$NON-NLS-1$
				// do this after executing the replace to be able to
				// compute the number of children.
				internalSetChildren(null);
				break;
				
			// Position of move source < position of move target.
			// Hence move source handles the delete and the 
			// insert at the target position.	
			case 2:
				fContent= getContent(document);
				fMode= DELETE;
				document.replace(getOffset(), getLength(), ""); //$NON-NLS-1$
				if (!fTarget.isDeleted()) {
					// Insert target
					IRegion targetRange= fTarget.getRegion();
					fMode= INSERT;
					document.replace(targetRange.getOffset(), targetRange.getLength(), fContent);
				}
				clearContent();
				break;
			default:
				Assert.isTrue(false, "Should never happen"); //$NON-NLS-1$
		}
	}

	/* package */ String getContent() {
		return fContent;
	}
	
	/* package */ List getContentChildren() {
		return fContentChildren;
	}
	
	/* package */ int getContentOffset() {
		return fContentOffset;
	}
	
	/* package */ void clearContent() {
		fContent= null;
		fContentChildren= null;
		fContentOffset= -1;
	}
	
	/* package */ void update(DocumentEvent event, TreeIterationInfo info) {
		if (fMode == DELETE) {			// source got deleted
			super.update(event, info); 
		} else if (fMode == INSERT) {	// text got inserted at target position
			fTarget.update(event);
			List children= internalGetChildren();
			if (children != null) {
				internalSetChildren(null);
				int moveDelta= fTarget.getOffset() - getOffset();
				move(children, moveDelta);
			}
			fTarget.internalSetChildren(children);
		} else {
			Assert.isTrue(false);
		}
	}	
	
	//---- content management --------------------------------------------------
	
	private String getContent(IDocument document) throws BadLocationException {
		String result= document.get(getOffset(), getLength());
		if (fModifier != null) {
			IDocument newDocument= new Document(result);
			Map editMap= new HashMap();
			TextEdit newEdit= createEdit(editMap);
			List replaces= new ArrayList(Arrays.asList(fModifier.getModifications(result)));
			try {
				insertEdits(newEdit, replaces);
				newEdit.apply(newDocument);
			} catch (MalformedTreeException e) {
				throw new BadLocationException();
			}
			restorePositions(editMap, getOffset());
			result= newDocument.get();
		}
		return result;		
	}
	
	private TextEdit createEdit(Map editMap) {
		int delta= getOffset();
		MultiTextEdit result= new MultiTextEdit(0, getLength());
		// don't but the root edit into the edit map. The sourc edit
		// will be updated by the perform method.
		createEdit(this, result, editMap, delta);
		return result;
	}
	
	private static void createEdit(TextEdit source, TextEdit target, Map editMap, int delta) {
		TextEdit[] children= source.getChildren();
		for (int i= 0; i < children.length; i++) {
			TextEdit child= children[i];
			RangeMarker marker= new RangeMarker(child.getOffset() - delta, child.getLength());
			target.addChild(marker);
			editMap.put(marker, child);
			createEdit(child, marker, editMap, delta);
		}
	}
	
	private void insertEdits(TextEdit root, List edits) {
		while(edits.size() > 0) {
			ReplaceEdit edit= (ReplaceEdit)edits.remove(0);
			insert(root, edit, edits);
		}
	}
	private static void insert(TextEdit parent, ReplaceEdit edit, List edits) {
		if (!parent.hasChildren()) {
			parent.addChild(edit);
			return;
		}
		TextEdit[] children= parent.getChildren();
		// First dive down to find the right parent.
		for (int i= 0; i < children.length; i++) {
			TextEdit child= children[i];
			if (child.covers(edit)) {
				insert(child, edit, edits);
				return;
			} else if (edit.covers(child)) {
				parent.removeChild(i);
				edit.addChild(child);
			} else {
				IRegion intersect= intersect(edit, child);
				if (intersect != null) {
					ReplaceEdit[] splits= splitEdit(edit, intersect);
					insert(child, splits[0], edits);
					edits.add(splits[1]);
				}
			}
		}
		parent.addChild(edit);
	}
		
	public static IRegion intersect(TextEdit op1, TextEdit op2) {
		int offset1= op1.getOffset();
		int length1= op1.getLength();
		int end1= offset1 + length1 - 1;
		int offset2= op2.getOffset();
		if (end1 < offset2)
			return null;
		int length2= op2.getLength();
		int end2= offset2 + length2 - 1;
		if (end2 < offset1)
			return null;
		if (offset1 < offset2) {
			int end= Math.max(end1, end2);
			return new Region(offset2, end - offset2 + 1);
		} else {
			int end= Math.max(end1, end2);
			return new Region(offset1, end - offset1 + 1); 
		}
	}
		
	private static ReplaceEdit[] splitEdit(ReplaceEdit edit, IRegion intersect) {
		if (edit.getOffset() != intersect.getOffset()) {
			return splitIntersectRight(edit, intersect);
		} else {
			return splitIntersectLeft(edit, intersect);
		}
	}
		
	private static ReplaceEdit[] splitIntersectRight(ReplaceEdit edit, IRegion intersect) {
		ReplaceEdit[] result= new ReplaceEdit[2];
		// this is the actual delete. We use replace to only deal with one type
		result[0]= new ReplaceEdit(intersect.getOffset(), intersect.getLength(), ""); //$NON-NLS-1$
		result[1]= new ReplaceEdit(
							edit.getOffset(), 
							intersect.getOffset() - edit.getOffset(), 
							edit.getText());
		return result;
	}
		
	private static ReplaceEdit[] splitIntersectLeft(ReplaceEdit edit, IRegion intersect) {
		ReplaceEdit[] result= new ReplaceEdit[2];
		result[0]= new ReplaceEdit(intersect.getOffset(), intersect.getLength(), edit.getText());
		result[1]= new ReplaceEdit(	// this is the actual delete. We use replace to only deal with one type
							intersect.getOffset() + intersect.getLength(), 
							edit.getLength() - intersect.getLength(),
							""); //$NON-NLS-1$
		return result;
	}
		
	private static void restorePositions(Map editMap, int delta) {
		for (Iterator iter= editMap.keySet().iterator(); iter.hasNext();) {
			TextEdit marker= (TextEdit)iter.next();
			TextEdit edit= (TextEdit)editMap.get(marker);
			if (marker.isDeleted()) {
				edit.markAsDeleted();
			} else {
				edit.adjustOffset(marker.getOffset() - edit.getOffset() + delta);
				edit.adjustLength(marker.getLength() - edit.getLength());
			}
		}
	}		
}
