/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

import com.ibm.icu.text.MessageFormat;

/**
 * Shows a list of items to the user with a text entry field for a string
 * pattern used to filter the list of items.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public abstract class FilteredItemsSelectionDialog extends
		SelectionStatusDialog {

	private static final String DIALOG_BOUNDS_SETTINGS = "DialogBoundsSettings"; //$NON-NLS-1$

	private static final String SHOW_STATUS_LINE = "ShowStatusLine"; //$NON-NLS-1$

	private static final String HISTORY_SETTINGS = "History"; //$NON-NLS-1$

	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$

	/**
	 * Represents an empty selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int NONE = 0;

	/**
	 * Pattern input field selection where carret is at the beginning (used only
	 * for initial pattern).
	 */
	public static final int CARET_BEGINNING = 1;

	/**
	 * Represents a full selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int FULL_SELECTION = 2;

	private Text pattern;

	private TableViewer list;

	private DetailsContentViewer details;

	/**
	 * It is a duplicate of a field in the CLabel class in DetailsContentViewer.
	 * It is maintained, because the <code>setDetailsLabelProvider()</code>
	 * could be called before content area is created.
	 */
	private ILabelProvider detailsLabelProvider;

	private ItemsListLabelProvider itemsListLabelProvider;

	private MenuManager menuManager;

	private boolean multi;

	private ToolBar toolBar;

	private ToolItem toolItem;

	private Label progressLabel;

	private ToggleStatusLineAction toggleStatusLineAction;

	private RemoveHistoryItemAction removeHistoryItemAction;

	private IStatus status;

	private RefreshJob refreshJob;

	private RefreshCacheJob refreshCacheJob;

	private ProgressMessageRefreshJob refreshProgressMessageJob = new ProgressMessageRefreshJob();

	private Object[] lastSelection;

	private ContentProvider contentProvider;

	private AbstractFilterJob filterJob;

	private ItemsFilter filter;

	private List lastCompletedResult;

	private ItemsFilter lastCompletedFilter;

	private String initialPatternText;

	private int selectionMode;

	private Object[] lastRefreshSelection;
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Creates a new instance of the class
	 * 
	 * @param shell
	 *            shell to parent the dialog on
	 * @param multi
	 *            multiselection flag
	 */
	public FilteredItemsSelectionDialog(Shell shell, boolean multi) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.multi = multi;
		contentProvider = new ContentProvider();
		refreshCacheJob = new RefreshCacheJob();
		refreshJob = new RefreshJob(refreshCacheJob);
		selectionMode = NONE;
	}

	/**
	 * Creates a new instance of the class
	 * 
	 * @param shell
	 *            shell to parent the dialog on
	 */
	public FilteredItemsSelectionDialog(Shell shell) {
		this(shell, false);
	}

	/**
	 * Adds viewer filter to the dialog items list
	 * 
	 * @param filter
	 *            the new filter
	 */
	protected void addListFilter(ViewerFilter filter) {
		contentProvider.addFilter(filter);
	}

	/**
	 * Sets a new label provider for items in the list.
	 * 
	 * @param listLabelProvider
	 *            the label provider for items in the list
	 */
	public void setListLabelProvider(ILabelProvider listLabelProvider) {
		getItemsListLabelProvider().setProvider(listLabelProvider);
	}

	/**
	 * Returns the label decorator for selected items in the list
	 * 
	 * @return the label decorator for selected items in the list
	 */
	private ILabelDecorator getListSelectionLabelDecorator() {
		return getItemsListLabelProvider().getSelectionDecorator();
	}

	/**
	 * Sets the label decorator for selected items in the list
	 * 
	 * @param listSelectionLabelDecorator
	 *            the label decorator for selected items in the list
	 */
	public void setListSelectionLabelDecorator(
			ILabelDecorator listSelectionLabelDecorator) {
		getItemsListLabelProvider().setSelectionDecorator(
				listSelectionLabelDecorator);
	}

	/**
	 * Returns the item list label provider
	 * 
	 * @return the item list label provider
	 */
	private ItemsListLabelProvider getItemsListLabelProvider() {
		if (itemsListLabelProvider == null) {
			itemsListLabelProvider = new ItemsListLabelProvider(
					new LabelProvider(), null);
		}
		return itemsListLabelProvider;
	}

	/**
	 * Sets label provider for the details label.
	 * 
	 * @param detailsLabelProvider
	 *            the label provider for the details field
	 */
	public void setDetailsLabelProvider(ILabelProvider detailsLabelProvider) {
		this.detailsLabelProvider = detailsLabelProvider;
		if (details != null) {
			details.setLabelProvider(detailsLabelProvider);
		}
	}

	private ILabelProvider getDetailsLabelProvider() {
		if (detailsLabelProvider == null) {
			detailsLabelProvider = new LabelProvider();
		}
		return detailsLabelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		pattern.setFocus();
	}

	/**
	 * Restores dialog from persisted settings. In the abstract class it
	 * restores a status of the details line and the selection history.
	 * 
	 * @param settings
	 *            settings used to restore dialog
	 */
	protected void restoreDialog(IDialogSettings settings) {
		boolean toggleStatusLine = true;

		if (settings.get(SHOW_STATUS_LINE) != null) {
			toggleStatusLine = settings.getBoolean(SHOW_STATUS_LINE);
		}

		toggleStatusLineAction.setChecked(toggleStatusLine);

		details.setVisible(toggleStatusLine);

		String setting = settings.get(HISTORY_SETTINGS);
		if (setting != null) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(
						setting));
				this.contentProvider.loadHistory(memento);
			} catch (WorkbenchException e) {
				// Simply don't restore the settings
				StatusManager
						.getManager()
						.handle(
								new Status(
										IStatus.ERROR,
										WorkbenchPlugin.PI_WORKBENCH,
										IStatus.ERROR,
										WorkbenchMessages.FilteredItemsSelectionDialog_restoreError,
										e));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		storeDialog(getDialogSettings());
		return super.close();
	}

	/**
	 * Stores dialog settings
	 * 
	 * @param settings
	 *            settings used to store dialog
	 */
	protected void storeDialog(IDialogSettings settings) {
		settings.put(SHOW_STATUS_LINE, toggleStatusLineAction.isChecked());

		XMLMemento memento = XMLMemento.createWriteRoot(HISTORY_SETTINGS);
		this.contentProvider.saveHistory(memento);
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
			settings.put(HISTORY_SETTINGS, writer.getBuffer().toString());
		} catch (IOException e) {
			// Simply don't store the settings
			StatusManager
					.getManager()
					.handle(
							new Status(
									IStatus.ERROR,
									WorkbenchPlugin.PI_WORKBENCH,
									IStatus.ERROR,
									WorkbenchMessages.FilteredItemsSelectionDialog_storeError,
									e));
		}
	}

	private Control createHeader(Composite parent) {
		Composite header = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		Label label = new Label(header, SWT.NONE);
		label
				.setText((getMessage() != null && getMessage().trim().length() > 0) ? getMessage()
						: WorkbenchMessages.FilteredItemsSelectionDialog_patternLabel);
		label.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					pattern.setFocus();
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);

		createViewMenu(header);
		return header;
	}

	private void createViewMenu(Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		toolItem = new ToolItem(toolBar, SWT.PUSH, 0);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		toolBar.setLayoutData(data);

		toolItem.setImage(WorkbenchImages
				.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU));
		toolItem
				.setToolTipText(WorkbenchMessages.FilteredItemsSelectionDialog_menu);
		toolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showViewMenu();
			}
		});

		menuManager = new MenuManager();

		fillViewMenu(menuManager);
	}

	/**
	 * Fills the menu of the dialog
	 * 
	 * @param menuManager
	 *            the menu manager
	 */
	protected void fillViewMenu(IMenuManager menuManager) {
		toggleStatusLineAction = new ToggleStatusLineAction();
		menuManager.add(toggleStatusLineAction);
	}

	private void showViewMenu() {
		Menu menu = menuManager.createContextMenu(getShell());
		Rectangle bounds = toolItem.getBounds();
		Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
		topLeft = toolBar.toDisplay(topLeft);
		menu.setLocation(topLeft.x, topLeft.y);
		menu.setVisible(true);
	}

	private void createPopupMenu() {
		removeHistoryItemAction = new RemoveHistoryItemAction();

		MenuManager manager = new MenuManager();
		manager.add(removeHistoryItemAction);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				List selectedElements = ((StructuredSelection) list
						.getSelection()).toList();

				Object item = null;

				for (Iterator it = selectedElements.iterator(); it.hasNext();) {
					item = it.next();
					if (item instanceof ItemsListSeparator
							|| !isHistoryElement(item)) {
						removeHistoryItemAction.setEnabled(false);
						return;
					}
					removeHistoryItemAction.setEnabled(true);
				}
			}
		});

		Menu menu = manager.createContextMenu(getShell());
		list.getTable().setMenu(menu);
	}

	/**
	 * Creates an extra content area, which will be located above the details.
	 * 
	 * @param parent
	 *            parent to create the dialog widgets in
	 * @return an extra content area
	 */
	protected abstract Control createExtendedContentArea(Composite parent);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(dialogArea, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		content.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		content.setLayout(layout);

		Control header = createHeader(content);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		header.setLayoutData(gd);

		pattern = new Text(content, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		pattern.setLayoutData(gd);

		Label listLabel = new Label(content, SWT.NONE);
		listLabel
				.setText(WorkbenchMessages.FilteredItemsSelectionDialog_listLabel);

		listLabel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					list.getTable().setFocus();
				}
			}
		});

		progressLabel = new Label(content, SWT.RIGHT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		progressLabel.setLayoutData(gd);

		list = new TableViewer(content, (multi ? SWT.MULTI : SWT.SINGLE)
				| SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
		list.setContentProvider(contentProvider);
		list.setLabelProvider(getItemsListLabelProvider());
		list.setInput(new Object[0]);
		list.setItemCount(contentProvider.getElements(null).length);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		list.getTable().setLayoutData(gd);

		createPopupMenu();

		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applyFilter();
			}
		});

		pattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (list.getTable().getItemCount() > 0) {
						list.getTable().setFocus();
					}
				}
			}
		});

		list.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event
						.getSelection();
				handleSelected(selection);
			}
		});

		list.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick();
			}
		});

		list.getTable().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP) {
					StructuredSelection selection = (StructuredSelection) list
							.getSelection();

					if (selection.size() == 1) {
						Object element = selection.getFirstElement();
						if (element.equals(list.getElementAt(0))) {
							pattern.setFocus();
						}
						if (list.getElementAt(list.getTable()
								.getSelectionIndex() - 1) instanceof ItemsListSeparator)
							// list.getTable().deselect(list.getTable().getSelectionIndex()
							// - 1);
							list.getTable().setSelection(
									list.getTable().getSelectionIndex() - 1);
						list.getTable().notifyListeners(SWT.Selection,
								new Event());

					}
				}

				if (e.keyCode == SWT.ARROW_DOWN) {
					if (list
							.getElementAt(list.getTable().getSelectionIndex() + 1) instanceof ItemsListSeparator)
						// list.getTable().deselect(list.getTable().getSelectionIndex()
						// + 1);
						list.getTable().setSelection(
								list.getTable().getSelectionIndex() + 1);
					list.getTable().notifyListeners(SWT.Selection, new Event());
				}

			}
		});

		createExtendedContentArea(content);

		details = new DetailsContentViewer(content, SWT.BORDER | SWT.FLAT);
		details.setVisible(toggleStatusLineAction.isChecked());
		details.setContentProvider(new NullContentProvider());
		details.setLabelProvider(getDetailsLabelProvider());

		applyDialogFont(content);

		restoreDialog(getDialogSettings());

		if (initialPatternText != null) {
			pattern.setText(initialPatternText);
		}

		switch (selectionMode) {
		case CARET_BEGINNING:
			pattern.setSelection(0, 0);
			break;
		case FULL_SELECTION:
			pattern.setSelection(0, initialPatternText.length());
			break;
		}

		// apply filter even if pattern is empty (display history)
		applyFilter();

		return dialogArea;
	}

	/**
	 * This method is a hook for subclasses to override default dialog behavior.
	 * The <code>handleDoubleClick()</code> method handles double clicks on
	 * the list of filtered elements.
	 */
	protected void handleDoubleClick() {
		okPressed();
	}

	/**
	 * Refreshes the details field using current selection in the items list.
	 */
	private void refreshDetails() {
		StructuredSelection selection = (StructuredSelection) list
				.getSelection();

		if (selection.size() == 1) {
			Object element = selection.getFirstElement();

			if (element instanceof ItemsListSeparator) {
				details.setInput(null);
			} else {
				details.setInput(element);
			}
		} else {
			details.setInput(null);
		}
	}

	/**
	 * This method is a hook for subclasses to override default dialog behavior.
	 * Handles selection in the list. Updates labels of selected and unselected
	 * items.
	 * 
	 * @param selection
	 *            the new selection
	 */
	protected void handleSelected(StructuredSelection selection) {
		IStatus status = new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
				IStatus.OK, EMPTY_STRING, null);

		if (selection.size() == 0) {
			status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.ERROR, EMPTY_STRING, null); 

			if (lastSelection != null
					&& getListSelectionLabelDecorator() != null) {
				list.update(lastSelection, null);
			}

			lastSelection = null;

		} else {
			status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.ERROR, EMPTY_STRING, null);

			List items = selection.toList();

			Object item = null;
			IStatus tempStatus = null;

			for (Iterator it = items.iterator(); it.hasNext();) {
				Object o = it.next();

				if (o instanceof ItemsListSeparator) {
					continue;
				}

				item = o;
				tempStatus = validateItem(item);

				if (tempStatus.isOK()) {
					status = new Status(IStatus.OK,
							WorkbenchPlugin.PI_WORKBENCH, IStatus.OK, EMPTY_STRING, null);
				} else {
					status = tempStatus;
					// if any selected element is not valid status is set to
					// ERROR
					break;
				}
			}

			if (lastSelection != null
					&& getListSelectionLabelDecorator() != null) {
				list.update(lastSelection, null);
			}

			if (getListSelectionLabelDecorator() != null) {
				list.update(items.toArray(), null);
			}

			lastSelection = items.toArray();
		}

		refreshDetails();
		updateStatus(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_BOUNDS_SETTINGS);
		if (section == null) {
			section = settings.addNewSection(DIALOG_BOUNDS_SETTINGS);
			section.put(DIALOG_HEIGHT, 500);
			section.put(DIALOG_WIDTH, 600);
		}
		return section;
	}

	/**
	 * Returns the dialog settings. Returned object can't be null.
	 * 
	 * @return return dialog settings for this dialog
	 */
	protected abstract IDialogSettings getDialogSettings();

	/**
	 * Refreshes the dialog - have to be called in UI thread.
	 */
	public void refresh() {
		if (list != null && !list.getTable().isDisposed()) {

			if (list != null) {
				// preserve selection
				lastRefreshSelection = ((StructuredSelection) list
						.getSelection()).toArray();
			}

			list.setItemCount(contentProvider.getElements(null).length);
			list.refresh();

			if (list.getTable().getItemCount() > 0) {
				// preserve previous selection
				if (lastRefreshSelection != null
						&& lastRefreshSelection.length > 0)
					list.setSelection(new StructuredSelection(
							lastRefreshSelection));

				if (list.getTable().getSelectionIndices().length == 0) {
					list.setSelection(new StructuredSelection(contentProvider
							.getElements(null)[0]));
				}
			} else {
				list.setSelection(StructuredSelection.EMPTY);
			}
		}

		updateProgressLabel();
	}

	/**
	 * Updates the progress label - should be called in UI thread.
	 */
	public void updateProgressLabel() {
		if (!progressLabel.isDisposed()) {
			progressLabel.setText(contentProvider.getProgressMessage());
		}
	}

	/**
	 * Notifies the content provider - fires filtering of content provider
	 * elements. During the filtering a separator between history and workspace
	 * matches is added. (It is a rather long action - should be called in a
	 * job.)
	 * 
	 * @param checkDuplicates
	 *            true if data concerning elements duplication should be
	 *            computed - it takes much more time than the standard filtering
	 * @param monitor
	 *            a progress monitor or null if no monitor's available
	 */
	public void reloadCache(boolean checkDuplicates,
			GranualProgressMonitor monitor) {
		if (list != null && !list.getTable().isDisposed()
				&& contentProvider != null) {
			contentProvider.reloadCache(checkDuplicates, monitor);
		}
	}

	/**
	 * Schedule refresh job.
	 * 
	 * @param checkDuplicates
	 *            true if data concerning elements duplication should be
	 *            computed - it takes much more time than standard filtering
	 */
	public void scheduleRefresh(boolean checkDuplicates) {

		boolean allJobsCancelled = refreshCacheJob.cancel();

		boolean refreshJobCancelled = refreshJob.cancel();

		allJobsCancelled = allJobsCancelled && refreshJobCancelled;

		if (!allJobsCancelled) {
		//	Job old = refreshCacheJob;
			refreshCacheJob = new RefreshCacheJob(refreshCacheJob);
		//	RefreshJob oldRefreshJob = refreshJob;
			refreshJob = new RefreshJob(refreshCacheJob, refreshJob);
		}

		refreshCacheJob.setCheckDuplicates(checkDuplicates);
		refreshCacheJob.schedule();
	}

	/**
	 * Schedules progerss message refresh.
	 */
	public void scheduleProgressMessageRefresh() {
		if (refreshProgressMessageJob.cancel())
			refreshProgressMessageJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {

		List selectedElements = ((StructuredSelection) list.getSelection())
				.toList();

		List objectsToReturn = new ArrayList();

		Object item = null;

		for (Iterator it = selectedElements.iterator(); it.hasNext();) {
			item = it.next();

			if (!(item instanceof ItemsListSeparator)) {
				accessedHistoryItem(item);
				objectsToReturn.add(item);
			}
		}

		setResult(objectsToReturn);
	}

	/*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#updateStatus(org.eclipse.core.runtime.IStatus)
	 */
	protected void updateStatus(IStatus status) {
		this.status = status;
		super.updateStatus(status);
	}

	/*
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		if (status != null
				&& (status.isOK() || status.getCode() == IStatus.INFO)) {
			super.okPressed();
		}
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. A full selection is used in the pattern
	 * input field.
	 * 
	 * @param text
	 *            initial pattern for the filter
	 * @see FilteredItemsSelectionDialog#FULL_SELECTION
	 */
	public void setInitialPattern(String text) {
		setInitialPattern(text, FULL_SELECTION);
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. The <code>selectioMode</code> is used to
	 * choose selection type for the input field.
	 * 
	 * @param text
	 *            initial pattern for the filter
	 * @param selectionMode
	 *            one of: {@link FilteredItemsSelectionDialog#NONE},
	 *            {@link FilteredItemsSelectionDialog#CARET_BEGINNING},
	 *            {@link FilteredItemsSelectionDialog#FULL_SELECTION}
	 */
	public void setInitialPattern(String text, int selectionMode) {
		this.initialPatternText = text;
		this.selectionMode = selectionMode;
	}

	/**
	 * Gets initial Pattern.
	 * 
	 * @return initial pattern, or null if initial patern is not set
	 */
	protected String getInitialPattern() {
		return this.initialPatternText;
	}

	/**
	 * Returns the current selction
	 * 
	 * @return the current selection
	 */
	protected StructuredSelection getSelectedItems() {
		return (StructuredSelection) list.getSelection();
	}

	/**
	 * Validates the item. When items on the items list are selected or
	 * deselected, it validates each items in the selection and the dialog
	 * status depends on it.
	 * 
	 * @param item
	 *            an item to be checked
	 * @return status of the item
	 */
	protected abstract IStatus validateItem(Object item);

	/**
	 * Creates an instance of a filter.
	 * 
	 * @return a filter for items on the items list
	 */
	protected abstract ItemsFilter createFilter();

	/**
	 * Applies the filter created by <code>createFilter()</code> method to the
	 * items list. It causes refiltering.
	 */
	protected void applyFilter() {

		ItemsFilter newFilter = createFilter();

		// get rid of similiar patterns, for example: *a**b and ***a*b
		if (filter != null && filter.equalsFilter(newFilter)) {
			return;
		}

		stopCurrentFilterJob();

		this.filter = newFilter;

		if (this.filter != null) {
			scheduleFilterJob();
		}

	}

	/**
	 * Returns comparator to sort items inside content provider.
	 * 
	 * @return comparator to sort items content provider
	 */
	protected abstract Comparator getItemsComparator();

	/**
	 * Fills content provider with objects.
	 * 
	 * @param contentProvider
	 *            provider to fill items. During adding items it using
	 *            ItemsFilter to filter items
	 * @param itemsFilter
	 *            the filter
	 * @param progressMonitor
	 *            it is used for tack searching progress. It is responsibility
	 *            for refresh of progress. The state of this progress illustrate
	 *            a state of filtering process .
	 * @throws CoreException
	 */
	protected abstract void fillContentProvider(
			AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException;

	/**
	 * Removes selected items from history
	 * 
	 * @param items
	 *            items to be removed
	 */
	private void removeSelectedItems(List items) {
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			Object item = iter.next();
			removeHistoryItem(item);
		}
	}

	/**
	 * Removes item from history
	 * 
	 * @param item
	 *            to remove
	 * @return removed item
	 */
	protected Object removeHistoryItem(Object item) {
		return contentProvider.removeHistoryElement(item);
	}

	/**
	 * Adds item to history
	 * 
	 * @param item
	 *            the item to be added
	 */
	protected void accessedHistoryItem(Object item) {
		contentProvider.addHistoryElement(item);
	}

	/**
	 * Gets history comparator
	 * 
	 * @return decorated comparator
	 */
	private Comparator getHistoryComparator() {
		return new HistoryComparator();
	}

	/**
	 * Gets history object selected elemnts
	 * 
	 * @return history of selected elements, or null if it is not set
	 */
	protected SelectionHistory getSelectionHistory() {
		return this.contentProvider.getSelectionHistory();
	}

	/**
	 * Sets new history
	 * 
	 * @param selectionHistory
	 *            the history
	 */
	protected void setSelectionHistory(SelectionHistory selectionHistory) {
		if (this.contentProvider != null)
			this.contentProvider.setSelectionHistory(selectionHistory);
	}

	/**
	 * Schedules filtering job. Depending on the filter decides which job will
	 * be scheduled. If last filtering done (last complited filter) is not null
	 * and new filter is a subfilter of the last one it schedules job searching
	 * in cache. If it is the first filtering or new filter isn't a subfilter of
	 * the last one, a full search is scheduled.
	 */
	private synchronized void scheduleFilterJob() {

		if (filter.getPattern().length() == 0) {
			filterJob = new HistoryResultFilterJob(contentProvider, filter);
		} else if (lastCompletedFilter != null && lastCompletedFilter != filter
				&& lastCompletedFilter.isSubFilter(filter)) {
			filterJob = new CachedResultFilterJob(contentProvider, filter,
					lastCompletedResult);
		} else {
			filterJob = new FilterJob(contentProvider, filter);
		}
		filterJob.schedule();

	}

	/**
	 * Stops current filtered job
	 */
	private void stopCurrentFilterJob() {

		if (filterJob != null) {
			filterJob.stop();
		}

	}

	/**
	 * Tells whether the given item is a history item.
	 * 
	 * @param item
	 *            the item to be investigated
	 * @return true if the given item is in history
	 */
	public boolean isHistoryElement(Object item) {
		return this.contentProvider.isHistoryElement(item);
	}

	/**
	 * Tells whether the given item is a duplicate.
	 * 
	 * @param item
	 *            the item to be investigated
	 * @return true if the item is duplicate
	 */
	public boolean isDuplicateElement(Object item) {
		return this.contentProvider.isDuplicateElement(item);
	}

	/**
	 * Returns name for then given object.
	 * 
	 * @param item
	 *            the object
	 * @return name of the given item
	 */
	public abstract String getElementName(Object item);

	private class ToggleStatusLineAction extends Action {

		/**
		 * Creates a new instance of the class
		 */
		public ToggleStatusLineAction() {
			super(
					WorkbenchMessages.FilteredItemsSelectionDialog_toggleStatusAction,
					IAction.AS_CHECK_BOX);
		}

		public void run() {
			details.setVisible(isChecked());
		}
	}

	/**
	 * Only refreshes UI on the basis of an already sorted and filtered set of
	 * items.
	 * 
	 * Standard invocation scenario: filtering job (AbstractFilterJob class -
	 * Job class) -> cache refresh without checking for duplicates
	 * (CacheRefreshJob class - Job class)-> ui refresh (RefreshJob class -
	 * UIJob class) -> cache refresh with checking for duplicates
	 * (CacheRefreshJob class - Job class) -> ui refresh (RefreshJob class -
	 * UIJob class). The scenario is rather complicated, but it had to be
	 * applied, because:
	 * <ul>
	 * <li> refreshing cache is rather a long action and cannot be run in the UI -
	 * cannot be run in a UIJob</li>
	 * <li> refreshing cache checking for duplicates is twice as long as
	 * refreshing cache without checking for duplicates; results of the search
	 * could be displayed earlier</li>
	 * <li> refreshing the UI have to be run in a UIJob</li>
	 * </ul>
	 */
	private class RefreshJob extends UIJob {

		private boolean cancelling = false;

		private RefreshJob previousJob;

		private RefreshCacheJob associatedRefreshCacheJob;

		/**
		 * Creates a new instance of the class
		 * 
		 * @param associatedRefreshCacheJob
		 *            cache refresh job which run before this UI job
		 */
		public RefreshJob(RefreshCacheJob associatedRefreshCacheJob) {
			this(associatedRefreshCacheJob, null);
		}

		/**
		 * Creates a new instance of the class
		 * 
		 * @param associatedRefreshCacheJob
		 *            cache refresh job which run before this UI job
		 * @param previousJob
		 *            previous Ui refresh job (which is being cancelled)
		 */
		public RefreshJob(RefreshCacheJob associatedRefreshCacheJob,
				RefreshJob previousJob) {
			super(FilteredItemsSelectionDialog.this.getParentShell()
					.getDisplay(),
					WorkbenchMessages.FilteredItemsSelectionDialog_refreshJob);
			this.associatedRefreshCacheJob = associatedRefreshCacheJob;
			this.previousJob = previousJob;
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			cancelling = false;
			if (previousJob != null && previousJob != this) {
				try {
					previousJob.join();
				} catch (InterruptedException e) {
				}
				previousJob = null;
			}

			if (cancelling)
				return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
						IStatus.OK, EMPTY_STRING, null);

			if (FilteredItemsSelectionDialog.this != null) {
				FilteredItemsSelectionDialog.this.refresh();
			}
			// prevent changing state of newly created (refresh cache) jobs
			if (!cancelling
					&& this.associatedRefreshCacheJob == FilteredItemsSelectionDialog.this.refreshCacheJob) {
				if (!associatedRefreshCacheJob.isCheckDuplicates()) {
					associatedRefreshCacheJob.setCheckDuplicates(true);
					associatedRefreshCacheJob.schedule(100);
				}
			}

			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, EMPTY_STRING, null);
		}

		protected void canceling() {
			super.canceling();
			cancelling = true;
		}
	}

	/**
	 * Refreshes the progress message.
	 * 
	 */
	private class ProgressMessageRefreshJob extends UIJob {

		private boolean cancelling = false;

		/**
		 * Creates a new instance of the class
		 */
		public ProgressMessageRefreshJob() {
			super(
					FilteredItemsSelectionDialog.this.getParentShell()
							.getDisplay(),
					WorkbenchMessages.FilteredItemsSelectionDialog_progressRefreshJob);
			setSystem(true);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {

			cancelling = false;

			if (FilteredItemsSelectionDialog.this != null) {
				FilteredItemsSelectionDialog.this.updateProgressLabel();
			}

			if (cancelling)
				refreshProgressMessageJob.schedule();

			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, EMPTY_STRING, null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#canceling()
		 */
		protected void canceling() {
			super.canceling();
			this.cancelling = true;
		}
	}

	/**
	 * A job responsible for computing filtered items list presented using
	 * <code>RefreshJob</code>.
	 * 
	 * @see RefreshJob
	 * 
	 */
	private class RefreshCacheJob extends Job {

		private boolean checkDuplicates = true;

		private boolean cancelling = false;

		private Job previousJob = null;

		/**
		 * Creates a new instance of the class
		 */
		public RefreshCacheJob() {
			this(null);
		}

		/**
		 * Creates a new instance of the class
		 * 
		 * @param previousJob
		 *            previous job to be cancelled/joined
		 */
		public RefreshCacheJob(Job previousJob) {
			super(
					WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob);
			this.previousJob = previousJob;
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			cancelling = false;
			if (previousJob != null && previousJob != this) {
				try {
					previousJob.join();
				} catch (InterruptedException e) {
				}
				previousJob = null;
			}
			if (cancelling)
				return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
						IStatus.OK, EMPTY_STRING, null);

			if (FilteredItemsSelectionDialog.this != null) {
				GranualProgressMonitor wrappedMonitor = new GranualProgressMonitor(
						monitor, contentProvider, true);
				FilteredItemsSelectionDialog.this.reloadCache(checkDuplicates,
						wrappedMonitor);
			}

			if (!cancelling) {
				refreshJob.schedule();
			}

			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, EMPTY_STRING, null);

		}

		/**
		 * 
		 * @param checkDuplicates
		 *            true if data concerning elements duplication should be
		 *            computed - it takes much more time than standard filtering
		 */
		public void setCheckDuplicates(boolean checkDuplicates) {
			this.checkDuplicates = checkDuplicates;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#canceling()
		 */
		protected void canceling() {
			super.canceling();
			cancelling = true;
			contentProvider.stopReloadingCache();
		}

		/**
		 * @return Returns the checkDuplicates.
		 */
		public boolean isCheckDuplicates() {
			return checkDuplicates;
		}

	}

	private class RemoveHistoryItemAction extends Action {

		/**
		 * Creates a new instance of the class
		 */
		public RemoveHistoryItemAction() {
			super(
					WorkbenchMessages.FilteredItemsSelectionDialog_removeItemsFromHistoryAction);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			List selectedElements = ((StructuredSelection) list.getSelection())
					.toList();
			removeSelectedItems(selectedElements);
		}
	}

	private class ItemsListLabelProvider extends LabelProvider implements
			IColorProvider, ILabelProviderListener {
		private ILabelProvider provider;

		private ILabelDecorator selectionDecorator;

		// Need to keep our own list of listeners
		private ListenerList listeners = new ListenerList();

		/**
		 * Creates a new instance of the class
		 * 
		 * @param provider
		 *            the label provider for all items, not null
		 * @param selectionDecorator
		 *            the decorator for selected items
		 */
		public ItemsListLabelProvider(ILabelProvider provider,
				ILabelDecorator selectionDecorator) {
			Assert.isNotNull(provider);
			this.provider = provider;
			this.selectionDecorator = selectionDecorator;

			provider.addListener(this);

			if (selectionDecorator != null) {
				selectionDecorator.addListener(this);
			}
		}

		/**
		 * @param newSelectionDecorator
		 *            new label decorator for selected items in the list
		 */
		public void setSelectionDecorator(ILabelDecorator newSelectionDecorator) {
			if (selectionDecorator != null) {
				selectionDecorator.removeListener(this);
				selectionDecorator.dispose();
			}

			selectionDecorator = newSelectionDecorator;

			if (selectionDecorator != null) {
				selectionDecorator.addListener(this);
			}
		}

		/**
		 * @return the label decorator for selected items in the list
		 */
		public ILabelDecorator getSelectionDecorator() {
			return selectionDecorator;
		}

		/**
		 * @param newProvider
		 *            new label provider for items in the list, not null
		 */
		public void setProvider(ILabelProvider newProvider) {
			Assert.isNotNull(newProvider);
			provider.removeListener(this);
			provider.dispose();

			provider = newProvider;

			if (provider != null) {
				provider.addListener(this);
			}
		}

		/**
		 * @return the label provider for items in the list
		 */
		public ILabelProvider getProvider() {
			return provider;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			if (element instanceof ItemsListSeparator) {
				return WorkbenchImages
						.getImage(IWorkbenchGraphicConstants.IMG_OBJ_SEPARATOR);
			}

			return provider.getImage(element);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof ItemsListSeparator) {
				return getSeparatorLabel(((ItemsListSeparator) element)
						.getName());
			}

			String str = provider.getText(element);

			if (selectionDecorator != null && element != null) {

				// ((StructuredSelection)list.getSelection()).toList().contains(element))
				// cannot be used - virtual tables produce cycles in
				// update item - get selection invocation scenarios

				int[] selectionIndices = list.getTable().getSelectionIndices();
				List elements = Arrays
						.asList(contentProvider.getElements(null));
				for (int i = 0; i < selectionIndices.length; i++) {
					if (elements.size() > i
							&& element
									.equals(elements.get(selectionIndices[i]))) {
						str = selectionDecorator.decorateText(str, element);
						break;
					}
				}

			}

			return str;
		}

		private String getSeparatorLabel(String separatorLabel) {
			Rectangle rect = list.getTable().getBounds();

			int borderWidth = list.getTable().computeTrim(0, 0, 0, 0).width;

			int imageWidth = WorkbenchImages.getImage(
					IWorkbenchGraphicConstants.IMG_OBJ_SEPARATOR).getBounds().width;

			int width = rect.width - borderWidth - imageWidth;

			GC gc = new GC(list.getTable());
			gc.setFont(list.getTable().getFont());

			int fSeparatorWidth = gc.getAdvanceWidth('-');
			int fMessageLength = gc.textExtent(separatorLabel).x;

			gc.dispose();

			StringBuffer dashes = new StringBuffer();
			int chars = (((width - fMessageLength) / fSeparatorWidth) / 2) - 2;
			for (int i = 0; i < chars; i++) {
				dashes.append('-');
			}

			StringBuffer result = new StringBuffer();
			result.append(dashes);
			result.append(" " + separatorLabel + " "); //$NON-NLS-1$//$NON-NLS-2$
			result.append(dashes);
			return result.toString().trim();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			provider.removeListener(this);
			provider.dispose();

			if (selectionDecorator != null) {
				selectionDecorator.removeListener(this);
				selectionDecorator.dispose();
			}

			super.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
		 *      java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			if (provider.isLabelProperty(element, property)) {
				return true;
			}
			if (selectionDecorator != null
					&& selectionDecorator.isLabelProperty(element, property)) {
				return true;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			if (element instanceof ItemsListSeparator) {
				return Display.getCurrent().getSystemColor(
						SWT.COLOR_WIDGET_NORMAL_SHADOW);
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProviderListener#labelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
		 */
		public void labelProviderChanged(LabelProviderChangedEvent event) {
			Object[] l = listeners.getListeners();
			for (int i = 0; i < listeners.size(); i++) {
				((ILabelProviderListener) l[i]).labelProviderChanged(event);
			}
		}
	}

	/**
	 * Used in ItemsListContentProvider, separates history and non-history
	 * items.
	 */
	protected class ItemsListSeparator {

		private String name;

		/**
		 * Creates a new instance of the class
		 * 
		 * @param name
		 *            the name of the separator
		 */
		public ItemsListSeparator(String name) {
			this.name = name;
		}

		/**
		 * Returns the name of this separator.
		 * 
		 * @return the name of the separator
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * GranualProgressMonitor is used for monitoring progress of filtering
	 * process. It updates progress message and refreshes dialog after concrete
	 * part of work. State of this monitor illustrates state of filtering or
	 * cache refreshing process.
	 * 
	 * The <code>GranualProgressMonitor</code> progress monitor changes amount
	 * of work to be done before next update is to be scheduled (increases
	 * granuality). For 0-10% updates are scheduled for all whole numbers {1, 2,
	 * 3, . . ., 10 } surpassed. For 10-100% updates are done every 10%.
	 * 
	 * @see GranualProgressMonitor#internalWorked(double)
	 */
	private static class GranualProgressMonitor extends ProgressMonitorWrapper {

		private ContentProvider contentProvider;

		private String name;

		private int totalWork;

		private double worked;

		private boolean done;

		private boolean isFiltering;

		/**
		 * Creates instance of FilteringProgressMonitor
		 * 
		 * @param monitor
		 *            progress to be wrapped
		 * @param contentProvider
		 * @param isFiltering
		 *            if this progress monitor is attached to a filtering job;
		 *            if false the job ought to be a cache/UI refresh job;
		 *            filtering jobs have higher priority - if there's a running
		 *            filtering jobs progress updates triggered from a
		 *            non-filtering job will not be displayed on UI !
		 */
		public GranualProgressMonitor(IProgressMonitor monitor,
				ContentProvider contentProvider, boolean isFiltering) {
			super(monitor);
			this.contentProvider = contentProvider;
			this.isFiltering = isFiltering;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#setTaskName(java.lang.String)
		 */
		public void setTaskName(String name) {
			super.setTaskName(name);
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#beginTask(java.lang.String,
		 *      int)
		 */
		public void beginTask(String name, int totalWork) {
			super.beginTask(name, totalWork);
			if (this.name == null)
				this.name = name;
			this.totalWork = totalWork;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#worked(int)
		 */
		public void worked(int work) {
			super.worked(work);
			internalWorked(work);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#done()
		 */
		public void done() {
			done = true;
			contentProvider.setProgressMessage("", isFiltering); //$NON-NLS-1$
			super.done();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#setCanceled(boolean)
		 */
		public void setCanceled(boolean b) {
			done = true;
			super.setCanceled(b);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#internalWorked(double)
		 */
		public void internalWorked(double work) {
			worked = worked + work;
			if ((((int) (((worked - work) * 10) / totalWork)) < ((int) ((worked * 10) / totalWork)))
					|| (((int) ((worked * 10) / totalWork)) == 0))
				if (!isCanceled())
					contentProvider.setProgressMessage(getMessage(),
							isFiltering);
		}

		private String getMessage() {
			if (done) {
				return ""; //$NON-NLS-1$
			} else if (totalWork == 0) {
				return name;
			} else {
				return MessageFormat
						.format(
								"{0} ({1}%)" //$NON-NLS-1$
								,
								new Object[] {
										name,
										new Integer(
												(int) ((worked * 100) / totalWork)) });
			}
		}

	}

	/**
	 * Abstract job for filtering elements. It is a pattern job for filtering
	 * cached elements and full filtering.
	 */
	private abstract static class AbstractFilterJob extends Job {

		/**
		 * ContenProvider used to store results of the filtering process.
		 */
		protected ContentProvider contentProvider;

		/**
		 * Filter used during the filtering process.
		 */
		protected ItemsFilter itemsFilter;

		/**
		 * Creates a new instance of the filtering job.
		 * 
		 * @param contentProvider
		 * @param itemsFilter
		 * @param previousFilterJob
		 */
		protected AbstractFilterJob(ContentProvider contentProvider,
				ItemsFilter itemsFilter) {
			super(WorkbenchMessages.FilteredItemsSelectionDialog_jobLabel);
			this.contentProvider = contentProvider;
			this.itemsFilter = itemsFilter;
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected final IStatus run(IProgressMonitor parent) {
			GranualProgressMonitor monitor = new GranualProgressMonitor(parent,
					contentProvider, true);
			return doRun(monitor);
		}

		/**
		 * Stops job
		 */
		public void stop() {
			cancel();
		}

		/**
		 * Executes job using the given filtering progress monitor. A hook for
		 * subclasses.
		 * 
		 * @param monitor
		 *            progress monitor
		 * @return result of the exceution
		 */
		protected IStatus doRun(GranualProgressMonitor monitor) {
			try {
				internalRun(monitor);
			} catch (CoreException e) {
				this.stop();
				return new Status(
						IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH,
						IStatus.ERROR,
						WorkbenchMessages.FilteredItemsSelectionDialog_jobError,
						e);
			} catch (OperationCanceledException e) {
				return canceled(e);
			}
			return ok();
		}

		/**
		 * Filters items
		 * 
		 * @param monitor
		 *            for monitoring progress
		 * @throws CoreException
		 */
		protected abstract void filterContent(GranualProgressMonitor monitor)
				throws CoreException;

		/**
		 * Main method for jobs.
		 * 
		 * @param monitor
		 * @throws CoreException
		 */
		private void internalRun(GranualProgressMonitor monitor)
				throws CoreException {

			if (monitor.isCanceled())
				throw new OperationCanceledException();
			
			this.contentProvider.reset();

			filterContent(monitor);

			if (monitor.isCanceled()) 
				throw new OperationCanceledException();
			
			contentProvider.refresh(false);
		}

		private IStatus canceled(Exception e) {
			return new Status(IStatus.CANCEL, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.CANCEL,
					WorkbenchMessages.FilteredItemsSelectionDialog_jobCancel, e);
		}

		private IStatus ok() {
			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, EMPTY_STRING, null);
		}
	}

	/**
	 * Filters elements using cache.
	 */
	private static class CachedResultFilterJob extends AbstractFilterJob {
		private List lastResult;

		/**
		 * Create instance of CachedResultFilterJob
		 * 
		 * @param contentProvider
		 * @param itemsFilter
		 * @param lastResult
		 * @param previousFilterJob
		 */
		public CachedResultFilterJob(ContentProvider contentProvider,
				ItemsFilter itemsFilter, List lastResult) {
			super(contentProvider, itemsFilter);
			this.lastResult = lastResult;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractFilterJob#filterContent(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.FilteringProgressMonitor)
		 */
		protected void filterContent(GranualProgressMonitor monitor) {
			if (lastResult != null) {

				int length = this.lastResult.size() / 500;
				monitor
						.beginTask(
								WorkbenchMessages.FilteredItemsSelectionDialog_cacheSearchJob_taskName,
								length);

				for (int pos = 0; pos < this.lastResult.size(); pos++) {

					Object item = this.lastResult.get(pos);
					if (monitor.isCanceled())
						break;
					this.contentProvider.add(item, itemsFilter);

					if ((pos % 500) == 0)
						monitor.worked(1);

				}
			}
			monitor.done();
		}
	}

	/**
	 * Filters items in indicated set and history. During filtering it refresh
	 * dialog (progres monitor and elements list).
	 */
	private class FilterJob extends AbstractFilterJob {

		/**
		 * Creates new instance of FilterJob
		 * 
		 * @param contentProvider
		 * @param itemsFilter
		 * @param previousFilterJob
		 */
		public FilterJob(ContentProvider contentProvider,
				ItemsFilter itemsFilter) {
			super(contentProvider, itemsFilter);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractFilterJob#filterContent(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.FilteringProgressMonitor)
		 */
		protected void filterContent(GranualProgressMonitor monitor)
				throws CoreException {

			if (monitor != null && monitor.isCanceled())
				return;

			lastCompletedFilter = null;
			lastCompletedResult = null;

			this.contentProvider.addHistoryItems(this.itemsFilter);

			SubProgressMonitor subMonitor = null;
			if (monitor != null) {
				monitor
						.beginTask(
								WorkbenchMessages.FilteredItemsSelectionDialog_searchJob_taskName,
								1000);
				subMonitor = new SubProgressMonitor(monitor, 500);

			}

			fillContentProvider(this.contentProvider, this.itemsFilter,
					subMonitor);

			if (monitor != null && !monitor.isCanceled()) {
				monitor.worked(100);
				this.contentProvider.rememberResult(this.itemsFilter);
				monitor.worked(400);
				monitor.done();
			}

		}
	}

	/**
	 * Only filters hostory items.
	 */
	private class HistoryResultFilterJob extends AbstractFilterJob {

		/**
		 * Creates new instance of HistoryResultFilterJob
		 * 
		 * @param contentProvider
		 * @param itemsFilter
		 * @param previousFilterJob
		 */
		public HistoryResultFilterJob(ContentProvider contentProvider,
				ItemsFilter itemsFilter) {
			super(contentProvider, itemsFilter);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractFilterJob#filterContent(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.FilteringProgressMonitor)
		 */
		protected void filterContent(GranualProgressMonitor monitor) {

			this.contentProvider.addHistoryItems(this.itemsFilter);

			if (monitor != null && !monitor.isCanceled()) {
				monitor.done();
			}
		}
	}

	/**
	 * History stores a list of key, object pairs. The list is bounded at size
	 * MAX_HISTORY_SIZE. If the list exceeds this size the eldest element is
	 * removed from the list. An element can be added/renewed with a call to
	 * <code>accessed(Object)</code>.
	 * 
	 * The history can be stored to/loaded from an xml file.
	 */
	protected static abstract class SelectionHistory {

		private static final String DEFAULT_ROOT_NODE_NAME = "historyRootNode"; //$NON-NLS-1$

		private static final String DEFAULT_INFO_NODE_NAME = "infoNode"; //$NON-NLS-1$

		private static final int MAX_HISTORY_SIZE = 60;

		private final List historyList;

		private final String rootNodeName;

		private final String infoNodeName;

		private SelectionHistory(String rootNodeName, String infoNodeName) {

			historyList = Collections.synchronizedList(new LinkedList() {

				private static final long serialVersionUID = 0L;

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.LinkedList#add(java.lang.Object)
				 */
				public boolean add(Object arg0) {
					if (this.size() > MAX_HISTORY_SIZE)
						this.removeFirst();
					if (!this.contains(arg0))
						return super.add(arg0);
					return false;
				}

			});

			this.rootNodeName = rootNodeName;
			this.infoNodeName = infoNodeName;
		}

		/**
		 * Creates new instance of SelectionHistory
		 */
		public SelectionHistory() {
			this(DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
		}

		/**
		 * Adds object to history
		 * 
		 * @param object
		 *            the item to be added to the history
		 */
		public synchronized void accessed(Object object) {
			historyList.add(object);
		}

		/**
		 * Returns true if history contains object
		 * 
		 * @param object
		 *            the item for which check will be executed
		 * @return true if history contains object false in other way
		 */
		public synchronized boolean contains(Object object) {
			return historyList.contains(object);
		}

		/**
		 * Returns true if history is empty
		 * 
		 * @return true if history is empty
		 */
		public synchronized boolean isEmpty() {
			return historyList.isEmpty();
		}

		/**
		 * Remove element from history
		 * 
		 * @param element
		 *            to remove form the history
		 * @return removed element
		 */
		public synchronized boolean remove(Object element) {
			return historyList.remove(element);
		}

		/**
		 * Load history elements from memento.
		 * 
		 * @param memento
		 *            memento from which the history will be retrieved
		 */
		public void load(IMemento memento) {

			XMLMemento historyMemento = (XMLMemento) memento
					.getChild(rootNodeName);

			if (historyMemento == null)
				return;

			IMemento[] mementoElements = historyMemento
					.getChildren(infoNodeName);
			for (int i = 0; i < mementoElements.length; ++i) {
				IMemento mementoElement = mementoElements[i];
				Object object = restoreItemFromMemento(mementoElement);
				if (object != null) {
					historyList.add(object);
				}
			}
		}

		/**
		 * Save history elements to memento
		 * 
		 * @param memento
		 *            memento to which the history will be added
		 */
		public void save(IMemento memento) {

			IMemento historyMemento = memento.createChild(rootNodeName);

			Object[] items = getHistoryItems();
			for (int i = 0; i < items.length; i++) {
				Object item = items[i];
				IMemento elementMemento = historyMemento
						.createChild(infoNodeName);
				storeItemToMemento(item, elementMemento);
			}

		}

		/**
		 * Gets array of history items
		 * 
		 * @return array of history elements
		 */
		public synchronized Object[] getHistoryItems() {
			return historyList.toArray();
		}

		/**
		 * Creates an object using given memento
		 * 
		 * @param memento
		 *            memento used for creating new object
		 * 
		 * @return the restored object
		 */
		protected abstract Object restoreItemFromMemento(IMemento memento);

		/**
		 * Store object in <code>IMemento</code>
		 * 
		 * @param item
		 *            the item to store
		 * @param memento
		 *            the memento to store to
		 */
		protected abstract void storeItemToMemento(Object item, IMemento memento);

	}

	/**
	 * Filters elements using SearchPattern for comparation name of items with
	 * pattern.
	 */
	protected abstract class ItemsFilter {

		protected SearchPattern patternMatcher;

		/**
		 * Creates new instance of SearchFilter
		 */
		public ItemsFilter() {
			this(new SearchPattern());
		}

		/**
		 * Creates new instance of ItemsFilter
		 * 
		 * @param searchPattern
		 *            the pattern to be used when filtering
		 */
		public ItemsFilter(SearchPattern searchPattern) {
			patternMatcher = searchPattern;
			String stringPattern = ""; //$NON-NLS-1$
			if (pattern != null && !pattern.getText().equals("*")) { //$NON-NLS-1$
				stringPattern = pattern.getText();
			}
			patternMatcher.setPattern(stringPattern);
		}

		/**
		 * Check if <code>ItemsFilter</code> is sub-filter of this. In basic
		 * version it depends on pattern.
		 * 
		 * @param filter
		 *            the filter to be checked
		 * @return true if filter is sub-filter of this false if filter isn't
		 *         sub-filter
		 */
		public boolean isSubFilter(ItemsFilter filter) {
			if (filter != null) {
				return this.patternMatcher.isSubPattern(filter.patternMatcher);
			}
			return false;
		}

		/**
		 * Checks whether the provided filter is equal to this filter.
		 * 
		 * @param iFilter
		 *            filter to be checked
		 * @return true if the given ItemsFilter is equal to this filter
		 */
		public boolean equalsFilter(ItemsFilter iFilter) {
			if (iFilter != null
					&& iFilter.patternMatcher
							.equalsPattern(this.patternMatcher)) {
				return true;
			}
			return false;
		}

		/**
		 * Ckeckes whether the pattern is camelCase
		 * 
		 * @return true if text is camelCase pattern false if text don't
		 *         implement camelCase cases
		 */
		public boolean isCamelCasePattern() {
			return patternMatcher.getMatchRule() == SearchPattern.RULE_CAMELCASE_MATCH;
		}

		/**
		 * Gets pattern string
		 * 
		 * @return pattern for this filter
		 */
		public String getPattern() {
			return patternMatcher.getPattern();
		}

		/**
		 * Returns the rule to apply for matching keys.
		 * 
		 * @return match rule
		 */
		public int getMatchRule() {
			return patternMatcher.getMatchRule();
		}

		/**
		 * Matches text with filter
		 * 
		 * @param text
		 * @return true if text and filter pattern was matched, false if not
		 *         matched
		 */
		protected boolean matches(String text) {
			return patternMatcher.matches(text);
		}
		
		public boolean matchesRawNamePattern(String name) {
			
			String prefix = patternMatcher.getPattern();
			String text = name;
			
			int textLength= text.length();
			int prefixLength= prefix.length();
			if (textLength < prefixLength)
				return false;
			for (int i= prefixLength - 1; i >= 0; i--) {
				if (Character.toLowerCase(prefix.charAt(i)) != Character.toLowerCase(text.charAt(i)))
					return false;
			}
			return true;
		}
		
		/**
		 * Matches uses during prefix match
		 * 
		 * @param item 
		 * @return whether the item matches the raw name pattern
		 */
		public boolean matchesRawNamePattern(Object item) {
			return true;
		}

		/**
		 * Matches items against filter conditions
		 * 
		 * @param item
		 * @return true if item matches against filter conditions false
		 *         otherwise
		 */
		public abstract boolean matchItem(Object item);

		/**
		 * Checks consistency of items. Item is inconsitent if was changed or
		 * removed
		 * 
		 * @param item
		 * @return true if item is consistent false if item is inconsitent
		 */
		public abstract boolean isConsistentItem(Object item);

	}

	/**
	 * An interface to content providers for FilterItemsSelectionDialog
	 */
	protected abstract class AbstractContentProvider {
		/**
		 * Adds items to content provider. During this itms are filtered by
		 * filter. It's depend on
		 * <code> matchsElement(AbstarctListItem item) <code>.
		 * 
		 * @param item
		 * @param itemsFilter
		 */
		public abstract void add(Object item, ItemsFilter itemsFilter);
	}

	/**
	 * Collects filtered elements. Conatains one synchronized sorted set for
	 * collecting filtered elements. All collected elements are sorted using
	 * comparator. Comparator is return by getElementComparator() method. To
	 * filtering elements it use implementation of ItemsFilter. The keyg function
	 * of filter used in to filtering is matchsItem(Object item).
	 * 
	 * The <code>ContentProvider</code> class also provides item filtering
	 * methods. The filtering has beeen moved from the standard TableView
	 * getFilteredItems() method to content provider, because
	 * ILazyContentProvider and virtual tables are used. This class is
	 * responsible for adding a separator below history items and marking each
	 * items as duplicate if its name repeats more than once on the filtered
	 * list.
	 */
	private class ContentProvider extends AbstractContentProvider implements
			IStructuredContentProvider, ILazyContentProvider {

		private SelectionHistory selectionHistory;

		/**
		 * Raw result of the searching (unsorted, unfiltered)
		 * 
		 * Standard object flow: items -> lastSortedItems -> lastFilteredItems
		 */
		private Set items;

		/**
		 * Those of the items that are duplicates
		 */
		private Set duplicates;

		private String progressMessage = ""; //$NON-NLS-1$

		/**
		 * List of <code>ViewerFilter</code>s to be used during filtering
		 */
		private List filters;

		/**
		 * Result of the last filtering.
		 * 
		 * Standard object flow: items -> lastSortedItems -> lastFilteredItems
		 */
		private List lastFilteredItems;

		/**
		 * Result of the last sorting.
		 * 
		 * Standard object flow: items -> lastSortedItems -> lastFilteredItems
		 */
		private List lastSortedItems;

		/**
		 * Used for getFilteredElements() method cancelling (when the job that
		 * invoked the method was cancelled).
		 * 
		 * Method cancelling could be based (only) on monitor cancelling
		 * unfortunately sometimes the method getFilteredElements() could be run
		 * with a null monitor, the reset flag have to be left intact
		 */
		private boolean reset;

		/**
		 * Creates new instance of ContentProvider
		 * 
		 * @param selectionHistory
		 */
		public ContentProvider(SelectionHistory selectionHistory) {
			this();
			this.selectionHistory = selectionHistory;
		}

		/**
		 * Creates new instance of ContentProvider
		 * 
		 */
		public ContentProvider() {
			this.items = Collections.synchronizedSet(new HashSet(2048));
			this.duplicates = Collections.synchronizedSet(new HashSet(256));
			this.lastFilteredItems = Collections
					.synchronizedList(new ArrayList(2048));
			this.lastSortedItems = Collections.synchronizedList(new ArrayList(
					2048));
		}

		/**
		 * Sets selection history
		 * 
		 * @param selectionHistory
		 *            The selectionHistory to set.
		 */
		public void setSelectionHistory(SelectionHistory selectionHistory) {
			this.selectionHistory = selectionHistory;
		}

		/**
		 * @return Returns the selectionHistory.
		 */
		public SelectionHistory getSelectionHistory() {
			return selectionHistory;
		}

		/**
		 * Remove all content items and resets progress message
		 */
		public void reset() {
			reset = true;
			this.items.clear();
			this.duplicates.clear();
			this.lastSortedItems.clear();
			this.lastFilteredItems.clear();
			this.progressMessage = ""; //$NON-NLS-1$
		}

		/**
		 * Stops reloading cache - getFilteredItems() method.
		 */
		public void stopReloadingCache() {
			reset = true;
		}

		/**
		 * Adds filtered item
		 * 
		 * @param item
		 * @param itemsFilter
		 */
		public void add(Object item, ItemsFilter itemsFilter) {
			if (itemsFilter == filter) {
				if (itemsFilter != null) {
					if (itemsFilter.matchItem(item)) {
						this.items.add(item);
					}
				} else {
					this.items.add(item);
				}
			}
		}

		/**
		 * Add all history items to contentProvider
		 * 
		 * @param itemsFilter
		 */
		public void addHistoryItems(ItemsFilter itemsFilter) {
			if (this.selectionHistory != null) {
				Object[] items = this.selectionHistory.getHistoryItems();
				for (int i = 0; i < items.length; i++) {
					Object item = items[i];
					if (itemsFilter == filter) {
						if (itemsFilter != null) {
							if (itemsFilter.matchItem(item)) {
								if (itemsFilter.isConsistentItem(item)) {
									this.items.add(item);
								} else {
									this.selectionHistory.remove(item);
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Refresh dialog.
		 * 
		 * @param checkDuplicates
		 *            true if data concerning elements duplication should be
		 *            computed - it takes much more time than standard filtering
		 */
		public void refresh(boolean checkDuplicates) {
			scheduleRefresh(checkDuplicates);
		}

		/**
		 * Sets progress message
		 * 
		 * @param progressMessage
		 * @param isFiltering
		 *            if this progress update was triggered by a filtering job
		 *            or a refresh job; if it was triggered by a refresh job and
		 *            a filtering job is running, the progress won't be
		 *            displayed
		 */
		public void setProgressMessage(String progressMessage,
				boolean isFiltering) {
			if (!isFiltering && filterJob != null
					&& filterJob.getState() == Job.RUNNING)
				return;
			this.progressMessage = progressMessage;
			scheduleProgressMessageRefresh();
		}

		/**
		 * Gets progress message
		 * 
		 * @return progress message
		 */
		public String getProgressMessage() {
			return progressMessage;
		}

		/**
		 * Remove items from history and refresh view
		 * 
		 * @param item
		 *            to remove
		 * 
		 * @return removed item
		 */
		public Object removeHistoryElement(Object item) {
			if (this.selectionHistory != null)
				this.selectionHistory.remove(item);
			if (filter == null || filter.getPattern().length() == 0) {
				items.remove(item);
				duplicates.remove(item);
				this.lastSortedItems.remove(item);
			}

			synchronized (lastSortedItems) {
				Collections.sort(lastSortedItems, getHistoryComparator());
			}
			this.refresh(false);
			return item;
		}

		/**
		 * Adds item to history and refresh view
		 * 
		 * @param item
		 *            to add
		 */
		public void addHistoryElement(Object item) {
			if (this.selectionHistory != null)
				this.selectionHistory.accessed(item);
			if (filter == null || !filter.matchItem(item)) {
				this.items.remove(item);
				this.duplicates.remove(item);
				this.lastSortedItems.remove(item);
			}
			synchronized (lastSortedItems) {
				Collections.sort(lastSortedItems, getHistoryComparator());
			}
			this.refresh(false);
		}

		/**
		 * @param item
		 * @return true if given item is part of the hisory
		 */
		public boolean isHistoryElement(Object item) {
			if (this.selectionHistory != null) {
				return this.selectionHistory.contains(item);
			}
			return false;
		}

		/**
		 * @param item
		 * @param isDuplicate
		 */
		public void setDuplicateElement(Object item, boolean isDuplicate) {
			if (this.items.contains(item)) {
				if (isDuplicate)
					this.duplicates.add(item);
				else
					this.duplicates.remove(item);
			}
		}

		/**
		 * @param item
		 * @return true if item is duplicate
		 */
		public boolean isDuplicateElement(Object item) {
			return duplicates.contains(item);
		}

		/**
		 * Load history from memento.
		 * 
		 * @param memento
		 *            memento from which the history will be retrieved
		 */
		public void loadHistory(IMemento memento) {
			if (this.selectionHistory != null)
				this.selectionHistory.load(memento);
		}

		/**
		 * Save history to memento
		 * 
		 * @param memento
		 *            memento to which the history will be added
		 */
		public void saveHistory(IMemento memento) {
			this.selectionHistory.save(memento);
		}

		/**
		 * Get filtered items
		 * 
		 * @return filtered items
		 */
		private Object[] getItems(boolean sort) {
			if (sort || lastSortedItems.size() != items.size()) {
				synchronized (lastSortedItems) {
					lastSortedItems.clear();
					lastSortedItems.addAll(items);
					Collections.sort(lastSortedItems, getHistoryComparator());
				}
			}
			return lastSortedItems.toArray();
		}

		/**
		 * Remember result of filtering
		 * 
		 * @param itemsFilter
		 */
		public synchronized void rememberResult(ItemsFilter itemsFilter) {
			if (itemsFilter == filter && lastCompletedFilter == null) {
				lastCompletedResult = Collections.synchronizedList(Arrays
						.asList(getItems(false)));
				// synchronization
				if (lastCompletedResult.size() == 0 && itemsFilter != filter) {
					lastCompletedFilter = null;
					lastCompletedResult = null;
				} else {
					lastCompletedFilter = itemsFilter;
				}
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			if (lastFilteredItems.size() != items.size())
				reloadCache(false, null);
			return lastFilteredItems.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
		 */
		public void updateElement(int index) {

			if (lastFilteredItems.size() != items.size())
				reloadCache(false, null);
			FilteredItemsSelectionDialog.this.list.replace((lastFilteredItems
					.size() > index) ? lastFilteredItems.get(index) : null,
					index);

		}

		/**
		 * Main method responsible for getting the filtered items and checking
		 * for duplicates. It is based on the
		 * {@link ContentProvider#getFilteredItems(Object, IProgressMonitor)}.
		 * 
		 * @param checkDuplicates
		 *            true if data concerning elements duplication should be
		 *            computed - it takes much more time than standard filtering
		 * 
		 * @param monitor
		 *            progress monitor
		 */
		public void reloadCache(boolean checkDuplicates,
				GranualProgressMonitor monitor) {

			reset = false;

			if (monitor != null) {
				// the work is divided into two actions of the same length
				int totalWork = 100;
				if (checkDuplicates)
					totalWork = 200;

				monitor
						.beginTask(
								WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob,
								totalWork);
			}

			// the TableViewer's root (the input) is treated as parent
			lastFilteredItems.clear();
			// if (reset)
			// return;
			lastFilteredItems.addAll(Arrays.asList(getFilteredItems(list
					.getInput(), monitor != null ? new SubProgressMonitor(
					monitor, 100) : null)));

			if (reset || (monitor != null && monitor.isCanceled())) {
				if (monitor != null)
					monitor.done();
				return;
			}

			if (checkDuplicates) {
				checkDuplicates(monitor);
			}

			if (monitor != null)
				monitor.done();

		}

		private void checkDuplicates(GranualProgressMonitor monitor) {
			synchronized (lastFilteredItems) {
				IProgressMonitor subMonitor = null;
				int reportEvery = lastFilteredItems.size() / 20;
				if (monitor != null) {
					subMonitor = new SubProgressMonitor(monitor, 100);
					subMonitor
							.beginTask(
									WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob_checkDuplicates,
									5);
				}
				HashMap helperMap = new HashMap();
				for (int i = 0; i < lastFilteredItems.size(); i++) {
					if (reset
							|| (subMonitor != null && subMonitor.isCanceled()))
						return;
					Object item = lastFilteredItems.get(i);

					if (!(item instanceof ItemsListSeparator)) {
						Object previousItem = helperMap.put(
								getElementName(item), item);
						if (previousItem != null) {
							setDuplicateElement(previousItem, true);
							setDuplicateElement(item, true);
						} else {
							setDuplicateElement(item, false);
						}
					}

					if (subMonitor != null && reportEvery != 0
							&& (i + 1) % reportEvery == 0)
						subMonitor.worked(1);
				}
				helperMap.clear();
			}
		}

		/**
		 * Returns an array of items filtered using the provided
		 * <code>ViewerFilter</code>s with a separator added.
		 * 
		 * @param parent
		 *            the parent
		 * @param monitor
		 *            progress monitor
		 * @return an array of filtered items
		 */
		protected Object[] getFilteredItems(Object parent,
				IProgressMonitor monitor) {
			int ticks = 100;

			if (monitor != null) {
				monitor
						.beginTask(
								WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob_getFilteredElements,
								ticks);
				if (filters != null) {
					ticks /= (filters.size() + 2);
				} else {
					ticks /= 2;
				}
			}

			// get already sorted array
			Object[] filteredElements = getItems(false);

			if (monitor != null)
				monitor.worked(ticks);

			// filter the elements using provided ViewerFilters
			if (filters != null && filteredElements != null) {
				for (Iterator iter = filters.iterator(); iter.hasNext();) {
					ViewerFilter f = (ViewerFilter) iter.next();
					filteredElements = f.filter(list, parent, filteredElements);
					if (monitor != null)
						monitor.worked(ticks);
				}
			}

			if (filteredElements == null) {
				if (monitor != null)
					monitor.done();
				return new Object[0];
			}

			ArrayList preaparedElements = new ArrayList();
			boolean hasHistory = false;

			if (filteredElements.length > 0) {
				if (isHistoryElement(filteredElements[0])) {
					hasHistory = true;
				}
			}

			int reportEvery = filteredElements.length / ticks;

			// add separator
			for (int i = 0; i < filteredElements.length; i++) {
				Object item = filteredElements[i];

				if (hasHistory && !isHistoryElement(item)) {
					preaparedElements
							.add(new ItemsListSeparator(
									WorkbenchMessages.FilteredItemsSelectionDialog_separatorLabel));
					hasHistory = false;
				}

				preaparedElements.add(item);

				if (monitor != null && reportEvery != 0
						&& ((i + 1) % reportEvery == 0))
					monitor.worked(1);
			}

			if (monitor != null)
				monitor.done();

			return preaparedElements.toArray();
		}

		/**
		 * Adds a filter to this content provider. For an example usage of such
		 * filters look at the project org.eclipse.ui.ide, class
		 * org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog.CustomWorkingSetFilter.
		 * 
		 * @param filter
		 *            the filter to be added
		 */
		public void addFilter(ViewerFilter filter) {
			if (filters == null) {
				filters = new ArrayList();
			}
			filters.add(filter);
			// currently filters are only added when dialog is restored
			// if it is changed, refreshing the whole tableviewer should be
			// added
		}

	}

	/**
	 * A content provider that does nothing.
	 */
	private class NullContentProvider implements IContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * DetailsContentViewer objects are wrappers for labels.
	 * DetailsContentViewer provides means to change label's image and text when
	 * the attached LabelProvider is updated.
	 */
	private class DetailsContentViewer extends ContentViewer {

		private CLabel label;

		/**
		 * Unfortunately it was impossible to delegate displaying border to
		 * label. The ViewForm is used because CLabel displays shadow when
		 * border is present.
		 */
		private ViewForm viewForm;

		/**
		 * Constructs a new instance of this class given its parent and a style
		 * value describing its behavior and appearance.
		 * 
		 * @param parent
		 *            the parent component
		 * @param style
		 *            SWT style bits
		 */
		public DetailsContentViewer(Composite parent, int style) {
			viewForm = new ViewForm(parent, style);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			viewForm.setLayoutData(gd);
			label = new CLabel(viewForm, SWT.FLAT);
			label.setFont(parent.getFont());
			viewForm.setContent(label);
			hookControl(label);
		}

		/**
		 * Shows/hides the content viewer.
		 * 
		 * @param visible
		 *            if the content viewer should be visible.
		 */
		public void setVisible(boolean visible) {
			GridData gd = (GridData) viewForm.getLayoutData();
			gd.exclude = !visible;
			viewForm.getParent().layout();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
		 *      java.lang.Object)
		 */
		protected void inputChanged(Object input, Object oldInput) {

			if (oldInput == null) {
				if (input == null) {
					return;
				}
				refresh();
				return;
			}

			refresh();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ContentViewer#handleLabelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
		 */
		protected void handleLabelProviderChanged(
				LabelProviderChangedEvent event) {
			if (event != null) {
				refresh(event.getElements());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#getControl()
		 */
		public Control getControl() {
			return label;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#getSelection()
		 */
		public ISelection getSelection() {
			// not supported
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#refresh()
		 */
		public void refresh() {
			if (getInput() != null) {
				ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
				doRefresh(labelProvider.getText(getInput()), labelProvider
						.getImage(this.getInput()));
			} else {
				doRefresh("", null);//$NON-NLS-1$
			}
		}

		/**
		 * Sets the given text and image to the label.
		 * 
		 * @param text
		 *            the new text
		 * @param image
		 *            the new image
		 */
		private void doRefresh(String text, Image image) {
			label.setText(text);
			label.setImage(image);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection,
		 *      boolean)
		 */
		public void setSelection(ISelection selection, boolean reveal) {
			// not supported
		}

		/**
		 * Refreshes the label if currently chosen element is on the list.
		 * 
		 * @param objs
		 *            list of changed object
		 */
		private void refresh(Object[] objs) {
			if (objs == null || getInput() == null) {
				return;
			}
			Object input = getInput();
			for (int i = 0; i < objs.length; i++) {
				if (objs[i].equals(input)) {
					refresh();
					break;
				}
			}
		}

	}
	
	/**
	 * Compares items
	 * It takes into consideration camelCase 
	 */
	private class CamelCaseComparator implements Comparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			
			int leftCategory= getCamelCaseCategory(o1);
	     	int rightCategory= getCamelCaseCategory(o2);
	     	if (leftCategory < rightCategory)
	     		return -1;
	     	if (leftCategory > rightCategory)
	     		return +1;
	     	
	     	return getItemsComparator().compare(o1, o2);
		}
		
		private int getCamelCaseCategory(Object item) {
			if (filter == null)
				return 0;
			if (!filter.isCamelCasePattern())
				return 0;
			return filter.matchesRawNamePattern(item) ? 0 : 1;
		}

	}

	/**
	 * Compares items
	 * It takes into consideration history
	 */
	private class HistoryComparator implements Comparator {

		private CamelCaseComparator camelCaseComparator;
		
		/**
		 * 
		 */
		public HistoryComparator() {
			this.camelCaseComparator = new CamelCaseComparator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if ((isHistoryElement(o1) && isHistoryElement(o2))
					|| (!isHistoryElement(o1) && !isHistoryElement(o2)))
				return this.camelCaseComparator.compare(o1, o2);

			if (isHistoryElement(o1))
				return -2;
			if (isHistoryElement(o2))
				return +2;

			return 0;
		}

	}

}
