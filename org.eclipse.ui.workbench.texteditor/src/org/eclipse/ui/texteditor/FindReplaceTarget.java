/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IRegion;


/**
 * Internal find/replace target wrapping the editor's source viewer.
 * @since 2.1
 */
class FindReplaceTarget implements IFindReplaceTarget, IFindReplaceTargetExtension, IFindReplaceTargetExtension2, IFindReplaceTargetExtension3 {

	/** The editor */
	private AbstractTextEditor fEditor;
	/** The find/replace target */
	private IFindReplaceTarget fTarget;

	/**
	 * Creates a new find/replace target.
	 *
	 * @param editor the editor
	 * @param target the wrapped find/replace target
	 */
	public FindReplaceTarget(AbstractTextEditor editor, IFindReplaceTarget target) {
		fEditor= editor;
		fTarget= target;
	}

	/**
	 * Returns the wrapped find/replace target.
	 *
	 * @return the wrapped find/replace target
	 */
	private IFindReplaceTarget getTarget() {
		return fTarget;
	}

	/**
	 * Returns the find/replace target extension.
	 *
	 * @return the find/replace target extension
	 */
	private IFindReplaceTargetExtension getExtension() {
		if (fTarget instanceof IFindReplaceTargetExtension)
			return (IFindReplaceTargetExtension) fTarget;
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTarget#canPerformFind()
	 */
	public boolean canPerformFind() {
		if (getTarget() != null)
			return getTarget().canPerformFind();
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTarget#findAndSelect(int, java.lang.String, boolean, boolean, boolean)
	 */
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
		if (getTarget() != null)
			return getTarget().findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);
		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension3#findAndSelect(int, String, boolean, boolean, boolean, boolean)
	 * @since 3.0
	 */
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		if (getTarget() instanceof IFindReplaceTargetExtension3)
			return ((IFindReplaceTargetExtension3)getTarget()).findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord, regExSearch);

		// fallback
		if (!regExSearch && getTarget() != null)
			return getTarget().findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);

		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTarget#getSelection()
	 */
	public Point getSelection() {
		if (getTarget() != null)
			return getTarget().getSelection();
		return new Point(-1, -1);
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTarget#getSelectionText()
	 */
	public String getSelectionText() {
		if (getTarget() != null)
			return getTarget().getSelectionText();
		return ""; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTarget#isEditable()
	 */
	public boolean isEditable() {
		if (getTarget() != null) {
			if (getTarget().isEditable())
				return true;
			return fEditor.isEditorInputModifiable();
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTarget#replaceSelection(java.lang.String)
	 */
	public void replaceSelection(String text) {
		if (getTarget() != null)
			getTarget().replaceSelection(text);
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension3#replaceSelection(String, boolean)
	 * @since 3.0
	 */
	public void replaceSelection(String text, boolean regExReplace) {
		if (getTarget() instanceof IFindReplaceTargetExtension3) {
			((IFindReplaceTargetExtension3)getTarget()).replaceSelection(text, regExReplace);
			return;
		}

		// fallback
		if (!regExReplace && getTarget() != null)
			getTarget().replaceSelection(text);
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#beginSession()
	 */
	public void beginSession() {
		if (getExtension() != null)
			getExtension().beginSession();
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#endSession()
	 */
	public void endSession() {
		if (getExtension() != null)
			getExtension().endSession();
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#getScope()
	 */
	public IRegion getScope() {
		if (getExtension() != null)
			return getExtension().getScope();
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#setScope(org.eclipse.jface.text.IRegion)
	 */
	public void setScope(IRegion scope) {
		if (getExtension() != null)
			getExtension().setScope(scope);
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#getLineSelection()
	 */
	public Point getLineSelection() {
		if (getExtension() != null)
			return getExtension().getLineSelection();
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#setSelection(int, int)
	 */
	public void setSelection(int offset, int length) {
		if (getExtension() != null)
			getExtension().setSelection(offset, length);
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#setScopeHighlightColor(org.eclipse.swt.graphics.Color)
	 */
	public void setScopeHighlightColor(Color color) {
		if (getExtension() != null)
			getExtension().setScopeHighlightColor(color);
	}

	/*
	 * @see org.eclipse.jface.text.IFindReplaceTargetExtension#setReplaceAllMode(boolean)
	 */
	public void setReplaceAllMode(boolean replaceAll) {
		if (getExtension() != null)
			getExtension().setReplaceAllMode(replaceAll);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IFindReplaceTargetExtension2#validateTargetState()
	 */
	public boolean validateTargetState() {
		return fEditor.validateEditorInputState();
	}
}
