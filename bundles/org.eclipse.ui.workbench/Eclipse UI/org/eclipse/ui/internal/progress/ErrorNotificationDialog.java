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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * The ErrorNotificationDialog is is the dialog that comes up when an error has
 * occured.
 */
public class ErrorNotificationDialog extends Dialog {
    TableViewer errorViewer;

    Button clearButton;

    List detailsList;

    private Clipboard clipboard;

    private ErrorInfo selectedError = null;

    /**
     * Reserve room for this many details list items.
     */
    private static final int DETAILS_LIST_ITEM_COUNT = 7;

    /**
     * The nesting indent.
     */
    private static final String NESTING_INDENT = "  "; //$NON-NLS-1$

    /**
     * Create a new instance of the receiver.
     * 
     * @param parentShell
     */
    public ErrorNotificationDialog(Shell parentShell) {
        super(parentShell == null ? ProgressManagerUtil.getDefaultParent()
                : parentShell);
        setBlockOnOpen(false);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
                | SWT.RESIZE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(ProgressMessages
                .getString("ErrorNotificationDialog.ErrorNotificationTitle")); //$NON-NLS-1$
        newShell.addDisposeListener(new DisposeListener() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            public void widgetDisposed(DisposeEvent e) {
                getManager().clearDialog();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#getShellStyle()
     */
    protected int getShellStyle() {
        return super.getShellStyle() | SWT.MIN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        initializeDialogUnits(parent);
        Composite topArea = (Composite) super.createDialogArea(parent);
        errorViewer = new TableViewer(topArea, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        errorViewer.setSorter(getViewerSorter());
        errorViewer.getControl().addMouseListener(new MouseAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
             */
            public void mouseDoubleClick(MouseEvent e) {
                openErrorDialog();
            }
        });
        errorViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {

                        clearButton.setEnabled(!errorViewer.getSelection()
                                .isEmpty());
                        setDetailsContents();
                    }
                });
        Control control = errorViewer.getControl();
        GridData data = new GridData(GridData.FILL_BOTH
                | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        data.widthHint = convertWidthInCharsToPixels(60);
        data.heightHint = convertHeightInCharsToPixels(10);
        control.setLayoutData(data);
        initContentProvider();
        initLabelProvider();
        applyDialogFont(parent);

        createDetailsList(topArea);
        return topArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {

        clearButton = createButton(
                parent,
                IDialogConstants.CLIENT_ID + 2,
                ProgressMessages
                        .getString("ErrorNotificationDialog.ClearButtonTitle"), false); //$NON-NLS-1$
        clearButton.setEnabled(false);
        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ISelection rawSelection = errorViewer.getSelection();
                if (rawSelection != null
                        && rawSelection instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) rawSelection;
                    getManager().removeErrors(selection.toList());
                }
                refresh();
            }
        });
        Button button = createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, true);
        button.addSelectionListener(new SelectionListener() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                close();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetDefaultSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Return a viewer sorter for looking at the jobs.
     * 
     * @return
     */
    private ViewerSorter getViewerSorter() {
        return new ViewerSorter() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
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
                //Nothing of interest here
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
                if (newInput != null)
                    refresh();
            }
        };
        errorViewer.setContentProvider(provider);
        errorViewer.setInput(getManager());
    }

    /**
     * Get the notificationManager that this is being created for.
     * 
     * @return
     */
    private ErrorNotificationManager getManager() {
        return ProgressManager.getInstance().errorManager;
    }

    /**
     * Refresh the contents of the viewer.
     */
    void refresh() {
        errorViewer.refresh();
    }

    private void initLabelProvider() {
        ITableLabelProvider provider = new ITableLabelProvider() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
             */
            public void addListener(ILabelProviderListener listener) {
                //Do nothing
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
             */
            public void dispose() {
                //Do nothing
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
             *      int)
             */
            public Image getColumnImage(Object element, int columnIndex) {
                return JFaceResources.getImageRegistry().get(
                        ErrorNotificationManager.ERROR_JOB_KEY);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
             *      int)
             */
            public String getColumnText(Object element, int columnIndex) {
                return ((ErrorInfo) element).getDisplayString();
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
                //Do nothing
            }
        };
        errorViewer.setLabelProvider(provider);
    }

    /**
     * Open the error dialog on the current selection.
     */
    private void openErrorDialog() {
        ErrorInfo element = getSingleSelection();
        if (element == null)
            return;
        ErrorDialog.openError(getShell(), element.getDisplayString(), null,
                element.getErrorStatus());
    }

    /**
     * Get the single selection. Return null if the selection is not just one
     * element.
     * 
     * @return ErrorInfo or <code>null</code>.
     */
    private ErrorInfo getSingleSelection() {
        ISelection rawSelection = errorViewer.getSelection();
        if (rawSelection != null
                && rawSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) rawSelection;
            if (selection.size() == 1)
                return (ErrorInfo) selection.getFirstElement();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    public boolean close() {
        getManager().clearAllErrors();
        Rectangle shellPosition = getShell().getBounds();
        boolean result = super.close();
        ProgressManagerUtil.animateDown(shellPosition);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
     */
    protected void initializeBounds() {
        super.initializeBounds();
        Rectangle shellPosition = getShell().getBounds();
        ProgressManagerUtil.animateUp(shellPosition);
    }

    /**
     * Create this dialog's drop-down list component.
     * 
     * @param detailsParent
     *            the parent composite
     */
    private void createDetailsList(Composite detailsParent) {
        // create the list
        detailsList = new List(detailsParent, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.MULTI);

        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
                | GridData.GRAB_VERTICAL);
        data.heightHint = detailsList.getItemHeight() * DETAILS_LIST_ITEM_COUNT;
        data.horizontalSpan = 2;
        detailsList.setLayoutData(data);
        Menu copyMenu = new Menu(detailsList);
        MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
        copyItem.addSelectionListener(new SelectionListener() {
            /*
             * @see SelectionListener.widgetSelected (SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                copyToClipboard();
            }

            /*
             * @see SelectionListener.widgetDefaultSelected(SelectionEvent)
             */
            public void widgetDefaultSelected(SelectionEvent e) {
                copyToClipboard();
            }
        });
        copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
        detailsList.setMenu(copyMenu);
    }

    /**
     * Set the contents of the details list to be the status from the selected
     * error.
     */
    private void setDetailsContents() {

        Collection statusList = new ArrayList();

        ErrorInfo info = getSingleSelection();

        if (info != null) {
            selectedError = info;
            statusList.add(selectedError.getErrorStatus().getMessage());
            if (selectedError.getErrorStatus().getException() != null) {
                Throwable exception = selectedError.getErrorStatus()
                        .getException();
                statusList.add(exception.toString());
                StackTraceElement[] elements = exception.getStackTrace();
                for (int i = 0; i < elements.length; i++) {
                    statusList.add(elements[i].toString());
                }
            }
            IStatus[] statuses = (selectedError.getErrorStatus().getChildren());
            for (int i = 0; i < statuses.length; i++) {
                statusList.add(NESTING_INDENT + statuses[i].getMessage());
            }
        }

        String[] items = new String[statusList.size()];
        statusList.toArray(items);

        detailsList.setItems(items);

    }

    /**
     * Copy the contents of the statuses to the clipboard.
     */
    private void copyToClipboard() {

        if (selectedError == null)
            return;

        if (clipboard != null)
            clipboard.dispose();

        StringBuffer statusBuffer = new StringBuffer();
        populateCopyBuffer(selectedError.getErrorStatus(), statusBuffer, 0);
        clipboard = new Clipboard(detailsList.getDisplay());
        clipboard.setContents(new Object[] { statusBuffer.toString() },
                new Transfer[] { TextTransfer.getInstance() });
    }

    /**
     * Put the details of the status of the error onto the stream.
     * 
     * @param buildingStatus
     * @param buffer
     * @param nesting
     */
    private void populateCopyBuffer(IStatus buildingStatus,
            StringBuffer buffer, int nesting) {

        for (int i = 0; i < nesting; i++) {
            buffer.append(NESTING_INDENT); //$NON-NLS-1$
        }
        buffer.append(buildingStatus.getMessage());

        if (buildingStatus.getException() != null) {
            Throwable exception = buildingStatus.getException();
            buffer.append("\n"); //$NON-NLS-1$
            buffer.append(exception.toString());
            StackTraceElement[] elements = exception.getStackTrace();
            for (int i = 0; i < elements.length; i++) {
                buffer.append("\n"); //$NON-NLS-1$
                buffer.append(elements[i].toString());

            }
        }

        buffer.append("\n"); //$NON-NLS-1$
        IStatus[] children = buildingStatus.getChildren();
        for (int i = 0; i < children.length; i++) {
            populateCopyBuffer(children[i], buffer, nesting + 1);
        }
    }
}