/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * The ProgressMonitorJobsDialog is the progress monitor dialog used by the
 * progress service to allow locks to show the current jobs.
 */
public class ProgressMonitorJobsDialog extends ProgressMonitorDialog {

	private ProgressTreeViewer viewer;

	/**
	 * The height of the viewer. Set when the details button is selected.
	 */
	private int viewerHeight = -1;

	Composite viewerComposite;

	private Button detailsButton;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 */
	public ProgressMonitorJobsDialog(Shell parent) {
		super(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite top = (Composite) super.createDialogArea(parent);

		detailsButton = new Button(top, SWT.PUSH);
		detailsButton.setText(ProgressMessages.getString("ProgressMonitorJobsDialog.DetailsTitle")); //$NON-NLS-1$
		detailsButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				handleDetailsButtonSelect();
			}
		});
		
		setButtonLayoutData(detailsButton);

		//Create a dummy label as a spacer.
		new Label(parent, SWT.NONE);

		viewerComposite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		viewerComposite.setLayout(layout);
		
		GridData viewerData =
			new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		viewerData.horizontalSpan = 2;
		viewerData.heightHint = 0;
		viewerComposite.setLayoutData(viewerData);

		return top;
	}

	/**
	 * The details button has been selected. Open or close the progress viewer
	 * as appropriate.
	 *  
	 */
	void handleDetailsButtonSelect() {
		Shell shell = getShell();
		Point shellSize = shell.getSize();
		Composite composite = (Composite) getDialogArea();

		if (viewer != null) {
			viewer.getControl().dispose();
			viewer = null;
			composite.layout();
			shell.setSize(shellSize.x, shellSize.y - viewerHeight);
			detailsButton.setText(ProgressMessages.getString("ProgressMonitorJobsDialog.DetailsTitle")); //$NON-NLS-1$
		} else {
			viewer = new ProgressTreeViewer(viewerComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			viewer.setUseHashlookup(true);
			viewer.setSorter(new ViewerSorter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
				 *      java.lang.Object, java.lang.Object)
				 */
				public int compare(Viewer testViewer, Object e1, Object e2) {
					return ((Comparable) e1).compareTo(e2);
				}
			});

			IContentProvider provider = new ProgressContentProvider(viewer);
			viewer.setContentProvider(provider);
			viewer.setInput(provider);
			viewer.setLabelProvider(new ProgressLabelProvider());

			GridData viewerData =
				new GridData(
					GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			int heightHint = convertHeightInCharsToPixels(10);
			viewerData.heightHint = heightHint;
			viewer.getControl().setLayoutData(viewerData);

			Point size = viewer.getControl().computeSize(viewerComposite.getBounds().width,heightHint);
			viewer.getControl().setSize(size);
			viewerComposite.layout();
			viewer.getControl().setVisible(true);
			
			viewerHeight = viewer.getControl().getBounds().height;
			
			detailsButton.setText(ProgressMessages.getString("ProgressMonitorJobsDialog.HideTitle")); //$NON-NLS-1$
			shell.setSize(shellSize.x, shellSize.y + viewerHeight);

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		detailsButton.setCursor(arrowCursor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#clearCursors()
	 */
	protected void clearCursors() {
		if (detailsButton != null && !detailsButton.isDisposed()) {
			detailsButton.setCursor(null);
		}
		super.clearCursors();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#setOperationCancelButtonEnabled(boolean)
	 */
	protected void setOperationCancelButtonEnabled(boolean b) {
		super.setOperationCancelButtonEnabled(b);
		detailsButton.setEnabled(b);
	}

}
