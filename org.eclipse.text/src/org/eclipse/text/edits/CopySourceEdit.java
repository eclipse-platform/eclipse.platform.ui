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

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

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
public final class CopySourceEdit extends AbstractTransferEdit {

	private String fContent= ""; //$NON-NLS-1$
	private CopyTargetEdit fTarget;
	/* package */ int fCounter;
	private ISourceModifier fModifier;

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

	/* non Java-doc
	 * @see TextEdit#postProcessCopy
	 */	
	protected void postProcessCopy(TextEditCopier copier) {
		if (fTarget != null) {
			((CopySourceEdit)copier.getCopy(this)).setTargetEdit((CopyTargetEdit)copier.getCopy(fTarget));
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#checkIntegrity
	 */	
	protected void checkIntegrity() throws MalformedTreeException {
		if (fTarget == null)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopySourceEdit.no_target")); //$NON-NLS-1$
		if (fTarget.getSourceEdit() != this)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopySourceEdit.different_source")); //$NON-NLS-1$
	}
	
	/* package */ void perform(IDocument document) throws BadLocationException {
		fContent= getContent(document);
		if (++fCounter == 2 && !fTarget.isDeleted()) {
			try {
				IRegion region= fTarget.getRegion();
				document.replace(region.getOffset(), region.getLength(), fContent);
			} finally {
				clearContent();
			}
		}
	}

	private String getContent(IDocument document) throws BadLocationException {
		String result= document.get(getOffset(), getLength());
		if (fModifier != null) {
			IDocument newDocument= new Document(result);
			TextEdit newEdit= new MultiTextEdit(0, getLength());
			ReplaceEdit[] replaces= fModifier.getModifications(result);
			try {
				for (int i= 0; i < replaces.length; i++) {
					newEdit.addChild(replaces[i]);
				}
				newEdit.apply(newDocument);
			} catch (MalformedTreeException e) {
				throw new BadLocationException();
			}
			result= newDocument.get();
		}
		return result;
	}
	
	/* package */ void update(DocumentEvent event, TreeIterationInfo info) {
		// Executing the copy source edit means inserting the text
		// at target position. So we have to update the edits around
		// the target.
		fTarget.update(event);
	}
	
	/* package */ String getContent() {
		return fContent;
	}
	
	/* package */ void clearContent() {
		fContent= null;
	}
}
