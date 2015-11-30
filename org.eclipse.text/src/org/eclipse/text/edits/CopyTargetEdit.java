/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.edits;

import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

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
public final class CopyTargetEdit extends TextEdit {

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

	/*
	 * @see TextEdit#doCopy
	 */
	@Override
	protected TextEdit doCopy() {
		return new CopyTargetEdit(this);
	}

	/*
	 * @see TextEdit#postProcessCopy
	 */
	@Override
	protected void postProcessCopy(TextEditCopier copier) {
		if (fSource != null) {
			CopyTargetEdit target= (CopyTargetEdit)copier.getCopy(this);
			CopySourceEdit source= (CopySourceEdit)copier.getCopy(fSource);
			if (target != null && source != null)
				target.setSourceEdit(source);
		}
	}

	/*
	 * @see TextEdit#accept0
	 */
	@Override
	protected void accept0(TextEditVisitor visitor) {
		boolean visitChildren= visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor);
		}
	}

	/*
	 * @see TextEdit#traverseConsistencyCheck
	 */
	@Override
	int traverseConsistencyCheck(TextEditProcessor processor, IDocument document, List<List<TextEdit>> sourceEdits) {
		return super.traverseConsistencyCheck(processor, document, sourceEdits) + 1;
	}

	/*
	 * @see TextEdit#performConsistencyCheck
	 */
	@Override
	void performConsistencyCheck(TextEditProcessor processor, IDocument document) throws MalformedTreeException {
		if (fSource == null)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopyTargetEdit.no_source")); //$NON-NLS-1$
		if (fSource.getTargetEdit() != this)
			throw new MalformedTreeException(getParent(), this, TextEditMessages.getString("CopyTargetEdit.different_target")); //$NON-NLS-1$
	}

	/*
	 * @see TextEdit#performDocumentUpdating
	 */
	@Override
	int performDocumentUpdating(IDocument document) throws BadLocationException {
		String source= fSource.getContent();
		document.replace(getOffset(), getLength(), source);
		fDelta= source.length() - getLength();
		fSource.clearContent();
		return fDelta;
	}

	/*
	 * @see TextEdit#deleteChildren
	 */
	@Override
	boolean deleteChildren() {
		return false;
	}
}
