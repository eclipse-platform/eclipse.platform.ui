/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources;

import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * A {@link FilteredTree} subclass that hosts a {@link CommonViewer} as its
 * embedded tree viewer, allowing the Project Explorer to use the e4
 * FilteredTree infrastructure while retaining full Common Navigator behaviour.
 *
 * <p>
 * The filter bar is hidden by default and can be shown on demand via
 * {@link #setShowFilterControls(boolean)}.
 * </p>
 */
public class ProjectExplorerFilteredTree extends FilteredTree {

	private final String viewerId;

	/**
	 * Creates the filtered tree. Uses the protected no-arg super constructor so
	 * that {@link #doCreateTreeViewer} is already overridden before
	 * {@link #init} creates the controls.
	 *
	 * @param parent   parent composite (typically the displayAreas stack composite)
	 * @param viewerId the view-site ID passed to {@link CommonViewer}
	 */
	public ProjectExplorerFilteredTree(Composite parent, String viewerId) {
		super(parent); // protected ctor – does NOT call init() yet
		this.viewerId = viewerId;
		init(SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter());
	}

	@Override
	protected void init(int treeStyle, PatternFilter filter) {
		super.init(treeStyle, filter); // creates filterComposite and treeComposite
		setShowFilterControls(false);  // hide the filter bar initially
	}

	/**
	 * Returns a {@link CommonViewer} instead of the default
	 * {@link org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree.NotifyingTreeViewer}.
	 */
	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		return new CommonViewer(viewerId, parent, style);
	}

	@Override
	public CommonViewer getViewer() {
		return (CommonViewer) super.getViewer();
	}
}
