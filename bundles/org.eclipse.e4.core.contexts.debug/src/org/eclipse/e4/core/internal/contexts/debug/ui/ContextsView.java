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

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.Computation;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Tree;
import org.osgi.framework.Bundle;

public class ContextsView {

	private static final String TARGET_IMG = "icons/full/obj16/target.gif"; //$NON-NLS-1$

	// TBD this is from internal AbstractPartRenderer.OWNING_ME
	// make that API
	private static final String OWNING_ME = "modelElement"; //$NON-NLS-1$

	protected TreeViewer treeViewer;
	protected TreeViewer dataViewer;
	protected ContextAllocation allocationsViewer;
	protected TreeViewer linksViewer;

	protected ContextTreeProvider treeProvider;

	protected Button diffButton;
	protected Button snapshotButton;
	protected Button updateButton;
	protected Button autoUpdateButton;
	protected Button targetButton;

	protected Cursor targetCursor;
	protected Cursor displayCursor;

	protected Image targetImage;

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

		Bundle myBundle = Activator.getDefault().getBundle();
		URL targetImageURL = myBundle.getEntry(TARGET_IMG);
		ImageDescriptor desc = ImageDescriptor.createFromURL(targetImageURL);
		targetImage = desc.createImage();

		targetButton = new Button(treeComposite, SWT.NONE);
		targetButton.setImage(targetImage);
		targetButton.setToolTipText(ContextMessages.targetButtonTooltip);

		targetCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_CROSS);

		targetButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				displayCursor = targetButton.getCursor();
				targetButton.setCursor(targetCursor);
				targetButton.setCapture(true);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		final Display display = parent.getDisplay();
		targetButton.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				// nothing
			}
			public void mouseDown(MouseEvent e) {
				// nothing
			}
			public void mouseUp(MouseEvent e) {
				Control control = display.getCursorControl();
				if (targetButton == control)
					return;
				IEclipseContext targetContext = null;
				while (control != null) {
					Object data = control.getData(OWNING_ME);
					if (data instanceof MContext) {
						targetContext = ((MContext) data).getContext();
						if (targetContext != null)
							break;
					}
					control = control.getParent();
				}
				if (targetContext != null) {
					List<WeakContextRef> contexts = new ArrayList<WeakContextRef>();
					while (targetContext != null) {
						contexts.add(new WeakContextRef((EclipseContext) targetContext));
						targetContext = targetContext.getParent();
					}
					Collections.reverse(contexts);
					TreePath path = new TreePath(contexts.toArray());

					TreeSelection selection = new TreeSelection(path);
					treeViewer.setSelection(selection, true);
					treeViewer.getTree().setFocus();
				}
				targetButton.setCursor(displayCursor);
				targetButton.setCapture(false);
			}
		});

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
		buttons.setLayout(new GridLayout(2, true));
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

		Group updateGroup = new Group(buttons, SWT.NONE);
		updateGroup.setLayout(new GridLayout(2, false));
		updateGroup.setText(ContextMessages.refreshGroup);
		updateGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		autoUpdateButton = new Button(updateGroup, SWT.CHECK);
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

		updateButton = new Button(updateGroup, SWT.PUSH);
		updateButton.setText(ContextMessages.updateButton);
		updateButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fullRefresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

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

	@PreDestroy
	public void dispose() {
		if (targetCursor != null) {
			targetCursor.dispose();
			targetCursor = null;
		}
		if (targetImage != null) {
			targetImage.dispose();
			targetImage = null;
		}
		displayCursor = null;
	}

}
