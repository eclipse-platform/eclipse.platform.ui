/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.compare.internal.patch.PatchDiffNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ExcludedPatchDiffNodesFilter extends ViewerFilter {

	private static ViewerFilter filter;

	public static ViewerFilter getInstance() {
		if (filter == null)
			filter = new ExcludedPatchDiffNodesFilter();
		return filter;
	}

	public ExcludedPatchDiffNodesFilter() {
		super();
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof PatchDiffNode) {
			PatchDiffNode node = (PatchDiffNode) element;
			return node.isEnabled();
		}
		return true;
	}
}
