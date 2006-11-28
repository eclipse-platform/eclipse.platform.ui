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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
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

	private Text pattern;

	private ItemsTableViewer list;

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

	private RefreshJob refreshJob = new RefreshJob();

	private Object[] lastSelection;

	private ContentProvider contentProvider;

	private AbstractFilterJob filterJob;

	private ItemsFilter filter;

	private List lastCompletedResult;

	private ItemsFilter lastCompletedFilter;

	private String initialPatternText;

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
		list.addFilter(filter);
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
				WorkbenchPlugin.log(e);
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
			WorkbenchPlugin.log(e);
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
							|| !((AbstractListItem) item).isHistory()) {
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

		list = new ItemsTableViewer(content, (multi ? SWT.MULTI : SWT.SINGLE)
				| SWT.BORDER | SWT.V_SCROLL);
		list.setContentProvider(contentProvider);
		list.setLabelProvider(getItemsListLabelProvider());
		list.setInput(new Object[0]);
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
					if (pattern.getCaretPosition() == pattern.getCharCount()
							&& list.getTable().getItemCount() > 0) {
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
					}
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
			pattern.setSelection(0, initialPatternText.length());
		}

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
				IStatus.OK, "", null); //$NON-NLS-1$

		if (selection.size() == 0) {
			status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.ERROR, "", null); //$NON-NLS-1$

			if (lastSelection != null
					&& getListSelectionLabelDecorator() != null) {
				list.update(lastSelection, null);
			}

			lastSelection = null;

		} else {
			status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.ERROR, "", null); //$NON-NLS-1$

			List items = selection.toList();

			AbstractListItem item = null;
			IStatus tempStatus = null;

			for (Iterator it = items.iterator(); it.hasNext();) {
				Object o = it.next();

				if (o instanceof ItemsListSeparator) {
					continue;
				}

				item = (AbstractListItem) o;
				tempStatus = validateItem(item);

				if (tempStatus.isOK()) {
					status = new Status(IStatus.OK,
							WorkbenchPlugin.PI_WORKBENCH, IStatus.OK, "", null); //$NON-NLS-1$
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
	 * Refreshes the dialog
	 */
	public void refresh() {
		if (list != null && !list.getTable().isDisposed()) {
			list.getTable().setRedraw(false);
			list.refresh(false);
			list.getTable().setRedraw(true);

			if (list.getTable().getItemCount() > 0) {
				list
						.setSelection(new StructuredSelection(list
								.getElementAt(0)));
			} else {
				list.setSelection(StructuredSelection.EMPTY);
			}
		}

		if (!progressLabel.isDisposed()) {
			progressLabel.setText(contentProvider.getProgressMessage());
		}
	}

	/**
	 * Schedule refresh job
	 */
	public void scheduleRefresh() {
		refreshJob.cancel();
		refreshJob.schedule();
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

			if (item instanceof AbstractListItem) {
				accessedHistoryItem((AbstractListItem) item);
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
	 * selection input on the dialog.
	 * 
	 * @param text
	 *            initial pattern for the filter
	 */
	public void setInitialPattern(String text) {
		this.initialPatternText = text;
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
	protected abstract IStatus validateItem(AbstractListItem item);

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
		stopCurrentFilterJob();
		
		this.filter = createFilter();
		this.contentProvider.reset();

		if (this.filter != null) {
			if (this.filter.getPattern().length() == 0) {
				filter = null;
				this.contentProvider.refresh();
			} else {
				scheduleFilterJob();
			}
		}
	}

	/**
	 * Returns comparator to sort items inside content provider.
	 * 
	 * @return comparator to sort items content provider
	 */
	protected abstract Comparator getItemsComparator();

	/**
	 * Fills content provider with AbstractListItem objects.
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
			AbstractListItem item = (AbstractListItem) iter.next();
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
	protected AbstractListItem removeHistoryItem(AbstractListItem item) {
		return contentProvider.removeHistoryElement(item);
	}

	/**
	 * Adds item to history
	 * 
	 * @param listItem
	 *            the item to be added
	 */
	protected void accessedHistoryItem(AbstractListItem listItem) {
		contentProvider.addHistoryElement(listItem);
	}

	/**
	 * Gets history comparator
	 * 
	 * @return decorated comparator
	 */
	private Comparator getHistoryComparator() {
		return new Comparator() {

			public int compare(Object o1, Object o2) {
				AbstractListItem item1 = ((AbstractListItem) o1);
				AbstractListItem item2 = ((AbstractListItem) o2);

				if ((item1.isHistory() && item2.isHistory())
						|| (!item1.isHistory() && !item2.isHistory()))
					return getItemsComparator().compare(o1, o2);

				if (item1.isHistory())
					return -1;
				if (item2.isHistory())
					return +1;

				return 0;
			}
		};
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
		if (lastCompletedFilter != null
				&& lastCompletedFilter.isSubFilter(filter)) {
			filterJob = new CachedResultFilterJob(contentProvider, filter,
					lastCompletedResult);
		} else {
			lastCompletedFilter = null;
			lastCompletedResult = null;
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
			filterJob = null;
		}
	}

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

	private class RefreshJob extends UIJob {

		/**
		 * Creates a new instance of the class
		 */
		public RefreshJob() {
			super(FilteredItemsSelectionDialog.this.getParentShell()
					.getDisplay(),
					WorkbenchMessages.FilteredItemsSelectionDialog_refreshJob);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (FilteredItemsSelectionDialog.this != null) {
				FilteredItemsSelectionDialog.this.refresh();
			}
			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, "", null); //$NON-NLS-1$
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

			if (selectionDecorator != null
					&& ((StructuredSelection) list.getSelection()).toList()
							.contains(element)) {
				str = selectionDecorator.decorateText(str, element);
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
	 * FilteringProgressMonitor to monitoring progress of filtering process. It
	 * updates progress message and refresh dialog after concrete part of work.
	 * State of this monitor illustrate state of filtering process.
	 */
	private static class FilteringProgressMonitor extends
			ProgressMonitorWrapper {

		private ContentProvider contentProvider;
		
		private String name;

		private int totalWork;

		private double worked;

		private boolean done;

		/**
		 * Creates instance of FilteringProgressMonitor
		 * 
		 * @param monitor
		 * @param contentProvider
		 */
		public FilteringProgressMonitor(IProgressMonitor monitor,
				ContentProvider contentProvider) {
			super(monitor);
			this.contentProvider = contentProvider;
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
			contentProvider.setProgressMessage(""); //$NON-NLS-1$
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
					contentProvider.setProgressMessage(getMessage());
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
			FilteringProgressMonitor monitor = new FilteringProgressMonitor(
					parent, contentProvider);
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
		protected IStatus doRun(FilteringProgressMonitor monitor) {
			try {
				internalRun(monitor);
			} catch (CoreException e) {
				this.stop();
				WorkbenchPlugin.log(e);
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
		protected abstract void filterContent(FilteringProgressMonitor monitor)
				throws CoreException;

		/**
		 * Main method for jobs.
		 * 
		 * @param monitor
		 * @throws CoreException
		 */
		private void internalRun(FilteringProgressMonitor monitor)
				throws CoreException {

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			filterContent(monitor);

			contentProvider.refresh();

			if (monitor.isCanceled())
				throw new OperationCanceledException();

		}

		private IStatus canceled(Exception e) {
			return new Status(IStatus.CANCEL, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.CANCEL,
					WorkbenchMessages.FilteredItemsSelectionDialog_jobCancel, e);
		}

		private IStatus ok() {
			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, "", null); //$NON-NLS-1$
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
		protected void filterContent(FilteringProgressMonitor monitor) {
			for (Iterator iter = this.lastResult.iterator(); iter.hasNext();) {
				AbstractListItem item = (AbstractListItem) iter.next();
				if (monitor.isCanceled())
					break;
				this.contentProvider.add(item, itemsFilter);
			}
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
		protected void filterContent(FilteringProgressMonitor monitor)
				throws CoreException {

			this.contentProvider.addHistoryItems(this.itemsFilter);

			fillContentProvider(this.contentProvider, this.itemsFilter, monitor);

			if (monitor != null && !monitor.isCanceled()) {
				monitor.done();
				this.contentProvider.rememberResult(this.itemsFilter);
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
					// TODO Auto-generated method stub
					if (this.size() > MAX_HISTORY_SIZE)
						this.removeFirst();
					return super.add(arg0);
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
				if (object != null)
					historyList.add(object);
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
				AbstractListItem item = (AbstractListItem) items[i];
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
		 * Creates an instance of an AbstractListItem using given memento
		 * 
		 * @param memento
		 *            memento used for creating new AbstractListItem instance
		 * 
		 * @return the restored AbstractListItem
		 */
		protected abstract AbstractListItem restoreItemFromMemento(
				IMemento memento);

		/**
		 * Store <code>AbstractListItem</code> object in <code>IMemento</code>
		 * 
		 * @param item
		 *            the item to store
		 * @param memento
		 *            the memento to store to
		 */
		protected abstract void storeItemToMemento(AbstractListItem item,
				IMemento memento);

	}

	/**
	 * Filters elements using SearchPattern for comparation name of items with
	 * pattern.
	 */
	protected abstract class ItemsFilter {

		private SearchPattern patternMatcher;
		
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
			patternMatcher.setPattern((pattern != null) ? pattern.getText()
					: ""); //$NON-NLS-1$
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
			if (filter != null
					&& filter.getPattern().startsWith(this.getPattern())) {
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

		/**
		 * Matches items against filter conditions
		 * 
		 * @param item
		 * @return true if item matches against filter conditions false
		 *         otherwise
		 */
		public abstract boolean matchItem(AbstractListItem item);

		/**
		 * Checks consistency of items. Item is inconsitent if was changed or
		 * removed
		 * 
		 * @param item
		 * @return true if item is consistent false if item is inconsitent
		 */
		public abstract boolean isConsistentItem(AbstractListItem item);

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
		public abstract void add(AbstractListItem item, ItemsFilter itemsFilter);
	}

	/**
	 * Collects filtered elements. Conatains one synchronized sorted set for
	 * collecting filtered elements. All collected elements are sorted using
	 * comparator. Comparator is return by getElementComparator() method. To
	 * filtering elements it use implementation of ItemsFilter. The key function
	 * of filter used in to filtering is matchsElement(AbstarctListItem item)
	 */
	private class ContentProvider extends AbstractContentProvider implements
			IStructuredContentProvider {

		private SelectionHistory selectionHistory;

		private SortedSet sortedItems;

		private String progressMessage = ""; //$NON-NLS-1$

		/**
		 * Creates new instance of ContentProvider
		 * 
		 * @param selectionHistory
		 */
		public ContentProvider(SelectionHistory selectionHistory) {
			this.sortedItems = Collections.synchronizedSortedSet(new TreeSet(
					getHistoryComparator()));
			this.selectionHistory = selectionHistory;
		}

		/**
		 * Creates new instance of ContentProvider
		 * 
		 * @param selectionHistory
		 */
		public ContentProvider() {
			this.sortedItems = Collections.synchronizedSortedSet(new TreeSet(
					getHistoryComparator()));
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
			this.sortedItems.clear();
			this.progressMessage = ""; //$NON-NLS-1$
		}

		/**
		 * Adds filtered item
		 * 
		 * @param item
		 * @param itemsFilter
		 */
		public void add(AbstractListItem item, ItemsFilter itemsFilter) {
			if (itemsFilter == filter)
				if (itemsFilter != null) {
					if (itemsFilter.matchItem(item)) {
						if (!item.isHistory()) {
							if (!(this.selectionHistory != null && this.selectionHistory
								.contains(item)))
								this.sortedItems.add(item);
						} else {
							this.sortedItems.add(item);
						}
					}
				} else if (!item.isHistory()) {
					if (!(this.selectionHistory != null && this.selectionHistory
							.contains(item))) {
						this.sortedItems.add(item);
					}
				} else {
					this.sortedItems.add(item);
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
					AbstractListItem item = (AbstractListItem) items[i];
					if (itemsFilter == filter) {
						if (itemsFilter != null) {
							if (itemsFilter.matchItem(item)) {
								if (itemsFilter.isConsistentItem(item)) {
									this.sortedItems.add(item);
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
		 */
		public void refresh() {
			scheduleRefresh();
		}

		/**
		 * Sets progress message
		 * 
		 * @param progressMessage
		 */
		public void setProgressMessage(String progressMessage) {
			this.progressMessage = progressMessage;
			this.refresh();
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
		public AbstractListItem removeHistoryElement(AbstractListItem item) {
			this.sortedItems.remove(item);
			item.unmarkHistory();
			this.sortedItems.add(item);
			if (this.selectionHistory != null)
				this.selectionHistory.remove(item);
			this.refresh();
			return item;
		}

		/**
		 * Adds item to history and refresh view
		 * 
		 * @param item
		 *            to add
		 */
		public void addHistoryElement(AbstractListItem item) {
			this.sortedItems.remove(item);
			item.markAsHistory();
			if (filter != null && filter.matchItem(item))
				this.sortedItems.add(item);
			if (this.selectionHistory != null)
				this.selectionHistory.accessed(item);
			this.refresh();
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
		private Object[] getItems() {
			return sortedItems.toArray();
		}

		/**
		 * Remember result of filtering
		 * 
		 * @param itemsFilter
		 */
		public void rememberResult(ItemsFilter itemsFilter) {
			if (lastCompletedResult == null) {
				lastCompletedFilter = itemsFilter;
				lastCompletedResult = Collections
						.synchronizedList(new ArrayList(this.sortedItems));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getItems();
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

			if (!oldInput.equals(input)) {
				refresh();
			}
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
	 * AbstractListItem objects are decorators for objects which are to show in
	 * the dialog list. Each such a decorator has history flag and duplicate
	 * flag. History flag helps to sort decorated objects (history marked are at
	 * the beggining of the list) and duplicate is used by label providers.
	 */
	protected static abstract class AbstractListItem {

		private boolean duplicate = false;

		private boolean isHistory = false;

		/**
		 * Checks item is duplicate
		 * 
		 * @return true if item is duplicated false in other way
		 */
		public boolean isDuplicate() {
			return this.duplicate;
		}

		/**
		 * Sets item as duplicate
		 * 
		 * @param duplicate
		 */
		public void setDuplicate(boolean duplicate) {
			this.duplicate = duplicate;
		}

		/**
		 * Checks if item is part of the selected history
		 * 
		 * @return true if it's duplicate, else false
		 */
		public boolean isHistory() {
			return this.isHistory;
		}

		/**
		 * Marks item as a part of history
		 */
		public void markAsHistory() {
			this.isHistory = true;
		}

		/**
		 * Unmarks history item
		 */
		public void unmarkHistory() {
			this.isHistory = false;
		}

		/**
		 * Gets name of item. It's used as element name on dialog view.
		 * Duplicate marking depend on this name.
		 * 
		 * @return name of item
		 */
		public abstract String getName();

		/**
		 * @return embedded object
		 */
		public abstract Object getObject();

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj instanceof AbstractListItem) {
				AbstractListItem item = (AbstractListItem) obj;
				return getObject().equals(item.getObject());
			}
			return super.equals(obj);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return getObject().hashCode();
		}

	}

	/**
	 * Additional functionality comparing to the super class. It puts separator
	 * below history items and marks each items as duplicate if its name repeats
	 * more than once on the filtered list.
	 */
	private class ItemsTableViewer extends TableViewer {

		/**
		 * Creates new instance of ItemsTableViewer
		 * 
		 * @param parent
		 */
		public ItemsTableViewer(Composite parent) {
			super(parent);
		}

		/**
		 * Creates new instance of ItemsTableViewer
		 * 
		 * @param parent
		 * @param style
		 */
		public ItemsTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		/**
		 * Creates new instance of ItemsTableViewer
		 * 
		 * @param table
		 */
		public ItemsTableViewer(Table table) {
			super(table);
		}

		/**
		 * Additional functionality comparing to the overriden method. It puts
		 * separator below history items and marks each items as duplicate if
		 * its name repeats more than once on the filtered list.
		 * 
		 * @see org.eclipse.jface.viewers.StructuredViewer#getFilteredChildren(java.lang.Object)
		 */
		protected Object[] getFilteredChildren(Object parent) {
			ArrayList preaparedElements = new ArrayList();
			boolean hasHistory = false;
			HashMap helperMap = new HashMap();

			Object[] filteredElements = super.getFilteredChildren(parent);

			for (int i = 0; i < filteredElements.length; i++) {
				AbstractListItem item = (AbstractListItem) filteredElements[i];

				AbstractListItem previousItem = (AbstractListItem) helperMap
						.put(item.getName(), item);
				if (previousItem != null) {
					previousItem.setDuplicate(true);
					item.setDuplicate(true);
				} else {
					item.setDuplicate(false);
				}

				if (item.isHistory()) {
					hasHistory = true;
				}

				if (hasHistory && !item.isHistory()) {
					preaparedElements
							.add(new ItemsListSeparator(
									WorkbenchMessages.FilteredItemsSelectionDialog_separatorLabel));
					hasHistory = false;
				}

				preaparedElements.add(item);
			}

			helperMap.clear();

			return preaparedElements.toArray();
		}
	}

}
