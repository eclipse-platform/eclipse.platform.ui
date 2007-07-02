/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.statushandlers;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.internal.statushandlers.StatusNotificationManager.StatusInfo;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;

/**
 * A dialog for displaying
 * 
 */
public class StatusDialog extends ErrorDialog {

	/*
	 * Preference used to indicate whether the user should be prompted to
	 * confirm the execution of the job's goto action
	 */
	private static final String PREF_SKIP_GOTO_ACTION_PROMPT = "pref_skip_goto_action_prompt"; //$NON-NLS-1$

	/*
	 * The id of the goto action button
	 */
	private static final int GOTO_ACTION_ID = IDialogConstants.CLIENT_ID + 1;

	private TableViewer statusListViewer;

	private StatusInfo selectedStatus;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 * @param msg
	 * @param statusInfo
	 * @param displayMask
	 */
	public StatusDialog(Shell parentShell, StatusInfo statusInfo,
			int displayMask, boolean modal) {
		super(parentShell, (String)statusInfo.getStatus().getProperty(
				StatusAdapter.TITLE_PROPERTY), statusInfo.getStatus()
				.getStatus().getMessage(), statusInfo.getStatus().getStatus(),
				displayMask);
		setShellStyle(SWT.RESIZE | SWT.MAX | SWT.MIN | getShellStyle());
		this.selectedStatus = statusInfo;
		setBlockOnOpen(false);

		if (!modal) {
			setShellStyle(~SWT.APPLICATION_MODAL & getShellStyle());
		}

		String reason = WorkbenchMessages.StatusDialog_checkDetailsMessage;
		if (statusInfo.getStatus().getStatus().getException() != null) {
			reason = statusInfo.getStatus().getStatus().getException()
					.getMessage() == null ? statusInfo.getStatus().getStatus()
					.getException().toString() : statusInfo.getStatus()
					.getStatus().getException().getMessage();
		}
		this.message = NLS.bind(WorkbenchMessages.StatusDialog_reason,
				new Object[] { statusInfo.getDisplayString(), reason });
	}

	/**
	 * Method which should be invoked when new errors become available for
	 * display
	 */
	void refresh() {

		if (AUTOMATED_MODE) {
			return;
		}

		// Do not refresh if we are in the process of
		// opening or shutting down
		if (dialogArea == null || dialogArea.isDisposed()) {
			return;
		}

		if (isMultipleStatusDialog()) {
			if (statusListViewer == null) {
				// The job list doesn't exist so create it.
				setMessage(ProgressMessages.JobErrorDialog_MultipleErrorsMessage);
				getShell().setText(
						ProgressMessages.JobErrorDialog_MultipleErrorsTitle);
				createStatusListArea((Composite) dialogArea);
				showDetailsArea();
			}
			refreshStatusList();
		}
		updateEnablements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ErrorDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		IAction gotoAction = getGotoAction();
		String text = null;
		if (gotoAction != null) {
			text = gotoAction.getText();
		}
		if (text == null) {
			// Text is set to this initiallybut will be changed for active job
			text = ProgressMessages.JobErrorDialog_CustomJobText;
		}
		createButton(parent, GOTO_ACTION_ID, text, false);
		super.createButtonsForButtonBar(parent);
	}

