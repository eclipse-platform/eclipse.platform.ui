/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.ui.internal.about;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;

/**
 * Displays information about the product plugins.
 * 
 * PRIVATE this class is internal to the ide
 */
public class AboutPluginsPage extends TableListPage {

	public class BundleTableLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/**
		 * Queue containing bundle signing info to be resolved.
		 */
		private LinkedList resolveQueue = new LinkedList();

		/**
		 * Queue containing bundle data that's been resolve and needs updating.
		 */
		private List updateQueue = new ArrayList();

		/*
		 * this job will attempt to discover the signing state of a given bundle
		 * and then send it along to the update job
		 */
		private Job resolveJob = new Job(AboutPluginsPage.class.getName()) {
			{
				setSystem(true);
				setPriority(Job.SHORT);
			}

			protected IStatus run(IProgressMonitor monitor) {
				while (true) {
					// If the UI has not been created, nothing to do.
					if (vendorInfo == null)
						return Status.OK_STATUS;
					// If the UI has been disposed since we were asked to
					// render, nothing to do.
					Table table = vendorInfo.getTable();
					// the table has been disposed since we were asked to render
					if (table == null || table.isDisposed())
						return Status.OK_STATUS;
					AboutBundleData data = null;
					synchronized (resolveQueue) {
						if (resolveQueue.isEmpty())
							return Status.OK_STATUS;
						data = (AboutBundleData) resolveQueue.removeFirst();
					}
					try {
						// following is an expensive call
						data.isSigned();

						synchronized (updateQueue) {
							updateQueue.add(data);
						}
						// start the update job
						updateJob.schedule();
					} catch (IllegalStateException e) {
						// the bundle we're testing has been unloaded. Do
						// nothing.
					}
				}
			}
		};

		/*
		 * this job is responsible for feeding label change events into the
		 * viewer as they become available from the resolve job
		 */
		private Job updateJob = new WorkbenchJob(PlatformUI.getWorkbench()
				.getDisplay(), AboutPluginsPage.class.getName()) {
			{
				setSystem(true);
				setPriority(Job.DECORATE);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.
			 * runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				while (true) {
					Shell dialogShell = getShell();
					// the shell has gone down since we were asked to render
					if (dialogShell == null || dialogShell.isDisposed())
						return Status.OK_STATUS;
					AboutBundleData[] data = null;
					synchronized (updateQueue) {
						if (updateQueue.isEmpty())
							return Status.OK_STATUS;

						data = (AboutBundleData[]) updateQueue
								.toArray(new AboutBundleData[updateQueue.size()]);
						updateQueue.clear();

					}
					fireLabelProviderChanged(new LabelProviderChangedEvent(
							BundleTableLabelProvider.this, data));
				}
			}
		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
		 * .lang.Object, int)
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

					synchronized (resolveQueue) {
						resolveQueue.add(data);
					}
					resolveJob.schedule();

