/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 * 		font should be activated and used by other components.
 *      Robin Stocker <robin@nibor.org> - Add filter text field
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *      Simon Scholz <simon.scholz@vogella.com> - Bug 488704, 491316
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.ConfigureColumns;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.TextMatcher;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;

/**
 * Displays information about the product plugins.
 *
 * PRIVATE this class is internal to the IDE
 */
public class AboutPluginsPage extends ProductInfoPage {

	public class BundleTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Queue containing bundle signing info to be resolved.
		 */
		private LinkedList<AboutBundleData> resolveQueue = new LinkedList<>();

		/**
		 * Queue containing bundle data that's been resolve and needs updating.
		 */
		private List<AboutBundleData> updateQueue = new ArrayList<>();

		/*
		 * this job will attempt to discover the signing state of a given bundle and
		 * then send it along to the update job
		 */
		private Job resolveJob = new Job(AboutPluginsPage.class.getName()) {
			{
				setSystem(true);
				setPriority(Job.SHORT);
			}

			@Override
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
						data = resolveQueue.removeFirst();
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
		 * this job is responsible for feeding label change events into the viewer as
		 * they become available from the resolve job
		 */
		private Job updateJob = new WorkbenchJob(PlatformUI.getWorkbench().getDisplay(),
				AboutPluginsPage.class.getName()) {
			{
				setSystem(true);
				setPriority(Job.DECORATE);
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				while (true) {
					Control page = getControl();
					// the page has gone down since we were asked to render
					if (page == null || page.isDisposed())
						return Status.OK_STATUS;
					AboutBundleData[] data = null;
					synchronized (updateQueue) {
						if (updateQueue.isEmpty())
							return Status.OK_STATUS;

						data = updateQueue.toArray(new AboutBundleData[updateQueue.size()]);
						updateQueue.clear();

					}
					fireLabelProviderChanged(new LabelProviderChangedEvent(BundleTableLabelProvider.this, data));
				}
			}
		};

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				if (element instanceof AboutBundleData) {
					final AboutBundleData data = (AboutBundleData) element;
					if (data.isSignedDetermined()) {
						return WorkbenchImages.getImage(data.isSigned() ? IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_YES
								: IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_NO);
					}

					synchronized (resolveQueue) {
						resolveQueue.add(data);
					}
					resolveJob.schedule();

					return WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_UNKNOWN);
				}
			}
			return null;
		}

		@Override
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

	// This id is used when the page is created inside its own dialog
	private static final String ID = "productInfo.plugins"; //$NON-NLS-1$

	/**
	 * Table height in dialog units (value 200).
	 */
	private static final int TABLE_HEIGHT = 200;

	private static final int MORE_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int SIGNING_ID = MORE_ID + 1;
	private static final int COLUMNS_ID = MORE_ID + 2;

	private static final IPath baseNLPath = IPath.fromOSString("$nl$"); //$NON-NLS-1$

	private static final String PLUGININFO = "about.html"; //$NON-NLS-1$

	private static final int PLUGIN_NAME_COLUMN_INDEX = 2;

	private static final int SIGNING_AREA_PERCENTAGE = 30;

	private TableViewer vendorInfo;

	private Button moreInfo, signingInfo;

	private String message;

	private String helpContextId = IWorkbenchHelpContextIds.ABOUT_PLUGINS_DIALOG;

	private String columnTitles[] = { WorkbenchMessages.AboutPluginsDialog_signed,
			WorkbenchMessages.AboutPluginsDialog_provider, WorkbenchMessages.AboutPluginsDialog_pluginName,
			WorkbenchMessages.AboutPluginsDialog_version, WorkbenchMessages.AboutPluginsDialog_pluginId,

	};
	private Bundle[] bundles = WorkbenchPlugin.getDefault().getBundles();
	private SashForm sashForm;
	private BundleSigningInfo signingArea;

	public void setBundles(Bundle[] bundles) {
		this.bundles = bundles;
	}

	public void setHelpContextId(String id) {
		this.helpContextId = id;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	protected void handleSigningInfoPressed() {
		if (signingArea == null) {
			signingArea = new BundleSigningInfo();
			AboutBundleData bundleInfo = (AboutBundleData) vendorInfo.getStructuredSelection().getFirstElement();
			signingArea.setData(bundleInfo);

			signingArea.createContents(sashForm);
			sashForm.setWeights(new int[] { 100 - SIGNING_AREA_PERCENTAGE, SIGNING_AREA_PERCENTAGE });
			signingInfo.setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_hide);

		} else {
			// hide
			signingInfo.setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_show);
			signingArea.dispose();
			signingArea = null;
			sashForm.setWeights(new int[] { 100 });
		}
	}

	@Override
	public void createPageButtons(Composite parent) {

		moreInfo = createButton(parent, MORE_ID, WorkbenchMessages.AboutPluginsDialog_moreInfo);
		moreInfo.setEnabled(false);

		signingInfo = createButton(parent, SIGNING_ID, WorkbenchMessages.AboutPluginsDialog_signingInfo_show);
		signingInfo.setEnabled(false);

		createButton(parent, COLUMNS_ID, WorkbenchMessages.AboutPluginsDialog_columns);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

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
		setControl(outer);
	}

	private void calculateAboutBundleData(Consumer<Collection<AboutBundleData>> aboutBundleDataConsumer,
			Display display) {
		Job loadBundleDataJob = Job.create(WorkbenchMessages.AboutPluginsPage_Load_Bundle_Data, monitor -> {
			// create a data object for each bundle, remove duplicates, and
			// include only resolved bundles (bug 65548)

			AboutBundleData.ExtendedSigningInfo info = Platform.getAdapterManager().getAdapter(this,
					AboutBundleData.ExtendedSigningInfo.class);

			SubMonitor subMonitor = SubMonitor.convert(monitor, bundles.length + 1);
			Map<String, AboutBundleData> map = new HashMap<>();
			for (Bundle bundle : bundles) {
				subMonitor.split(1);
				AboutBundleData data = new AboutBundleData(bundle, info);
				if (BundleUtility.isReady(data.getState()) && !map.containsKey(data.getVersionedId())) {
					map.put(data.getVersionedId(), data);
				}
			}
			subMonitor.split(1);
			display.asyncExec(() -> aboutBundleDataConsumer.accept(map.values()));
		});
		loadBundleDataJob.schedule();
	}

	/**
	 * Create the table part of the dialog.
	 *
	 * @param parent the parent composite to contain the dialog area
	 */
	protected void createTable(Composite parent) {
		final Text filterText = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		filterText.setLayoutData(GridDataFactory.fillDefaults().create());
		filterText.setMessage(WorkbenchMessages.AboutPluginsDialog_filterTextMessage);
		filterText.setFocus();

		vendorInfo = new TableViewer(parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		vendorInfo.setUseHashlookup(true);
		vendorInfo.getTable().setHeaderVisible(true);
		vendorInfo.getTable().setLinesVisible(true);
		vendorInfo.getTable().setFont(parent.getFont());
		vendorInfo.addSelectionChangedListener(event -> checkEnablement());

		final TableComparator comparator = new TableComparator();
		vendorInfo.setComparator(comparator);
		int[] columnWidths = { convertHorizontalDLUsToPixels(30), // signature
				convertHorizontalDLUsToPixels(120), convertHorizontalDLUsToPixels(120),
				convertHorizontalDLUsToPixels(70), convertHorizontalDLUsToPixels(130), };

		// create table headers
		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn column = new TableColumn(vendorInfo.getTable(), SWT.NULL);
			if (i == PLUGIN_NAME_COLUMN_INDEX) { // prime initial sorting
				updateTableSorting(i);
			}
			column.setWidth(columnWidths[i]);
			column.setText(columnTitles[i]);
			final int columnIndex = i;
			column.addSelectionListener(widgetSelectedAdapter(e -> updateTableSorting(columnIndex)));
		}

		vendorInfo.setContentProvider(ArrayContentProvider.getInstance());
		vendorInfo.setLabelProvider(new BundleTableLabelProvider());

		final BundlePatternFilter searchFilter = new BundlePatternFilter();
		filterText.addModifyListener(e -> {
			searchFilter.setPattern(filterText.getText());
			vendorInfo.getTable().setRedraw(false);
			try {
				vendorInfo.refresh();
			} finally {
				vendorInfo.getTable().setRedraw(true);
			}
		});
		vendorInfo.addFilter(searchFilter);

		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
		vendorInfo.getTable().setLayoutData(gridData);

		calculateAboutBundleData(bundleData -> {
			if (!vendorInfo.getTable().isDisposed()) {
				vendorInfo.setInput(bundleData);
			}
		}, parent.getDisplay());

		addCopySupport(vendorInfo.getTable());
	}

	/**
	 * Update the sort information on both the comparator and the table.
	 *
	 * @param columnIndex the index to sort by
	 * @since 3.4
	 */
	private void updateTableSorting(final int columnIndex) {
		TableComparator comparator = (TableComparator) vendorInfo.getComparator();
		// toggle direction if it's the same column
		if (columnIndex == comparator.getSortColumn()) {
			comparator.setAscending(!comparator.isAscending());
		}
		comparator.setSortColumn(columnIndex);
		vendorInfo.getTable().setSortColumn(vendorInfo.getTable().getColumn(columnIndex));
		vendorInfo.getTable().setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
		vendorInfo.refresh(false);
	}

	/**
	 * Return an URL to the plugin's about.html file (what is shown when "More info"
	 * is pressed) or null if no such file exists. The method does nl lookup to
	 * allow for i18n.
	 *
	 * @param bundleInfo the bundle info
	 * @param makeLocal  whether to make the about content local
	 * @return the URL or <code>null</code>
	 */
	private URL getMoreInfoURL(AboutBundleData bundleInfo, boolean makeLocal) {
		Bundle bundle = Platform.getBundle(bundleInfo.getId());
		if (bundle == null) {
			return null;
		}

		URL aboutUrl = FileLocator.find(bundle, baseNLPath.append(PLUGININFO), null);
		if (!makeLocal) {
			return aboutUrl;
		}
		if (aboutUrl != null) {
			try {
				URL result = FileLocator.toFileURL(aboutUrl);
				try {
					// Make local all content in the "about" directory.
					// This is needed to handle jar'ed plug-ins.
					// See Bug 88240 [About] About dialog needs to extract
					// subdirs.
					URL about = new URL(aboutUrl, "about_files"); //$NON-NLS-1$
					if (about != null) {
						FileLocator.toFileURL(about);
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

	@Override
	String getId() {
		return ID;
	}

	private void checkEnablement() {
		// enable if there is an item selected and that
		// item has additional info
		IStructuredSelection selection = vendorInfo.getStructuredSelection();
		if (selection.getFirstElement() instanceof AboutBundleData) {
			AboutBundleData selected = (AboutBundleData) selection.getFirstElement();
			moreInfo.setEnabled(selectionHasInfo(selected));
			signingInfo.setEnabled(true);
			if (signingArea != null) {
				signingArea.setData(selected);
			}
		} else {
			signingInfo.setEnabled(false);
			moreInfo.setEnabled(false);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case MORE_ID:
			handleMoreInfoPressed();
			break;
		case SIGNING_ID:
			handleSigningInfoPressed();
			break;
		case COLUMNS_ID:
			handleColumnsPressed();
			break;
		default:
			super.buttonPressed(buttonId);
			break;
		}
	}

	/**
	 * Check if the currently selected plugin has additional information to show.
	 *
	 * @param bundleInfo
	 *
	 * @return true if the selected plugin has additional info available to display
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
	 * The More Info button was pressed. Open a browser showing the license
	 * information for the selected bundle or an error dialog if the browser cannot
	 * be opened.
	 */
	protected void handleMoreInfoPressed() {
		if (vendorInfo == null) {
			return;
		}

		if (vendorInfo.getSelection().isEmpty())
			return;

		AboutBundleData bundleInfo = (AboutBundleData) vendorInfo.getStructuredSelection().getFirstElement();

		if (!AboutUtils.openBrowser(getShell(), getMoreInfoURL(bundleInfo, true))) {
			String message = NLS.bind(WorkbenchMessages.AboutPluginsDialog_unableToOpenFile, PLUGININFO,
					bundleInfo.getId());
			StatusUtil.handleStatus(WorkbenchMessages.AboutPluginsDialog_errorTitle + ": " + message, //$NON-NLS-1$
					StatusManager.SHOW);
		}
	}

	private void handleColumnsPressed() {
		ConfigureColumns.forTable(vendorInfo.getTable(), this);
	}
}

class TableComparator extends ViewerComparator {

	private int sortColumn = 0;
	private int lastSortColumn = 0;
	private boolean ascending = true;
	private boolean lastAscending = true;

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (sortColumn == 0 && e1 instanceof AboutBundleData && e2 instanceof AboutBundleData) {
			AboutBundleData d1 = (AboutBundleData) e1;
			AboutBundleData d2 = (AboutBundleData) e2;
			int diff = getSignedSortValue(d1) - getSignedSortValue(d2);
			// If values are different, or there is no secondary column defined,
			// we are done
			if (diff != 0 || lastSortColumn == 0)
				return ascending ? diff : -diff;
			// try a secondary sort
			if (viewer instanceof TableViewer) {
				TableViewer tableViewer = (TableViewer) viewer;
				IBaseLabelProvider baseLabel = tableViewer.getLabelProvider();
				if (baseLabel instanceof ITableLabelProvider) {
					ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
					String e1p = tableProvider.getColumnText(e1, lastSortColumn);
					String e2p = tableProvider.getColumnText(e2, lastSortColumn);
					int result = getComparator().compare(e1p, e2p);
					return lastAscending ? result : (-1) * result;
				}
			}
			// we couldn't determine a secondary sort, call it equal
			return 0;
		}
		if (viewer instanceof TableViewer) {
			TableViewer tableViewer = (TableViewer) viewer;
			IBaseLabelProvider baseLabel = tableViewer.getLabelProvider();
			if (baseLabel instanceof ITableLabelProvider) {
				ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
				String e1p = tableProvider.getColumnText(e1, sortColumn);
				String e2p = tableProvider.getColumnText(e2, sortColumn);
				int result = getComparator().compare(e1p, e2p);
				// Secondary column sort
				if (result == 0) {
					if (lastSortColumn != 0) {
						e1p = tableProvider.getColumnText(e1, lastSortColumn);
						e2p = tableProvider.getColumnText(e2, lastSortColumn);
						result = getComparator().compare(e1p, e2p);
						return lastAscending ? result : (-1) * result;
					} // secondary sort is by column 0
					if (e1 instanceof AboutBundleData && e2 instanceof AboutBundleData) {
						AboutBundleData d1 = (AboutBundleData) e1;
						AboutBundleData d2 = (AboutBundleData) e2;
						int diff = getSignedSortValue(d1) - getSignedSortValue(d2);
						return lastAscending ? diff : -diff;
					}
				}
				// primary column sort
				return ascending ? result : (-1) * result;
			}
		}

		return super.compare(viewer, e1, e2);
	}

	/**
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
	 * @param sortColumn The sortColumn to set.
	 */
	public void setSortColumn(int sortColumn) {
		if (this.sortColumn != sortColumn) {
			lastSortColumn = this.sortColumn;
			lastAscending = this.ascending;
			this.sortColumn = sortColumn;
		}
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

class BundlePatternFilter extends ViewerFilter {

	private TextMatcher matcher;

	public void setPattern(String searchPattern) {
		if (searchPattern == null || searchPattern.isEmpty()) {
			this.matcher = null;
		} else {
			String pattern = "*" + searchPattern + "*"; //$NON-NLS-1$//$NON-NLS-2$
			this.matcher = new TextMatcher(pattern, true, false);
		}
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (matcher == null) {
			return true;
		}

		if (element instanceof AboutBundleData) {
			AboutBundleData data = (AboutBundleData) element;
			return matcher.match(data.getName()) || matcher.match(data.getProviderName())
					|| matcher.match(data.getId());
		}
		else if (element instanceof AboutBundleGroupData data) {
			return matcher.match(data.getName()) || matcher.match(data.getProviderName())
					|| matcher.match(data.getId());
		}
		return true;
	}
}
