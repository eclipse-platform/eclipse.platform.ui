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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IColorProvider;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.TableItem;
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

/**
 * Shows a list of items to the user with a text entry field for a string
 * pattern used to filter the list of resources.
 * 
 * @since 3.3
 */
public abstract class AbstractSearchDialog extends SelectionStatusDialog {

	private static final String DIALOG_BOUNDS_SETTINGS = "DialogBoundsSettings"; //$NON-NLS-1$

	private static final String SHOW_STATUS_LINE = "ShowStatusLine"; //$NON-NLS-1$

	private static final String SEARCHER_SETTINGS = "Searcher"; //$NON-NLS-1$

	private Text pattern;

	private TableViewer list;

	private TableViewer details;

	private SearchListContentProvider listContentProvider;

	private DetailsContentProvider detailsContentProvider;

	private MenuManager menuManager;

	private AbstractSearcher searcher;

	private boolean multi;

	private ToolBar toolBar;

	private ToolItem toolItem;

	private Label progressLabel;

	private Object[] lastSelection;

	private ToggleStatusLineAction toggleStatusLineAction;

	int currentIndex;

	/**
	 * Creates a new instance of the class
	 * 
	 * @param shell
	 *            shell to parent the dialog on
	 * @param multi
	 *            multiselection flag
	 */
	public AbstractSearchDialog(Shell shell, boolean multi) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.multi = multi;
	}

	/**
	 * Creates a new instance of the class
	 * 
	 * @param shell
	 *            shell to parent the dialog on
	 */
	public AbstractSearchDialog(Shell shell) {
		this(shell, false);
	}

	protected abstract ILabelProvider getListLabelProvider();

	protected abstract ILabelDecorator getListSelectionLabelDecorator();

	protected abstract ILabelProvider getDetailsLabelProvider();

	/**
	 * Sets searcher for the dialog
	 * 
	 * @param searcher
	 *            the searcher to set
	 */
	public void setSearcher(AbstractSearcher searcher) {
		this.searcher = searcher;
		searcher.setRefreshJob(new RefreshJob());
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
		boolean toggleStatusLine = settings.getBoolean(SHOW_STATUS_LINE);
		toggleStatusLineAction.setChecked(toggleStatusLine);

		GridData gd = (GridData) details.getControl().getLayoutData();
		gd.exclude = !toggleStatusLine;

		String setting = settings.get(SEARCHER_SETTINGS);
		if (setting != null) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(
						setting));
				searcher.restoreState(memento);
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
		searcher.saveState(memento);
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

		progressLabel = new Label(content, SWT.RIGHT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		progressLabel.setLayoutData(gd);

		list = new TableViewer(content, (multi ? SWT.MULTI : SWT.SINGLE)
				| SWT.BORDER | SWT.V_SCROLL);
		listContentProvider = new SearchListContentProvider();
		list.setContentProvider(listContentProvider);
		listContentProvider.setElements(new Object[0]);
		list.setLabelProvider(new ListLabelProvider(getListLabelProvider(),
				getListSelectionLabelDecorator()));
		list.setInput(new Object[0]);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		list.getControl().setLayoutData(gd);

		list.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event
						.getSelection();
				handleSelected(selection);
			}
		});

		list.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				TableItem tableItem = list.getTable().getItem(
						new Point(e.x, e.y));

				if (tableItem != null) {
					currentIndex = list.getTable().indexOf(tableItem);
					refreshDetails();
				}
			}
		});

		pattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (pattern.getCaretPosition() == pattern.getCharCount()
							&& list.getTable().getItemCount() > 0) {

						// if (lastSelection != null) {
						// list.setSelection(new StructuredSelection(
						// lastSelection));
						// currentIndex = list.getTable().getSelectionIndex();
						// } else {
						// if (list.getTable().getItemCount() > 0) {
						// list.setSelection(new StructuredSelection(list
						// .getElementAt(list.getTable()
						// .getTopIndex())));
						//
						// if (e.stateMask == SWT.CONTROL) {
						// list.setSelection(null);
						// }
						//
						// currentIndex = list.getTable().getTopIndex();
						// } else {
						// currentIndex = -1;
						// }
						// }

						list.getControl().setFocus();
						refreshDetails();
					}
				}
			}
		});

		list.getControl().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.PAGE_DOWN || e.keyCode == SWT.END) {
					int height = list.getTable().getClientArea().height;
					int rows = height / list.getTable().getItemHeight();

					if (list.getTable().getItemCount() > 0) {
						currentIndex = list.getTable().getTopIndex() + rows - 1;
						if (currentIndex > list.getTable().getItemCount() - 1) {
							currentIndex = list.getTable().getItemCount() - 1;
						}

						refreshDetails();
					}
				}

				if (e.keyCode == SWT.PAGE_UP || e.keyCode == SWT.HOME) {
					if (list.getTable().getItemCount() > 0) {
						currentIndex = list.getTable().getTopIndex();
						refreshDetails();
					}
				}
			}

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (currentIndex < list.getTable().getItemCount() - 1) {
						currentIndex = currentIndex + 1;
						refreshDetails();
					}
				} else if (e.keyCode == SWT.ARROW_UP) {
					if (currentIndex > 0) {
						currentIndex = currentIndex - 1;
						refreshDetails();
					} else {
						pattern.setFocus();
					}
				}
			}
		});

		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				searcher.setFilterParam(AbstractSearcher.PATTERN, pattern
						.getText().trim());
			}
		});

		// details field
		details = new TableViewer(content, SWT.BORDER);
		detailsContentProvider = new DetailsContentProvider();
		details.setContentProvider(detailsContentProvider);
		detailsContentProvider.setElements(new Object[0]);
		details.setLabelProvider(new ListLabelProvider(
				getDetailsLabelProvider(), null));
		details.setInput(new Object[0]);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = listLabel.getSize().y;
		details.getControl().setLayoutData(gd);

		applyDialogFont(content);

		restoreDialog(getDialogSettings());

		return dialogArea;
	}

	private void refreshDetails() {
		if (currentIndex != -1) {
			Object element = list.getElementAt(currentIndex);

			if (element instanceof SearchListSeparator)
				detailsContentProvider.setElements(new Object[0]);
			else {
				AbstractSearchItem item = (AbstractSearchItem) (list
						.getElementAt(currentIndex));

				Object o = searcher.getDetails(item);

				detailsContentProvider.setElements(new Object[] { o });
			}
		} else
			detailsContentProvider.setElements(new Object[0]);

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
				tempStatus = searcher.validateElement(item);

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
			list.getControl().setRedraw(false);
			listContentProvider.setElements(searcher.getElements());
			list.refresh();
			list.getControl().setRedraw(true);

			if (list.getTable().getItemCount() > 0) {
				list.setSelection(new StructuredSelection(list
						.getElementAt(list.getTable().getTopIndex())));

				currentIndex = list.getTable().getTopIndex();
			} else {
				currentIndex = -1;
			}
		}

		if (!progressLabel.isDisposed()) {
			progressLabel.setText(searcher.getProgressMessage());
		}
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

		AbstractSearchItem item = null;

		for (Iterator it = selectedElements.iterator(); it.hasNext();) {
			item = (AbstractSearchItem) it.next();
			objectsToReturn.add(searcher.getObjectToReturn(item));
		}

		setResult(objectsToReturn);
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
			GridData gd = (GridData) details.getControl().getLayoutData();
			gd.exclude = !isChecked();
			details.getControl().getParent().layout();
		}
	}

	private class RefreshJob extends UIJob {

		/**
		 * Creates a new instance of the class
		 */
		public RefreshJob() {
			super(AbstractSearchDialog.this.getParentShell().getDisplay(),
					WorkbenchMessages.AbstractSearchDialog_refreshJob);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (AbstractSearchDialog.this != null) {
				AbstractSearchDialog.this.refresh();
			}

			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH,
					IStatus.OK, "", null); //$NON-NLS-1$
		}
	}

	private class ListLabelProvider extends LabelProvider implements
			IColorProvider, ILabelProviderListener {
		private ILabelProvider provider;

		private ILabelDecorator selectionDecorator;

		// Need to keep our own list of listeners
		private ListenerList listeners = new ListenerList();

		public ListLabelProvider(ILabelProvider provider,
				ILabelDecorator selectionDecorator) {
			this.provider = provider;
			this.selectionDecorator = selectionDecorator;

			provider.addListener(this);

			if (selectionDecorator != null) {
				selectionDecorator.addListener(this);
			}
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

			GC gc = new GC(list.getControl());
			gc.setFont(list.getControl().getFont());

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

	/**
	 * Used in SearchListContentProvider & SearchListLabelProvider
	 * 
	 * @since 3.3
	 */
	private class SearchListSeparator {

		private String name;

		public SearchListSeparator(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
