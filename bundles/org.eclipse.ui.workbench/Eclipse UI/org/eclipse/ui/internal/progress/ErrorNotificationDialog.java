/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IWorkbenchActionConstants;
/**
 * The ErrorNotificationDialog is is the dialog that comes up when an
 * error has occured.
 */
public class ErrorNotificationDialog extends Dialog {
	Action clearSelectionAction;
	Action clearAllErrorsAction;
	Action showErrorAction;
	TableViewer errorViewer;
	/**
	 * Create a new instance of the receiver.
	 * @param parentShell
	 */
	public ErrorNotificationDialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(false);
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ProgressMessages
				.getString("ErrorNotificationDialog.ErrorNotificationTitle")); //$NON-NLS-1$
		newShell.addDisposeListener(new DisposeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				getManager().clearDialog();
			}
		});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite topArea = (Composite) super.createDialogArea(parent);
		errorViewer = new TableViewer(topArea, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		errorViewer.setSorter(getViewerSorter());
		errorViewer.getControl().addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
				openErrorDialog();
			}
		});
		Control control = errorViewer.getControl();
		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		data.widthHint = convertWidthInCharsToPixels(50);
		data.heightHint = convertHeightInCharsToPixels(10);
		control.setLayoutData(data);
		initContentProvider();
		initLabelProvider();
		initContextMenu();
		applyDialogFont(parent);
		return topArea;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.CLOSE_ID,
				IDialogConstants.CLOSE_LABEL, true);
		button.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				close();
			}
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
				close();
			}
		});
	}
	/**
	 * Create the clear all errors action for the receiver.
	 * @return Action
	 */
	private void createClearAllErrorsAction() {
		clearAllErrorsAction = new Action(ProgressMessages.getString("ProgressView.ClearAllAction")) {//$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				getManager().clearAllErrors();
				errorViewer.refresh();
			}
		};
	}
	/**
	 * Create the show error action for the receiver.
	 * @return Action
	 */
	private void createShowErrorAction() {
		showErrorAction = new Action(ProgressMessages.getString("ProgressView.ShowErrorAction")) {//$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				openErrorDialog();
			}
		};
	}
	
	/**
	 * Create the clear selection action for the receiver.
	 * @return Action
	 */
	private void createClearSelectionAction() {
		clearSelectionAction = new Action(ProgressMessages
				.getString("ErrorNotificationDialog.ClearSelectionAction")) {//$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				ISelection rawSelection = errorViewer.getSelection();
				if (rawSelection != null && rawSelection instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) rawSelection;
					getManager().removeErrors(selection.toList());
				}
				refresh();
			}
		};
	}
	/**
	 * Return a viewer sorter for looking at the jobs.
	 * @return
	 */
	private ViewerSorter getViewerSorter() {
		return new ViewerSorter() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
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
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				//Nothing of interest here
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return getManager().getErrors().toArray();
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				if (newInput != null)
					refresh();
			}
		};
		errorViewer.setContentProvider(provider);
		errorViewer.setInput(getManager());
	}
	/**
	 * Get the notificationManager that this is being created for.
	 * @return
	 */
	private ErrorNotificationManager getManager() {
		return ProgressManager.getInstance().errorManager;
	}
	/**
	 * Initialize the context menu for the receiver.
	 */
	private void initContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		Menu menu = menuMgr.createContextMenu(errorViewer.getControl());
		createShowErrorAction();
		createClearSelectionAction();
		createClearAllErrorsAction();
		menuMgr.add(clearSelectionAction);
		menuMgr.add(clearAllErrorsAction);
		menuMgr.add(showErrorAction);
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		errorViewer.getControl().setMenu(menu);
	}
	/**
	 * Refresh the contents of the viewer.
	 */
	void refresh() {
		errorViewer.refresh();
	}
	private void initLabelProvider() {
		ITableLabelProvider provider = new ITableLabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void addListener(ILabelProviderListener listener) {
				//Do nothing
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
			 */
			public void dispose() {
				//Do nothing
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
			 */
			public Image getColumnImage(Object element, int columnIndex) {
				return JFaceResources.getImageRegistry()
						.get(ErrorNotificationManager.ERROR_JOB_KEY);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
			 */
			public String getColumnText(Object element, int columnIndex) {
				return ((ErrorInfo) element).getDisplayString();
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
			 */
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void removeListener(ILabelProviderListener listener) {
				//Do nothing
			}
		};
		errorViewer.setLabelProvider(provider);
	}
	/**
	 * Open the error dialog on the current selection.
	 */
	private void openErrorDialog() {
		ISelection rawSelection = errorViewer.getSelection();
		if (rawSelection != null && rawSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) rawSelection;
			if (selection.size() == 1) {
				ErrorInfo element = (ErrorInfo) selection.getFirstElement();
				ErrorDialog.openError(getShell(), element.getDisplayString(), null, element
						.getErrorStatus());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		getManager().clearAllErrors();
		return super.close();
	}
}
