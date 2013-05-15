/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.internal.contexts.Computation;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
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
import org.eclipse.swt.widgets.Tree;

public class ContextsView {

	public static final String SELECT_EVENT = "e4/contexts/debug/selectContext"; //$NON-NLS-1$
	public static final String REFRESH_EVENT = "e4/contexts/debug/refreshView"; //$NON-NLS-1$

	protected TreeViewer treeViewer;
	protected TreeViewer dataViewer;
	protected ContextAllocation allocationsViewer;
	protected TreeViewer linksViewer;

	protected ContextTreeProvider treeProvider;

	protected Button diffButton;
	protected Button snapshotButton;
	protected Button autoUpdateButton;

	@Inject
	public ContextsView(Composite parent, MPart part) {
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

		treeViewer = new TreeViewer(treeComposite);
		Tree tree = treeViewer.getTree();
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		tree.setLayoutData(gridData);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				@SuppressWarnings("unchecked")
				WeakReference<EclipseContext> selected = (WeakReference<EclipseContext>) selection.getFirstElement();
				selectedContext((selected == null) ? null : selected.get());
			}
		});

		treeProvider = new ContextTreeProvider(this, parent.getDisplay());
		treeViewer.setContentProvider(treeProvider);
		treeViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				@SuppressWarnings("unchecked")
				WeakReference<EclipseContext> ref = (WeakReference<EclipseContext>) element;
				EclipseContext parentContext = ref.get();
				if (parentContext != null)
					return parentContext.toString();
				return ContextMessages.contextGCed;
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

		Composite buttons = new Composite(treeComposite, SWT.NONE);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		Group leaksHelper = new Group(buttons, SWT.NONE);
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

		autoUpdateButton = new Button(buttons, SWT.CHECK);
		autoUpdateButton.setText(ContextMessages.autoUpdateButton);
		autoUpdateButton.setSelection(true);
		autoUpdateButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (autoUpdateButton.getSelection()) {
					treeProvider.setAutoUpdates(true);
					fullRefresh();
				} else {
					treeProvider.setAutoUpdates(false);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		createToolbar(part);
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	protected void fullRefresh() {
		refresh();
		ITreeSelection selection = (ITreeSelection) treeViewer.getSelection();
		if (!selection.isEmpty()) {
			@SuppressWarnings("unchecked")
			WeakReference<EclipseContext> ref = (WeakReference<EclipseContext>) selection.getFirstElement();
			EclipseContext selectedContext = ref.get();
			selectedContext(selectedContext);
		}
	}

	protected void selectedContext(IEclipseContext selected) {
		dataViewer.setInput(selected);
		allocationsViewer.setInput(selected);
		linksViewer.setInput(selected);
	}

	public void refresh() {
		treeViewer.refresh();
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

	private void createToolbar(MPart part) {
		MToolBar toolBar = part.getToolbar();
		if (toolBar != null) // assume it was already filled last time view was opened
			return;

		toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		// target button
		MDirectToolItem toolItem = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		toolItem.setIconURI("platform:/plugin/org.eclipse.e4.core.contexts.debug/icons/full/obj16/target.gif");
		toolItem.setTooltip(ContextMessages.targetButtonTooltip);
		toolItem.setContributionURI("bundleclass://org.eclipse.e4.core.contexts.debug/org.eclipse.e4.core.internal.contexts.debug.ui.FindTargetAction");
		toolBar.getChildren().add(toolItem);

		// refresh button
		MDirectToolItem toolItem2 = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		toolItem2.setIconURI("platform:/plugin/org.eclipse.e4.core.contexts.debug/icons/full/obj16/refresh.gif");
		toolItem2.setTooltip(ContextMessages.refreshButtonTooltip);
		toolItem2.setContributionURI("bundleclass://org.eclipse.e4.core.contexts.debug/org.eclipse.e4.core.internal.contexts.debug.ui.RefreshViewAction");
		toolBar.getChildren().add(toolItem2);
	}

	@Inject
	@Optional
	public void setSelection(@EventTopic(SELECT_EVENT) TreePath path) {
		if (path == null)
			return;
		TreeSelection selection = new TreeSelection(path);
		treeViewer.setSelection(selection, true);
		treeViewer.getTree().setFocus();
	}

	@Inject
	@Optional
	public void setSelection(@EventTopic(REFRESH_EVENT) Object data) {
		fullRefresh();
	}

}
