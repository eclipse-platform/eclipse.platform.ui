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
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

/**
 * A move target edit denotes the target of a move operation. Move
 * target edits are only valid inside an edit tree if they have a
 * corresponding source edit. Furthermore a target edit can't
 * can't be a direct or indirect child of its associated source edit. 
 * Violating one of two requirements will result in a <code>
 * MalformedTreeException</code> when executing the edit tree.
 * <p>
 * Move target edits can't be used as a parent for other edits.
 * Trying to add an edit to a move target edit results in a <code>
 * MalformedTreeException</code> as well.
 * 
 * @see org.eclipse.text.edits.MoveSourceEdit
 * @see org.eclipse.text.edits.CopyTargetEdit
 * 
 * @since 3.0
 */
public final class MoveTargetEdit extends AbstractTransferEdit {

	private MoveSourceEdit fSource;

	/**
	 * Constructs a new move target edit
	 * 
	 * @param offset the edit's offset
	 */
	public MoveTargetEdit(int offset) {
		super(offset, 0);
	}

	/**
	 * Constructs an new move target edit
	 * 
	 * @param offset the edit's offset
	 * @param source the corresponding source edit
	 */
	public MoveTargetEdit(int offset, MoveSourceEdit source) {
		this(offset);
		setSourceEdit(source);
	}

	/*
	 * Copy constructor
	 */
	private MoveTargetEdit(MoveTargetEdit other) {
		super(other);
	}

	/**
	 * Returns the associated source edit or <code>null</code>
	 * if no source edit is associated yet.
	 * 
	 * @return the source edit or <code>null</code>
	 */
	public MoveSourceEdit getSourceEdit() {
		return fSource;
	}
					
	/**
	 * Sets the source edit.
	 * 
	 * @param edit the source edit
	 * 
	 * @exception MalformedTreeException is thrown if the target edit
	 *  is a direct or indirect child of the source edit
	 */	
	public void setSourceEdit(MoveSourceEdit edit) {
		if (fSource != edit) {
			fSource= edit;
			fSource.setTargetEdit(this);
			TextEdit parent= getParent();
			while (parent != null) {
				if (parent == fSource)
					throw new MalformedTreeException(parent, this, TextEditMessages.getString("MoveTargetEdit.wrong_parent")); //$NON-NLS-1$
				parent= parent.getParent();
			}
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#doCopy
	 */	
	protected TextEdit doCopy() {
		return new MoveTargetEdit(this);
	}
	
	/* non Java-doc
	 * @see TextEdit#postProcessCopy
	 */	
	protected void postProcessCopy(TextEditCopier copier) {
		if (fSource != null) {
			((MoveTargetEdit)copier.getCopy(this)).setSourceEdit((MoveSourceEdit)copier.getCopy(fSource));
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#checkIntegrity
	 */	
	protected void checkIntegrity() {
		if (fSource == null)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("MoveTargetEdit.no_source")); //$NON-NLS-1$
		if (fSource.getTargetEdit() != this)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("MoveTargetEdit.different_target")); //$NON-NLS-1$
	}
	
	/* non Java-doc
	 * @see TextEdit#perform
	 */	
	/* package */ void perform(IDocument document) throws BadLocationException {
		if (++fSource.fCounter == 2) {
			String source= fSource.getContent();
			fMode= INSERT;
			document.replace(getOffset(), getLength(), source);
		}
	}
	
	/* package */ void update(DocumentEvent event, TreeIterationInfo info) {
		if (fMode == INSERT) {
			// we have to substract the delta since <code>super.updateTextRange</code>
			// add the delta to the move source's children.
			int moveDelta= getOffset() - fSource.getContentOffset();
			
			super.update(event, info);			

			List sourceChildren= fSource.getContentChildren();
			move(sourceChildren, moveDelta); 
			internalSetChildren(sourceChildren);
			
		} else {
			Assert.isTrue(false);
		}
		
	}
}
