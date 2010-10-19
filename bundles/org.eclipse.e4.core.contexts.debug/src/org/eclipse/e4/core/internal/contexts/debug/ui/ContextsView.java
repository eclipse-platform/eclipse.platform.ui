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

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class ContextsView {

	protected TreeViewer treeViewer;
	protected TreeViewer dataViewer;
	protected ContextAllocation allocationsViewer;
	protected TreeViewer linksViewer;

	@Inject
	public ContextsView(Composite parent, IEclipseContext context) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Composite treeComposite = new Composite(sashForm, SWT.NONE);
		treeComposite.setLayout(new GridLayout());
		GridLayout compositeTreeLayout = new GridLayout();
		compositeTreeLayout.marginHeight = 0;
		compositeTreeLayout.marginWidth = 0;
		treeComposite.setLayout(compositeTreeLayout);

		treeComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Label treeLabel = new Label(treeComposite, SWT.NONE);
		treeLabel.setText(ContextMessages.contextTreeLabel);

		FilteredTree contextTree = new FilteredTree(treeComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		contextTree.setLayoutData(gridData);
		treeViewer = contextTree.getViewer();
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				IEclipseContext selected = (IEclipseContext) selection.getFirstElement();
				dataViewer.setInput(selected);
				allocationsViewer.setInput(selected);
				linksViewer.setInput(selected);
			}
		});

		treeViewer.setContentProvider(new ContextTreeProvider(this));
		treeViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return element.toString();
			}
		});
		treeViewer.setSorter(new ViewerSorter());
		treeViewer.setInput(new Object()); // can't use null

		final TabFolder folder = new TabFolder(sashForm, SWT.TOP);

		ContextData contextData = new ContextData(folder);
		dataViewer = contextData.createControls();

		allocationsViewer = new ContextAllocation(folder);
		allocationsViewer.createControls();

		ContextLinks links = new ContextLinks(folder);
		linksViewer = links.createControls();

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	public void refresh(EclipseContext changedContext) {
		treeViewer.refresh(changedContext);
	}

	@Focus
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

}
