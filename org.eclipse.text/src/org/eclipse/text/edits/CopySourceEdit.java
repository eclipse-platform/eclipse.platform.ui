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

import java.util.List;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * A copy source edit denotes the source of a copy operation. Copy
 * source edits are only valid inside an edit tree if they have a
 * corresponding traget edit. Furthermore the corresponding
 * target edit can't be a direct or indirect child of the source 
 * edit. Violating one of two requirements will result in a <code>
 * MalformedTreeException</code> when executing the edit tree.
 * <p>
 * A copy source edit can manange an optional source modifier. A
 * source modifier can provide a set of replace edits which will
 * to applied to the source before it gets inserted at the target
 * position.
 * 
 * @see org.eclipse.text.edits.CopyTargetEdit
 *  
 * @since 3.0 
 */
public final class CopySourceEdit extends TextEdit {

	private CopyTargetEdit fTarget;
	private ISourceModifier fModifier;
	
	private String fSourceContent= ""; //$NON-NLS-1$
	private TextEdit fSourceRoot;
	
	/**
	 * Constructs a new copy source edit.
	 * 
	 * @param offset the edit's offset
	 * @param length the edit's length
	 */
	public CopySourceEdit(int offset, int length) {
		super(offset, length);
	}

	/**
	 * Constructs a new copy source edit.
	 * 
	 * @param offset the edit's offset
	 * @param length the edit's length
	 * @param target the edit's target
	 */
	public CopySourceEdit(int offset, int length, CopyTargetEdit target) {
		this(offset, length);
		setTargetEdit(target);
	}

	/*
	 * Copy Constructor
	 */
	private CopySourceEdit(CopySourceEdit other) {
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
	public CopyTargetEdit getTargetEdit() {
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
	public void setTargetEdit(CopyTargetEdit edit) throws MalformedTreeException {
		Assert.isNotNull(edit);
		if (fTarget != edit) {
			fTarget= edit;
			fTarget.setSourceEdit(this);
		}
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
		return new CopySourceEdit(this);
	}
	
	//---- API for CopyTargetEdit ------------------------------------------------

	/* package */ String getContent() {
		return fSourceContent;
	}
	
	/* package */ void clearContent() {
		fSourceContent= null;
	}
	
	/* non Java-doc
	 * @see TextEdit#postProcessCopy
	 */	
	protected void postProcessCopy(TextEditCopier copier) {
		if (fTarget != null) {
			CopySourceEdit source= (CopySourceEdit)copier.getCopy(this);
			CopyTargetEdit target= (CopyTargetEdit)copier.getCopy(fTarget);
			if (source != null && target != null)
				source.setTargetEdit(target);
		}
	}
	
	//---- pass one ----------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see org.eclipse.text.edits.TextEdit#traversePassOne
	 */
	/* package */ void traversePassOne(TextEditProcessor processor, IDocument document) {
		if (processor.considerEdit(this)) {
			MultiTextEdit root= new MultiTextEdit(getOffset(), getLength());
			root.internalSetChildren(internalGetChildren());
			try {
				fSourceContent= document.get(getOffset(), getLength());
			} catch (BadLocationException cannotHappen) {
				Assert.isTrue(false);
			}
			fSourceRoot= root.copy();
			fixCopyTree(fSourceRoot);
			fSourceRoot.moveTree(-getOffset());
		}
		super.traversePassOne(processor, document);
	}
	
	/* non Java-doc
	 * @see TextEdit#performPassOne
	 */	
	/* package */ void performPassOne(TextEditProcessor processor, IDocument document) throws MalformedTreeException {
		if (fTarget == null)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopySourceEdit.no_target")); //$NON-NLS-1$
		if (fTarget.getSourceEdit() != this)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopySourceEdit.different_source")); //$NON-NLS-1$
		
		try {
			if (fSourceRoot.hasChildren()) {
				EditDocument subDocument= new EditDocument(fSourceContent);
				fSourceRoot.apply(subDocument, TextEdit.NONE);
				if (needsTransformation())
					applyTransformation(subDocument);
				fSourceContent= subDocument.get();
				fSourceRoot= null;
			} else {
				if (needsTransformation()) {
					EditDocument subDocument= new EditDocument(fSourceContent);
					applyTransformation(subDocument);
					fSourceContent= subDocument.get();
				}
			}
		} catch (BadLocationException cannotHappen) {
			Assert.isTrue(false);
		}		
	}
	
	private boolean needsTransformation() {
		return fModifier != null;
	}
	
	private void applyTransformation(IDocument document) throws MalformedTreeException {
		TextEdit newEdit= new MultiTextEdit(0, document.getLength());
		ReplaceEdit[] replaces= fModifier.getModifications(document.get());
		for (int i= 0; i < replaces.length; i++) {
			newEdit.addChild(replaces[i]);
		}
		try {
			newEdit.apply(document, TextEdit.NONE);
		} catch (BadLocationException cannotHappen) {
			Assert.isTrue(false);
		}
	}
	
	private void fixCopyTree(TextEdit edit) {
		TextEdit replacement= null;
		if (edit instanceof MoveSourceEdit) {
			MoveSourceEdit ms= (MoveSourceEdit)edit;
			if (ms.getTargetEdit() == null) {
				replacement= new DeleteEdit(ms.getOffset(), ms.getLength());
			}
		} else if (edit instanceof MoveTargetEdit) {
			MoveTargetEdit mt= (MoveTargetEdit)edit;
			if (mt.getSourceEdit() == null) {
				replacement= new RangeMarker(mt.getOffset(), mt.getLength());
			}
		} else if (edit instanceof CopySourceEdit) {
			CopySourceEdit cs= (CopySourceEdit)edit;
			if (cs.getTargetEdit() == null) {
				replacement= new RangeMarker(cs.getOffset(), cs.getLength());
			}
		} else if (edit instanceof CopyTargetEdit) {
			CopyTargetEdit ct= (CopyTargetEdit)edit;
			if (ct.getSourceEdit() == null) {
				replacement= new RangeMarker(ct.getOffset(), ct.getLength());
			}
		}
		if (replacement != null) {
			// replace at parent
			{
				TextEdit parent= edit.getParent();
				List children= parent.internalGetChildren();
				
				int index= children.indexOf(edit);
				children.remove(index);
				children.add(index, replacement);
				edit.internalSetParent(null);
				replacement.internalSetParent(parent);
			}
			
			// move children
			{
				TextEdit[] children= edit.removeChildren();
				for (int i= 0; i < children.length; i++) {
					TextEdit child= children[i];
					replacement.addChild(child);
					fixCopyTree(child);
				}
			}
		} else {
			TextEdit[] children= edit.getChildren();
			for (int i= 0; i < children.length; i++) {
				fixCopyTree(children[i]);
			}
		}
	}
	
	//---- pass two ----------------------------------------------------------------
	
	/* non Java-doc
	 * @see TextEdit#performPassTwo
	 */	
	/* package */ int performPassTwo(IDocument document) throws BadLocationException {
		fDelta= 0;
		return fDelta;
	}

	//---- pass three ----------------------------------------------------------------
	
	/* non Java-doc
	 * @see TextEdit#deleteChildren
	 */	
	/* package */ boolean deleteChildren() {
		return false;
	}	
}
