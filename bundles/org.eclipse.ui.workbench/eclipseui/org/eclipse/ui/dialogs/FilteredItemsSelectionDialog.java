/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  IBM Corporation - initial API and implementation
 *  Willian Mitsuda <wmitsuda@gmail.com>
 *     - Fix for bug 196553 - [Dialogs] Support IColorProvider/IFontProvider in FilteredItemsSelectionDialog
 *  Peter Friese <peter.friese@gentleware.com>
 *     - Fix for bug 208602 - [Dialogs] Open Type dialog needs accessible labels
 *  Simon Muschel <smuschel@gmx.de> - bug 258493
 *  Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *  Patrik Suzzi <psuzzi@gmail.com> - Bug 485133
 *  Lucas Bullen <lbullen@redhat.com> - Bug 525974, 531332
 *  Emmanuel Chebbi <emmanuel.chebbi@outlook.fr> - Bug 214491
 *     - [Dialogs] FilteredItemsSelectionDialog should respect setInitialSelections()
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.BoldStylerProvider;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Shows a list of items to the user with a text entry field for a string
 * pattern used to filter the list of items.
 *
 * @since 3.3
 */
public abstract class FilteredItemsSelectionDialog extends SelectionStatusDialog {

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
	 * Pattern input field selection where caret is at the beginning (used only for
	 * initial pattern).
	 */
	public static final int CARET_BEGINNING = 1;

	/**
	 * Represents a full selection in the pattern input field (used only for initial
	 * pattern).
	 */
	public static final int FULL_SELECTION = 2;

	private Text pattern;

	private TableViewer tableViewer;

	private DetailsContentViewer details;

	/**
	 * It is a duplicate of a field in the CLabel class in DetailsContentViewer. It
	 * is maintained, because the <code>setDetailsLabelProvider()</code> could be
	 * called before content area is created.
	 */
	private ILabelProvider detailsLabelProvider;

	private ItemsListLabelProvider itemsListLabelProvider;

	private MenuManager menuManager;

	private MenuManager contextMenuManager;

	private boolean multi;

	private ToolBar toolBar;

	private ToolItem toolItem;

	private Label progressLabel;

	private ToggleStatusLineAction toggleStatusLineAction;

	private RemoveHistoryItemAction removeHistoryItemAction;

	private ActionContributionItem removeHistoryActionContributionItem;

	private IStatus status;

	private RefreshCacheJob refreshCacheJob;

	private RefreshProgressMessageJob refreshProgressMessageJob = new RefreshProgressMessageJob();

	private Object[] currentSelection;

	private ContentProvider contentProvider;

	private FilterHistoryJob filterHistoryJob;

	private FilterJob filterJob;

	private ItemsFilter filter;

	private ItemsFilter currentlyCompletingFilter;

	private List<Object> lastCompletedResult;

	private ItemsFilter lastCompletedFilter;

	private String initialPatternText;

	private int selectionMode;

	private ItemsListSeparator itemsListSeparator;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private boolean refreshWithLastSelection = false;

	private IHandlerActivation showViewHandler;

	private IStyledStringHighlighter styledStringHighlighter;

	/**
	 * Used to set initial selection in {@link #refresh()}.
	 */
	private boolean isShownForTheFirstTime = true;

	/**
	 * Creates a new instance of the class.
	 *
	 * @param shell shell to parent the dialog on
	 * @param multi indicates whether dialog allows to select more than one position
	 *              in its list of items
	 */
	public FilteredItemsSelectionDialog(Shell shell, boolean multi) {
		super(shell);
		this.multi = multi;
		filterHistoryJob = new FilterHistoryJob();
		filterJob = new FilterJob();
		contentProvider = new ContentProvider();
		refreshCacheJob = new RefreshCacheJob();
		itemsListSeparator = new ItemsListSeparator(WorkbenchMessages.FilteredItemsSelectionDialog_separatorLabel);
		selectionMode = NONE;
	}

	/**
	 * Creates a new instance of the class. Created dialog won't allow to select
	 * more than one item.
	 *
	 * @param shell shell to parent the dialog on
	 */
	public FilteredItemsSelectionDialog(Shell shell) {
		this(shell, false);
	}

	/**
	 * Adds viewer filter to the dialog items list.
	 *
	 * @param filter the new filter
	 */
	protected void addListFilter(ViewerFilter filter) {
		contentProvider.addFilter(filter);
	}

	/**
	 * Sets a new label provider for items in the list. If the label provider also
	 * implements
	 * {@link org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider .IStyledLabelProvider},
	 * the style text labels provided by it will be used provided that the
	 * corresponding preference is set.
	 *
	 * @see IWorkbenchPreferenceConstants#USE_COLORED_LABELS
	 *
	 * @param listLabelProvider the label provider for items in the list
	 */
	public void setListLabelProvider(ILabelProvider listLabelProvider) {
		getItemsListLabelProvider().setProvider(listLabelProvider);
	}

	/**
	 * Returns the label decorator for selected items in the list.
	 *
	 * @return the label decorator for selected items in the list
	 */
	private ILabelDecorator getListSelectionLabelDecorator() {
		return getItemsListLabelProvider().getSelectionDecorator();
	}

	/**
	 * Sets the label decorator for selected items in the list.
	 *
	 * @param listSelectionLabelDecorator the label decorator for selected items in
	 *                                    the list
	 */
	public void setListSelectionLabelDecorator(ILabelDecorator listSelectionLabelDecorator) {
		getItemsListLabelProvider().setSelectionDecorator(listSelectionLabelDecorator);
	}

	/**
	 * Returns the item list label provider.
	 *
	 * @return the item list label provider
	 */
	private ItemsListLabelProvider getItemsListLabelProvider() {
		if (itemsListLabelProvider == null) {
			itemsListLabelProvider = new ItemsListLabelProvider(
					new TypeItemLabelProvider(), null);
		}
		return itemsListLabelProvider;
	}

	/**
	 * Sets label provider for the details field.
	 *
	 * For a single selection, the element sent to
	 * {@link ILabelProvider#getImage(Object)} and
	 * {@link ILabelProvider#getText(Object)} is the selected object, for multiple
	 * selection a {@link String} with amount of selected items is the element.
	 *
	 * @see #getSelectedItems() getSelectedItems() can be used to retrieve selected
	 *      items and get the items count.
	 *
	 * @param detailsLabelProvider the label provider for the details field
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

	@Override
	public void create() {
		super.create();
		pattern.setFocus();
	}

	/**
	 * Restores dialog using persisted settings. The default implementation restores
	 * the status of the details line and the selection history.
	 *
	 * @param settings settings used to restore dialog
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
				IMemento memento = XMLMemento.createReadRoot(new StringReader(setting));
				this.contentProvider.loadHistory(memento);
			} catch (WorkbenchException e) {
				// Simply don't restore the settings
				StatusManager.getManager().handle(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
						WorkbenchMessages.FilteredItemsSelectionDialog_restoreError, e));
			}
		}
	}

	@Override
	public boolean close() {
		this.filterJob.cancel();
		this.refreshCacheJob.cancel();
		this.refreshProgressMessageJob.cancel();
		if (showViewHandler != null) {
			IHandlerService service = PlatformUI.getWorkbench().getService(IHandlerService.class);
			service.deactivateHandler(showViewHandler);
			showViewHandler.getHandler().dispose();
			showViewHandler = null;
		}
		if (menuManager != null)
			menuManager.dispose();
		if (contextMenuManager != null)
			contextMenuManager.dispose();
		storeDialog(getDialogSettings());
		return super.close();
	}

	/**
	 * Stores dialog settings.
	 *
	 * @param settings settings used to store dialog
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
			StatusManager.getManager().handle(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
					WorkbenchMessages.FilteredItemsSelectionDialog_storeError, e));
		}
	}

	/**
	 * Create a new header which is labelled by headerLabel.
	 *
	 * @return Label the label of the header
	 */
	private Label createHeader(Composite parent) {
		Composite header = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		Label headerLabel = new Label(header, SWT.NONE);
		headerLabel.setText((getMessage() != null && getMessage().trim().length() > 0) ? getMessage()
				: WorkbenchMessages.FilteredItemsSelectionDialog_patternLabel);
		headerLabel.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
				e.detail = SWT.TRAVERSE_NONE;
				pattern.setFocus();
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		headerLabel.setLayoutData(gd);

		createViewMenu(header);
		header.setLayoutData(gd);
		return headerLabel;
	}

	/**
	 * Create the labels for the list and the progress. Return the list label.
	 *
	 * @return Label
	 */
	private Label createLabels(Composite parent) {
		Composite labels = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		labels.setLayout(layout);

		Label listLabel = new Label(labels, SWT.NONE);
		listLabel.setText(WorkbenchMessages.FilteredItemsSelectionDialog_listLabel);

		listLabel.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
				e.detail = SWT.TRAVERSE_NONE;
				tableViewer.getTable().setFocus();
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		listLabel.setLayoutData(gd);

		progressLabel = new Label(labels, SWT.RIGHT);
		progressLabel.setLayoutData(gd);

		labels.setLayoutData(gd);
		return listLabel;
	}

