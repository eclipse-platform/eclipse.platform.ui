/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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

    protected boolean alreadyClosed = false;

    private IProgressMonitor wrapperedMonitor;

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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite top = (Composite) super.createDialogArea(parent);
        createExtendedDialogArea(parent);
        return top;
    }

	/**
	 * Create the extensions to the dialog area.
	 * @param parent
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
            detailsButton.setText(ProgressMessages.ProgressMonitorJobsDialog_DetailsTitle);
        } else {
            //Abort if there are no jobs visible
            if (ProgressManager.getInstance().getRootElements(Policy.DEBUG_SHOW_ALL_JOBS).length == 0) {
                detailsButton.setEnabled(false);
                return;
            }

            viewer = new DetailedProgressViewer(viewerComposite, SWT.MULTI
                    | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
            viewer.setComparator(new ViewerComparator() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
                 *      java.lang.Object, java.lang.Object)
                 */
                public int compare(Viewer testViewer, Object e1, Object e2) {
                    return ((Comparable) e1).compareTo(e2);
                }
            });

            viewer.setContentProvider(new ProgressViewerContentProvider(viewer,true,false){
            	public Object[] getElements(Object inputElement) {
            		return super.getElements(inputElement);
            	}}
            );
            
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
                ProgressMessages.ProgressMonitorJobsDialog_DetailsTitle, 
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
        if (arrowCursor == null) {
			arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
		}
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
    	if(alreadyClosed)
    		return;
    	
        super.updateForSetBlocked(reason);
        enableDetails(true);
        if (viewer == null) {
			handleDetailsButtonSelect();
		}
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
     * Set the enable state of the details button now or when it will be
     * created.
     * 
     * @param enableState
     *            a boolean to indicate the preferred' state
     */
    protected void enableDetails(boolean enableState) {
        if (detailsButton == null) {
			enableDetailsButton = enableState;
		} else {
			detailsButton.setEnabled(enableState);
		}
    }

    /**
     * Start watching the ticks. When the long operation time has 
     * passed open the dialog.
     */
    public void watchTicks() {
        watchTime = System.currentTimeMillis();
    }

    /**
     * Create a monitor for the receiver that wrappers the superclasses monitor.
     *  
     */
    public void createWrapperedMonitor() {
        wrapperedMonitor = new IProgressMonitorWithBlocking() {

            IProgressMonitor superMonitor = ProgressMonitorJobsDialog.super
                    .getProgressMonitor();

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
             *      int)
             */
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
                if ((System.currentTimeMillis() - watchTime) > ProgressManager
                        .getInstance().getLongOperationTime()) {
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

                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    public void run() {
						//Reset the watch if it is not safe to open
						 if (!ProgressManagerUtil.safeToOpen(ProgressMonitorJobsDialog.this,null)){
							  watchTicks();
							  return;
						 }
			                 
                        if (!alreadyClosed) {
							open();
						}
                    }
                });
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#done()
             */
            public void done() {
                superMonitor.done();
                checkTicking();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
             */
            public void internalWorked(double work) {
                superMonitor.internalWorked(work);
                checkTicking();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
             */
            public boolean isCanceled() {
                return superMonitor.isCanceled();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
             */
            public void setCanceled(boolean value) {
                superMonitor.setCanceled(value);

            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
             */
            public void setTaskName(String name) {
                superMonitor.setTaskName(name);
                checkTicking();

            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
             */
            public void subTask(String name) {
                superMonitor.subTask(name);
                checkTicking();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
             */
            public void worked(int work) {
                superMonitor.worked(work);
                checkTicking();

            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
             */
            public void clearBlocked() {
                //We want to open on blocking too
                if (superMonitor instanceof IProgressMonitorWithBlocking) {
					((IProgressMonitorWithBlocking) superMonitor)
                            .clearBlocked();
				}

            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
             */
            public void setBlocked(IStatus reason) {
                openDialog();
                if (superMonitor instanceof IProgressMonitorWithBlocking) {
					((IProgressMonitorWithBlocking) superMonitor)
                            .setBlocked(reason);
				}

            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#getProgressMonitor()
     */
    public IProgressMonitor getProgressMonitor() {
        if (wrapperedMonitor == null) {
			createWrapperedMonitor();
		}
        return wrapperedMonitor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#close()
     */
    public boolean close() {
        alreadyClosed = true;//As this sometimes delayed cache if it was already closed
        boolean result = super.close();
        if (!result) {//If it fails reset the flag
            alreadyClosed = false;
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
    	return true;
    }
}
