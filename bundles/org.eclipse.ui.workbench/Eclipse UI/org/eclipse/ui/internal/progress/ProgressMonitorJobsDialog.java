/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.progress;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
/**
 * The ProgressMonitorJobsDialog is the progress monitor dialog used by the
 * progress service to allow locks to show the current jobs.
 */
public class ProgressMonitorJobsDialog extends ProgressMonitorDialog {
	private NewProgressViewer viewer;
	/**
	 * The height of the viewer. Set when the details button is selected.
	 */
	private int viewerHeight = -1;
	Composite viewerComposite;
	private Button detailsButton;
	
	//Cache initial enablement in case the enablement state is set
	//before the button is created
	protected boolean enableDetailsButton = false;
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 */
	public ProgressMonitorJobsDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite) super.createDialogArea(parent);
		viewerComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		viewerComposite.setLayout(layout);
		GridData viewerData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
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
			detailsButton.setText(ProgressMessages
					.getString("ProgressMonitorJobsDialog.DetailsTitle")); //$NON-NLS-1$
		} else {
			viewer = new NewProgressViewer(viewerComposite, SWT.MULTI
					| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
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
			IContentProvider provider = new ProgressTreeContentProvider(viewer,
					true);
			viewer.setContentProvider(provider);
			viewer.setInput(provider);
			viewer.setLabelProvider(new ProgressLabelProvider());
			GridData viewerData = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			int heightHint = convertHeightInCharsToPixels(10);
			viewerData.heightHint = heightHint;
			viewer.getControl().setLayoutData(viewerData);
			Point size = viewer.getControl().computeSize(
					viewerComposite.getBounds().width, heightHint);
			viewer.getControl().setSize(size);
			viewerComposite.layout();
			viewer.getControl().setVisible(true);
			viewerHeight = viewer.getControl().getBounds().height;
			detailsButton.setText(ProgressMessages
					.getString("ProgressMonitorJobsDialog.HideTitle")); //$NON-NLS-1$
			shell.setSize(shellSize.x, shellSize.y + viewerHeight);
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createDetailsButton(parent);
	}
	/**
	 * Create a spacer label to get the layout to not bunch the widgets.
	 * 
	 * @param parent
	 *            The parent of the new button.
	 */
	protected void createSpacer(Composite parent) {
		//Make a label to force the spacing
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));
	}
	/**
	 * Create the details button for the receiver.
	 * 
	 * @param parent
	 *            The parent of the new button.
	 */
	protected void createDetailsButton(Composite parent) {
		detailsButton = createButton(parent, IDialogConstants.DETAILS_ID,
				ProgressMessages
						.getString("ProgressMonitorJobsDialog.DetailsTitle"), //$NON-NLS-1$
				false);
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
		detailsButton.setCursor(arrowCursor);
		detailsButton.setEnabled(enableDetailsButton);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 1; // this is incremented by createButton
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.END;
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		// Add the buttons to the button bar.
		if (arrowCursor == null)
			arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
		createButtonsForButtonBar(composite);
		return composite;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#clearCursors()
	 */
	protected void clearCursors() {
		if (detailsButton != null && !detailsButton.isDisposed()) {
			detailsButton.setCursor(null);
		}
		super.clearCursors();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#updateForSetBlocked(org.eclipse.core.runtime.IStatus)
	 */
	protected void updateForSetBlocked(IStatus reason) {
		super.updateForSetBlocked(reason);
		enableDetails(true);
		if (viewer == null) //Open the viewer if there is a block
			handleDetailsButtonSelect();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#run(boolean,
	 *      boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		//if it is run in the UI Thread don't do anything.
		if (!fork) {
			enableDetails(false);
		}
		super.run(fork, cancelable, runnable);
	}
	/**
	 * Set the enable state of the details button now or 
	 * when it will be created.
	 * @param enableState a boolean to indicate the preferred'
	 * state
	 */
	protected void enableDetails(boolean enableState) {
		if (detailsButton == null)
			enableDetailsButton = enableState;
		else
			detailsButton.setEnabled(enableState);
	}
}