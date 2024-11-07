/*******************************************************************************
 * Copyright (c) 2004, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Policy;

/**
 * The ProgressMonitorJobsDialog is the progress monitor dialog used by the
 * progress service to allow locks to show the current jobs.
 */
public class ProgressMonitorJobsDialog extends ProgressMonitorDialog {
	private DetailedProgressViewer viewer;

	/**
	 * The height of the viewer. Set when the details button is selected.
	 */
	private int viewerHeight = -1;

	Composite viewerComposite;

	private Button detailsButton;

	private long watchTime = -1;

	/** Flag to prevent nested closes. */
	protected boolean alreadyClosed = false;

	private IProgressMonitor wrapperedMonitor;

	/**
	 * Cache initial enablement in case the enablement state is set before the
	 * button is created
	 */
	protected boolean enableDetailsButton = false;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param parent dialogs parent shell
	 */
	public ProgressMonitorJobsDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite) super.createDialogArea(parent);
		createExtendedDialogArea(parent);
		return top;
	}

	/**
	 * Create the extensions to the dialog area.
	 *
	 * @param parent the parent composite
	 */
	protected void createExtendedDialogArea(Composite parent) {
		viewerComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		viewerComposite.setLayout(layout);
		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewerData.horizontalSpan = 2;
		viewerData.heightHint = 0;
		viewerComposite.setLayoutData(viewerData);
	}

	/**
	 * The details button has been selected. Open or close the progress viewer as
	 * appropriate.
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
			detailsButton.setText(ProgressMessages.ProgressMonitorJobsDialog_DetailsTitle);
		} else {
			// Abort if there are no jobs visible
			if (ProgressManager.getInstance().getRootElements(Policy.DEBUG_SHOW_ALL_JOBS).length == 0) {
				detailsButton.setEnabled(false);
				return;
			}

			viewer = new DetailedProgressViewer(viewerComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			viewer.setComparator(ProgressManagerUtil.getProgressViewerComparator());

			viewer.setContentProvider(new ProgressViewerContentProvider(viewer, true, false) {
				@Override
				public Object[] getElements(Object inputElement) {
					return super.getElements(inputElement);
				}
			});

			viewer.setLabelProvider(new ProgressLabelProvider());
			viewer.setInput(this);
			GridData viewerData = new GridData(GridData.FILL_BOTH);
			viewer.getControl().setLayoutData(viewerData);
			GridData viewerCompositeData = (GridData) viewerComposite.getLayoutData();
			viewerCompositeData.heightHint = convertHeightInCharsToPixels(10);
			viewerComposite.layout(true);
			viewer.getControl().setVisible(true);
			viewerHeight = viewerComposite.computeTrim(0, 0, 0, viewerCompositeData.heightHint).height;
			detailsButton.setText(ProgressMessages.ProgressMonitorJobsDialog_HideTitle);
			shell.setSize(shellSize.x, shellSize.y + viewerHeight);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createDetailsButton(parent);
	}

	/**
	 * Create a spacer label to get the layout to not bunch the widgets.
	 *
	 * @param parent The parent of the new button.
	 */
	protected void createSpacer(Composite parent) {
		// Make a label to force the spacing
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
	}

	/**
	 * Create the details button for the receiver.
	 *
	 * @param parent The parent of the new button.
	 */
	protected void createDetailsButton(Composite parent) {
		detailsButton = createButton(parent, IDialogConstants.DETAILS_ID,
				ProgressMessages.ProgressMonitorJobsDialog_DetailsTitle, false);
		detailsButton.addSelectionListener(widgetSelectedAdapter(e -> handleDetailsButtonSelect()));
		detailsButton.setCursor(arrowCursor);
		detailsButton.setEnabled(enableDetailsButton);
	}

	@Override
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
		if (arrowCursor == null) {
			Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
			arrowCursor = cursor;
			composite.addDisposeListener(e -> cursor.dispose());
		}
		createButtonsForButtonBar(composite);
		return composite;
	}

	@Override
	protected void clearCursors() {
		if (detailsButton != null && !detailsButton.isDisposed()) {
			detailsButton.setCursor(null);
		}
		super.clearCursors();
	}

	@Override
	protected void updateForSetBlocked(IStatus reason) {
		if (alreadyClosed)
			return;

		super.updateForSetBlocked(reason);
		enableDetails(true);
		if (viewer == null) {
			handleDetailsButtonSelect();
		}
	}

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		// if it is run in the UI Thread don't do anything.
		if (!fork) {
			enableDetails(false);
		}
		super.run(fork, cancelable, runnable);
	}

	/**
	 * Set the enable state of the details button now or when it will be created.
	 *
	 * @param enableState a boolean to indicate the preferred' state
	 */
	protected void enableDetails(boolean enableState) {
		if (detailsButton == null) {
			enableDetailsButton = enableState;
		} else {
			detailsButton.setEnabled(enableState);
		}
	}

	/**
	 * Start watching the ticks. When the long operation time has passed open the
	 * dialog.
	 */
	public void watchTicks() {
		watchTime = System.currentTimeMillis();
	}

	/**
	 * Create a monitor for the receiver that wrappers the superclasses monitor.
	 */
	public void createWrapperedMonitor() {
		wrapperedMonitor = new IProgressMonitor() {

			IProgressMonitor superMonitor = ProgressMonitorJobsDialog.super.getProgressMonitor();

			@Override
			public void beginTask(String name, int totalWork) {
				superMonitor.beginTask(name, totalWork);
				checkTicking();
			}

			/**
			 * Check if we have ticked in the last 800ms.
			 */
			private void checkTicking() {
				if (watchTime < 0) {
					return;
				}
				if ((System.currentTimeMillis() - watchTime) > ProgressManager.getInstance().getLongOperationTime()) {
					watchTime = -1;
					openDialog();
				}
			}

			/**
			 * Open the dialog in the ui Thread
			 */
			private void openDialog() {
				if (!PlatformUI.isWorkbenchRunning()) {
					return;
				}

				PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
					// Reset the watch if it is not safe to open
					if (!ProgressManagerUtil.safeToOpen(ProgressMonitorJobsDialog.this, null)) {
						watchTicks();
						return;
					}

					if (!alreadyClosed) {
						open();
					}
				});
			}

			@Override
			public void done() {
				superMonitor.done();
				checkTicking();
			}

			@Override
			public void internalWorked(double work) {
				superMonitor.internalWorked(work);
				checkTicking();
			}

			@Override
			public boolean isCanceled() {
				return superMonitor.isCanceled();
			}

			@Override
			public void setCanceled(boolean value) {
				superMonitor.setCanceled(value);

			}

			@Override
			public void setTaskName(String name) {
				superMonitor.setTaskName(name);
				checkTicking();

			}

			@Override
			public void subTask(String name) {
				superMonitor.subTask(name);
				checkTicking();
			}

			@Override
			public void worked(int work) {
				superMonitor.worked(work);
				checkTicking();

			}

			@Override
			public void clearBlocked() {
				// We want to open on blocking too
				superMonitor.clearBlocked();

			}

			@Override
			public void setBlocked(IStatus reason) {
				openDialog();
				superMonitor.setBlocked(reason);
			}

		};
	}

	@Override
	public IProgressMonitor getProgressMonitor() {
		if (wrapperedMonitor == null) {
			createWrapperedMonitor();
		}
		return wrapperedMonitor;
	}

	@Override
	public boolean close() {
		alreadyClosed = true;// As this sometimes delayed cache if it was already closed
		boolean result = super.close();
		if (!result) {// If it fails reset the flag
			alreadyClosed = false;
		}
		return result;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
