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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A copy target edit denotes the target of a copy operation. Copy
 * target edits are only valid inside an edit tree if they have a
 * corresponding source edit. Furthermore a target edit can't
 * can't be a direct or indirect child of the associated source edit. 
 * Violating one of two requirements will result in a <code>
 * MalformedTreeException</code> when executing the edit tree.
 * <p>
 * Copy target edits can't be used as a parent for other edits.
 * Trying to add an edit to a copy target edit results in a <code>
 * MalformedTreeException</code> as well.
 * 
 * @see org.eclipse.text.edits.CopySourceEdit
 * 
 * @since 3.0
 */
public final class CopyTargetEdit extends AbstractTransferEdit {

	private CopySourceEdit fSource;

	/**
	 * Constructs a new copy target edit
	 * 
	 * @param offset the edit's offset
	 */
	public CopyTargetEdit(int offset) {
		super(offset, 0);
	}

	/**
	 * Constructs an new copy target edit
	 * 
	 * @param offset the edit's offset
	 * @param source the corresponding source edit
	 */
	public CopyTargetEdit(int offset, CopySourceEdit source) {
		this(offset);
		setSourceEdit(source);
	}

	/*
	 * Copy constructor
	 */
	private CopyTargetEdit(CopyTargetEdit other) {
		super(other);
	}

	/**
	 * Returns the associated source edit or <code>null</code>
	 * if no source edit is associated yet.
	 * 
	 * @return the source edit or <code>null</code>
	 */
	public CopySourceEdit getSourceEdit() {
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
	public void setSourceEdit(CopySourceEdit edit) throws MalformedTreeException {
		Assert.isNotNull(edit);
		if (fSource != edit) {
			fSource= edit;
			fSource.setTargetEdit(this);
			TextEdit parent= getParent();
			while (parent != null) {
				if (parent == fSource)
					throw new MalformedTreeException(parent, this, TextEditMessages.getString("CopyTargetEdit.wrong_parent")); //$NON-NLS-1$
				parent= parent.getParent();
			}
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#doCopy
	 */	
	protected TextEdit doCopy() {
		return new CopyTargetEdit(this);
	}

	/* non Java-doc
	 * @see TextEdit#postProcessCopy
	 */	
	protected void postProcessCopy(TextEditCopier copier) {
		if (fSource != null) {
			((CopyTargetEdit)copier.getCopy(this)).setSourceEdit((CopySourceEdit)copier.getCopy(fSource));
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#checkIntegrity
	 */	
	protected void checkIntegrity() throws MalformedTreeException {
		if (fSource == null)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopyTargetEdit.no_source")); //$NON-NLS-1$
		if (fSource.getTargetEdit() != this)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopyTargetEdit.different_target")); //$NON-NLS-1$
	}
	
	/* non Java-doc
	 * @see TextEdit#perform
	 */	
	/* package */ void perform(IDocument document) throws BadLocationException {
		if (++fSource.fCounter == 2 && !isDeleted()) {
			try {
				IRegion region= getRegion();
				document.replace(region.getOffset(), region.getLength(), fSource.getContent());
			} finally {
				fSource.clearContent();
			}
		}
	}
}
