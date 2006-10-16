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
import java.util.HashSet;
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
 * pattern used to filter the list of resources.
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

	private static final String SEARCHER_SETTINGS = "Searcher"; //$NON-NLS-1$

	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$

	private Text pattern;

	private TableViewer list;

	private TableViewer details;

	private SearchListContentProvider searchListContentProvider;

	private SearchListLabelProvider searchListLabelProvider;

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
		this.searcherHistory = new SearcherHistory();
		this.searcherModel = new SearcherModel();

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
	 * @return the label provider for items in the search list
	 */
	public ILabelProvider getListLabelProvider() {
		return getSearchListLabelProvider().getProvider();
	}

	protected String getPattern() {
		return pattern.getText();
	}

	protected void addListFilter(ViewerFilter filter) {
		list.addFilter(filter);
	}

	/**
	 * @param listLabelProvider
	 *            the label provider for items in the search list
	 */
	public void setListLabelProvider(ILabelProvider listLabelProvider) {
		getSearchListLabelProvider().setProvider(listLabelProvider);
	}

	/**
	 * @return the label decorator for selected items in the search list
	 */
	public ILabelDecorator getListSelectionLabelDecorator() {
		return getSearchListLabelProvider().getSelectionDecorator();
	}

	/**
	 * @param listSelectionLabelDecorator
	 *            the label decorator for selected items in the search list
	 */
	public void setListSelectionLabelDecorator(
			ILabelDecorator listSelectionLabelDecorator) {
		getSearchListLabelProvider().setSelectionDecorator(
				listSelectionLabelDecorator);
	}

	private SearchListLabelProvider getSearchListLabelProvider() {
		if (searchListLabelProvider == null) {
			searchListLabelProvider = new SearchListLabelProvider(
					new LabelProvider(), null);
		}
		return searchListLabelProvider;
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

		String setting = settings.get(SEARCHER_SETTINGS);
		if (setting != null) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(
						setting));
				restoreState(memento);
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

		XMLMemento memento = XMLMemento.createWriteRoot(SEARCHER_SETTINGS);
		saveState(memento);
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
			settings.put(SEARCHER_SETTINGS, writer.getBuffer().toString());
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
		label.setText(WorkbenchMessages.AbstractSearchDialog_patternLabel);
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
		toolItem.setToolTipText(WorkbenchMessages.AbstractSearchDialog_menu);
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
					if (item instanceof SearchListSeparator
							|| !((AbstractSearchItem) item).isHistory()) {
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
		listLabel.setText(WorkbenchMessages.AbstractSearchDialog_listLabel);

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
		searchListContentProvider = new SearchListContentProvider();
		list.setContentProvider(searchListContentProvider);
		searchListContentProvider.setElements(new Object[0]);
		list.setLabelProvider(getSearchListLabelProvider());
		list.setInput(new Object[0]);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		list.getTable().setLayoutData(gd);

		createPopupMenu();

		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setFilter(createFilter(pattern.getText().trim()));
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

			if (element instanceof SearchListSeparator) {
				detailsContentProvider.setElements(new Object[0]);
				details.getTable().setEnabled(false);
			} else {
				Object o = getItemDetails(element);
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

			AbstractSearchItem item = null;
			IStatus tempStatus = null;

			for (Iterator it = items.iterator(); it.hasNext();) {
				Object o = it.next();

				if (o instanceof SearchListSeparator) {
					continue;
				}

				item = (AbstractSearchItem) o;
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
			searchListContentProvider.setElements(searcherModel.getElements());
			list.refresh();
			list.getTable().setRedraw(true);

			if (list.getTable().getItemCount() > 0) {
				list.setSelection(new StructuredSelection(list
						.getElementAt(list.getTable().getTopIndex())));
			} else {
				list.setSelection(StructuredSelection.EMPTY);
			}

			if (list.getElementAt(0) instanceof SearchListSeparator) {
				list.remove(list.getElementAt(0));
			}
		}

		if (!progressLabel.isDisposed()) {
			progressLabel.setText(searcherModel.getProgressMessage());
		}
	}

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

			if (item instanceof AbstractSearchItem) {
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
			super(WorkbenchMessages.AbstractSearchDialog_toggleStatusAction,
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
					WorkbenchMessages.AbstractSearchDialog_refreshJob);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (FilteredItemsSelectionDialog.this != null) {
				FilteredItemsSelectionDialog.this.refresh();
			}
			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, "", null); //$NON-NLS-1$
		}
	}

	private class SearchListLabelProvider extends LabelProvider implements
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
		public SearchListLabelProvider(ILabelProvider provider,
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
		 *            new label decorator for selected items in the search list
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
		 * @return the label decorator for selected items in the search list
		 */
		public ILabelDecorator getSelectionDecorator() {
			return selectionDecorator;
		}

		/**
		 * @param newProvider
		 *            new label provider for items in the search list, not null
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
		 * @return the label provider for items in the search list
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
			if (element instanceof SearchListSeparator) {
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
			if (element instanceof SearchListSeparator) {
				return getSeparatorLabel(((SearchListSeparator) element)
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
			if (element instanceof SearchListSeparator) {
				return Display.getCurrent().getSystemColor(
						SWT.COLOR_WIDGET_NORMAL_SHADOW);
			} else if (element instanceof AbstractSearchItem
					&& ((AbstractSearchItem) element).isHistory()) {
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

	private class SearchListContentProvider implements
			IStructuredContentProvider {

		private Object[] elements;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {

			List list = new ArrayList();

			boolean hasHistory = false;

			for (int i = 0; i < elements.length; i++) {
				if (((AbstractSearchItem) elements[i]).isHistory()) {
					hasHistory = true;
				}

				if (hasHistory
						&& !((AbstractSearchItem) elements[i]).isHistory()) {
					list
							.add(new SearchListSeparator(
									WorkbenchMessages.AbstractSearchDialog_separatorLabel));
					hasHistory = false;
				}

				list.add(elements[i]);
			}

			return list.toArray();
		}

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
					WorkbenchMessages.AbstractSearchDialog_removeItemsFromHistoryAction);
		}

		public void run() {
			List selectedElements = ((StructuredSelection) list.getSelection())
					.toList();
			removeSelectedItems(selectedElements);
		}
	}

	/**
	 * Used in SearchListContentProvider & SearchListLabelProvider
	 * 
	 * @since 3.3
	 */
	protected class SearchListSeparator {

		private String name;

		/**
		 * Creates a new instance of the class
		 * 
		 * @param name
		 *            the name of the separator
		 */
		public SearchListSeparator(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	// --------------------S E A R C H E R---------------------------

	private SearcherHistory searcherHistory;

	private SearcherModel searcherModel;

	private AbstractSearchJob searchJob;

	private List lastCompletedResult;

	private SearchFilter filter;

	private SearchFilter lastComplitedFilter = null;

	/**
	 * Get descritoions for concrete object
	 * 
	 * @param item
	 * @return item description
	 */
	protected abstract Object getItemDetails(Object item);

	/**
	 * Validate and return status of the object
	 * 
	 * @param item
	 * @return status of the item
	 */
	protected abstract IStatus validateItem(Object item);

	/**
	 * Get object respondent to item, which is return by dialog
	 * 
	 * @param item
	 * @return respondent object
	 */
	protected abstract Object getObjectToReturn(Object item);

	/**
	 * Create instance of filter. It could be override to change behaviours of
	 * filtering.
	 * 
	 * @param text
	 * @return filter to matching elements
	 */
	protected abstract SearchFilter createFilter(String text);

	/**
	 * Returns comparator to sort elements inside model.
	 * 
	 */
	protected abstract Comparator getElementsComparator();

	/**
	 * Search elements mtehod used by SearchJob. It get elements filtered
	 * elements and add it to model. During this operation searchPorgress and
	 * dialog is refreshing.
	 * 
	 * @param contentProvider
	 * @throws CoreException
	 */
	protected abstract void searchElements(ContentProvider contentProvider)
			throws CoreException;

	/**
	 * Store <code>Object</code> in <code>Element</code>
	 * 
	 * @param object
	 *            The object to store
	 * @param element
	 *            The Element to store to
	 */
	protected abstract void setAttributes(Object object, IMemento element);

	/**
	 * Return a new instance of an Object given <code>element</code>
	 * 
	 * @param element
	 *            The element containing required information to create the
	 *            Object
	 */
	protected abstract Object createFromElement(IMemento element);

	/**
	 * Remove selected items form history set.
	 * 
	 * @param items
	 */
	private void removeSelectedItems(List items) {
		searcherModel.removeElements(items);
	}

	/**
	 * Restore a state of history of selected elements from memento
	 * 
	 * @param memento
	 */
	private void restoreState(IMemento memento) {
		searcherHistory.load(memento);
	}

	/**
	 * Save a state of history of selected elements to memento
	 * 
	 * @param memento
	 */
	private void saveState(IMemento memento) {
		searcherHistory.save(memento);
	}

	/**
	 * Schedule search job. Depend on filter it decide which job will be
	 * schedule. If last searching done(last complited filter is not null) and
	 * new filter is subfilter of last one it schedule job schearching in cache.
	 * If it is first searching or new filter isn't subfilter of last one it
	 * schedule full seach.
	 * 
	 */
	private void scheduleSearchJob() {
		stop();
		searcherModel = new SearcherModel();
		if (lastComplitedFilter != null
				&& lastComplitedFilter.isSubFilter(filter)) {
			searchJob = new CachedResultSearchJob(lastCompletedResult,
					searcherModel, filter);
		} else {
			lastComplitedFilter = null;
			lastCompletedResult = null;
			searchJob = new SearchJob(searcherModel, filter, searcherHistory);
		}
		searchJob.schedule();
	}

	protected void stop() {
		if (searchJob != null) {
			searchJob.stop();
			searchJob = null;
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
				AbstractSearchItem searchItem1 = ((AbstractSearchItem) o1);
				AbstractSearchItem searchItem2 = ((AbstractSearchItem) o2);

				if ((searchItem1.isHistory() && searchItem2.isHistory())
						|| (!searchItem1.isHistory() && !searchItem2
								.isHistory()))
					return getElementsComparator().compare(o1, o2);

				if (searchItem1.isHistory())
					return -1;
				if (searchItem2.isHistory())
					return +1;

				return 0;
			}

		};
	}

	private void rememberResult(final List result) {
		if (lastCompletedResult == null) {
			lastComplitedFilter = filter;
			lastCompletedResult = result;
		}
	}

	/**
	 * Get filter
	 * 
	 * @return current filter
	 */
	protected SearchFilter getFilter() {
		return this.filter;
	}

	/**
	 * Set new filter
	 * 
	 * @param searchFilter
	 */
	protected void setFilter(SearchFilter searchFilter) {
		stop();
		this.filter = searchFilter;
		if (this.filter != null)
			if (this.filter.getText().length() == 0) {
				filter = null;
				searcherModel.reset();
			} else {
				scheduleSearchJob();
			}
	}

	/**
	 * @param element
	 * @return Object
	 */
	protected Object removeHistoryElement(Object element) {
		return this.searcherHistory.remove(element);
	}

	/**
	 * @return Collection 
	 */
	protected Collection getHistoryElements() {
		return searcherHistory.getValues();
	}

	/**
	 * @param key
	 * @return true if history contains key false in other way
	 */
	public boolean isHistoryContainsKey(Object key) {
		return searcherHistory.containsKey(key);
	}

	protected void accessedHistory(AbstractSearchItem searchItem) {
		searcherHistory.accessed(searchItem);
	}

	/**
	 * Data model for searcher. It collects matched elements. It is resist to
	 * concurrent access. It conatains one synchronized sorted set for
	 * collecting and sorting data elements using comparator. Comparator is
	 * return by getElementComparator() method implemented in searcher.
	 */
	private class SearcherModel {

		private SortedSet elements;

		private String progressMessage = ""; //$NON-NLS-1$

		/**
		 * Create model using synchronized sorted set.
		 */
		public SearcherModel() {
			this.elements = Collections.synchronizedSortedSet(new TreeSet(
					getElementsComparator()));
		}

		/**
		 * Get searched elements
		 * 
		 * @return searched elements
		 */
		public Object[] getElements() {
			SortedSet sortedElements = new TreeSet(getHistoryComparator());
			sortedElements.addAll(Arrays.asList(elements.toArray()));
			return sortedElements.toArray();
		}

		/**
		 * Get searched elements
		 * 
		 * @return searched elements
		 */
		public List getElementsList() {
			SortedSet sortedElements = new TreeSet(getHistoryComparator());
			sortedElements.addAll(Arrays.asList(elements.toArray()));
			return new ArrayList(sortedElements);
		}

		/**
		 * Get progress message
		 * 
		 * @return progress message
		 */
		public String getProgressMessage() {
			return progressMessage;
		}

		/**
		 * Set elements
		 * 
		 * @param items
		 */
		public void setElements(Object[] items) {
			elements.clear();
			elements.addAll(Arrays.asList(items));
			refresh();
		}

		/**
		 * Set history (selected elements)
		 * 
		 * @param items
		 */
		public void setHistory(Object[] items) {
			elements.addAll(Arrays.asList(items));
			refresh();
		}

		/**
		 * Clear elemnts and history
		 */
		public void reset() {
			this.elements.clear();
			refresh();
		}

		/**
		 * Add Element to elements collection
		 * 
		 * @param item
		 */
		public void addElement(Object item) {
			this.elements.add(item);
		}

		/**
		 * Schedule refresh job on the dialog
		 */
		public void refresh() {
			refreshJob.schedule();
		}

		/**
		 * Set progress message
		 * 
		 * @param progressMessage
		 */
		public void setProgressMessage(String progressMessage) {
			this.progressMessage = progressMessage;
			refresh();
		}

		/**
		 * Remove elements form model
		 * 
		 * @param items
		 */
		public void removeElements(List items) {
			for (Iterator iter = items.iterator(); iter.hasNext();) {
				AbstractSearchItem item = (AbstractSearchItem) iter.next();
				item.unmarkHistory();
				searcherHistory.removeKey(item);
				if (lastCompletedResult != null)
					lastCompletedResult.remove(item);
			}
			refresh();
		}

	}

	/**
	 * SearcherProgressMonitor to monitoring progress of searching process. It
	 * updates progress message and refresh dialog after concrete part of work.
	 * 
	 * @since 3.3
	 * 
	 */
	private static class SearcherProgressMonitor extends ProgressMonitorWrapper {

		private SearcherModel model;

		private String name;

		private int totalWork;

		private double worked;

		private boolean done;

		/**
		 * @param monitor
		 * @param model
		 */
		public SearcherProgressMonitor(IProgressMonitor monitor,
				SearcherModel model) {
			super(monitor);
			this.model = model;
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
			model.setProgressMessage(""); //$NON-NLS-1$
			super.done();
		}

		/**
		 * 
		 */
		public void internalWorked(double work) {
			worked = worked + work;
			if ((((int) (((worked - work) * 10) / totalWork)) < ((int) ((worked * 10) / totalWork)))
					|| (((int) ((worked * 10) / totalWork)) == 0))
				model.setProgressMessage(getMessage());
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
	 * Abstract job for searching elements. It is a pattern job for searching
	 * elements in cache and full searching.
	 * 
	 * @since 3.3
	 * 
	 */
	private abstract static class AbstractSearchJob extends Job {

		protected SearcherModel model;

		protected SearchFilter searchFilter;

		protected AbstractSearchJob(SearcherModel model,
				SearchFilter searchFilter) {
			super(WorkbenchMessages.AbstractSearcher_job_label);
			this.model = model;
			this.searchFilter = searchFilter;
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected final IStatus run(IProgressMonitor parent) {
			SearcherProgressMonitor monitor = new SearcherProgressMonitor(
					parent, model);
			return doRun(monitor);
		}

		/**
		 * Stop job
		 */
		public void stop() {
			cancel();
		}

		protected IStatus doRun(SearcherProgressMonitor monitor) {
			try {
				internalRun(monitor);
			} catch (CoreException e) {
				this.stop();
				WorkbenchPlugin.log(e);
				return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
						IStatus.ERROR,
						WorkbenchMessages.AbstractSearcher_job_error, e);
			} catch (OperationCanceledException e) {
				return canceled(e);
			}
			return ok();
		}

		/**
		 * Search elements using filter
		 * 
		 * @param filteredHistory
		 *            set of history elements
		 * @param monitor
		 *            for monitoring progress
		 * @throws CoreException
		 */
		protected abstract void searchResults(Set filteredHistory,
				SearcherProgressMonitor monitor) throws CoreException;

		/**
		 * Get filtered history
		 * 
		 * @return lit of filtered history elements
		 */
		protected abstract List getFilteredHistory();

		/**
		 * Main method for jobs.
		 * 
		 * @param monitor
		 * @throws CoreException
		 */
		private void internalRun(SearcherProgressMonitor monitor)
				throws CoreException {

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			model.reset();

			List elements = getFilteredHistory();

			model.setHistory(elements.toArray());

			searchResults(new HashSet(elements), monitor);

			model.refresh();

			if (monitor.isCanceled())
				throw new OperationCanceledException();

		}

		private IStatus canceled(Exception e) {
			return new Status(IStatus.CANCEL, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.CANCEL,
					WorkbenchMessages.AbstractSearcher_job_cancel, e);
		}

		private IStatus ok() {
			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, "", null); //$NON-NLS-1$
		}
	}

	/**
	 * Search matches element stored in cache.
	 * 
	 * @since 3.3
	 * 
	 */
	private static class CachedResultSearchJob extends AbstractSearchJob {
		private List lastResult;

		/**
		 * @param ticket
		 *            of job
		 * @param lastResult
		 *            is list of last complited searched results (cache)
		 * @param model
		 *            to collect cearched elements
		 * @param searchFilter
		 *            for filtering elements
		 */
		public CachedResultSearchJob(List lastResult, SearcherModel model,
				SearchFilter searchFilter) {
			super(model, searchFilter);
			this.lastResult = lastResult;
		}

		protected void searchResults(Set filteredHistory,
				SearcherProgressMonitor monitor) {

			for (Iterator iter = lastResult.iterator(); iter.hasNext();) {
				AbstractSearchItem searchitem = (AbstractSearchItem) iter
						.next();
				if (monitor.isCanceled())
					break;
				if (filteredHistory.contains(searchitem))
					continue;
				if (searchFilter.matchesElement(searchitem))
					model.addElement(searchitem);
			}

		}

		protected List getFilteredHistory() {
			List result = new ArrayList();

			return result;
		}
	}

	/**
	 * Search matched elements in indicated set and in history. During searching
	 * it refresh progres Monitor.
	 * 
	 * @since 3.3
	 * 
	 */
	private class SearchJob extends AbstractSearchJob {

		SearcherHistory searcherHistory;

		/**
		 * @param ticket
		 *            of job
		 * @param model
		 *            to collect cearched elements
		 * @param searcher
		 * @param searchFilter
		 *            for filtering elements
		 */
		public SearchJob(SearcherModel model, SearchFilter searchFilter,
				SearcherHistory searcherHistory) {
			super(model, searchFilter);
			this.searcherHistory = searcherHistory;
		}

		public void stop() {
			super.stop();
		}

		protected List getFilteredHistory() {
			Collection values = searcherHistory.getValues();
			List result = new ArrayList();
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				AbstractSearchItem searchItem = (AbstractSearchItem) iter
						.next();

				if (searchFilter == getFilter()
						&& searchFilter.matchesElement(searchItem))
					if (searchFilter.isConsistentElement(searchItem))
						result.add(searchItem);
					else
						searcherHistory.removeKey(searchItem);
			}
			return result;
		}

		protected void searchResults(Set filteredHistory,
				SearcherProgressMonitor monitor) throws CoreException {

			ContentProvider contentProvider = new ContentProvider(this.model,
					this.searchFilter, this.searcherHistory, monitor);

			searchElements(contentProvider);

			if (!monitor.isCanceled())
				rememberResult(this.model.getElementsList());
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
	private class SearcherHistory {

		private static final String DEFAULT_ROOT_NODE_NAME = "historyRootNode"; //$NON-NLS-1$

		private static final String DEFAULT_INFO_NODE_NAME = "infoNode"; //$NON-NLS-1$

		private static final int MAX_HISTORY_SIZE = 60;

		private final Map history;

		private final String rootNodeName;

		private final String infoNodeName;

		private SearcherHistory(String rootNodeName, String infoNodeName) {
			history = Collections.synchronizedMap(new LinkedHashMap(80, 0.75f,
					true) {
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
		public SearcherHistory() {
			this(DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
		}

		/**
		 * @param object
		 */
		public synchronized void accessed(Object object) {
			history.put(object, object);
		}

		/**
		 * @param object
		 * @return true if history caontains object false in other way
		 */
		public synchronized boolean contains(Object object) {
			return history.containsKey(object);
		}

		/**
		 * @param key
		 * @return true if history contains key object false in other way
		 */
		public synchronized boolean containsKey(Object key) {
			return history.containsKey(key);
		}

		/**
		 * @return true if history is empty
		 */
		public synchronized boolean isEmpty() {
			return history.isEmpty();
		}

		/**
		 * @param object
		 *            to remove form the history
		 * @return removed object
		 */
		public synchronized Object remove(Object object) {
			Object removed = history.remove(object);
			return removed;
		}

		/**
		 * @param key
		 *            to remove form the history
		 * @return removed key
		 */
		public synchronized Object removeKey(Object key) {
			Object removed = history.remove(key);
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
				Object object = createFromElement(mementoElement);
				if (object != null)
					history.put(object, object);
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
				Object object = values.next();
				IMemento elementMemento = historyMemento
						.createChild(infoNodeName);
				setAttributes(object, elementMemento);
			}

		}

		protected Set getKeys() {
			return history.keySet();
		}

		/**
		 * @return collection of history elements
		 */
		public synchronized Collection getValues() {
			return Collections.synchronizedCollection(history.values());
		}

	}

	/**
	 * Filters elements using searchPatter for comparation name of resources
	 * with pattern.
	 * 
	 * @since 3.3
	 * 
	 */
	protected abstract static class SearchFilter {

		private String text;

		private SearchPattern nameMatcher;

		/**
		 * @param text
		 *            of the pattern
		 */
		public SearchFilter(String text) {
			this.text = text;
			nameMatcher = new SearchPattern(text);
		}

		/**
		 * @param searchPattern
		 */
		public SearchFilter(SearchPattern searchPattern) {
			this.text = searchPattern.getPattern();
			nameMatcher = searchPattern;
		}

		/**
		 * @return text of the pattern
		 */
		public String getText() {
			return text;
		}

		/**
		 * Check if <code>SearchFilter filter</code> is sub-filter of this. In
		 * basic version it depends on pattern. It will be override to change
		 * behaviour of Searcher.
		 * 
		 * @param filter
		 * @return true if filter is sub-filter of this false if filter isn't
		 *         sub-filter
		 */
		public boolean isSubFilter(SearchFilter filter) {
			if (filter != null && filter.getNamePattern().startsWith(this.text)) {
				return true;
			}
			return false;
		}

		/**
		 * @return true if text is camelCase pattern false if text don't
		 *         implement camelCase cases
		 */
		public boolean isCamelCasePattern() {
			return nameMatcher.getMatchKind() == SearchPattern.R_CAMELCASE_MATCH;
		}

		/**
		 * Set new pattern
		 * 
		 * @param namePattern
		 */
		public void setNamePattern(String namePattern) {
			text = namePattern;
			nameMatcher = new SearchPattern(namePattern);
		}

		/**
		 * @return pattern for this filter
		 */
		public String getNamePattern() {
			return nameMatcher.getPattern();
		}

		/**
		 * @return search flag
		 */
		public int getSearchFlags() {
			return nameMatcher.getMatchKind();
		}

		protected boolean matchesName(String name) {
			return nameMatcher.matches(name);
		}

		/**
		 * @param element
		 * @return true if element matches wit false
		 */
		public abstract boolean matchesElement(Object element);

		/**
		 * Check consistency of elements. Element is inconsitent if is changed
		 * element or remove
		 * 
		 * @param searchitem
		 * @return true if element is consistent false if element is inconsitent
		 */
		public abstract boolean isConsistentElement(Object searchitem);

	}

	/**
	 * Filters elements using searchPatter for comparation name of resources
	 * with pattern.
	 * 
	 * @since 3.3
	 * 
	 */
	protected static class ContentProvider {

		private SearcherModel model;

		private SearchFilter filter;

		private SearcherHistory history;

		private IProgressMonitor monitor;

		/**
		 * @param model
		 * @param filter
		 * @param history
		 * @param monitor
		 */
		public ContentProvider(SearcherModel model, SearchFilter filter,
				SearcherHistory history, IProgressMonitor monitor) {
			this.model = model;
			this.filter = filter;
			this.history = history;
			this.monitor = monitor;
		}

		/**
		 * @return Returns the monitor.
		 */
		public IProgressMonitor getProgressMonitor() {
			return monitor;
		}

		public void addSearchItem(AbstractSearchItem item) {
			if (this.filter.matchesElement(item)
					&& !this.history.containsKey(item))
				this.model.addElement(item);
		}

		public void beginTask(String name, int totalWork) {
			this.monitor.beginTask(name, totalWork);
		}

		public void endTask() {
			this.monitor.done();
		}

		public void worked(int work) {
			this.monitor.worked(work);
		}

		public boolean isDeactivated() {
			return this.monitor.isCanceled();
		}

	}

	/**
	 * AbstractSearchItem represents one searched item. It's opaque serched
	 * element and mark it as history or as duplicate. History flag helps
	 * comparator during sort. Elements which are mark as history are at first
	 * places od the list. The duplicate flag help dialog recognize which
	 * elements are a duplicated .
	 * 
	 * @since 3.3
	 */
	protected abstract class AbstractSearchItem {

		private boolean duplicate = false;

		private boolean isHistory = false;

		/**
		 * Check if it is duplicate
		 * 
		 * @return true if it's duplicate, else false
		 */
		public boolean isDuplicate() {
			return this.duplicate;
		}

		/**
		 * 
		 * Mark it as a duplicate
		 */
		public void markAsDuplicate() {
			this.duplicate = true;
		}

		/**
		 * Check if it is part of the selected history
		 * 
		 * @return true if it's duplicate, else false
		 */
		public boolean isHistory() {
			return this.isHistory;
		}

		/**
		 * 
		 * Mark it as a history
		 */
		public void markAsHistory() {
			this.isHistory = true;
		}

		/**
		 * 
		 * Unmark item as a history
		 */
		public void unmarkHistory() {
			this.isHistory = false;
		}

	}

}