					return WorkbenchImages
							.getImage(IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_UNKNOWN);
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
		 * lang.Object, int)
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

	// This id should *not* be the same id used for contributing the page in
	// the installationPage extension. It is used by ProductInfoDialog
	// to ensure a different namespace for button contributions than the id
	// for the page appearing in the InstallationDialog
	private static final String ID = "productInfo.plugins"; //$NON-NLS-1$

	/**
	 * Table height in dialog units (value 200).
	 */
	private static final int TABLE_HEIGHT = 200;

	private static final IPath baseNLPath = new Path("$nl$"); //$NON-NLS-1$

	private static final String PLUGININFO = "about.html"; //$NON-NLS-1$

	private static final int PLUGIN_NAME_COLUMN_INDEX = 2;

	private static final int SIGNING_AREA_PERCENTAGE = 30;

	private TableViewer vendorInfo;

	private Action signingInfo;

	private String message;

	private String helpContextId = IWorkbenchHelpContextIds.ABOUT_PLUGINS_DIALOG;

	private String columnTitles[] = {
			WorkbenchMessages.AboutPluginsDialog_signed,
			WorkbenchMessages.AboutPluginsDialog_provider,
			WorkbenchMessages.AboutPluginsDialog_pluginName,
			WorkbenchMessages.AboutPluginsDialog_version,
			WorkbenchMessages.AboutPluginsDialog_pluginId,

	};
	private Bundle[] bundles = WorkbenchPlugin.getDefault().getBundles();
	private AboutBundleData[] bundleInfos;
	private SashForm sashForm;
	private BundleSigningInfo signingArea;

	public void setBundles(Bundle[] bundles) {
		this.bundles = bundles;
	}

	public void setHelpContextId(String id) {
		this.helpContextId = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 */
	protected void handleSigningInfoPressed() {
		if (signingArea == null) {
			signingArea = new BundleSigningInfo();
			AboutBundleData bundleInfo = (AboutBundleData) ((IStructuredSelection) vendorInfo
					.getSelection()).getFirstElement();
			signingArea.setData(bundleInfo);

			signingArea.createContents(sashForm);
			sashForm.setWeights(new int[] { 100 - SIGNING_AREA_PERCENTAGE,
					SIGNING_AREA_PERCENTAGE });
			signingInfo
					.setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_hide);

		} else {
			// hide
			signingInfo
					.setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_show);
			signingArea.dispose();
			signingArea = null;
			sashForm.setWeights(new int[] { 100 });
		}
	}

	protected AbstractContributionFactory makeContributionFactory() {
		return new AbstractContributionFactory(getInstallationDialog()
				.getButtonBarURI(), null) {

			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {

				signingInfo = new Action(
						WorkbenchMessages.AboutPluginsDialog_signingInfo_show) {
					public void run() {
						handleSigningInfoPressed();
					}
				};
				checkSigningEnablement();
				additions.addContributionItem(new ActionContributionItem(
						signingInfo), getInstallationDialog()
						.getActivePageExpression(AboutPluginsPage.this));
			}
		};
	}

	protected Control createPageControl(Composite parent) {
		initializeDialogUnits(parent);

		// create a data object for each bundle, remove duplicates, and include
		// only resolved bundles (bug 65548)
		Map map = new HashMap();
		for (int i = 0; i < bundles.length; ++i) {
			AboutBundleData data = new AboutBundleData(bundles[i]);
			if (BundleUtility.isReady(data.getState())
					&& !map.containsKey(data.getVersionedId())) {
				map.put(data.getVersionedId(), data);
			}
		}
		bundleInfos = (AboutBundleData[]) map.values().toArray(
				new AboutBundleData[0]);
		WorkbenchPlugin.class.getSigners();

		sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
		FillLayout layout = new FillLayout();
		sashForm.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		GridData data = new GridData(GridData.FILL_BOTH);
		sashForm.setLayoutData(data);

		Composite outer = createOuterComposite(sashForm);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(outer, helpContextId);

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
		vendorInfo = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		vendorInfo.setUseHashlookup(true);
		vendorInfo.getTable().setHeaderVisible(true);
		vendorInfo.getTable().setLinesVisible(true);
		vendorInfo.getTable().setFont(parent.getFont());
		vendorInfo.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				checkSigningEnablement();
				AboutPluginsPage.this.selectionChanged();
			}
		});

		final TableComparator comparator = new TableComparator();
		vendorInfo.setComparator(comparator);
		int[] columnWidths = {
				convertHorizontalDLUsToPixels(30), // signature
				convertHorizontalDLUsToPixels(120),
				convertHorizontalDLUsToPixels(120),
				convertHorizontalDLUsToPixels(70),
				convertHorizontalDLUsToPixels(130), };

		// create table headers
		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn column = new TableColumn(vendorInfo.getTable(),
					SWT.NULL);
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
	 * Return an url to the plugin's about.html file (what is shown when
	 * "More info" is pressed) or null if no such file exists. The method does
	 * nl lookup to allow for i18n.
	 * 
	 * @param bundleInfo
	 *            the bundle info
	 * @param makeLocal
	 *            whether to make the about content local
	 * @return the url or <code>null</code>
	 */
	private URL getMoreInfoURL(AboutBundleData bundleInfo, boolean makeLocal) {
		Bundle bundle = Platform.getBundle(bundleInfo.getId());
		if (bundle == null) {
			return null;
		}

		URL aboutUrl = Platform.find(bundle, baseNLPath.append(PLUGININFO),
				null);
		if (!makeLocal) {
			return aboutUrl;
		}
		if (aboutUrl != null) {
			try {
				URL result = Platform.asLocalURL(aboutUrl);
				try {
					// Make local all content in the "about" directory.
					// This is needed to handle jar'ed plug-ins.
					// See Bug 88240 [About] About dialog needs to extract
					// subdirs.
					URL about = new URL(aboutUrl, "about_files"); //$NON-NLS-1$
					if (about != null) {
						Platform.asLocalURL(about);
					}
				} catch (IOException e) {
					// skip the about dir if its not found or there are other
					// problems.
				}
				return result;
			} catch (IOException e) {
				// do nothing
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.about.ProductInfoPage#getId()
	 */
	String getId() {
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.about.ColumnsPage#getTable()
	 */
	protected Table getTable() {
		return vendorInfo.getTable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.about.TableListPage#getURL()
	 */
	protected URL getURL() {
		if (vendorInfo == null)
			return null;

		if (vendorInfo.getSelection().isEmpty())
			return null;

		AboutBundleData bundleInfo = (AboutBundleData) ((IStructuredSelection) vendorInfo
				.getSelection()).getFirstElement();
		URL url = getMoreInfoURL(bundleInfo, true);

		// only report problems if the -debug command line argument is used
		if (url == null && WorkbenchPlugin.DEBUG) {
			WorkbenchPlugin.log("Problem reading plugin info for: " //$NON-NLS-1$
					+ bundleInfo.getName());
		}
		return url;
	}

	private void checkSigningEnablement() {
		// enable if there is an item selected and that
		// item has additional info
		IStructuredSelection selection = (IStructuredSelection) vendorInfo
				.getSelection();
		if (selection.getFirstElement() instanceof AboutBundleData) {
			AboutBundleData selected = (AboutBundleData) selection
					.getFirstElement();
			signingInfo.setEnabled(true);
			if (signingArea != null) {
				signingArea.setData(selected);
			}
		} else {
			signingInfo.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.about.TableListPage#getSelectionValue()
	 */
	protected Collection getSelectionValue() {
		if (vendorInfo == null)
			return null;
		IStructuredSelection selection = (IStructuredSelection) vendorInfo
				.getSelection();
		if (selection.getFirstElement() instanceof AboutBundleData) {
			ArrayList list = new ArrayList(1);
			list.add(selection.getFirstElement());
			return list;
		}
		return null;
	}
}

class TableComparator extends ViewerComparator {

	private int sortColumn = 0;
	private boolean ascending = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.
	 * viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (sortColumn == 0 && e1 instanceof AboutBundleData
				&& e2 instanceof AboutBundleData) {
			AboutBundleData d1 = (AboutBundleData) e1;
			AboutBundleData d2 = (AboutBundleData) e2;
			int diff = getSignedSortValue(d1) - getSignedSortValue(d2);
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
				return ascending ? result : (-1) * result;
			}
		}

		return super.compare(viewer, e1, e2);
	}

	/**
	 * @param data
	 * @return a sort value depending on the signed state
	 */
	private int getSignedSortValue(AboutBundleData data) {
		if (!data.isSignedDetermined()) {
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
	 * @param sortColumn
	 *            The sortColumn to set.
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
	 * @param ascending
	 *            The ascending to set.
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
}
