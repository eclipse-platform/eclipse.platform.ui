/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import java.util.Set;
import org.eclipse.e4.core.internal.contexts.Computation;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class ContextLinks {

	static private class LinksContentProvider implements ITreeContentProvider {

		private EclipseContext selectedContext;

		public LinksContentProvider() {
			// placeholder
		}

		public void dispose() {
			selectedContext = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			selectedContext = (EclipseContext) newInput;
		}

		public Object[] getElements(Object inputElement) {
			if (selectedContext == null)
				return new Object[0];
			Set<String> listeners = selectedContext.getRawListenerNames();
			return listeners.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof String) {
				Set<Computation> tmp = selectedContext.getListeners((String) parentElement);
				return tmp.toArray();
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof String) {
				Set<Computation> tmp = selectedContext.getListeners((String) element);
				return !tmp.isEmpty();
			}
			return false;
		}
	}

	final private TabFolder folder;
	private TreeViewer dataViewer;
	private TabItem tabData;

	public ContextLinks(TabFolder folder) {
		this.folder = folder;
	}

	public TreeViewer createControls() {
		tabData = new TabItem(folder, SWT.NONE, 2);
		tabData.setText(ContextMessages.linksTab);

		Composite pageData = new Composite(folder, SWT.NONE);
		tabData.setControl(pageData);

		new Label(pageData, SWT.NONE).setText(ContextMessages.linksLabel);

		GridLayout rightPaneLayout = new GridLayout();
		rightPaneLayout.marginHeight = 0;
		rightPaneLayout.marginWidth = 0;
		pageData.setLayout(rightPaneLayout);

		FilteredTree dataTree = new FilteredTree(pageData, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(), true);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		dataTree.setLayoutData(gridData);
		dataViewer = dataTree.getViewer();

		LinksContentProvider contentProvider = new LinksContentProvider();

		dataViewer.setContentProvider(contentProvider);
		dataViewer.setLabelProvider(new LabelProvider());

		dataTree.getPatternFilter().setIncludeLeadingWildcard(true);
		return dataViewer;
	}
}