	private void createViewMenu(Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		toolItem = new ToolItem(toolBar, SWT.PUSH, 0);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		toolBar.setLayoutData(data);

		toolBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				showViewMenu();
			}
		});

		toolItem.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU));
		toolItem.setToolTipText(WorkbenchMessages.FilteredItemsSelectionDialog_menu);
		toolItem.addSelectionListener(widgetSelectedAdapter(e -> showViewMenu()));

		menuManager = new MenuManager();

		fillViewMenu(menuManager);

		IHandlerService service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		IHandler handler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				showViewMenu();
				return null;
			}
		};
		showViewHandler = service.activateHandler(IWorkbenchCommandConstants.WINDOW_SHOW_VIEW_MENU, handler,
				new ActiveShellExpression(getShell()));
	}

	/**
	 * Fills the menu of the dialog.
	 *
	 * @param menuManager the menu manager
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
	 * Hook that allows to add actions to the context menu.
	 * <p>
	 * Subclasses may extend in order to add other actions.
	 * </p>
	 *
	 * @param menuManager the context menu manager
	 * @since 3.5
	 */
	protected void fillContextMenu(IMenuManager menuManager) {
		List<?> selectedElements = tableViewer.getStructuredSelection().toList();

		Object item = null;

		for (Iterator<?> it = selectedElements.iterator(); it.hasNext();) {
			item = it.next();
			if (item instanceof ItemsListSeparator || !isHistoryElement(item)) {
				return;
			}
		}

		if (selectedElements.size() > 0) {
			removeHistoryItemAction
					.setText(WorkbenchMessages.FilteredItemsSelectionDialog_removeItemsFromHistoryAction);

			menuManager.add(removeHistoryActionContributionItem);

		}
	}

	private void createPopupMenu() {
		removeHistoryItemAction = new RemoveHistoryItemAction();
		removeHistoryActionContributionItem = new ActionContributionItem(removeHistoryItemAction);

		contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(this::fillContextMenu);

		final Table table = tableViewer.getTable();
		Menu menu = contextMenuManager.createContextMenu(table);
		table.setMenu(menu);
	}

	/**
	 * Creates an extra content area, which will be located above the details.
	 *
	 * @param parent parent to create the dialog widgets in
	 * @return an extra content area
	 */
	protected abstract Control createExtendedContentArea(Composite parent);

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(dialogArea, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		content.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		content.setLayout(layout);

		final Label headerLabel = createHeader(content);

		pattern = new Text(content, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		pattern.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = LegacyActionTools.removeMnemonics(headerLabel.getText());
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		pattern.setLayoutData(gd);

		final Label listLabel = createLabels(content);

		tableViewer = new TableViewer(content, (multi ? SWT.MULTI : SWT.SINGLE) | SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
		tableViewer.getTable().getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				if (e.childID == ACC.CHILDID_SELF) {
					e.result = LegacyActionTools.removeMnemonics(listLabel.getText());
				}
			}
		});
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(getItemsListLabelProvider());
		tableViewer.setInput(new Object[0]);
		tableViewer.setItemCount(contentProvider.getNumberOfElements());
		gd = new GridData(GridData.FILL_BOTH);
		applyDialogFont(tableViewer.getTable());
		gd.heightHint = tableViewer.getTable().getItemHeight() * 15;
		tableViewer.getTable().setLayoutData(gd);

		createPopupMenu();

		pattern.addModifyListener(e -> applyFilter());

		pattern.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (tableViewer.getTable().getItemCount() > 0) {
						tableViewer.getTable().setFocus();
					}
				}
			}
		});

		tableViewer.addSelectionChangedListener(event -> {
			StructuredSelection selection = (StructuredSelection) event.getSelection();
			handleSelected(selection);
		});

		tableViewer.addDoubleClickListener(event -> handleDoubleClick());

		tableViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.DEL) {

					List<?> selectedElements = ((StructuredSelection) tableViewer.getSelection()).toList();

					Object item = null;
					boolean isSelectedHistory = true;

					for (Iterator<?> it = selectedElements.iterator(); it.hasNext();) {
						item = it.next();
						if (item instanceof ItemsListSeparator || !isHistoryElement(item)) {
							isSelectedHistory = false;
							break;
						}
					}
					if (isSelectedHistory)
						removeSelectedItems(selectedElements);

				}

				if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.SHIFT) != 0 && (e.stateMask & SWT.CTRL) != 0) {
					IStructuredSelection selection = tableViewer.getStructuredSelection();

					if (selection.size() == 1) {
						Object element = selection.getFirstElement();
						if (element.equals(tableViewer.getElementAt(0))) {
							pattern.setFocus();
						}
						if (tableViewer.getElementAt(tableViewer.getTable().getSelectionIndex() - 1) instanceof ItemsListSeparator)
							tableViewer.getTable().setSelection(tableViewer.getTable().getSelectionIndex() - 1);
						tableViewer.getTable().notifyListeners(SWT.Selection, new Event());

					}
				}

				if (e.keyCode == SWT.ARROW_DOWN && (e.stateMask & SWT.SHIFT) != 0 && (e.stateMask & SWT.CTRL) != 0) {

					if (tableViewer.getElementAt(tableViewer.getTable().getSelectionIndex() + 1) instanceof ItemsListSeparator)
						tableViewer.getTable().setSelection(tableViewer.getTable().getSelectionIndex() + 1);
					tableViewer.getTable().notifyListeners(SWT.Selection, new Event());
				}

			}
		});

		createExtendedContentArea(content);

		details = new DetailsContentViewer(content, SWT.BORDER | SWT.FLAT);
		details.setVisible(toggleStatusLineAction.isChecked());
		details.setContentProvider(new IContentProvider() {
			// intentionally empty
		});
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
	 * This method is a hook for subclasses to override default dialog behavior. The
	 * <code>handleDoubleClick()</code> method handles double clicks on the list of
	 * filtered elements.
	 * <p>
	 * Current implementation makes double-clicking on the list do the same as
	 * pressing <code>OK</code> button on the dialog.
	 */
	protected void handleDoubleClick() {
		okPressed();
	}

	/**
	 * Refreshes the details field according to the current selection in the items
	 * list.
	 */
	private void refreshDetails() {
		StructuredSelection selection = getSelectedItems();

		switch (selection.size()) {
		case 0:
			details.setInput(null);
			break;
		case 1:
			details.setInput(selection.getFirstElement());
			break;
		default:
			details.setInput(NLS.bind(WorkbenchMessages.FilteredItemsSelectionDialog_nItemsSelected,
					Integer.valueOf(selection.size())));
			break;
		}

	}

	/**
	 * Handle selection in the items list by updating labels of selected and
	 * unselected items and refresh the details field using the selection.
	 *
	 * @param selection the new selection
	 */
	protected void handleSelected(StructuredSelection selection) {
		IStatus status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, EMPTY_STRING, null);

		Object[] lastSelection = currentSelection;

		currentSelection = selection.toArray();

		if (selection.isEmpty()) {
			status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, EMPTY_STRING, null);

			if (lastSelection != null && getListSelectionLabelDecorator() != null) {
				tableViewer.update(lastSelection, null);
			}

			currentSelection = null;

		} else {
			status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, EMPTY_STRING, null);

			List<?> items = selection.toList();

			Object item = null;
			IStatus tempStatus = null;

			for (Object o : items) {
				if (o instanceof ItemsListSeparator) {
					continue;
				}

				item = o;
				tempStatus = validateItem(item);

				if (tempStatus.isOK()) {
					status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, EMPTY_STRING, null);
				} else {
					status = tempStatus;
					// if any selected element is not valid status is set to
					// ERROR
					break;
				}
			}

			if (lastSelection != null && getListSelectionLabelDecorator() != null) {
				tableViewer.update(lastSelection, null);
			}

			if (getListSelectionLabelDecorator() != null) {
				tableViewer.update(currentSelection, null);
			}
		}

		refreshDetails();
		updateStatus(status);
	}

	@Override
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
	 * Refreshes the dialog - has to be called in UI thread.
	 */
	public void refresh() {
		if (tableViewer != null && !tableViewer.getTable().isDisposed()) {

			List<Object> lastRefreshSelection = ((StructuredSelection) tableViewer.getSelection()).toList();
			tableViewer.getTable().deselectAll();

			tableViewer.setItemCount(contentProvider.getNumberOfElements());
			tableViewer.refresh();

			if (tableViewer.getTable().getItemCount() > 0) {
				if (isShownForTheFirstTime) {
					isShownForTheFirstTime = false;
					lastRefreshSelection = prepareInitialSelection(lastRefreshSelection);
				}
				// preserve previous selection
				if (refreshWithLastSelection && lastRefreshSelection != null && lastRefreshSelection.size() > 0) {
					tableViewer.setSelection(new StructuredSelection(lastRefreshSelection));
				} else {
					refreshWithLastSelection = true;
					tableViewer.getTable().setSelection(0);
					tableViewer.getTable().notifyListeners(SWT.Selection, new Event());
				}
			} else {
				tableViewer.setSelection(StructuredSelection.EMPTY);
			}

		}

		scheduleProgressMessageRefresh();
	}

	/**
	 * Gets the elements that should be selected when the dialog opens.
	 * <p>
	 * Sets the <code>refreshWithLastSelection</code> to true if needed to make sure
	 * that the initial selection is properly set.
	 *
	 * @param currentSelection the elements selected by default.
	 *
	 * @return the initial selection specified by the user or the currentSelection
	 *         if no initial selection has been set.
	 */
	private List<Object> prepareInitialSelection(List<Object> currentSelection) {
		boolean hasNoInitialSelection = getInitialElementSelections().isEmpty();
		if (hasNoInitialSelection) {
			return currentSelection;
		}
		refreshWithLastSelection = true;
		if (!multi) {
			// if multi selection is disabled then only the first item is selected
			Object firstSelectedItem = getInitialElementSelections().get(0);
			return Collections.singletonList(firstSelectedItem);
		}
		return getInitialElementSelections();
	}

	/**
	 * Updates the progress label.
	 */
	@Deprecated
	public void updateProgressLabel() {
		scheduleProgressMessageRefresh();
	}

	/**
	 * Notifies the content provider - fires filtering of content provider elements.
	 * During the filtering, a separator between history and workspace matches is
	 * added.
	 * <p>
	 * This is a long running operation and should be called in a job.
	 *
	 * @param checkDuplicates <code>true</code> if data concerning elements
	 *                        duplication should be computed - it takes much more
	 *                        time than the standard filtering
	 * @param monitor         a progress monitor or <code>null</code> if no monitor
	 *                        is available
	 */
	public void reloadCache(boolean checkDuplicates, IProgressMonitor monitor) {
		if (tableViewer != null && !tableViewer.getTable().isDisposed() && contentProvider != null) {
			contentProvider.reloadCache(checkDuplicates, monitor);
		}
	}

	/**
	 * Schedule refresh job.
	 */
	public void scheduleRefresh() {
		refreshCacheJob.cancelAll();
		refreshCacheJob.schedule();
	}

	/**
	 * Schedules progress message refresh.
	 */
	public void scheduleProgressMessageRefresh() {
		refreshProgressMessageJob.scheduleProgressRefresh(null);
	}

	@Override
	protected void computeResult() {

		List<?> selectedElements = tableViewer.getStructuredSelection().toList();

		List<Object> objectsToReturn = new ArrayList<>();

		Object item = null;

		for (Iterator<?> it = selectedElements.iterator(); it.hasNext();) {
			item = it.next();

			if (!(item instanceof ItemsListSeparator)) {
				accessedHistoryItem(item);
				objectsToReturn.add(item);
			}
		}

		setResult(objectsToReturn);
	}

	/*
	 * @see
	 * org.eclipse.ui.dialogs.SelectionStatusDialog#updateStatus(org.eclipse.core.
	 * runtime.IStatus)
	 */
	@Override
	protected void updateStatus(IStatus status) {
		this.status = status;
		super.updateStatus(status);
	}

	/*
	 * @see Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (status != null && (status.isOK() || status.getCode() == IStatus.INFO)) {
			super.okPressed();
		}
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. A full selection is used in the pattern input
	 * field.
	 *
	 * @param text initial pattern for the filter
	 * @see FilteredItemsSelectionDialog#FULL_SELECTION
	 */
	public void setInitialPattern(String text) {
		setInitialPattern(text, FULL_SELECTION);
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. The <code>selectionMode</code> is used to
	 * choose selection type for the input field.
	 *
	 * @param text          initial pattern for the filter
	 * @param selectionMode one of: {@link FilteredItemsSelectionDialog#NONE},
	 *                      {@link FilteredItemsSelectionDialog#CARET_BEGINNING},
	 *                      {@link FilteredItemsSelectionDialog#FULL_SELECTION}
	 */
	public void setInitialPattern(String text, int selectionMode) {
		this.initialPatternText = text;
		this.selectionMode = selectionMode;
	}

	/**
	 * Gets initial pattern.
	 *
	 * @return initial pattern, or <code>null</code> if initial pattern is not set
	 */
	protected String getInitialPattern() {
		return this.initialPatternText;
	}

	/**
	 * Returns the current selection.
	 *
	 * @return the current selection
	 */
	protected StructuredSelection getSelectedItems() {

		StructuredSelection selection = (StructuredSelection) tableViewer.getStructuredSelection();

		List<?> selectedItems = selection.toList();
		Object itemToRemove = null;

		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object item = it.next();
			if (item instanceof ItemsListSeparator) {
				itemToRemove = item;
				break;
			}
		}

		if (itemToRemove == null)
			return new StructuredSelection(selectedItems);
		// Create a new selection without the collision
		List<?> newItems = new ArrayList<>(selectedItems);
		newItems.remove(itemToRemove);
		return new StructuredSelection(newItems);

	}

	/**
	 * Validates the item. When items on the items list are selected or deselected,
	 * it validates each item in the selection and the dialog status depends on all
	 * validations.
	 *
	 * @param item an item to be checked
	 * @return status of the dialog to be set
	 */
	protected abstract IStatus validateItem(Object item);

	/**
	 * Creates an instance of a filter.
	 *
	 * @return a filter for items on the items list. Can be <code>null</code>, no
	 *         filtering will be applied then, causing no item to be shown in the
	 *         list.
	 */
	protected abstract ItemsFilter createFilter();

	/**
	 * Applies the filter created by <code>createFilter()</code> method to the items
	 * list. When new filter is different than previous one it will cause
	 * refiltering.
	 */
	protected void applyFilter() {

		ItemsFilter newFilter = createFilter();

		// don't apply filtering for patterns which mean the same, for example:
		// *a**b and ***a*b
		if (filter != null && filter.equalsFilter(newFilter)) {
			return;
		}

		filterHistoryJob.cancel();
		filterJob.cancel();

		this.filter = newFilter;

		if (this.filter != null) {
			filterHistoryJob.schedule();
		}
	}

	/**
	 * Returns comparator to sort items inside content provider. Returned object
	 * will be probably created as an anonymous class. Parameters passed to the
	 * <code>compare(java.lang.Object, java.lang.Object)</code> are going to be the
	 * same type as the one used in the content provider.
	 *
	 * @return comparator to sort items content provider
	 */
	protected abstract Comparator getItemsComparator();

	/**
	 * Fills the content provider with matching items.
	 *
	 * @param contentProvider collector to add items to.
	 *                        {@link FilteredItemsSelectionDialog.AbstractContentProvider#add(Object, FilteredItemsSelectionDialog.ItemsFilter)}
	 *                        only adds items that pass the given
	 *                        <code>itemsFilter</code>.
	 * @param itemsFilter     the items filter
	 * @param progressMonitor must be used to report search progress. The state of
	 *                        this progress monitor reflects the state of the
	 *                        filtering process.
	 * @throws CoreException Something went wrong.
	 */
	protected abstract void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException;

	/**
	 * Removes selected items from history.
	 *
	 * @param items items to be removed
	 */
	private void removeSelectedItems(List<?> items) {
		for (Object item : items) {
			removeHistoryItem(item);
		}
		refreshWithLastSelection = false;
		contentProvider.refresh();
	}

	/**
	 * Removes an item from history.
	 *
	 * @param item an item to remove
	 * @return removed item
	 */
	protected Object removeHistoryItem(Object item) {
		return contentProvider.removeHistoryElement(item);
	}

	/**
	 * Adds item to history.
	 *
	 * @param item the item to be added
	 */
	protected void accessedHistoryItem(Object item) {
		contentProvider.addHistoryElement(item);
	}

	/**
	 * Returns a history comparator.
	 *
	 * @return decorated comparator
	 */
	private Comparator<Object> getHistoryComparator() {
		return new HistoryComparator();
	}

	/**
	 * Returns the history of selected elements.
	 *
	 * @return history of selected elements, or <code>null</code> if it is not set
	 */
	protected SelectionHistory getSelectionHistory() {
		return this.contentProvider.getSelectionHistory();
	}

	/**
	 * Sets new history.
	 *
	 * @param selectionHistory the history
	 */
	protected void setSelectionHistory(SelectionHistory selectionHistory) {
		if (this.contentProvider != null)
			this.contentProvider.setSelectionHistory(selectionHistory);
	}

	/**
	 * Indicates whether the given item is a history item.
	 *
	 * @param item the item to be investigated
	 * @return <code>true</code> if the given item exists in history,
	 *         <code>false</code> otherwise
	 */
	public boolean isHistoryElement(Object item) {
		return this.contentProvider.isHistoryElement(item);
	}

	/**
	 * Indicates whether the given item is a duplicate.
	 *
	 * @param item the item to be investigated
	 * @return <code>true</code> if the item is duplicate, <code>false</code>
	 *         otherwise
	 */
	public boolean isDuplicateElement(Object item) {
		return this.contentProvider.isDuplicateElement(item);
	}

	/**
	 * Sets separator label
	 *
	 * @param separatorLabel the label showed on separator
	 */
	public void setSeparatorLabel(String separatorLabel) {
		this.itemsListSeparator = new ItemsListSeparator(separatorLabel);
	}

	/**
	 * Returns name for then given object.
	 *
	 * @param item an object from the content provider. Subclasses should pay
	 *             attention to the passed argument. They should either only pass
	 *             objects of a known type (one used in content provider) or make
	 *             sure that passed parameter is the expected one (by type checking
	 *             like <code>instanceof</code> inside the method).
	 * @return name of the given item
	 */
	public abstract String getElementName(Object item);

	private class ToggleStatusLineAction extends Action {

		/**
		 * Creates a new instance of the class.
		 */
		public ToggleStatusLineAction() {
			super(WorkbenchMessages.FilteredItemsSelectionDialog_toggleStatusAction, IAction.AS_CHECK_BOX);
		}

		@Override
		public void run() {
			details.setVisible(isChecked());
		}
	}

	/**
	 * Only refreshes UI on the basis of an already sorted and filtered set of
	 * items.
	 * <p>
	 * Standard invocation scenario:
	 * </p>
	 * <ol>
	 * <li>filtering job (<code>FilterJob</code> class extending <code>Job</code>
	 * class)</li>
	 * <li>cache refresh without checking for duplicates
	 * (<code>RefreshCacheJob</code> class extending <code>Job</code> class)</li>
	 * <li>UI refresh (<code>RefreshJob</code> class extending <code>UIJob</code>
	 * class)</li>
	 * <li>cache refresh with checking for duplicates (<code>CacheRefreshJob</code>
	 * class extending <code>Job</code> class)</li>
	 * <li>UI refresh (<code>RefreshJob</code> class extending <code>UIJob</code>
	 * class)</li>
	 * </ol>
	 * The scenario is rather complicated, but it had to be applied, because:
	 * <ul>
	 * <li>refreshing cache is rather a long action and cannot be run in the UI -
	 * cannot be run in a UIJob</li>
	 * <li>refreshing cache checking for duplicates is twice as long as refreshing
	 * cache without checking for duplicates; results of the search could be
	 * displayed earlier</li>
	 * <li>refreshing the UI have to be run in a UIJob</li>
	 * </ul>
	 *
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.FilterJob
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.RefreshJob
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.RefreshCacheJob
	 */
	private class RefreshJob extends UIJob {

		/**
		 * Creates a new instance of the class.
		 */
		public RefreshJob() {
			super(FilteredItemsSelectionDialog.this.getParentShell().getDisplay(),
					WorkbenchMessages.FilteredItemsSelectionDialog_refreshJob);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled())
				return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH, IStatus.OK, EMPTY_STRING, null);

			if (FilteredItemsSelectionDialog.this != null) {
				FilteredItemsSelectionDialog.this.refresh();
			}

			return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, EMPTY_STRING, null);
		}

	}

	/**
	 * Refreshes the progress message cyclically with 500 milliseconds delay.
	 * <code>RefreshProgressMessageJob</code> is strictly connected with
	 * <code>GranualProgressMonitor</code> and use it to to get progress message and
	 * to decide about break of cyclical refresh.
	 */
	private class RefreshProgressMessageJob extends UIJob {

		private volatile GranualProgressMonitor progressMonitor;

		/**
		 * Creates a new instance of the class.
		 */
		public RefreshProgressMessageJob() {
			super(FilteredItemsSelectionDialog.this.getParentShell().getDisplay(),
					WorkbenchMessages.FilteredItemsSelectionDialog_progressRefreshJob);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {

			if (!progressLabel.isDisposed()) {
				progressLabel.setText(progressMonitor != null ? progressMonitor.getMessage() : EMPTY_STRING);
			} else {
				return Status.CANCEL_STATUS;
			}

			if (progressMonitor == null || progressMonitor.isDone()) {
				return new Status(IStatus.CANCEL, PlatformUI.PLUGIN_ID, IStatus.CANCEL, EMPTY_STRING, null);
			}

			// Schedule cyclical with 500 milliseconds delay
			schedule(500);

			return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, EMPTY_STRING, null);
		}

		/**
		 * Schedule progress refresh job.
		 *
		 * @param progressMonitor used during refresh progress label
		 */
		public void scheduleProgressRefresh(GranualProgressMonitor progressMonitor) {
			this.progressMonitor = progressMonitor;
			// Schedule with initial delay to avoid flickering when the user
			// types quickly
			schedule(200);
		}

	}

	/**
	 * A job responsible for computing filtered items list presented using
	 * <code>RefreshJob</code>.
	 *
	 * @see FilteredItemsSelectionDialog.RefreshJob
	 */
	private class RefreshCacheJob extends Job {

		private RefreshJob refreshJob = new RefreshJob();

		/**
		 * Creates a new instance of the class.
		 */
		public RefreshCacheJob() {
			super(WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob);
			setSystem(true);
		}

		/**
		 * Stops the job and all sub-jobs.
		 */
		public void cancelAll() {
			cancel();
			refreshJob.cancel();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return new Status(IStatus.CANCEL, WorkbenchPlugin.PI_WORKBENCH, IStatus.CANCEL, EMPTY_STRING, null);
			}

			if (FilteredItemsSelectionDialog.this != null) {
				GranualProgressMonitor wrappedMonitor = new GranualProgressMonitor(monitor);
				FilteredItemsSelectionDialog.this.reloadCache(true, wrappedMonitor);
			}

			if (!monitor.isCanceled()) {
				refreshJob.schedule();
			}

			return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, EMPTY_STRING, null);

		}

		@Override
		protected void canceling() {
			super.canceling();
			contentProvider.stopReloadingCache();
		}

	}

	private class RemoveHistoryItemAction extends Action {

		/**
		 * Creates a new instance of the class.
		 */
		public RemoveHistoryItemAction() {
			super(WorkbenchMessages.FilteredItemsSelectionDialog_removeItemsFromHistoryAction);
		}

		@Override
		public void run() {
			List<?> selectedElements = ((StructuredSelection) tableViewer.getSelection()).toList();
			removeSelectedItems(selectedElements);
		}
	}

	private static boolean showColoredLabels() {
		return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
	}

	private class ItemsListLabelProvider extends StyledCellLabelProvider implements ILabelProviderListener {
		private ILabelProvider provider;

		private ILabelDecorator selectionDecorator;

		// Need to keep our own list of listeners
		private ListenerList<ILabelProviderListener> listeners = new ListenerList<>();

		/**
		 * Creates a new instance of the class.
		 *
		 * @param provider           the label provider for all items, not
		 *                           <code>null</code>
		 * @param selectionDecorator the decorator for selected items, can be
		 *                           <code>null</code>
		 */
		public ItemsListLabelProvider(ILabelProvider provider, ILabelDecorator selectionDecorator) {
			Assert.isNotNull(provider);
			this.provider = provider;
			this.selectionDecorator = selectionDecorator;

			setOwnerDrawEnabled(showColoredLabels() && provider instanceof IStyledLabelProvider);

			provider.addListener(this);

			if (selectionDecorator != null) {
				selectionDecorator.addListener(this);
			}
		}

		/**
		 * Sets new selection decorator.
		 *
		 * @param newSelectionDecorator new label decorator for selected items in the
		 *                              list
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
		 * Gets selection decorator.
		 *
		 * @return the label decorator for selected items in the list
		 */
		public ILabelDecorator getSelectionDecorator() {
			return selectionDecorator;
		}

		/**
		 * Sets new label provider.
		 *
		 * @param newProvider new label provider for items in the list, not
		 *                    <code>null</code>
		 */
		public void setProvider(ILabelProvider newProvider) {
			Assert.isNotNull(newProvider);
			provider.removeListener(this);
			provider.dispose();

			provider = newProvider;
			provider.addListener(this);

			setOwnerDrawEnabled(showColoredLabels() && provider instanceof IStyledLabelProvider);
		}

		private Image getImage(Object element) {
			if (element instanceof ItemsListSeparator) {
				return WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJ_SEPARATOR);
			}

			return provider.getImage(element);
		}

		private boolean isSelected(Object element) {
			if (element != null && currentSelection != null) {
				for (Object entry : currentSelection) {
					if (element.equals(entry))
						return true;
				}
			}
			return false;
		}

		private String getText(Object element) {
			if (element instanceof ItemsListSeparator) {
				return getSeparatorLabel(((ItemsListSeparator) element).getName());
			}

			String str = provider.getText(element);
			if (selectionDecorator != null && isSelected(element)) {
				return selectionDecorator.decorateText(str, element);
			}

			return str;
		}

		private StyledString getStyledText(Object element, IStyledLabelProvider provider) {
			StyledString string = provider.getStyledText(element);

			if (selectionDecorator != null && isSelected(element)) {
				String decorated = selectionDecorator.decorateText(string.getString(), element);
				return StyledCellLabelProvider.styleDecoratedString(decorated, null, string);
				// no need to add colors when element is selected
			}
			return string;
		}

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();

			if (!(element instanceof ItemsListSeparator) && provider instanceof IStyledLabelProvider) {
				IStyledLabelProvider styledLabelProvider = (IStyledLabelProvider) provider;
				StyledString styledString = getStyledText(element, styledLabelProvider);

				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
				cell.setImage(styledLabelProvider.getImage(element));
			} else {
				cell.setText(getText(element));
				cell.setImage(getImage(element));
			}
			cell.setFont(getFont(element));
			cell.setForeground(getForeground(element));
			cell.setBackground(getBackground(element));

			super.update(cell);
		}

		private String getSeparatorLabel(String separatorLabel) {
			Rectangle rect = tableViewer.getTable().getBounds();

			int borderWidth = tableViewer.getTable().computeTrim(0, 0, 0, 0).width;

			int imageWidth = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJ_SEPARATOR).getBounds().width;

			int width = rect.width - borderWidth - imageWidth;

			GC gc = new GC(tableViewer.getTable());
			gc.setFont(tableViewer.getTable().getFont());

			int fSeparatorWidth = gc.getAdvanceWidth('-');
			int fMessageLength = gc.textExtent(separatorLabel).x;

			gc.dispose();

			StringBuilder dashes = new StringBuilder();
			int chars = (((width - fMessageLength) / fSeparatorWidth) / 2) - 2;
			for (int i = 0; i < chars; i++) {
				dashes.append('-');
			}

			StringBuilder result = new StringBuilder();
			result.append(dashes);
			result.append(" " + separatorLabel + " "); //$NON-NLS-1$//$NON-NLS-2$
			result.append(dashes);
			return result.toString().trim();
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}

		@Override
		public void dispose() {
			provider.removeListener(this);
			provider.dispose();

			if (selectionDecorator != null) {
				selectionDecorator.removeListener(this);
				selectionDecorator.dispose();
			}

			super.dispose();
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			if (provider.isLabelProperty(element, property)) {
				return true;
			}
			if (selectionDecorator != null && selectionDecorator.isLabelProperty(element, property)) {
				return true;
			}
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		private Color getBackground(Object element) {
			if (element instanceof ItemsListSeparator) {
				return null;
			}
			if (provider instanceof IColorProvider) {
				return ((IColorProvider) provider).getBackground(element);
			}
			return null;
		}

		private Color getForeground(Object element) {
			if (element instanceof ItemsListSeparator) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			}
			if (provider instanceof IColorProvider) {
				return ((IColorProvider) provider).getForeground(element);
			}
			return null;
		}

		private Font getFont(Object element) {
			if (element instanceof ItemsListSeparator) {
				return null;
			}
			if (provider instanceof IFontProvider) {
				return ((IFontProvider) provider).getFont(element);
			}
			return null;
		}

		@Override
		public void labelProviderChanged(LabelProviderChangedEvent event) {
			for (ILabelProviderListener l : listeners) {
				l.labelProviderChanged(event);
			}
		}
	}

	/**
	 * Used in ItemsListContentProvider, separates history and non-history items.
	 */
	private static class ItemsListSeparator {

		private String name;

		/**
		 * Creates a new instance of the class.
		 *
		 * @param name the name of the separator
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
	 * GranualProgressMonitor is used for monitoring progress of filtering process.
	 * It is used by <code>RefreshProgressMessageJob</code> to refresh progress
	 * message. State of this monitor illustrates state of filtering or cache
	 * refreshing process.
	 */
	private class GranualProgressMonitor extends ProgressMonitorWrapper {

		private String name;

		private String subName;

		private int totalWork;

		private double worked;

		private boolean done;

		/**
		 * Creates instance of <code>GranualProgressMonitor</code>.
		 *
		 * @param monitor progress to be wrapped
		 */
		public GranualProgressMonitor(IProgressMonitor monitor) {
			super(monitor);
		}

		/**
		 * Checks if filtering has been done
		 *
		 * @return true if filtering work has been done false in other way
		 */
		public boolean isDone() {
			return done;
		}

		@Override
		public void setTaskName(String name) {
			super.setTaskName(name);
			this.name = name;
			this.subName = null;
		}

		@Override
		public void subTask(String name) {
			super.subTask(name);
			this.subName = name;
		}

		@Override
		public void beginTask(String name, int totalWork) {
			super.beginTask(name, totalWork);
			if (this.name == null)
				this.name = name;
			this.totalWork = totalWork;
			refreshProgressMessageJob.scheduleProgressRefresh(this);
		}

		@Override
		public void worked(int work) {
			super.worked(work);
			internalWorked(work);
		}

		@Override
		public void done() {
			done = true;
			super.done();
		}

		@Override
		public void setCanceled(boolean b) {
			done = b;
			super.setCanceled(b);
		}

		@Override
		public void internalWorked(double work) {
			worked = worked + work;
		}

		private String getMessage() {
			if (done)
				return ""; //$NON-NLS-1$

			String message;

			if (name == null) {
				message = subName == null ? "" : subName; //$NON-NLS-1$
			} else {
				message = subName == null ? name
						: NLS.bind(WorkbenchMessages.FilteredItemsSelectionDialog_subtaskProgressMessage,
								new Object[] { name, subName });
			}
			if (totalWork == 0)
				return message;

			return NLS.bind(WorkbenchMessages.FilteredItemsSelectionDialog_taskProgressMessage,
					new Object[] { message, Integer.valueOf((int) ((worked * 100) / totalWork)) });

		}

	}

	/**
	 * Filters items history and schedule filter job.
	 */
	private class FilterHistoryJob extends Job {

		/**
		 * Filter used during the filtering process.
		 */
		private ItemsFilter itemsFilter;

		/**
		 * Creates new instance of receiver.
		 */
		public FilterHistoryJob() {
			super(WorkbenchMessages.FilteredItemsSelectionDialog_jobLabel);
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			this.itemsFilter = filter;

			contentProvider.reset();

			refreshWithLastSelection = false;

			contentProvider.addHistoryItems(itemsFilter);

			if (!(lastCompletedFilter != null && lastCompletedFilter.isSubFilter(this.itemsFilter)))
				contentProvider.refresh();

			filterJob.schedule();

			return Status.OK_STATUS;
		}

	}

	/**
	 * Filters items in indicated set and history. During filtering, it refreshes
	 * the dialog (progress monitor and elements list).
	 *
	 * Depending on the filter, <code>FilterJob</code> decides which kind of search
	 * will be run inside <code>filterContent</code>. If the last filtering is done
	 * (last completed filter), is not null, and the new filter is a sub-filter
	 * ({@link FilteredItemsSelectionDialog.ItemsFilter#isSubFilter(FilteredItemsSelectionDialog.ItemsFilter)})
	 * of the last, then <code>FilterJob</code> only filters in the cache. If it is
	 * the first filtering or the new filter isn't a sub-filter of the last one, a
	 * full search is run.
	 */
	private class FilterJob extends Job {

		/**
		 * Filter used during the filtering process.
		 */
		protected ItemsFilter itemsFilter;

		/**
		 * Creates new instance of FilterJob
		 */
		public FilterJob() {
			super(WorkbenchMessages.FilteredItemsSelectionDialog_jobLabel);
			setSystem(true);
		}

		@Override
		protected final IStatus run(IProgressMonitor parent) {
			GranualProgressMonitor monitor = new GranualProgressMonitor(parent);
			return doRun(monitor);
		}

		/**
		 * Executes job using the given filtering progress monitor. A hook for
		 * subclasses.
		 *
		 * @param monitor progress monitor
		 * @return result of the execution
		 */
		protected IStatus doRun(GranualProgressMonitor monitor) {
			try {
				internalRun(monitor);
			} catch (CoreException e) {
				cancel();
				return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
						WorkbenchMessages.FilteredItemsSelectionDialog_jobError, e);
			}
			return Status.OK_STATUS;
		}

		/**
		 * Main method for the job.
		 */
		private void internalRun(GranualProgressMonitor monitor) throws CoreException {
			try {
				if (monitor.isCanceled())
					return;

				this.itemsFilter = filter;

				if (filter.getPattern().length() != 0) {
					filterContent(monitor);
				}

				if (monitor.isCanceled())
					return;

				contentProvider.refresh();
			} finally {
				monitor.done();
			}
		}

		/**
		 * Filters items.
		 *
		 * @param monitor for monitoring progress
		 * @throws CoreException Something went wrong.
		 */
		protected void filterContent(GranualProgressMonitor monitor) throws CoreException {

			if (lastCompletedFilter != null && lastCompletedFilter.isSubFilter(this.itemsFilter)) {

				int length = lastCompletedResult.size() / 500;
				monitor.beginTask(WorkbenchMessages.FilteredItemsSelectionDialog_cacheSearchJob_taskName, length);

				for (int pos = 0; pos < lastCompletedResult.size(); pos++) {

					Object item = lastCompletedResult.get(pos);
					if (monitor.isCanceled())
						break;
					contentProvider.add(item, itemsFilter);

					if ((pos % 500) == 0) {
						monitor.worked(1);
					}
				}

			} else {

				lastCompletedFilter = null;
				lastCompletedResult = null;

				SubMonitor subMonitor = SubMonitor.convert(monitor,
						WorkbenchMessages.FilteredItemsSelectionDialog_searchJob_taskName, 100);

				fillContentProvider(contentProvider, itemsFilter, subMonitor.split(95));

				if (monitor != null && !monitor.isCanceled()) {
					subMonitor.worked(2);
					contentProvider.rememberResult(itemsFilter);
					subMonitor.worked(3);
				}
			}

		}

	}

	/**
	 * History stores a list of key, object pairs. The list is bounded at a certain
	 * size. If the list exceeds this size the oldest element is removed from the
	 * list. An element can be added/renewed with a call to
	 * <code>accessed(Object)</code>.
	 * <p>
	 * The history can be stored to/loaded from an XML file.
	 * </p>
	 */
	protected static abstract class SelectionHistory {

		private static final String DEFAULT_ROOT_NODE_NAME = "historyRootNode"; //$NON-NLS-1$

		private static final String DEFAULT_INFO_NODE_NAME = "infoNode"; //$NON-NLS-1$

		private static final int MAX_HISTORY_SIZE = 60;

		private final Set<Object> historyList;

		private final String rootNodeName;

		private final String infoNodeName;

		private SelectionHistory(String rootNodeName, String infoNodeName) {

			historyList = Collections.synchronizedSet(new LinkedHashSet<>() {

				private static final long serialVersionUID = 0L;

				@Override
				public boolean add(Object arg0) {
					if (this.size() >= MAX_HISTORY_SIZE) {
						Iterator<?> iterator = this.iterator();
						iterator.next();
						iterator.remove();
					}
					return super.add(arg0);
				}

			});

			this.rootNodeName = rootNodeName;
			this.infoNodeName = infoNodeName;
		}

		/**
		 * Creates new instance of <code>SelectionHistory</code>.
		 */
		public SelectionHistory() {
			this(DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
		}

		/**
		 * Adds object to history.
		 *
		 * @param object the item to be added to the history
		 */
		public synchronized void accessed(Object object) {
			historyList.remove(object);
			historyList.add(object);
		}

		/**
		 * Returns <code>true</code> if history contains object.
		 *
		 * @param object the item for which check will be executed
		 * @return <code>true</code> if history contains object <code>false</code> in
		 *         other way
		 */
		public synchronized boolean contains(Object object) {
			return historyList.contains(object);
		}

		/**
		 * Returns <code>true</code> if history is empty.
		 *
		 * @return <code>true</code> if history is empty
		 */
		public synchronized boolean isEmpty() {
			return historyList.isEmpty();
		}

		/**
		 * Remove element from history.
		 *
		 * @param element to remove form the history
		 * @return <code>true</code> if this list contained the specified element
		 */
		public synchronized boolean remove(Object element) {
			return historyList.remove(element);
		}

		/**
		 * Load history elements from memento.
		 *
		 * @param memento memento from which the history will be retrieved
		 */
		public void load(IMemento memento) {

			XMLMemento historyMemento = (XMLMemento) memento.getChild(rootNodeName);

			if (historyMemento == null) {
				return;
			}

			IMemento[] mementoElements = historyMemento.getChildren(infoNodeName);
			for (IMemento mementoElement : mementoElements) {
				Object object = restoreItemFromMemento(mementoElement);
				if (object != null) {
					historyList.add(object);
				}
			}
		}

		/**
		 * Save history elements to memento.
		 *
		 * @param memento memento to which the history will be added
		 */
		public void save(IMemento memento) {

			IMemento historyMemento = memento.createChild(rootNodeName);

			Object[] items = getHistoryItems();
			for (Object item : items) {
				IMemento elementMemento = historyMemento.createChild(infoNodeName);
				storeItemToMemento(item, elementMemento);
			}

		}

		/**
		 * Gets array of history items.
		 *
		 * @return array of history elements
		 */
		public synchronized Object[] getHistoryItems() {
			return historyList.toArray();
		}

		/**
		 * Creates an object using given memento.
		 *
		 * @param memento memento used for creating new object
		 *
		 * @return the restored object
		 */
		protected abstract Object restoreItemFromMemento(IMemento memento);

		/**
		 * Store object in <code>IMemento</code>.
		 *
		 * @param item    the item to store
		 * @param memento the memento to store to
		 */
		protected abstract void storeItemToMemento(Object item, IMemento memento);

	}

	/**
	 * Filters elements using SearchPattern by comparing the names of items with the
	 * filter pattern.
	 */
	protected abstract class ItemsFilter {

		/**
		 * The {@link SearchPattern}.
		 */
		protected SearchPattern patternMatcher;

		/**
		 * Creates new instance of ItemsFilter.
		 */
		public ItemsFilter() {
			this(new SearchPattern());
		}

		/**
		 * Creates new instance of ItemsFilter.
		 *
		 * @param searchPattern the pattern to be used when filtering
		 */
		public ItemsFilter(SearchPattern searchPattern) {
			patternMatcher = searchPattern;
			String stringPattern = getPatternText();
			if (stringPattern != null && !stringPattern.equals("*")) { //$NON-NLS-1$
				patternMatcher.setPattern(stringPattern);
			} else {
				patternMatcher.setPattern(""); //$NON-NLS-1$
			}
		}

		/**
		 * Check if the given filter is a sub-filter of this filter. The default
		 * implementation checks if the <code>SearchPattern</code> from the given filter
		 * is a sub-pattern of the one from this filter.
		 * <p>
		 * <i>WARNING: This method is <b>not</b> defined in reading order, i.e.
		 * <code>a.isSubFilter(b)</code> is <code>true</code> iff <code>b</code> is a
		 * sub-filter of <code>a</code>, and not vice-versa. </i>
		 * </p>
		 *
		 * @param filter the filter to be checked, or <code>null</code>
		 * @return <code>true</code> if the given filter is sub-filter of this filter,
		 *         <code>false</code> if the given filter isn't a sub-filter or is
		 *         <code>null</code>
		 *
		 * @see org.eclipse.ui.dialogs.SearchPattern#isSubPattern(org.eclipse.ui.dialogs.SearchPattern)
		 */
		public boolean isSubFilter(ItemsFilter filter) {
			if (filter != null) {
				return this.patternMatcher.isSubPattern(filter.patternMatcher);
			}
			return false;
		}

		/**
		 * Checks whether the provided filter is equal to the current filter. The
		 * default implementation checks if <code>SearchPattern</code> from current
		 * filter is equal to the one from provided filter.
		 *
		 * @param filter filter to be checked, or <code>null</code>
		 * @return <code>true</code> if the given filter is equal to current filter,
		 *         <code>false</code> if given filter isn't equal to current one or if
		 *         it is <code>null</code>
		 *
		 * @see org.eclipse.ui.dialogs.SearchPattern#equalsPattern(org.eclipse.ui.dialogs.SearchPattern)
		 */
		public boolean equalsFilter(ItemsFilter filter) {
			if (filter != null && filter.patternMatcher.equalsPattern(this.patternMatcher)) {
				return true;
			}
			return false;
		}

		/**
		 * Checks whether the pattern's match rule is camel case.
		 *
		 * @return <code>true</code> if pattern's match rule is camel case,
		 *         <code>false</code> otherwise
		 */
		public boolean isCamelCasePattern() {
			return patternMatcher.getMatchRule() == SearchPattern.RULE_CAMELCASE_MATCH;
		}

		/**
		 * Returns the pattern string.
		 *
		 * @return pattern for this filter
		 *
		 * @see SearchPattern#getPattern()
		 */
		public String getPattern() {
			return patternMatcher.getPattern();
		}

		/**
		 * Returns the rule to apply for matching keys.
		 *
		 * @return an implementation-specific match rule
		 *
		 * @see SearchPattern#getMatchRule() for match rules returned by the default
		 *      implementation
		 */
		public int getMatchRule() {
			return patternMatcher.getMatchRule();
		}

		/**
		 * Matches text with filter.
		 *
		 * @param text the text to match with the filter
		 * @return <code>true</code> if text matches with filter pattern,
		 *         <code>false</code> otherwise
		 */
		protected boolean matches(String text) {
			return patternMatcher.matches(text);
		}

		/**
		 * General method for matching raw name pattern. Checks whether current pattern
		 * is prefix of name provided item.
		 *
		 * @param item item to check
		 * @return <code>true</code> if current pattern is a prefix of name provided
		 *         item, <code>false</code> if item's name is shorter than prefix or
		 *         sequences of characters don't match.
		 */
		public boolean matchesRawNamePattern(Object item) {
			String prefix = patternMatcher.getPattern();
			String text = getElementName(item);

			if (text == null)
				return false;

			int textLength = text.length();
			int prefixLength = prefix.length();
			if (textLength < prefixLength) {
				return false;
			}
			for (int i = prefixLength - 1; i >= 0; i--) {
				if (Character.toLowerCase(prefix.charAt(i)) != Character.toLowerCase(text.charAt(i)))
					return false;
			}
			return true;
		}

		/**
		 * Matches an item against filter conditions.
		 *
		 * @param item the item to match
		 * @return <code>true</code> if item matches against filter conditions,
		 *         <code>false</code> otherwise
		 */
		public abstract boolean matchItem(Object item);

		/**
		 * Checks consistency of an item. Item is inconsistent if was changed or
		 * removed.
		 *
		 * @param item the item to check.
		 * @return <code>true</code> if item is consistent, <code>false</code> if item
		 *         is inconsistent
		 */
		public abstract boolean isConsistentItem(Object item);

	}

	/**
	 * An interface to content providers for
	 * <code>FilterItemsSelectionDialog</code>.
	 */
	protected abstract class AbstractContentProvider {
		/**
		 * Adds the item to the content provider iff the filter matches the item.
		 * Otherwise does nothing.
		 *
		 * @param item        the item to add
		 * @param itemsFilter the filter
		 *
		 * @see FilteredItemsSelectionDialog.ItemsFilter#matchItem(Object)
		 */
		public abstract void add(Object item, ItemsFilter itemsFilter);
	}

	/**
	 * Collects filtered elements. Contains one synchronized, sorted set for
	 * collecting filtered elements. All collected elements are sorted using
	 * comparator. Comparator is returned by getElementComparator() method.
	 * Implementation of <code>ItemsFilter</code> is used to filter elements. The
	 * key function of filter used in to filtering is
	 * <code>matchElement(Object item)</code>.
	 * <p>
	 * The <code>ContentProvider</code> class also provides item filtering methods.
	 * The filtering has been moved from the standard TableView
	 * <code>getFilteredItems()</code> method to content provider, because
	 * <code>ILazyContentProvider</code> and virtual tables are used. This class is
	 * responsible for adding a separator below history items and marking each items
	 * as duplicate if its name repeats more than once on the filtered list.
	 */
	private class ContentProvider extends AbstractContentProvider
			implements IStructuredContentProvider, ILazyContentProvider {

		private SelectionHistory selectionHistory;

		/**
		 * Raw result of the searching (unsorted, unfiltered).
		 * <p>
		 * Standard object flow:
		 * {@code items -> lastSortedItems -> lastFilteredItems}
		 */
		private Set<Object> items;

		/**
		 * Items that are duplicates.
		 */
		private Set<Object> duplicates;

		/**
		 * List of <code>ViewerFilter</code>s to be used during filtering
		 */
		private List<Object> filters;

		/**
		 * Result of the last filtering.
		 * <p>
		 * Standard object flow:
		 * {@code items -> lastSortedItems -> lastFilteredItems}
		 */
		private List<Object> lastFilteredItems;

		/**
		 * Result of the last sorting.
		 * <p>
		 * Standard object flow:
		 * {@code items -> lastSortedItems -> lastFilteredItems}
		 */
		private List<Object> lastSortedItems;

		/**
		 * Used for <code>getFilteredItems()</code> method canceling (when the job that
		 * invoked the method was canceled).
		 * <p>
		 * Method canceling could be based (only) on monitor canceling unfortunately
		 * sometimes the method <code>getFilteredElements()</code> could be run with a
		 * null monitor, the <code>reset</code> flag have to be left intact.
		 */
		private boolean reset;

		/**
		 * Creates new instance of <code>ContentProvider</code>.
		 */
		public ContentProvider() {
			this.items = Collections.synchronizedSet(new HashSet<>(2048));
			this.duplicates = Collections.synchronizedSet(new HashSet<>(256));
			this.lastFilteredItems = new ArrayList<>();
			this.lastSortedItems = Collections.synchronizedList(new ArrayList<>(2048));
		}

		/**
		 * Sets selection history.
		 *
		 * @param selectionHistory The selectionHistory to set.
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
		 * Removes all content items and resets progress message.
		 */
		public void reset() {
			reset = true;
			this.items.clear();
			this.duplicates.clear();
			this.lastSortedItems.clear();
		}

		/**
		 * Stops reloading cache - <code>getFilteredItems()</code> method.
		 */
		public void stopReloadingCache() {
			reset = true;
		}

		/**
		 * Adds filtered item.
		 *
		 * @param item        the item to add.
		 * @param itemsFilter the filter to match
		 */
		@Override
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
		 * Add all history items to <code>contentProvider</code>.
		 *
		 * @param itemsFilter the filter to match
		 */
		public void addHistoryItems(ItemsFilter itemsFilter) {
			if (this.selectionHistory != null) {
				Object[] items = this.selectionHistory.getHistoryItems();
				for (Object item : items) {
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
		 */
		public void refresh() {
			scheduleRefresh();
		}

		/**
		 * Removes items from history and refreshes the view.
		 *
		 * @param item to remove
		 *
		 * @return removed item
		 */
		public Object removeHistoryElement(Object item) {
			if (this.selectionHistory != null)
				this.selectionHistory.remove(item);
			if (filter == null || filter.getPattern().isEmpty()) {
				items.remove(item);
				duplicates.remove(item);
				this.lastSortedItems.remove(item);
			}

			synchronized (lastSortedItems) {
				lastSortedItems.sort(getHistoryComparator());
			}
			return item;
		}

		/**
		 * Adds item to history and refresh view.
		 *
		 * @param item to add
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
				lastSortedItems.sort(getHistoryComparator());
			}
			this.refresh();
		}

		/**
		 * @param item the item to check
		 * @return <code>true</code> if given item is part of the history
		 */
		public boolean isHistoryElement(Object item) {
			if (this.selectionHistory != null) {
				return this.selectionHistory.contains(item);
			}
			return false;
		}

		/**
		 * Sets/unsets given item as duplicate.
		 *
		 * @param item        item to change
		 *
		 * @param isDuplicate duplicate flag
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
		 * Indicates whether given item is a duplicate.
		 *
		 * @param item item to check
		 * @return <code>true</code> if item is duplicate
		 */
		public boolean isDuplicateElement(Object item) {
			return duplicates.contains(item);
		}

		/**
		 * Load history from memento.
		 *
		 * @param memento memento from which the history will be retrieved
		 */
		public void loadHistory(IMemento memento) {
			if (this.selectionHistory != null) {
				this.selectionHistory.load(memento);
			}
		}

		/**
		 * Save history to memento.
		 *
		 * @param memento memento to which the history will be added
		 */
		public void saveHistory(IMemento memento) {
			if (this.selectionHistory != null) {
				this.selectionHistory.save(memento);
			}
		}

		/**
		 * Gets sorted items.
		 *
		 * @return sorted items
		 */
		private Object[] getSortedItems() {
			if (lastSortedItems.size() != items.size()) {
				synchronized (lastSortedItems) {
					lastSortedItems.clear();
					lastSortedItems.addAll(items);
					lastSortedItems.sort(getHistoryComparator());
				}
			}
			return lastSortedItems.toArray();
		}

		/**
		 * Remember result of filtering.
		 *
		 * @param itemsFilter the filter
		 */
		public void rememberResult(ItemsFilter itemsFilter) {
			List<Object> itemsList = Collections.synchronizedList(Arrays.asList(getSortedItems()));
			// synchronization
			if (itemsFilter == filter) {
				lastCompletedFilter = itemsFilter;
				lastCompletedResult = itemsList;
			}

		}

		@Override
		public Object[] getElements(Object inputElement) {
			return lastFilteredItems.toArray();
		}

		public int getNumberOfElements() {
			return lastFilteredItems.size();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void updateElement(int index) {

			FilteredItemsSelectionDialog.this.tableViewer
					.replace((lastFilteredItems.size() > index) ? lastFilteredItems.get(index) : null, index);

		}

		/**
		 * Main method responsible for getting the filtered items and checking for
		 * duplicates. It is based on the
		 * {@link FilteredItemsSelectionDialog.ContentProvider#getFilteredItems(Object, IProgressMonitor)}.
		 *
		 * @param checkDuplicates <code>true</code> if data concerning elements
		 *                        duplication should be computed - it takes much more
		 *                        time than standard filtering
		 *
		 * @param monitor         progress monitor
		 */
		public void reloadCache(boolean checkDuplicates, IProgressMonitor monitor) {
			reset = false;
			currentlyCompletingFilter = filter;

			// the work is divided into two actions of the same length
			int totalWork = checkDuplicates ? 200 : 100;

			SubMonitor subMonitor = SubMonitor.convert(monitor,
					WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob, totalWork);

			// the TableViewer's root (the input) is treated as parent

			lastFilteredItems = Arrays.asList(getFilteredItems(tableViewer.getInput(), subMonitor.split(100)));

			if (reset || subMonitor.isCanceled()) {
				return;
			}

			if (checkDuplicates) {
				checkDuplicates(subMonitor.split(100));
			}
		}

		private void checkDuplicates(IProgressMonitor monitor) {
			synchronized (lastFilteredItems) {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob_checkDuplicates,
						lastFilteredItems.size());
				HashMap<String, Object> helperMap = new HashMap<>();
				for (Object item : lastFilteredItems) {
					if (reset || subMonitor.isCanceled())
						return;

					if (!(item instanceof ItemsListSeparator)) {
						Object previousItem = helperMap.put(getElementName(item), item);
						if (previousItem != null) {
							setDuplicateElement(previousItem, true);
							setDuplicateElement(item, true);
						} else {
							setDuplicateElement(item, false);
						}
					}

					subMonitor.worked(1);
				}
				helperMap.clear();
			}
		}

		/**
		 * Returns an array of items filtered using the provided
		 * <code>ViewerFilter</code>s with a separator added.
		 *
		 * @param parent  the parent
		 * @param monitor progress monitor, can be <code>null</code>
		 * @return an array of filtered items
		 */
		protected Object[] getFilteredItems(Object parent, IProgressMonitor monitor) {
			int ticks = 100;
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}

			monitor.beginTask(WorkbenchMessages.FilteredItemsSelectionDialog_cacheRefreshJob_getFilteredElements,
					ticks);
			if (filters != null) {
				ticks /= (filters.size() + 2);
			} else {
				ticks /= 2;
			}

			// get already sorted array
			Object[] filteredElements = getSortedItems();

			monitor.worked(ticks);

			// filter the elements using provided ViewerFilters
			if (filters != null && filteredElements != null) {
				for (Object f : filters) {
					filteredElements = ((ViewerFilter) f).filter(tableViewer, parent, filteredElements);
					monitor.worked(ticks);
				}
			}

			if (filteredElements == null || monitor.isCanceled()) {
				monitor.done();
				return new Object[0];
			}

			ArrayList<Object> preparedElements = new ArrayList<>();
			int i = 0;
			boolean hasHistory = false;
			int reportEvery = filteredElements.length / ticks;

			for (; i < filteredElements.length; i++) {
				Object item = filteredElements[i];
				if (filter != null && filter.getPattern().equals(getElementName(item))) {
					if (isHistoryElement(item)) {
						preparedElements.add(0, item);
						hasHistory = true;
					} else {
						preparedElements.add(item);
					}
				} else {
					break;
				}

				if (reportEvery != 0 && ((i + 1) % reportEvery == 0)) {
					monitor.worked(1);
				}
			}

			if (filteredElements.length > i) {
				if (isHistoryElement(filteredElements[i])) {
					hasHistory = true;
				}
			}

			// add separators
			for (; i < filteredElements.length; i++) {
				Object item = filteredElements[i];
				if (hasHistory && !isHistoryElement(item)) {
					setSeparatorLabel(WorkbenchMessages.FilteredItemsSelectionDialog_separatorLabel);
					preparedElements.add(itemsListSeparator);
					hasHistory = false;
				}

				preparedElements.add(item);

				if (reportEvery != 0 && ((i + 1) % reportEvery == 0)) {
					monitor.worked(1);
				}
			}

			monitor.done();

			return preparedElements.toArray();
		}

		/**
		 * Adds a filter to this content provider. For an example usage of such filters
		 * look at the project <code>org.eclipse.ui.ide</code>, class
		 * <code>org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog.CustomWorkingSetFilter</code>.
		 *
		 *
		 * @param filter the filter to be added
		 */
		public void addFilter(ViewerFilter filter) {
			if (filters == null) {
				filters = new ArrayList<>();
			}
			filters.add(filter);
			// currently filters are only added when dialog is restored
			// if it is changed, refreshing the whole TableViewer should be
			// added
		}

	}

	/**
	 * DetailsContentViewer objects are wrappers for labels. DetailsContentViewer
	 * provides means to change label's image and text when the attached
	 * LabelProvider is updated.
	 */
	private class DetailsContentViewer extends ContentViewer {

		private CLabel label;

		/**
		 * Unfortunately, it was impossible to delegate displaying border to label. The
		 * <code>ViewForm</code> is used because <code>CLabel</code> displays shadow
		 * when border is present.
		 */
		private ViewForm viewForm;

		/**
		 * Constructs a new instance of this class given its parent and a style value
		 * describing its behavior and appearance.
		 *
		 * @param parent the parent component
		 * @param style  SWT style bits
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
		 * @param visible if the content viewer should be visible.
		 */
		public void setVisible(boolean visible) {
			viewForm.setVisible(visible);
			GridData gd = (GridData) viewForm.getLayoutData();
			gd.exclude = !visible;
			viewForm.getParent().layout();
		}

		@Override
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

		@Override
		protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
			if (event != null) {
				refresh(event.getElements());
			}
		}

		@Override
		public Control getControl() {
			return label;
		}

		@Override
		public ISelection getSelection() {
			// not supported
			return null;
		}

		@Override
		public void refresh() {
			Object input = this.getInput();
			if (input != null) {
				ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
				doRefresh(labelProvider.getText(input), labelProvider.getImage(input));
			} else {
				doRefresh(null, null);
			}
		}

		/**
		 * Sets the given text and image to the label.
		 *
		 * @param text  the new text or null
		 * @param image the new image
		 */
		private void doRefresh(String text, Image image) {
			if (text != null) {
				text = LegacyActionTools.escapeMnemonics(text);
			}
			label.setText(text);
			label.setImage(image);
		}

		@Override
		public void setSelection(ISelection selection, boolean reveal) {
			// not supported
		}

		/**
		 * Refreshes the label if currently chosen element is on the list.
		 *
		 * @param objs list of changed object
		 */
		private void refresh(Object[] objs) {
			if (objs == null || getInput() == null) {
				return;
			}
			Object input = getInput();
			for (Object obj : objs) {
				if (obj.equals(input)) {
					refresh();
					break;
				}
			}
		}
	}

	/**
	 * Compares items according to the history.
	 */
	private class HistoryComparator implements Comparator<Object> {
		final String filterPattern;
		final Comparator<Object> itemsComparator;

		HistoryComparator() {
			itemsComparator = getItemsComparator();
			if (currentlyCompletingFilter != null) {
				filterPattern = currentlyCompletingFilter.getPattern();
			} else {
				filterPattern = null;
			}
		}

		@Override
		public int compare(Object o1, Object o2) {
			// find perfect matches
			if (filterPattern != null) {
				// See if any are exact matches
				boolean m1 = filterPattern.equals(getElementName(o1));
				boolean m2 = filterPattern.equals(getElementName(o2));
				if (!m1 || !m2) {
					if (m1 && !m2) {
						return -1;
					}
					if (m2 && !m1) {
						return 1;
					}
				}
			}

			boolean h1 = isHistoryElement(o1);
			boolean h2 = isHistoryElement(o2);
			if (h1 == h2) {
				return itemsComparator.compare(o1, o2);
			}

			if (h1) {
				return -2;
			}
			if (h2) {
				return +2;
			}

			return 0;
		}

	}

	/**
	 * Get the control where the search pattern is entered. Any filtering should be
	 * done using an {@link ItemsFilter}. This control should only be accessed for
	 * listeners that wish to handle events that do not affect filtering such as
	 * custom traversal.
	 *
	 * @return Control or <code>null</code> if the pattern control has not been
	 *         created.
	 */
	public Control getPatternControl() {
		return pattern;
	}

	/**
	 * Get the text from pattern control.
	 *
	 * @return text from pattern control or empty string if pattern control is null
	 * @since 3.136
	 */
	protected String getPatternText() {
		if (pattern == null) {
			return null;
		}
		return pattern.getText();
	}

	/**
	 * A <code>LabelProvider</code> for (the table of) types.
	 */
	private class TypeItemLabelProvider extends LabelProvider implements ILabelDecorator, IStyledLabelProvider {

		private BoldStylerProvider boldStylerProvider;

		@Override
		public void dispose() {
			super.dispose();

			if (boldStylerProvider != null) {
				boldStylerProvider.dispose();
				boldStylerProvider = null;
			}
		}

		@Override
		public Image decorateImage(Image image, Object element) {
			return image;
		}

		@Override
		public String decorateText(String text, Object element) {
			return text;
		}

		@Override
		public StyledString getStyledText(Object element) {
			String text = getText(element);
			String namePattern = filter != null ? filter.getPattern() : null;

			return getStyledStringHighlighter().highlight(text, namePattern, getBoldStylerProvider().getBoldStyler());
		}

		private BoldStylerProvider getBoldStylerProvider() {
			if (boldStylerProvider == null) {
				boldStylerProvider = new BoldStylerProvider(getDialogArea().getFont());
			}
			return boldStylerProvider;
		}

	}

	/**
	 * @return Returns the styledStringHighlighter.
	 * @since 3.115
	 */
	public IStyledStringHighlighter getStyledStringHighlighter() {
		if (styledStringHighlighter == null) {
			styledStringHighlighter = new StyledStringHighlighter();
		}

		return styledStringHighlighter;
	}

	/**
	 * @param styledStringHighlighter The styledStringHighlighter to set.
	 * @since 3.115
	 */
	public void setStyledStringHighlighter(IStyledStringHighlighter styledStringHighlighter) {
		this.styledStringHighlighter = styledStringHighlighter;
	}

}