	/*
	 * Update the button enablements
	 */
	private void updateEnablements() {
		Button details = getButton(IDialogConstants.DETAILS_ID);
		if (details != null) {
			details.setEnabled(true);
		}
		Button gotoButton = getButton(GOTO_ACTION_ID);
		if (gotoButton != null) {
			IAction gotoAction = getGotoAction();
			boolean hasValidGotoAction = gotoAction != null;
			String text = gotoButton.getText();
			String newText = null;
			if (hasValidGotoAction) {
				newText = gotoAction.getText();
			}
			if (newText == null) {
				hasValidGotoAction = false;
				newText = ProgressMessages.JobErrorDialog_CustomJobText;
			}
			if (!newText.equals(text)) {
				gotoButton.setText(newText);
			}
			gotoButton.setEnabled(hasValidGotoAction);
			gotoButton.setVisible(hasValidGotoAction);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ErrorDialog#buttonPressed(int)
	 */
	protected void buttonPressed(int id) {
		if (id == GOTO_ACTION_ID) {
			IAction gotoAction = getGotoAction();
			if (gotoAction != null) {
				if (!isMultipleStatusDialog() || isPromptToClose()) {
					okPressed(); // close the dialog
					gotoAction.run(); // run the goto action
				}
			}
		}
		super.buttonPressed(id);
	}
	
	public boolean isModal()
	{
		return ((getShellStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);
	}

	/*
	 * Prompt to inform the user that the dialog will close and the errors
	 * cleared.
	 */
	private boolean isPromptToClose() {
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		if (!store.contains(PREF_SKIP_GOTO_ACTION_PROMPT)
				|| !store.getString(PREF_SKIP_GOTO_ACTION_PROMPT).equals(
						MessageDialogWithToggle.ALWAYS)) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle
					.openOkCancelConfirm(
							getShell(),
							ProgressMessages.JobErrorDialog_CloseDialogTitle,
							ProgressMessages.JobErrorDialog_CloseDialogMessage,
							ProgressMessages.JobErrorDialog_DoNotShowAgainMessage,
							false, store, PREF_SKIP_GOTO_ACTION_PROMPT);
			return dialog.getReturnCode() == OK;
		}
		return true;
	}

	private IAction getGotoAction() {
		Object property = null;

		StatusAdapter statusAdapter = selectedStatus.getStatus();
		Job job = (Job) (statusAdapter.getAdapter(Job.class));
		if (job != null) {
			property = job.getProperty(IProgressConstants.ACTION_PROPERTY);
		}

		if (property instanceof IAction) {
			return (IAction) property;
		}
		return null;
	}

	/**
	 * This method sets the message in the message label.
	 * 
	 * @param messageString -
	 *            the String for the message area
	 */
	private void setMessage(String messageString) {
		// must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed()) {
			return;
		}
		messageLabel.setText(message);
	}

	/**
	 * Create an area that allow the user to select one of multiple jobs that
	 * have reported errors
	 * 
	 * @param parent -
	 *            the parent of the area
	 */
	private void createStatusListArea(Composite parent) {
		// Display a list of jobs that have reported errors
		statusListViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		statusListViewer.setComparator(getViewerComparator());
		Control control = statusListViewer.getControl();
		GridData data = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		data.heightHint = convertHeightInCharsToPixels(10);
		control.setLayoutData(data);
		initContentProvider();
		initLabelProvider();
		statusListViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						handleSelectionChange();
					}
				});
		applyDialogFont(parent);
	}

	/*
	 * Return whether there are multiple errors to be displayed
	 */
	private boolean isMultipleStatusDialog() {
		return getManager().getErrors().size() > 1;
	}

	/*
	 * Get the notificationManager that this is being created for.
	 */
	private StatusNotificationManager getManager() {
		return StatusNotificationManager.getInstance();
	}

	/**
	 * Return the selected error info.
	 * 
	 * @return ErrorInfo
	 */
	public StatusInfo getSelectedError() {
		return selectedStatus;
	}

	/**
	 * Return a viewer sorter for looking at the jobs.
	 * 
	 * @return ViewerSorter
	 */
	private ViewerComparator getViewerComparator() {
		return new ViewerComparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((Comparable) e1).compareTo(e2);
			}
		};
	}

	/**
	 * Sets the content provider for the viewer.
	 */
	protected void initContentProvider() {
		IContentProvider provider = new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				// Nothing of interest here
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return getManager().getErrors().toArray();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				if (newInput != null) {
					refreshStatusList();
				}
			}
		};
		statusListViewer.setContentProvider(provider);
		statusListViewer.setInput(getManager());
		statusListViewer.setSelection(new StructuredSelection(selectedStatus));
	}

	/**
	 * Refresh the contents of the viewer.
	 */
	void refreshStatusList() {
		if (statusListViewer != null
				&& !statusListViewer.getControl().isDisposed()) {
			statusListViewer.refresh();
			Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
			getShell().setSize(newSize);
		}
		setStatus(selectedStatus.getStatus().getStatus());
	}

	private void initLabelProvider() {
		ITableLabelProvider provider = new ITableLabelProvider() {
			Map imageTable = new HashMap();

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void addListener(ILabelProviderListener listener) {
				// Do nothing
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
			 */
			public void dispose() {
				if (!imageTable.isEmpty()) {
					for (Iterator iter = imageTable.values().iterator(); iter
							.hasNext();) {
						Image image = (Image) iter.next();
						image.dispose();
					}
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
			 *      int)
			 */
			public Image getColumnImage(Object element, int columnIndex) {
				if (element != null) {
					StatusAdapter statusAdapter = ((StatusInfo) element)
							.getStatus();
					Job job = (Job) (statusAdapter.getAdapter(Job.class));
					if (job != null) {
						return getIcon(job);
					}
				}
				return null;
			}

			/*
			 * Get the icon for the job. Code copied from NewProgressViewer
			 */
			private Image getIcon(Job job) {
				if (job != null) {

					Object property = job
							.getProperty(IProgressConstants.ICON_PROPERTY);

					// If we already have an image cached, return it
					Image im = (Image) imageTable.get(property);
					if (im != null) {
						return im;
					}

					// Create an image from the job's icon property or family
					Display display = getShell().getDisplay();
					if (property instanceof ImageDescriptor) {
						im = ((ImageDescriptor) property).createImage(display);
						imageTable.put(property, im); // Cache for disposal
					} else if (property instanceof URL) {
						im = ImageDescriptor.createFromURL((URL) property)
								.createImage(display);
						imageTable.put(property, im); // Cache for disposal
					} else {
						im = ProgressManager.getInstance().getIconFor(job);
						// No need to cache since the progress manager will
					}
					return im;
				}
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
			 *      int)
			 */
			public String getColumnText(Object element, int columnIndex) {
				return ((StatusInfo) element).getDisplayString();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
			 *      java.lang.String)
			 */
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void removeListener(ILabelProviderListener listener) {
				// Do nothing
			}
		};
		statusListViewer.setLabelProvider(provider);
	}

	/**
	 * Get the single selection. Return null if the selection is not just one
	 * element.
	 * 
	 * @return ErrorInfo or <code>null</code>.
	 */
	private StatusInfo getSingleSelection() {
		ISelection rawSelection = statusListViewer.getSelection();
		if (rawSelection != null
				&& rawSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) rawSelection;
			if (selection.size() == 1) {
				return (StatusInfo) selection.getFirstElement();
			}
		}
		return null;
	}

	public boolean close() {
		Rectangle shellPosition = getShell().getBounds();
		boolean result = super.close();
		ProgressManagerUtil.animateDown(shellPosition);
		return result;
	}

	public int open() {
		int result = super.open();
		setStatus(selectedStatus.getStatus().getStatus());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 */
	protected void initializeBounds() {
		// We need to refesh here instead of in createContents
		// because the showDetailsArea requires that the content
		// composite be set
		refresh();
		super.initializeBounds();
		Rectangle shellPosition = getShell().getBounds();
		ProgressManagerUtil.animateUp(shellPosition);
	}

	/**
	 * The selection in the multiple job list has changed. Update widget
	 * enablements and repopulate the list.
	 */
	void handleSelectionChange() {
		StatusInfo newSelection = getSingleSelection();
		if (newSelection != null && newSelection != selectedStatus) {
			selectedStatus = newSelection;
			setStatus(selectedStatus.getStatus().getStatus());
			updateEnablements();
			showDetailsArea();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ErrorDialog#shouldShowDetailsButton()
	 */
	protected boolean shouldShowDetailsButton() {
		return true;
	}
}
