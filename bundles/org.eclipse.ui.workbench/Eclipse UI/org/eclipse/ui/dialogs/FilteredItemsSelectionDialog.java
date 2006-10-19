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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
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

	private TableViewer list;

	private TableViewer details;

	private ItemsListLabelProvider itemsListLabelProvider;

	private DetailsContentProvider detailsContentProvider;

	private ILabelProvider detailsLabelProvider;

	private MenuManager menuManager;

	private boolean multi;

	private ToolBar toolBar;

	private ToolItem toolItem;

	private Label progressLabel;

	private Object[] lastSelection;

	private ToggleStatusLineAction toggleStatusLineAction;

	private RemoveHistoryItemAction removeHistoryItemAction;

	private IStatus status;

	private RefreshJob refreshJob = new RefreshJob();

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
		this.history = new SelectionHistory();
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
	 * @return the label provider for items in the list
	 */
	public ILabelProvider getListLabelProvider() {
		return getItemsListLabelProvider().getProvider();
	}

	/**
	 * Adds viewer filter to item list
	 */
	protected void addListFilter(ViewerFilter filter) {
		list.addFilter(filter);
	}

	/**
	 * @param listLabelProvider
	 *            the label provider for items in the list
	 */
	public void setListLabelProvider(ILabelProvider listLabelProvider) {
		getItemsListLabelProvider().setProvider(listLabelProvider);
	}

	/**
	 * @return the label decorator for selected items in the list
	 */
	public ILabelDecorator getListSelectionLabelDecorator() {
		return getItemsListLabelProvider().getSelectionDecorator();
	}

	/**
	 * @param listSelectionLabelDecorator
	 *            the label decorator for selected items in the list
	 */
	public void setListSelectionLabelDecorator(
			ILabelDecorator listSelectionLabelDecorator) {
		getItemsListLabelProvider().setSelectionDecorator(
				listSelectionLabelDecorator);
	}

	private ItemsListLabelProvider getItemsListLabelProvider() {
		if (itemsListLabelProvider == null) {
			itemsListLabelProvider = new ItemsListLabelProvider(
					new LabelProvider(), null);
		}
		return itemsListLabelProvider;
	}

	/**
	 * @return the label provider for the details field
	 */
	public ILabelProvider getDetailsLabelProvider() {
		if (detailsLabelProvider == null) {
			detailsLabelProvider = new LabelProvider();
		}
		return detailsLabelProvider;
	}

	/**
	 * @param detailsLabelProvider
	 *            the label provider for the details field
	 */
	public void setDetailsLabelProvider(ILabelProvider detailsLabelProvider) {
		this.detailsLabelProvider = detailsLabelProvider;

		if (details != null) {
			details.setLabelProvider(detailsLabelProvider);
		}
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
	 * Restores dialog settings
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
		GridData gd = (GridData) details.getTable().getLayoutData();
		gd.exclude = !toggleStatusLine;

		String setting = settings.get(HISTORY_SETTINGS);
		if (setting != null) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(
						setting));
				history.load(memento);
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
		history.save(memento);
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
				.setText(WorkbenchMessages.FilteredItemsSelectionDialog_patternLabel);
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
	 * Fills the menu of this dialog
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
	 * Creates the contents of this dialog, initializes the listener and the
	 * update thread.
	 * 
	 * @param parent
	 *            parent to create the dialog widgets in
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
				| SWT.BORDER | SWT.V_SCROLL);
		contentProvider = new ContentProvider(createFilter(), this.history);
		list.setContentProvider(contentProvider);
		list.setLabelProvider(getItemsListLabelProvider());
		list.setInput(new Object[0]);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		list.getTable().setLayoutData(gd);

		createPopupMenu();

		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setFilter(createFilter());
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
				okPressed();
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

		// details field
		details = new TableViewer(content, SWT.BORDER);
		detailsContentProvider = new DetailsContentProvider();
		details.setContentProvider(detailsContentProvider);
		detailsContentProvider.setElements(new Object[0]);
		details.setLabelProvider(getDetailsLabelProvider());
		details.setInput(new Object[0]);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = listLabel.getSize().y;
		details.getTable().setLayoutData(gd);

		applyDialogFont(content);

		restoreDialog(getDialogSettings());

		return dialogArea;
	}

	private void refreshDetails() {
		StructuredSelection selection = (StructuredSelection) list
				.getSelection();

		if (selection.size() == 1) {
			Object element = selection.getFirstElement();

			if (element instanceof ItemsListSeparator) {
				detailsContentProvider.setElements(new Object[0]);
				details.getTable().setEnabled(false);
			} else {
				Object o = getItemDetails((AbstractListItem) element);
				detailsContentProvider.setElements(new Object[] { o });
				details.getTable().setEnabled(true);
			}
		} else {
			detailsContentProvider.setElements(new Object[0]);
			details.getTable().setEnabled(false);
		}

		details.refresh();
	}

	/**
	 * Handles selection in the list. Updates labels of selected and unselected
	 * items.
	 * 
	 * @param selection
	 */
	private void handleSelected(StructuredSelection selection) {
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

				if (tempStatus.getCode() == IStatus.OK) {
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
	 * Returns the dialog settings
	 */
	protected abstract IDialogSettings getDialogSettings();

	/**
	 * Refreshes the dialog
	 */
	public void refresh() {
		if (list != null && !list.getTable().isDisposed()) {
			list.getTable().setRedraw(false);
			list.refresh();
			list.getTable().setRedraw(true);

			if (list.getTable().getItemCount() > 0) {
				list
						.setSelection(new StructuredSelection(list
								.getElementAt(0)));
			} else {
				list.setSelection(StructuredSelection.EMPTY);
			}

			if (list.getElementAt(0) instanceof ItemsListSeparator) {
				list.remove(list.getElementAt(0));
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
				objectsToReturn.add(getObjectToReturn(item));
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
		if (status != null && status.getCode() == IStatus.OK) {
			super.okPressed();
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
			GridData gd = (GridData) details.getTable().getLayoutData();
			gd.exclude = !isChecked();
			details.getTable().getParent().layout();
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
			} else if (element instanceof AbstractListItem
					&& ((AbstractListItem) element).isHistory()) {
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

	private class DetailsContentProvider implements IStructuredContentProvider {

		private Object[] elements;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return elements;
		}

		/**
		 * Sets table of elements
		 * 
		 * @param elements
		 */
		public void setElements(Object[] elements) {
			this.elements = elements;
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

	/**
	 * Used in ItemsListContentProvider
	 * 
	 * @since 3.3
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
		 * @return the name of the separator
		 */
		public String getName() {
			return name;
		}
	}

	// --------------------S E A R C H E R---------------------------

	private SelectionHistory history;

	private ContentProvider contentProvider;

	private AbstractFilterJob filterJob;

	private List lastCompletedResult;

	private ItemsFilter filter;

	private ItemsFilter lastCompletedFilter = null;

	/**
	 * Gets details for concrete object
	 * 
	 * @param item
	 * @return item description
	 */
	protected abstract Object getItemDetails(AbstractListItem item);

	/**
	 * Validates and return status of the item
	 * 
	 * @param item
	 * @return status of the item
	 */
	protected abstract IStatus validateItem(AbstractListItem item);

	/**
	 * Get object respondent to item, which is return by dialog
	 * 
	 * @param item
	 * @return respondent object
	 */
	protected abstract Object getObjectToReturn(Object item);

	/**
	 * Creates instance of filter. It could be override to change behaviours of
	 * filtering.
	 * 
	 * @return filter to matching elements
	 */
	protected abstract ItemsFilter createFilter();

	/**
	 * Returns comparator to sort items inside model.
	 * 
	 */
	protected abstract Comparator getItemsComparator();

	/**
	 * Fills content provider with AbstractListItem objects.
	 * 
	 * @param contentProvider
	 *            provider to fill
	 * @param progressMonitor
	 * @throws CoreException
	 */
	protected abstract void fillContentProvider(
			AbstractContentProvider contentProvider,
			IProgressMonitor progressMonitor) throws CoreException;

	/**
	 * Creates an instance of an AbstractListItem using given memento
	 * 
	 * @param memento
	 *            memento used for creating new AbstractListItem instance
	 */
	protected abstract AbstractListItem restoreItemFromMemento(IMemento memento);

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

	/**
	 * Remove selected items form history set.
	 * 
	 * @param items
	 */
	private void removeSelectedItems(List items) {
		contentProvider.removeElements(items);
	}

	/**
	 * Schedule filter job. Depend on filter it decide which job will be
	 * schedule. If last filtering done(last complited filter is not null) and
	 * new filter is subfilter of last one it schedule job schearching in cache.
	 * If it is first filtering or new filter isn't subfilter of last one it
	 * schedule full seach.
	 */
	private synchronized void scheduleFilterJob() {
		stop();
		if (lastCompletedFilter != null
				&& lastCompletedFilter.isSubFilter(filter)) {
			filterJob = new CachedResultFilterJob(contentProvider,
					lastCompletedResult);
		} else {
			lastCompletedFilter = null;
			lastCompletedResult = null;
			filterJob = new FilterJob(contentProvider);
		}
		filterJob.schedule();
	}

	private void stop() {
		if (filterJob != null) {
			filterJob.stop();
			filterJob = null;
		}
	}

	/**
	 * Get hitory comparator
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
	 * Sets new filter
	 * 
	 * @param itemFilter
	 */
	protected void setFilter(ItemsFilter itemFilter) {
		stop();
		this.filter = itemFilter;
		this.contentProvider = new ContentProvider(filter, this.history);
		list.setContentProvider(contentProvider);

		if (this.filter != null) {
			if (this.filter.getPattern().length() == 0) {
				filter = null;
			} else {
				scheduleFilterJob();
			}
		}
	}

	/**
	 * Remove element from history
	 * 
	 * @param element
	 *            to remove
	 * @return removed object
	 */
	protected Object removeHistoryElement(Object element) {
		return this.history.remove(element);
	}

	protected void accessedHistory(AbstractListItem listItem) {
		history.accessed(listItem);
	}

	/**
	 * FilteringProgressMonitor to monitoring progress of filtering process. It
	 * updates progress message and refresh dialog after concrete part of work.
	 * 
	 * @since 3.3
	 * 
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

		public void setTaskName(String name) {
			super.setTaskName(name);
			this.name = name;
		}

		public void beginTask(String name, int totalWork) {
			super.beginTask(name, totalWork);
			if (this.name == null)
				this.name = name;
			this.totalWork = totalWork;
		}

		public void worked(int work) {
			super.worked(work);
			internalWorked(work);
		}

		public void done() {
			done = true;
			contentProvider.setProgressMessage(""); //$NON-NLS-1$
			super.done();
		}

		/**
		 * During work sets progress message after concrete part of work.
		 */
		public void internalWorked(double work) {
			worked = worked + work;
			if ((((int) (((worked - work) * 10) / totalWork)) < ((int) ((worked * 10) / totalWork)))
					|| (((int) ((worked * 10) / totalWork)) == 0))
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
	 * 
	 * @since 3.3
	 * 
	 */
	private abstract static class AbstractFilterJob extends Job {

		protected ContentProvider contentProvider;

		protected AbstractFilterJob(ContentProvider contentProvider) {
			super(WorkbenchMessages.FilteredItemsSelectionDialog_jobLabel);
			this.contentProvider = contentProvider;
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
		 * Stop job
		 */
		public void stop() {
			cancel();
		}

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
	 * Filters elements in cache.
	 * 
	 * @since 3.3
	 * 
	 */
	private static class CachedResultFilterJob extends AbstractFilterJob {
		private List lastResult;

		/**
		 * @param contentProvider
		 * @param lastResult
		 */
		public CachedResultFilterJob(ContentProvider contentProvider,
				List lastResult) {
			super(contentProvider);
			this.lastResult = lastResult;
		}

		protected void filterContent(FilteringProgressMonitor monitor) {

			for (Iterator iter = this.lastResult.iterator(); iter.hasNext();) {
				AbstractListItem item = (AbstractListItem) iter.next();
				if (monitor.isCanceled())
					break;
				this.contentProvider.add(item);
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
		 */
		public FilterJob(ContentProvider contentProvider) {
			super(contentProvider);
		}

		protected void filterContent(FilteringProgressMonitor monitor)
				throws CoreException {

			this.contentProvider.addHistoryItems();

			fillContentProvider(this.contentProvider, monitor);

			if (!monitor.isCanceled())
				this.contentProvider.rememberResult();
		}
	}

	/**
	 * History stores a list of key, object pairs. The list is bounded at size
	 * MAX_HISTORY_SIZE. If the list exceeds this size the eldest element is
	 * removed from the list. An element can be added/renewed with a call to
	 * <code>accessed(Object)</code>.
	 * 
	 * The history can be stored to/loaded from an xml file.
	 * 
	 * @since 3.3
	 * 
	 */
	private class SelectionHistory {

		private static final String DEFAULT_ROOT_NODE_NAME = "historyRootNode"; //$NON-NLS-1$

		private static final String DEFAULT_INFO_NODE_NAME = "infoNode"; //$NON-NLS-1$

		private static final int MAX_HISTORY_SIZE = 60;

		private final Map historyMap;

		private final String rootNodeName;

		private final String infoNodeName;

		private SelectionHistory(String rootNodeName, String infoNodeName) {
			historyMap = Collections.synchronizedMap(new LinkedHashMap(80,
					0.75f, true) {
				private static final long serialVersionUID = 1L;

				protected boolean removeEldestEntry(Map.Entry eldest) {
					return size() > MAX_HISTORY_SIZE;
				}
			});
			this.rootNodeName = rootNodeName;
			this.infoNodeName = infoNodeName;
		}

		/**
		 * 
		 */
		public SelectionHistory() {
			this(DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
		}

		/**
		 * @param object
		 */
		public synchronized void accessed(Object object) {
			historyMap.put(object, object);
		}

		/**
		 * @param object
		 * @return true if history caontains object false in other way
		 */
		public synchronized boolean contains(Object object) {
			return historyMap.containsKey(object);
		}

		/**
		 * @param key
		 * @return true if history contains key object false in other way
		 */
		public synchronized boolean containsKey(Object key) {
			return historyMap.containsKey(key);
		}

		/**
		 * @return true if history is empty
		 */
		public synchronized boolean isEmpty() {
			return historyMap.isEmpty();
		}

		/**
		 * Remove element from history
		 * 
		 * @param element
		 *            to remove form the history
		 * @return removed element
		 */
		public synchronized Object remove(Object element) {
			Object removed = historyMap.remove(element);
			return removed;
		}

		/**
		 * @param key
		 *            to remove form the history
		 * @return removed key
		 */
		public synchronized Object removeKey(Object key) {
			Object removed = historyMap.remove(key);
			return removed;
		}

		/**
		 * Load history elements from memento.
		 * 
		 * @param memento
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
					historyMap.put(object, object);
			}
		}

		/**
		 * Save history elements to memento
		 * 
		 * @param memento
		 */
		public void save(IMemento memento) {

			IMemento historyMemento = memento.createChild(rootNodeName);

			Iterator values = getValues().iterator();
			while (values.hasNext()) {
				AbstractListItem item = (AbstractListItem) values.next();
				IMemento elementMemento = historyMemento
						.createChild(infoNodeName);
				storeItemToMemento(item, elementMemento);
			}

		}

		protected Set getKeys() {
			return historyMap.keySet();
		}

		/**
		 * @return collection of history elements
		 */
		public synchronized Collection getValues() {
			return Collections.synchronizedCollection(historyMap.values());
		}

	}

	/**
	 * Filters elements using SearchPattern for comparation name of items with
	 * pattern.
	 * 
	 * @since 3.3
	 * 
	 */
	protected abstract class ItemsFilter {

		private SearchPattern patternMatcher;

		/**
		 * Creates new instance of ItemsFilter
		 * 
		 * @param text
		 *            of the pattern
		 */
		public ItemsFilter(String text) {
			this(new SearchPattern(text));
		}

		/**
		 * Creates new instance of SearchFilter
		 */
		public ItemsFilter() {
			this(pattern.getText());
		}

		/**
		 * Creates new instance of ItemsFilter
		 * 
		 * @param searchPattern
		 */
		public ItemsFilter(SearchPattern searchPattern) {
			patternMatcher = searchPattern;
		}

		/**
		 * Check if <code>ItemsFilter</code> is sub-filter of this. In basic
		 * version it depends on pattern.
		 * 
		 * @param filter
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
		public abstract boolean isItemConsistent(AbstractListItem item);

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
		 */
		public abstract void add(AbstractListItem item);
	}

	/**
	 * Collects filtered elements. Conatains one synchronized sorted set for
	 * collecting filtered elements. All collected elements are sorted using
	 * comparator. Comparator is return by getElementComparator() method. To
	 * filtering elements it use implementation of ItemsFilter. The key function
	 * of filter used in to filtering is matchsElement(AbstarctListItem item)
	 * 
	 * @since 3.3
	 * 
	 */
	private class ContentProvider extends AbstractContentProvider implements
			IStructuredContentProvider {

		private ItemsFilter itemsFilter;

		private SelectionHistory selectionHistory;

		private SortedSet sortedItems;

		private String progressMessage = ""; //$NON-NLS-1$

		/**
		 * Creates new instance of ContentProvider
		 * 
		 * @param itemsFilter
		 * @param selectionHistory
		 */
		public ContentProvider(ItemsFilter itemsFilter,
				SelectionHistory selectionHistory) {
			this.sortedItems = Collections.synchronizedSortedSet(new TreeSet(
					getItemsComparator()));
			this.itemsFilter = itemsFilter;
			this.selectionHistory = selectionHistory;
		}

		/**
		 * Creates new instance of ContentProvider
		 * 
		 * @param selectionHistory
		 */
		public ContentProvider(SelectionHistory selectionHistory) {
			this.sortedItems = Collections.synchronizedSortedSet(new TreeSet(
					getItemsComparator()));
			this.itemsFilter = createFilter();
			this.selectionHistory = selectionHistory;
		}

		/**
		 * Gets filter
		 * 
		 * @return Returns the ItemsFilter.
		 */
		public ItemsFilter getFilter() {
			return itemsFilter;
		}

		/**
		 * Remove all content items and resets progress message
		 */
		public void reset() {
			this.sortedItems = Collections.synchronizedSortedSet(new TreeSet(
					getItemsComparator()));
			this.progressMessage = ""; //$NON-NLS-1$
			this.refresh();
		}

		public void add(AbstractListItem item) {
			if (this.itemsFilter.matchItem(item)
			/* && !this.searcherHistory.containsKey(item) */)
				this.sortedItems.add(item);
		}

		/**
		 * Add all history items to contentProvider
		 */
		public void addHistoryItems() {
			Collection values = this.selectionHistory.getValues();
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				AbstractListItem item = (AbstractListItem) iter.next();
				if (this.itemsFilter.matchItem(item))
					if (this.itemsFilter.isItemConsistent(item))
						this.sortedItems.add(item);
					else
						this.selectionHistory.removeKey(item);
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
		 * 
		 */
		public void rememberResult() {
			if (lastCompletedResult == null) {
				lastCompletedFilter = itemsFilter;
				lastCompletedResult = Collections
						.synchronizedList(new ArrayList(this.sortedItems));
			}
		}

		/**
		 * Remove list of items from history
		 * 
		 * @param items
		 */
		public void removeElements(List items) {
			for (Iterator iter = items.iterator(); iter.hasNext();) {
				AbstractListItem item = (AbstractListItem) iter.next();
				item.unmarkHistory();
				selectionHistory.removeKey(item);
			}
			this.refresh();
		}

		/**
		 * Get filtered items
		 * 
		 * @return filtered items
		 */
		private Object[] getItems() {
			SortedSet sortedElements = new TreeSet(getHistoryComparator());
			sortedElements.addAll(Arrays.asList(sortedItems.toArray()));
			return sortedElements.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {

			List list = new ArrayList();

			boolean hasHistory = false;

			Object[] elements = getItems();

			for (int i = 0; i < elements.length; i++) {
				if (((AbstractListItem) elements[i]).isHistory()) {
					hasHistory = true;
				}

				if (hasHistory && !((AbstractListItem) elements[i]).isHistory()) {
					list
							.add(new ItemsListSeparator(
									WorkbenchMessages.FilteredItemsSelectionDialog_separatorLabel));
					hasHistory = false;
				}

				list.add(elements[i]);
			}

			return list.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// TODO Auto-generated method stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}

	}

	/**
	 * AbstractListItem objects are decorator for objects which are to show in
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
		 * Mark item as duplicated
		 */
		public void markAsDuplicate() {
			this.duplicate = true;
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
	}

}
