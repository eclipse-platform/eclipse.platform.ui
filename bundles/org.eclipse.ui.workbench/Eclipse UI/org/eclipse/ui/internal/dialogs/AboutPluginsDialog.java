/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 * 		font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.about.AboutBundleData;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * Displays information about the product plugins.
 *
 * PRIVATE
 *	this class is internal to the ide
 */
public class AboutPluginsDialog extends ProductInfoDialog {

	public class BundleTableLabelProvider extends LabelProvider implements ITableLabelProvider  {
		
		/**
	     * Scheduling rule for resolve jobs.
	     */
	    private ISchedulingRule resolveRule = new ISchedulingRule() {

			public boolean contains(ISchedulingRule rule) {
				return rule == this;
			}

			public boolean isConflicting(ISchedulingRule rule) {
				return rule == this;
			}};
			
		private Job sortingJob= new UIJob(getShell().getDisplay(), AboutPluginsDialog.class.getName()) {
			{
				setRule(resolveRule);
				setSystem(true);
				setPriority(Job.DECORATE);
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				fireLabelProviderChanged(new LabelProviderChangedEvent(
						BundleTableLabelProvider.this));
				return Status.OK_STATUS;
			}
		};
			
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				if (element instanceof AboutBundleData) {
					final AboutBundleData data = (AboutBundleData) element;
					if (data.isSignedDetermined()) {
						return WorkbenchImages
								.getImage(data.isSigned() ? IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_YES
										: IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_NO);
					} 
					Job resolveJob = new Job(data.getId()){ 

						protected IStatus run(IProgressMonitor monitor) {
							
							data.isSigned();
							Shell dialogShell = getShell();
							if (dialogShell == null || dialogShell.isDisposed())
								return Status.OK_STATUS;
								
							dialogShell.getDisplay().asyncExec(new Runnable() {

								public void run() {
									fireLabelProviderChanged(new LabelProviderChangedEvent(
											BundleTableLabelProvider.this, data));
								}
							});

							sortingJob.schedule();

							return Status.OK_STATUS;
						}
					}; 
					resolveJob.setRule(resolveRule);
					resolveJob.setSystem(true);
					resolveJob.schedule();
					return WorkbenchImages
							.getImage(IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_UNKNOWN);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof AboutBundleData) {
				AboutBundleData data = (AboutBundleData) element;
				switch (columnIndex) {
				case 1:
					return data.getProviderName();
				case 2:
					return data.getName();
				case 3:
					return data.getVersion();
				case 4:
					return data.getId();
				}
			}
			return ""; //$NON-NLS-1$
		}		
	}
	
    /**
     * Table height in dialog units (value 200).
     */
    private static final int TABLE_HEIGHT = 200;

	private static final IPath baseNLPath = new Path("$nl$"); //$NON-NLS-1$

    private static final String PLUGININFO = "about.html"; //$NON-NLS-1$

    private final static int MORE_ID = IDialogConstants.CLIENT_ID + 1;
    private final static int SIGNING_ID = MORE_ID + 1;
    
    private static final int PLUGIN_NAME_COLUMN_INDEX = 2;

    private TableViewer vendorInfo;

    private Button moreInfo, signingInfo;

    private String title;

    private String message;
    
    private String helpContextId;

    private String columnTitles[] = {
    		WorkbenchMessages.AboutPluginsDialog_signed,
            WorkbenchMessages.AboutPluginsDialog_provider,
            WorkbenchMessages.AboutPluginsDialog_pluginName,
            WorkbenchMessages.AboutPluginsDialog_version, 
            WorkbenchMessages.AboutPluginsDialog_pluginId,
            
    };

    private String productName;

    private AboutBundleData[] bundleInfos;
    
    /**
     * Constructor for AboutPluginsDialog.
     * 
     * @param parentShell the parent shell
     * @param productName the product name
     */
    public AboutPluginsDialog(Shell parentShell, String productName) {
        this(parentShell, productName, WorkbenchPlugin.getDefault()
                .getBundles(), null, null, IWorkbenchHelpContextIds.ABOUT_PLUGINS_DIALOG);
        WorkbenchPlugin.class.getSigners();
    }

    /**
     * Constructor for AboutPluginsDialog.
     * 
     * @param parentShell 
     * 			  the parent shell
     * @param productName
     *            must not be null
     * @param bundles
     *            must not be null
     * @param title 
     *            the title
     * @param message 
     * 			  the message
     * @param helpContextId 
     *            the help context id
     */
    public AboutPluginsDialog(Shell parentShell, String productName,
            Bundle[] bundles, String title, String message, String helpContextId) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.helpContextId = helpContextId;
        this.productName = productName;
        
        // create a data object for each bundle, remove duplicates, and include only resolved bundles (bug 65548)
        Map map = new HashMap();
        for (int i = 0; i < bundles.length; ++i) {
            AboutBundleData data = new AboutBundleData(bundles[i]);
            if (BundleUtility.isReady(data.getState()) && !map.containsKey(data.getVersionedId())) {
				map.put(data.getVersionedId(), data);
			}
        }
        bundleInfos = (AboutBundleData[]) map.values().toArray(
                new AboutBundleData[0]);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case MORE_ID:
            handleMoreInfoPressed();
            break;
        case SIGNING_ID:
        	handleSigningInfoPressed();
        	break;
        default:
            super.buttonPressed(buttonId);
            break;
        }
    }

    /**
	 */
	private void handleSigningInfoPressed() {
		DialogTray existingTray = getTray();
		if (existingTray instanceof BundleSigningTray) {
			// hide
			getButton(SIGNING_ID).setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_show); 
			closeTray();
		}
		else {
			//show
			getButton(SIGNING_ID).setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_hide);
			if (existingTray != null)
				closeTray();
			AboutBundleData bundleInfo = (AboutBundleData) ((IStructuredSelection) vendorInfo
					.getSelection()).getFirstElement();
			BundleSigningTray tray = new BundleSigningTray(this);
			tray.setData(bundleInfo);
			openTray(tray);
		}
		
	}

	/*
     * (non-Javadoc) Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        
        //signImage = new Image( this.getParentShell().getDisplay(), AboutPluginsDialog.class.getResourceAsStream("Signed.gif")); //$NON-NLS-1$
        
        if (title == null && productName != null) {
			title = NLS.bind(WorkbenchMessages.AboutPluginsDialog_shellTitle, productName);
		}

        if (title != null) {
			newShell.setText(title);
		}

        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
				helpContextId);
    }
    
    /**
     * Add buttons to the dialog's button bar.
     * 
     * Subclasses should override.
     * 
     * @param parent
     *            the button bar composite
     */
    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moreInfo = createButton(parent, MORE_ID, WorkbenchMessages.AboutPluginsDialog_moreInfo, false); 
        moreInfo.setEnabled(false);
        
        signingInfo = createButton(parent, SIGNING_ID, WorkbenchMessages.AboutPluginsDialog_signingInfo_show, false);
        signingInfo.setEnabled(false);

        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    /**
     * Create the contents of the dialog (above the button bar).
     * 
     * Subclasses should overide.
     * 
     * @param parent
     *            the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        Composite outer = (Composite) super.createDialogArea(parent);

        if (message != null) {
            Label label = new Label(outer, SWT.NONE);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            label.setFont(parent.getFont());
            label.setText(message);
        }

        createTable(outer);

        return outer;
    }

    /**
     * Create the table part of the dialog.
     * 
     * @param parent
     *            the parent composite to contain the dialog area
     */
    protected void createTable(Composite parent) {
        vendorInfo = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);
        vendorInfo.setUseHashlookup(true);
        vendorInfo.getTable().setHeaderVisible(true);
        vendorInfo.getTable().setLinesVisible(true);
        vendorInfo.getTable().setFont(parent.getFont());
        vendorInfo.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				// enable if there is an item selected and that
                // item has additional info
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.getFirstElement() instanceof AboutBundleData) {
					AboutBundleData selected = (AboutBundleData) selection.getFirstElement(); 
					moreInfo.setEnabled(selectionHasInfo(selected));
					signingInfo.setEnabled(true);
					if (getTray() instanceof BundleSigningTray) {
						((BundleSigningTray)getTray()).setData(selected);
					}
				}
				else {
					moreInfo.setEnabled(false);
					signingInfo.setEnabled(false);
				}
			}});
        
        final TableComparator comparator = new TableComparator();
        vendorInfo.setComparator(comparator);
        int[] columnWidths = {
        		convertHorizontalDLUsToPixels(30), //signature
        		convertHorizontalDLUsToPixels(120),
                convertHorizontalDLUsToPixels(120),
                convertHorizontalDLUsToPixels(70),
                convertHorizontalDLUsToPixels(130),
                };

        
        // create table headers
        for (int i = 0; i < columnTitles.length; i++) {
            TableColumn column = new TableColumn(vendorInfo.getTable(), SWT.NULL);
            if (i == PLUGIN_NAME_COLUMN_INDEX) { // prime initial sorting
            	updateTableSorting(i);
            }	
            column.setWidth(columnWidths[i]);
            column.setText(columnTitles[i]);
            final int columnIndex = i;
            column.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    updateTableSorting(columnIndex);
                }
            });
        }
                
        vendorInfo.setContentProvider(new ArrayContentProvider());        
        vendorInfo.setLabelProvider(new BundleTableLabelProvider());
       
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
                true);
        gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
        vendorInfo.getTable().setLayoutData(gridData);
        
        vendorInfo.setInput(bundleInfos);
    }

	/**
	 * Update the sort information on both the comparator and the table.
	 * 
	 * @param columnIndex
	 *            the index to sort by
	 * @since 3.4
	 */
	private void updateTableSorting(final int columnIndex) {
		TableComparator comparator = (TableComparator) vendorInfo
				.getComparator();
		// toggle direction if it's the same column
		if (columnIndex == comparator.getSortColumn()) {
			comparator.setAscending(!comparator.isAscending());
		}
		comparator.setSortColumn(columnIndex);
		vendorInfo.getTable().setSortColumn(
				vendorInfo.getTable().getColumn(columnIndex));
		vendorInfo.getTable().setSortDirection(
				comparator.isAscending() ? SWT.UP : SWT.DOWN);
		vendorInfo.refresh(false);
	}
	
	/**
     * Check if the currently selected plugin has additional information to
     * show.
     * @param bundleInfo 
     * 
     * @return true if the selected plugin has additional info available to
     *         display
     */
    private boolean selectionHasInfo(AboutBundleData bundleInfo) {
        
        URL infoURL = getMoreInfoURL(bundleInfo, false);

        // only report ini problems if the -debug command line argument is used
        if (infoURL == null && WorkbenchPlugin.DEBUG) {
        	WorkbenchPlugin.log("Problem reading plugin info for: " //$NON-NLS-1$
					+ bundleInfo.getName());
		}

        return infoURL != null;
    }

    /** 
     * The More Info button was pressed.  Open a browser showing the license information
     * for the selected bundle or an error dialog if the browser cannot be opened.
     */
    protected void handleMoreInfoPressed() {
        if (vendorInfo == null) {
			return;
		}
        
        if (vendorInfo.getSelection().isEmpty())
        	return;
        
        AboutBundleData bundleInfo = (AboutBundleData) ((IStructuredSelection) vendorInfo
				.getSelection()).getFirstElement();
        
        if (!openBrowser(getMoreInfoURL(bundleInfo, true))) {
			String message = NLS.bind(
					WorkbenchMessages.AboutPluginsDialog_unableToOpenFile,
					PLUGININFO, bundleInfo.getId());
			StatusUtil.handleStatus(
					WorkbenchMessages.AboutPluginsDialog_errorTitle
							+ ": " + message, StatusManager.SHOW, getShell()); //$NON-NLS-1$
		}
    }

    /**
     * Return an url to the plugin's about.html file (what is shown when "More info" is
     * pressed) or null if no such file exists.  The method does nl lookup to allow for
     * i18n.
     * 
     * @param bundleInfo the bundle info
     * @param makeLocal whether to make the about content local
     * @return the url or <code>null</code>
     */
    private URL getMoreInfoURL(AboutBundleData bundleInfo, boolean makeLocal) {
        Bundle bundle = Platform.getBundle(bundleInfo.getId());
        if (bundle == null) {
			return null;
		}

        URL aboutUrl = Platform.find(bundle, baseNLPath.append(PLUGININFO), null);
        if (!makeLocal) {
            return aboutUrl;
        }
		if (aboutUrl != null) {
		    try {
				URL result = Platform.asLocalURL(aboutUrl);
				try {
				    // Make local all content in the "about" directory.
				    // This is needed to handle jar'ed plug-ins.
				    // See Bug 88240 [About] About dialog needs to extract subdirs.
					URL about = new URL(aboutUrl, "about_files"); //$NON-NLS-1$
					if (about != null) {
						Platform.asLocalURL(about);
					}
				} catch (IOException e) {
					// skip the about dir if its not found or there are other problems.
				}
				return result;
		    } catch(IOException e) {
		        // do nothing
		    }
        }
		return null;
    }
}


