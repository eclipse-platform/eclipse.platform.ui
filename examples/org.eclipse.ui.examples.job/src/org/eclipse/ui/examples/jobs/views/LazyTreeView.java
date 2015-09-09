/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.views;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either
 * in this or another plug-in (e.g. the workspace). The view is connected to
 * the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label
 * provider can be shared between views in order to ensure that objects of the
 * same type are presented in the same way everywhere.
 * <p>
 */
public class LazyTreeView extends ViewPart {
	protected TreeViewer viewer;
	protected Button serializeButton, batchButton;

	@Override
	public void createPartControl(Composite top) {
		Composite parent = new Composite(top, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		parent.setLayoutData(data);
//		parent.setBackground(WorkbenchColors.getSystemColor(SWT.COLOR_WHITE));
		serializeButton = new Button(parent, SWT.CHECK | SWT.FLAT);
		serializeButton.setText("Serialize fetch jobs"); //$NON-NLS-1$
//		serializeButton.setBackground(WorkbenchColors.getSystemColor(SWT.COLOR_WHITE));
		serializeButton.setSelection(SlowElementAdapter.isSerializeFetching());
		serializeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SlowElementAdapter.setSerializeFetching(serializeButton.getSelection());
			}
		});
		serializeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		batchButton = new Button(parent, SWT.CHECK | SWT.FLAT);
		batchButton.setText("Batch returned children"); //$NON-NLS-1$
//		batchButton.setBackground(WorkbenchColors.getSystemColor(SWT.COLOR_WHITE));
		serializeButton.setSelection(SlowElementAdapter.isBatchFetchedChildren());
		batchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SlowElementAdapter.setBatchFetchedChildren(batchButton.getSelection());
			}
		});
		batchButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new DeferredContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setInput(new SlowElement("root")); //$NON-NLS-1$
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
