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

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.Computation;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class ContextsView {

	protected TreeViewer treeViewer;
	protected TreeViewer dataViewer;
	protected ContextAllocation allocationsViewer;
	protected TreeViewer linksViewer;

	protected Button diffButton;
	protected Button snapshotButton;

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

		Group leaksHelper = new Group(treeComposite, SWT.NONE);
		leaksHelper.setLayout(new GridLayout(2, true));
		leaksHelper.setText(ContextMessages.leaksGroup);
		leaksHelper.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		snapshotButton = new Button(leaksHelper, SWT.PUSH);
		snapshotButton.setText(ContextMessages.snapshotButton);
		snapshotButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				makeSnapshot();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		diffButton = new Button(leaksHelper, SWT.PUSH);
		diffButton.setText(ContextMessages.diffButton);
		diffButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				makeDiff();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		diffButton.setEnabled(false);
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	public void refresh(EclipseContext changedContext) {
		treeViewer.refresh(changedContext);
	}

	@Focus
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	protected ContextSnapshot snapshot;

	protected void makeSnapshot() {
		// TBD do we need to "freeze" the context system while we do this?
		snapshot = new ContextSnapshot();
		diffButton.setEnabled(true);
	}

	protected void makeDiff() {
		if (snapshot == null)
			return;
		Map<EclipseContext, Set<Computation>> snapshotDiff = snapshot.diff();
		if (snapshotDiff == null) {
			MessageBox dialog = new MessageBox(snapshotButton.getShell(), SWT.OK);
			dialog.setMessage(ContextMessages.noDiffMsg);
			dialog.setText(ContextMessages.diffDialogTitle);
			dialog.open();
			return;
		}
		LeaksDialog dialog = new LeaksDialog(snapshotButton.getShell());
		dialog.setInput(snapshotDiff);
		dialog.open();
	}

}
