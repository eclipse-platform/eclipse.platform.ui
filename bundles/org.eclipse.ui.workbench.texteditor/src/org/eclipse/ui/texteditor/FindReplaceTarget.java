/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IFindReplaceTargetExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.internal.RegExUtils;


/**
 * Internal find/replace target wrapping the editor's source viewer.
 * @since 2.1
 */
class FindReplaceTarget implements IFindReplaceTarget, IFindReplaceTargetExtension, IFindReplaceTargetExtension2,
		IFindReplaceTargetExtension3, IFindReplaceTargetExtension4 {

	/** The editor */
	private AbstractTextEditor fEditor;
	/** The find/replace target */
	private IFindReplaceTarget fTarget;
	/** The preference instance scope of editors to grab preferences */
	private static final String UI_EDITORS_INSTANCE_SCOPE_NODE_NAME = "org.eclipse.ui.editors"; //$NON-NLS-1$
	/** The preference key for batch search and replace */
	private static final String EDITOR_BATCH_REPLACE = "batchReplaceEnabled"; //$NON-NLS-1$
	
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

	@Override
	public boolean canPerformFind() {
		if (getTarget() != null)
			return getTarget().canPerformFind();
		return false;
	}

	@Override
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
		if (getTarget() != null)
			return getTarget().findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);
		return -1;
	}

	@Override
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		if (getTarget() instanceof IFindReplaceTargetExtension3)
			return ((IFindReplaceTargetExtension3)getTarget()).findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord, regExSearch);

		// fallback
		if (!regExSearch && getTarget() != null)
			return getTarget().findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);

		return -1;
	}

	@Override
	public Point getSelection() {
		if (getTarget() != null)
			return getTarget().getSelection();
		return new Point(-1, -1);
	}

	@Override
	public String getSelectionText() {
		if (getTarget() != null)
			return getTarget().getSelectionText();
		return ""; //$NON-NLS-1$
	}

	@Override
	public boolean isEditable() {
		if (getTarget() != null) {
			if (getTarget().isEditable())
				return true;
			return fEditor.isEditorInputModifiable();
		}
		return false;
	}

	@Override
	public void replaceSelection(String text) {
		if (getTarget() != null)
			getTarget().replaceSelection(text);
	}

	@Override
	public void replaceSelection(String text, boolean regExReplace) {
		if (getTarget() instanceof IFindReplaceTargetExtension3) {
			((IFindReplaceTargetExtension3)getTarget()).replaceSelection(text, regExReplace);
			return;
		}

		// fallback
		if (!regExReplace && getTarget() != null)
			getTarget().replaceSelection(text);
	}

	@Override
	public void beginSession() {
		if (getExtension() != null)
			getExtension().beginSession();
	}

	@Override
	public void endSession() {
		if (getExtension() != null)
			getExtension().endSession();
	}

	@Override
	public IRegion getScope() {
		if (getExtension() != null)
			return getExtension().getScope();
		return null;
	}

	@Override
	public void setScope(IRegion scope) {
		if (getExtension() != null)
			getExtension().setScope(scope);
	}

	@Override
	public Point getLineSelection() {
		if (getExtension() != null)
			return getExtension().getLineSelection();
		return null;
	}

	@Override
	public void setSelection(int offset, int length) {
		if (getExtension() != null)
			getExtension().setSelection(offset, length);
	}

	@Override
	public void setSelection(IRegion[] regions) {
		if (fTarget instanceof IFindReplaceTargetExtension4) {
			((IFindReplaceTargetExtension4) fTarget).setSelection(regions);
		}
	}

	@Override
	public void setScopeHighlightColor(Color color) {
		if (getExtension() != null)
			getExtension().setScopeHighlightColor(color);
	}

	@Override
	public void setReplaceAllMode(boolean replaceAll) {
		if (getExtension() != null)
			getExtension().setReplaceAllMode(replaceAll);
	}

	@Override
	public boolean validateTargetState() {
		return fEditor.validateEditorInputState();
	}

	@Override
	public boolean canBatchReplace() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(UI_EDITORS_INSTANCE_SCOPE_NODE_NAME);
		if (preferences == null) {
			return false;
		}
		return preferences
				.getBoolean(EDITOR_BATCH_REPLACE, false);
	}

	@Override
	public int batchReplace(String findString, String replaceString, boolean wholeWord, boolean caseSensitive,
			boolean regExSearch, boolean incrementalSearch) {
		// Compile the raw pattern early so it can throw an exception if it's not well
		// formed.
		// The information in that exception is displayed to the user.
		if (regExSearch) {
			Pattern.compile(findString);
		}

		IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());

		Pattern pattern = RegExUtils.createRegexSearchPattern(findString, wholeWord, caseSensitive, regExSearch);
		if (incrementalSearch) {
			IRegion region = getScope();
			try {
				String selectedLines = document.get(region.getOffset(), region.getLength());
				var count = pattern.split(selectedLines, -1).length - 1;
				if (count == 0) {
					return count;
				}
				String replacedLines = pattern.matcher(selectedLines).replaceAll(replaceString);

				document.replace(region.getOffset(), region.getLength(), replacedLines);

				return count;
			} catch (BadLocationException e) {
				return 0;
			}
		}

		String documentContent = document.get();
		var count = pattern.split(documentContent, -1).length - 1;
		document.set(pattern.matcher(documentContent).replaceAll(replaceString));

		return count;
	}

}