class TableComparator extends ViewerComparator {

	private int sortColumn = 0;
	private boolean ascending = true;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (sortColumn == 0 && e1 instanceof AboutBundleData && e2 instanceof AboutBundleData) {
			AboutBundleData d1= (AboutBundleData) e1;
			AboutBundleData d2= (AboutBundleData) e2;
			int diff= getSignedSortValue(d1) - getSignedSortValue(d2);
			return ascending ? diff : -diff;
		}
		if (viewer instanceof TableViewer) {
			TableViewer tableViewer = (TableViewer) viewer;
			IBaseLabelProvider baseLabel = tableViewer.getLabelProvider();
			if (baseLabel instanceof ITableLabelProvider) {
				ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
				String e1p = tableProvider.getColumnText(e1, sortColumn);
				String e2p = tableProvider.getColumnText(e2, sortColumn);
				int result = getComparator().compare(e1p, e2p);
				return ascending ?  result : (-1) * result;
			}
		}
		
		return super.compare(viewer, e1, e2);
	}

	/**
	 * @param data
	 * @return a sort value depending on the signed state
	 */
	private int getSignedSortValue(AboutBundleData data) {
		if (! data.isSignedDetermined()) {
			return 0;
		} else if (data.isSigned()) {
			return 1;
		} else {
			return -1;
		}
	}
	
	/**
	 * @return Returns the sortColumn.
	 */
	public int getSortColumn() {
		return sortColumn;
	}

	/**
	 * @param sortColumn The sortColumn to set.
	 */
	public void setSortColumn(int sortColumn) {
		this.sortColumn = sortColumn;
	}

	/**
	 * @return Returns the ascending.
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * @param ascending The ascending to set.
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
}
