/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.IFile;

public class PatchFileDiffNode extends PatchDiffNode implements IContentChangeListener {

	private final FileDiffResult result;

	public static PatchFileDiffNode createDiffNode(DiffNode parent, FileDiffResult result) {
		return new PatchFileDiffNode(result, parent, getKind(result), getAncestorElement(result), getLeftElement(result), getRightElement(result));
	}
	
	private static int getKind(FileDiffResult result) {
		if (!result.hasMatches())
			return Differencer.NO_CHANGE;
		return result.getDiff().getDiffType(result.getPatcher().isReversed()) | Differencer.RIGHT;
	}

	private static ITypedElement getRightElement(FileDiffResult result) {
		return new PatchFileTypedElement(result, true);
	}

	private static ITypedElement getLeftElement(FileDiffResult result) {
		return new PatchFileTypedElement(result, false);
	}

	private static ITypedElement getAncestorElement(FileDiffResult result) {
		return new PatchFileTypedElement(result, false);
	}

	public PatchFileDiffNode(FileDiffResult result, IDiffContainer parent, int kind,
			ITypedElement ancestor, ITypedElement left, ITypedElement right) {
		super(result.getDiff(), parent, kind, ancestor, left, right);
		this.result = result;
	}

	public FileDiffResult getDiffResult() {
		return result;
	}

	protected Patcher getPatcher() {
		return result.getPatcher();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffContainer#add(org.eclipse.compare.structuremergeviewer.IDiffElement)
	 */
	public void add(IDiffElement diff) {
		super.add(diff);
		// Listen for content changes in unmatched children so we can fire an input change
		if (diff instanceof HunkDiffNode) {
			HunkDiffNode node = (HunkDiffNode) diff;
			Object left = node.getLeft();
			if (left instanceof IContentChangeNotifier) {
				IContentChangeNotifier notifier = (IContentChangeNotifier) left;
				notifier.addContentChangeListener(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IContentChangeListener#contentChanged(org.eclipse.compare.IContentChangeNotifier)
	 */
	public void contentChanged(IContentChangeNotifier source) {
		fireChange();
	}
	
	public int getKind() {
		int kind = super.getKind();
		if (kind == Differencer.NO_CHANGE && getPatcher().hasCachedContents(getDiffResult().getDiff())) {
			return Differencer.CHANGE | Differencer.RIGHT;
		}
		return kind;
	}

	public boolean fileExists() {
		IFile file = getDiffResult().getTargetFile();
		return file != null && file.isAccessible();
	}

}
