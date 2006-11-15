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

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;

public class HunkDiffNode extends PatchDiffNode {

	private final HunkResult result;

	public static HunkDiffNode createDiffNode(PatchFileDiffNode parent, HunkResult result, boolean fullContext) {
		return new HunkDiffNode(result, parent, Differencer.NO_CHANGE, getAncestorElement(result, fullContext), getLeftElement(result, fullContext), getRightElement(result, fullContext));
	}
	
	private static ITypedElement getRightElement(HunkResult result, boolean fullContext) {
		return new HunkTypedElement(result, true /* isResult */, fullContext);
	}

	private static ITypedElement getLeftElement(HunkResult result,
			boolean fullContext) {
		if (fullContext && !result.isOK())
			return new UnmatchedHunkTypedElement(result);
		return new HunkTypedElement(result, false /* before state */, fullContext);
	}

	private static ITypedElement getAncestorElement(HunkResult result, boolean fullContext) {
		if (!fullContext || !result.isOK()) {
			// Don't provide an ancestor if the hunk didn't match or we're not doing fullContext
			return null;
		}
		// Make the ancestor the same as the left so we have an incoming change
		return getLeftElement(result, fullContext);
	}

	private HunkDiffNode(HunkResult result, PatchFileDiffNode parent, int kind, ITypedElement ancestor, ITypedElement left, ITypedElement right) {
		super(result.getHunk(), parent, kind, ancestor, left, right);
		this.result = result;
	}

	public HunkResult getHunkResult() {
		return result;
	}

	protected Patcher getPatcher() {
		return result.getDiffResult().getPatcher();
	}

	public boolean isManuallyMerged() {
		Object left = getLeft();
		if (left instanceof UnmatchedHunkTypedElement) {
			UnmatchedHunkTypedElement element = (UnmatchedHunkTypedElement) left;
			return element.isManuallyMerged();
		}
		return false;
	}

}
