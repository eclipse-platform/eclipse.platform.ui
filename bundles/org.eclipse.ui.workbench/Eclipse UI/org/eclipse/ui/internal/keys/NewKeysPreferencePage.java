/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 186522 - [KeyBindings] New Keys preference page does not resort by binding with conflicts
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keys.model.BindingElement;
import org.eclipse.ui.internal.keys.model.BindingModel;
import org.eclipse.ui.internal.keys.model.CommonModel;
import org.eclipse.ui.internal.keys.model.ConflictModel;
import org.eclipse.ui.internal.keys.model.ContextElement;
import org.eclipse.ui.internal.keys.model.ContextModel;
import org.eclipse.ui.internal.keys.model.KeyController;
import org.eclipse.ui.internal.keys.model.ModelElement;
import org.eclipse.ui.internal.keys.model.SchemeElement;
import org.eclipse.ui.internal.keys.model.SchemeModel;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;

/**
 * <p>
 * A preference page that is capable of displaying and editing the bindings
 * between commands and user input events. These are typically things like
 * keyboard shortcuts.
 * </p>
 * <p>
 * This preference page has four general types of methods. Create methods are
 * called when the page is first made visible. They are responsible for creating
 * all of the widgets, and laying them out within the preference page. Fill
 * methods populate the contents of the widgets that contain collections of data
 * from which items can be selected. The select methods respond to selection
 * events from the user, such as a button press or a table selection. The update
 * methods update the contents of various widgets based on the current state of
 * the user interface. For example, the command name label will always try to
 * match the current select in the binding table.
 * </p>
 * <p>
 * Updated in 3.4 to work with a model backed by the real KeyBinding and
 * ParameterizedCommand objects.
 * </p>
 * 
 * @since 3.2
 */
public class NewKeysPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	private static final String TRACING_COMPONENT = "NewKeysPref"; //$NON-NLS-1$

	public final static String TAG_DIALOG_SECTION = "org.eclipse.ui.preferences.keysPreferencePage"; //$NON-NLS-1$

	private static final String TAG_FILTER_ACTION_SETS = "actionSetFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_INTERNAL = "internalFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_UNCAT = "uncategorizedFilter"; //$NON-NLS-1$

	/**
	 * The number of items to show in the bindings table tree.
	 */
	private static final int ITEMS_TO_SHOW = 7;

	private static final int COMMAND_NAME_COLUMN = 0;
	private static final int KEY_SEQUENCE_COLUMN = 1;
	private static final int CONTEXT_COLUMN = 2;
	private static final int CATEGORY_COLUMN = 3;
	private static final int USER_DELTA_COLUMN = 4;
	private static int NUM_OF_COLUMNS = USER_DELTA_COLUMN + 1;

	private ComboViewer fSchemeCombo;

	private CategoryPatternFilter fPatternFilter;

	private CategoryFilterTree fFilteredTree;

	private boolean fFilterActionSetContexts = true;

	private boolean fFilterInternalContexts = true;

	private KeyController keyController;

	private Category fDefaultCategory;

	private Label commandNameValueLabel;

	private Text fBindingText;
	
	private Text fDescriptionText;

	private ComboViewer fWhenCombo;

	private IBindingService fBindingService;

	private KeySequenceText fKeySequenceText;

	private ListViewer conflictViewer;

	private ICommandImageService commandImageService;

	private ICommandService commandService;

	/**
	 * A FilteredTree that provides a combo which is used to organize and
	 * display elements in the tree according to the selected criteria.
	 * 
	 */
	protected class CategoryFilterTree extends FilteredTree {

		private CategoryPatternFilter filter;

		/**
		 * Constructor for PatternFilteredTree.
		 * 
		 * @param parent
		 * @param treeStyle
		 * @param filter
		 */
		protected CategoryFilterTree(Composite parent, int treeStyle,
				CategoryPatternFilter filter) {
			super(parent, treeStyle, filter);
			this.filter = filter;
		}

		public void filterCategories(boolean b) {
			filter.filterCategories(b);
			textChanged();
		}

		public boolean isFilteringCategories() {
			return filter.isFilteringCategories();
		}
	}

	private final class BindingModelComparator extends ViewerComparator {
		private LinkedList sortColumns = new LinkedList();
		private boolean ascending = true;

		public BindingModelComparator() {
			for (int i = 0; i < NUM_OF_COLUMNS; i++) {
				sortColumns.add(new Integer(i));
			}
		}

		public int getSortColumn() {
			return ((Integer) sortColumns.getFirst()).intValue();
		}

		public void setSortColumn(int column) {
			if (column == getSortColumn()) {
				return;
			}
			Integer sortColumn = new Integer(column);
			sortColumns.remove(sortColumn);
			sortColumns.addFirst(sortColumn);
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

		public final int compare(final Viewer viewer, final Object a,
				final Object b) {
			int result = 0;
			Iterator i = sortColumns.iterator();
			while (i.hasNext() && result == 0) {
				int column = ((Integer) i.next()).intValue();
				result = compareColumn(viewer, a, b, column);
			}
			return ascending ? result : (-1) * result;
		}

		private int compareColumn(final Viewer viewer, final Object a,
				final Object b, final int columnNumber) {
			if (columnNumber == USER_DELTA_COLUMN) {
				return sortUser(a, b);
			}
			IBaseLabelProvider baseLabel = ((TreeViewer) viewer)
					.getLabelProvider();
			if (baseLabel instanceof ITableLabelProvider) {
				ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
				String e1p = tableProvider.getColumnText(a, columnNumber);
				String e2p = tableProvider.getColumnText(b, columnNumber);
				if (e1p != null && e2p != null) {
					return getComparator().compare(e1p, e2p);
				}
			}
			return 0;
		}

		private int sortUser(final Object a, final Object b) {
			int typeA = ((BindingElement) a).getUserDelta().intValue();
			int typeB = ((BindingElement) b).getUserDelta().intValue();
			int result = typeA - typeB;
			return result;
		}

	}

	private final class ResortColumn extends SelectionAdapter {
		private final BindingModelComparator comparator;
		private final TreeColumn treeColumn;
		private final TreeViewer viewer;
		private final int column;

		private ResortColumn(BindingModelComparator comparator,
				TreeColumn treeColumn, TreeViewer viewer, int column) {
			this.comparator = comparator;
			this.treeColumn = treeColumn;
			this.viewer = viewer;
			this.column = column;
		}

		public void widgetSelected(SelectionEvent e) {
			if (comparator.getSortColumn() == column) {
				comparator.setAscending(!comparator.isAscending());
				viewer.getTree().setSortDirection(
						comparator.isAscending() ? SWT.UP : SWT.DOWN);
			} else {
				viewer.getTree().setSortColumn(treeColumn);
				comparator.setSortColumn(column);
			}
			try {
				viewer.getTree().setRedraw(false);
				viewer.refresh();
			} finally {
				viewer.getTree().setRedraw(true);
			}
		}
	}

	private static class ListLabelProvider extends LabelProvider {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return ((ModelElement) element).getName();
		}
	}

	private class BindingElementLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		/**
		 * A resource manager for this preference page.
		 */
		private final LocalResourceManager localResourceManager = new LocalResourceManager(
				JFaceResources.getResources());

		public final void dispose() {
			super.dispose();
			localResourceManager.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			String rc = getColumnText(element, 0);
			if (rc == null) {
				super.getText(element);
			}
			StringBuffer buf = new StringBuffer(rc);
			for (int i = 1; i < USER_DELTA_COLUMN; i++) {
				String text = getColumnText(element, i);
				if (text != null) {
					buf.append(' ');
					buf.append(text);
				}
			}
			return buf.toString();
		}

		public String getColumnText(Object element, int index) {
			BindingElement bindingElement = ((BindingElement) element);
			switch (index) {
			case COMMAND_NAME_COLUMN: // name
				return bindingElement.getName();
			case KEY_SEQUENCE_COLUMN: // keys
				TriggerSequence seq = bindingElement.getTrigger();
				return seq == null ? Util.ZERO_LENGTH_STRING : seq.format();
			case CONTEXT_COLUMN: // when
				ModelElement context = bindingElement.getContext();
				return context == null ? Util.ZERO_LENGTH_STRING : context
						.getName();
			case CATEGORY_COLUMN: // category
				return bindingElement.getCategory();
			case USER_DELTA_COLUMN: // user
				if (bindingElement.getUserDelta().intValue() == Binding.USER) {
					if (bindingElement.getConflict().equals(Boolean.TRUE)) {
						return "CU"; //$NON-NLS-1$
					}
					return " U"; //$NON-NLS-1$
				}
				if (bindingElement.getConflict().equals(Boolean.TRUE)) {
					return "C "; //$NON-NLS-1$
				}
				return "  "; //$NON-NLS-1$
			}
			return null;
		}

		public Image getColumnImage(Object element, int index) {
			BindingElement be = (BindingElement) element;
			switch (index) {
			case COMMAND_NAME_COLUMN:
				final String commandId = be.getId();
				final ImageDescriptor imageDescriptor = commandImageService
						.getImageDescriptor(commandId);
				if (imageDescriptor == null) {
					return null;
				}
				try {
					return localResourceManager.createImage(imageDescriptor);
				} catch (final DeviceResourceException e) {
					final String message = "Problem retrieving image for a command '" //$NON-NLS-1$
							+ commandId + '\'';
					final IStatus status = new Status(IStatus.ERROR,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
					WorkbenchPlugin.log(message, status);
				}
				return null;

//			case USER_DELTA_COLUMN:
//				if (be.getUserDelta().intValue() == Binding.USER) {
//					if (be.getConflict().equals(Boolean.TRUE)) {
//						return ImageFactory.getImage("plus"); //$NON-NLS-1$
//					}
//					return ImageFactory.getImage("change"); //$NON-NLS-1$
//				}
//				if (be.getConflict().equals(Boolean.TRUE)) {
//					return ImageFactory.getImage("minus"); //$NON-NLS-1$
//				}
//				return ImageFactory.getImage("blank"); //$NON-NLS-1$
			}

			return null;
		}
	}

	class ModelContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof BindingModel) {
				return ((BindingModel) parentElement).getBindings().toArray();
			}
			if (parentElement instanceof ContextModel) {
				return ((ContextModel) parentElement).getContexts().toArray();
			}
			if (parentElement instanceof SchemeModel) {
				return ((SchemeModel) parentElement).getSchemes().toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return ((ModelElement) element).getParent();
		}

		public boolean hasChildren(Object element) {
			return (element instanceof BindingModel)
					|| (element instanceof ContextModel)
					|| (element instanceof SchemeModel);
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.KEYS_PREFERENCE_PAGE);
		final Composite page = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		page.setLayout(layout);

		IDialogSettings settings = getDialogSettings();

		fPatternFilter = new CategoryPatternFilter(true, commandService
				.getCategory(null));
		if (settings.get(TAG_FILTER_UNCAT) != null) {
			fPatternFilter.filterCategories(settings
					.getBoolean(TAG_FILTER_UNCAT));
		}

		createSchemeControls(page);
		createTree(page);
		createTreeControls(page);
		createDataControls(page);
		createButtonBar(page);

		fill();

		applyDialogFont(page);

		// we want the description text control to span four lines, but because
		// we need the dialog's font for this information, we have to set it here
		// after the dialog font has been applied
		GC gc = new GC(fDescriptionText);
		gc.setFont(fDescriptionText.getFont());
		FontMetrics metrics = gc.getFontMetrics();
		gc.dispose();
		int height = metrics.getHeight() * 4;
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.heightHint = height;
		fDescriptionText.setLayoutData(gridData);
		
		return page;
	}

	/**
	 * Creates the button bar across the bottom of the preference page. This
	 * button bar contains the "Advanced..." button.
	 * 
	 * @param parent
	 *            The composite in which the button bar should be placed; never
	 *            <code>null</code>.
	 * @return The button bar composite; never <code>null</code>.
	 */
	private final Control createButtonBar(final Composite parent) {
		GridLayout layout;
		GridData gridData;
		int widthHint;

		// Create the composite to house the button bar.
		final Composite buttonBar = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		buttonBar.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		buttonBar.setLayoutData(gridData);

		// Advanced button.
		final Button filtersButton = new Button(buttonBar, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		filtersButton.setText(NewKeysPreferenceMessages.FiltersButton_Text);
		gridData.widthHint = Math.max(widthHint, filtersButton.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		filtersButton.setLayoutData(gridData);
		filtersButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				KeysPreferenceFiltersDialog dialog = new KeysPreferenceFiltersDialog(
						getShell());
				dialog.setFilterActionSet(fFilterActionSetContexts);
				dialog.setFilterInternal(fFilterInternalContexts);
				dialog.setFilterUncategorized(fFilteredTree
						.isFilteringCategories());
				if (dialog.open() == Window.OK) {
					fFilterActionSetContexts = dialog.getFilterActionSet();
					fFilterInternalContexts = dialog.getFilterInternal();
					fFilteredTree.filterCategories(dialog
							.getFilterUncategorized());

					// Apply context filters
					keyController.filterContexts(fFilterActionSetContexts,
							fFilterInternalContexts);

					ISelection currentContextSelection = fWhenCombo
							.getSelection();
					fWhenCombo.setInput(keyController.getContextModel());
					fWhenCombo.setSelection(currentContextSelection);
				}
			}
		});

		// Export bindings to CSV
		final Button exportButton = new Button(buttonBar, SWT.PUSH);
		// gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		exportButton.setText(NewKeysPreferenceMessages.ExportButton_Text);
		gridData.widthHint = Math.max(widthHint, exportButton.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		exportButton.setLayoutData(gridData);
		exportButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				keyController.exportCSV(((Button) e.getSource()).getShell());
			}

		});

		return buttonBar;
	}

	private final void createDataControls(final Composite parent) {
		GridLayout layout;
		GridData gridData;

		// Creates the data area.
		final Composite dataArea = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		dataArea.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		dataArea.setLayoutData(gridData);

		// LEFT DATA AREA
		// Creates the left data area.
		final Composite leftDataArea = new Composite(dataArea, SWT.NONE);
		layout = new GridLayout(3, false);
		leftDataArea.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.TOP;
		gridData.horizontalAlignment = SWT.FILL;
		leftDataArea.setLayoutData(gridData);

		// The command name label.
		final Label commandNameLabel = new Label(leftDataArea, SWT.NONE);
		commandNameLabel
				.setText(NewKeysPreferenceMessages.CommandNameLabel_Text);

		// The current command name.
		commandNameValueLabel = new Label(leftDataArea, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		commandNameValueLabel.setLayoutData(gridData);
		
		final Label commandDescriptionlabel = new Label(leftDataArea, SWT.LEAD);
		commandDescriptionlabel.setText(NewKeysPreferenceMessages.CommandDescriptionLabel_Text);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.BEGINNING;
		commandDescriptionlabel.setLayoutData(gridData);
		
		fDescriptionText = new Text(leftDataArea, SWT.MULTI | SWT.WRAP
				| SWT.BORDER | SWT.READ_ONLY);

		// The binding label.
		final Label bindingLabel = new Label(leftDataArea, SWT.NONE);
		bindingLabel.setText(NewKeysPreferenceMessages.BindingLabel_Text);

		// The key sequence entry widget.
		fBindingText = new Text(leftDataArea, SWT.BORDER);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = 200;
		fBindingText.setLayoutData(gridData);

		fBindingText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				fBindingService.setKeyFilterEnabled(false);
			}

			public void focusLost(FocusEvent e) {
				fBindingService.setKeyFilterEnabled(true);
			}
		});
		fBindingText.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (!fBindingService.isKeyFilterEnabled()) {
					fBindingService.setKeyFilterEnabled(true);
				}
			}
		});

		fKeySequenceText = new KeySequenceText(fBindingText);
		fKeySequenceText.setKeyStrokeLimit(4);
		fKeySequenceText
				.addPropertyChangeListener(new IPropertyChangeListener() {
					public final void propertyChange(
							final PropertyChangeEvent event) {
						if (!event.getOldValue().equals(event.getNewValue())) {
							final KeySequence keySequence = fKeySequenceText
									.getKeySequence();
							if (!keySequence.isComplete()) {
								return;
							}

							BindingElement activeBinding = (BindingElement) keyController
									.getBindingModel().getSelectedElement();
							if (activeBinding != null) {
								activeBinding.setTrigger(keySequence);
							}
							fBindingText.setSelection(fBindingText
									.getTextLimit());
						}
					}
				});

		// Button for adding trapped key strokes
		final Button addKeyButton = new Button(leftDataArea, SWT.LEFT
				| SWT.ARROW);
		addKeyButton
				.setToolTipText(NewKeysPreferenceMessages.AddKeyButton_ToolTipText);
		gridData = new GridData();
		gridData.heightHint = fSchemeCombo.getCombo().getTextHeight();
		addKeyButton.setLayoutData(gridData);

		// Arrow buttons aren't normally added to the tab list. Let's fix that.
		final Control[] tabStops = dataArea.getTabList();
		final ArrayList newTabStops = new ArrayList();
		for (int i = 0; i < tabStops.length; i++) {
			Control tabStop = tabStops[i];
			newTabStops.add(tabStop);
			if (fBindingText.equals(tabStop)) {
				newTabStops.add(addKeyButton);
			}
		}
		final Control[] newTabStopArray = (Control[]) newTabStops
				.toArray(new Control[newTabStops.size()]);
		dataArea.setTabList(newTabStopArray);

		// Construct the menu to attach to the above button.
		final Menu addKeyMenu = new Menu(addKeyButton);
		final Iterator trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
		while (trappedKeyItr.hasNext()) {
			final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
			final MenuItem menuItem = new MenuItem(addKeyMenu, SWT.PUSH);
			menuItem.setText(trappedKey.format());
			menuItem.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					fKeySequenceText.insert(trappedKey);
					fBindingText.setFocus();
					fBindingText.setSelection(fBindingText.getTextLimit());
				}
			});
		}
		addKeyButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent selectionEvent) {
				Point buttonLocation = addKeyButton.getLocation();
				buttonLocation = dataArea.toDisplay(buttonLocation.x,
						buttonLocation.y);
				Point buttonSize = addKeyButton.getSize();
				addKeyMenu.setLocation(buttonLocation.x, buttonLocation.y
						+ buttonSize.y);
				addKeyMenu.setVisible(true);
			}
		});

		// The when label.
		final Label whenLabel = new Label(leftDataArea, SWT.NONE);
		whenLabel.setText(NewKeysPreferenceMessages.WhenLabel_Text);

		// The when combo.
		fWhenCombo = new ComboViewer(leftDataArea);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		ViewerComparator comparator = new ViewerComparator();
		fWhenCombo.setComparator(comparator);
		fWhenCombo.getCombo().setVisibleItemCount(ITEMS_TO_SHOW);
		fWhenCombo.getCombo().setLayoutData(gridData);
		fWhenCombo.setContentProvider(new ModelContentProvider());
		fWhenCombo.setLabelProvider(new ListLabelProvider());
		fWhenCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public final void selectionChanged(final SelectionChangedEvent event) {
				ContextElement context = (ContextElement) ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				keyController.getContextModel().setSelectedElement(context);
			}
		});
		IPropertyChangeListener whenListener = new IPropertyChangeListener() {

			// Sets the combo selection when a new keybinding is selected?
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == keyController.getContextModel()
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event
								.getProperty())) {
					Object newVal = event.getNewValue();
					StructuredSelection structuredSelection = newVal == null ? null
							: new StructuredSelection(newVal);
					fWhenCombo.setSelection(structuredSelection, true);
				}
			}
		};
		keyController.addPropertyChangeListener(whenListener);

		// RIGHT DATA AREA
		// Creates the right data area.
		final Composite rightDataArea = new Composite(dataArea, SWT.NONE);
		layout = new GridLayout(1, false);
		rightDataArea.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		rightDataArea.setLayoutData(gridData);

		// The description label.
		final Label descriptionLabel = new Label(rightDataArea, SWT.NONE);
		descriptionLabel.setText(NewKeysPreferenceMessages.ConflictsLabel_Text);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		descriptionLabel.setLayoutData(gridData);

		conflictViewer = new ListViewer(rightDataArea, SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalIndent = 10;
		conflictViewer.getControl().setLayoutData(gridData);
		conflictViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection) {
					return ((Collection) inputElement).toArray();
				}
				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		conflictViewer.setLabelProvider(new BindingElementLabelProvider());
		conflictViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					// When the conflict viewer's selection changes, update the
					// model's current selection
					public void selectionChanged(SelectionChangedEvent event) {
						ModelElement binding = (ModelElement) ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						BindingModel bindingModel = keyController
								.getBindingModel();
						if (binding != null
								&& binding != bindingModel.getSelectedElement()) {
							StructuredSelection selection = new StructuredSelection(
									binding);

							bindingModel.setSelectedElement(binding);
							conflictViewer.setSelection(selection);

							boolean selectionVisible = false;
							TreeItem[] items = fFilteredTree.getViewer()
									.getTree().getItems();
							for (int i = 0; i < items.length; i++) {
								if (items[i].getData().equals(binding)) {
									selectionVisible = true;
									break;
								}
							}

							if (!selectionVisible) {
								fFilteredTree.getFilterControl().setText(""); //$NON-NLS-1$
								fFilteredTree.getViewer().refresh();
								bindingModel.setSelectedElement(binding);
								conflictViewer.setSelection(selection);
							}
						}
					}
				});

		IPropertyChangeListener conflictsListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == keyController.getConflictModel()
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event
								.getProperty())) {
					if (keyController.getConflictModel().getConflicts() != null) {
						Object newVal = event.getNewValue();
						StructuredSelection structuredSelection = newVal == null ? null
								: new StructuredSelection(newVal);
						conflictViewer.setSelection(structuredSelection, true);
					}
				} else if (ConflictModel.PROP_CONFLICTS.equals(event
						.getProperty())) {
					conflictViewer.setInput(event.getNewValue());
				} else if (ConflictModel.PROP_CONFLICTS_ADD.equals(event
						.getProperty())) {
					conflictViewer.add(event.getNewValue());
				} else if (ConflictModel.PROP_CONFLICTS_REMOVE.equals(event
						.getProperty())) {
					conflictViewer.remove(event.getNewValue());
				}
			}
		};
		keyController.addPropertyChangeListener(conflictsListener);

		IPropertyChangeListener dataUpdateListener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				BindingElement bindingElement = null;
				boolean weCare = false;
				if (event.getSource() == keyController.getBindingModel()
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event
								.getProperty())) {
					bindingElement = (BindingElement) event.getNewValue();
					weCare = true;
				} else if (event.getSource() == keyController.getBindingModel()
						.getSelectedElement()
						&& ModelElement.PROP_MODEL_OBJECT.equals(event
								.getProperty())) {
					bindingElement = (BindingElement) event.getSource();
					weCare = true;
				}
				if (bindingElement == null && weCare) {
					commandNameValueLabel.setText(""); //$NON-NLS-1$
					fDescriptionText.setText(""); //$NON-NLS-1$
					fBindingText.setText(""); //$NON-NLS-1$
					fWhenCombo.setSelection(null);
					fWhenCombo.getCombo().setVisible(false);
					whenLabel.setVisible(false);
				} else if (bindingElement != null) {
					commandNameValueLabel.setText(bindingElement.getName());
					String desc = bindingElement.getDescription();
					fDescriptionText.setText(desc==null?"":desc); //$NON-NLS-1$
					KeySequence trigger = (KeySequence) bindingElement
							.getTrigger();
					fKeySequenceText.setKeySequence(trigger);
					Object context = bindingElement.getContext();
					fWhenCombo.setSelection(context == null ? null
							: new StructuredSelection(context));
					fWhenCombo.getCombo().setVisible(context != null);
					whenLabel.setVisible(context != null);
				}
			}
		};
		keyController.addPropertyChangeListener(dataUpdateListener);

	}

	private void createTree(Composite parent) {
		fPatternFilter = new CategoryPatternFilter(true, fDefaultCategory);
		fPatternFilter.filterCategories(true);

		GridData gridData;

		fFilteredTree = new CategoryFilterTree(parent, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.BORDER, fPatternFilter);
		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		fFilteredTree.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		fFilteredTree.setLayoutData(gridData);

		final TreeViewer viewer = fFilteredTree.getViewer();
		// Make sure the filtered tree has a height of ITEMS_TO_SHOW
		final Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		final Object layoutData = tree.getLayoutData();
		if (layoutData instanceof GridData) {
			gridData = (GridData) layoutData;
			final int itemHeight = tree.getItemHeight();
			if (itemHeight > 1) {
				gridData.heightHint = ITEMS_TO_SHOW * itemHeight;
			}
		}

		BindingModelComparator comparator = new BindingModelComparator();
		viewer.setComparator(comparator);

		final TreeColumn commandNameColumn = new TreeColumn(tree, SWT.LEFT,
				COMMAND_NAME_COLUMN);
		commandNameColumn
				.setText(NewKeysPreferenceMessages.CommandNameColumn_Text);
		tree.setSortColumn(commandNameColumn);
		tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
		commandNameColumn.addSelectionListener(new ResortColumn(comparator,
				commandNameColumn, viewer, COMMAND_NAME_COLUMN));

		final TreeColumn triggerSequenceColumn = new TreeColumn(tree, SWT.LEFT,
				KEY_SEQUENCE_COLUMN);
		triggerSequenceColumn
				.setText(NewKeysPreferenceMessages.TriggerSequenceColumn_Text);
		triggerSequenceColumn.addSelectionListener(new ResortColumn(comparator,
				triggerSequenceColumn, viewer, KEY_SEQUENCE_COLUMN));

		final TreeColumn whenColumn = new TreeColumn(tree, SWT.LEFT,
				CONTEXT_COLUMN);
		whenColumn.setText(NewKeysPreferenceMessages.WhenColumn_Text);
		whenColumn.addSelectionListener(new ResortColumn(comparator,
				whenColumn, viewer, CONTEXT_COLUMN));

		final TreeColumn categoryColumn = new TreeColumn(tree, SWT.LEFT,
				CATEGORY_COLUMN);
		categoryColumn.setText(NewKeysPreferenceMessages.CategoryColumn_Text);
		categoryColumn.addSelectionListener(new ResortColumn(comparator,
				categoryColumn, viewer, CATEGORY_COLUMN));

		final TreeColumn userMarker = new TreeColumn(tree, SWT.LEFT,
				USER_DELTA_COLUMN);
		userMarker.setText(NewKeysPreferenceMessages.UserColumn_Text);
		userMarker.addSelectionListener(new ResortColumn(comparator,
				userMarker, viewer, USER_DELTA_COLUMN));

		viewer.setContentProvider(new ModelContentProvider());
		viewer.setLabelProvider(new BindingElementLabelProvider());

		fFilteredTree.getPatternFilter().setIncludeLeadingWildcard(true);
		final TreeColumn[] columns = viewer.getTree().getColumns();

		columns[COMMAND_NAME_COLUMN].setWidth(240);
		columns[KEY_SEQUENCE_COLUMN].setWidth(130);
		columns[CONTEXT_COLUMN].setWidth(130);
		columns[CATEGORY_COLUMN].setWidth(130);
		columns[USER_DELTA_COLUMN].setWidth(50);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			// When the viewer changes selection, update the model's current
			// selection
			public void selectionChanged(SelectionChangedEvent event) {
				ModelElement binding = (ModelElement) ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				keyController.getBindingModel().setSelectedElement(binding);
			}
		});

		IPropertyChangeListener treeUpdateListener = new IPropertyChangeListener() {

			// When the model changes a property, update the viewer
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == keyController.getBindingModel()
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event
								.getProperty())) {
					Object newVal = event.getNewValue();
					StructuredSelection structuredSelection = newVal == null ? null
							: new StructuredSelection(newVal);
					viewer.setSelection(structuredSelection, true);
				} else if (event.getSource() instanceof BindingElement
						&& ModelElement.PROP_MODEL_OBJECT.equals(event
								.getProperty())) {
					viewer.update(event.getSource(), null);
				} else if (BindingElement.PROP_CONFLICT.equals(event
						.getProperty())) {
					viewer.update(event.getSource(), null);
				} else if (BindingModel.PROP_BINDINGS.equals(event
						.getProperty())) {
					viewer.refresh();
				} else if (BindingModel.PROP_BINDING_ADD.equals(event
						.getProperty())) {
					viewer.add(keyController.getBindingModel(), event
							.getNewValue());
				} else if (BindingModel.PROP_BINDING_REMOVE.equals(event
						.getProperty())) {
					viewer.remove(event.getNewValue());
				} else if (BindingModel.PROP_BINDING_FILTER.equals(event
						.getProperty())) {
					viewer.refresh();
				}
			}
		};
		keyController.addPropertyChangeListener(treeUpdateListener);
		// as far as I got
	}

	private final Control createTreeControls(final Composite parent) {
		GridLayout layout;
		GridData gridData;
		int widthHint;

		// Creates controls related to the tree.
		final Composite treeControls = new Composite(parent, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginWidth = 0;
		treeControls.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		treeControls.setLayoutData(gridData);

		final Button addBindingButton = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		addBindingButton
				.setText(NewKeysPreferenceMessages.AddBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, addBindingButton.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		addBindingButton.setLayoutData(gridData);
		addBindingButton.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent event) {
				keyController.getBindingModel().copy();
			}
		});

		final Button removeBindingButton = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		removeBindingButton
				.setText(NewKeysPreferenceMessages.RemoveBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, removeBindingButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		removeBindingButton.setLayoutData(gridData);
		removeBindingButton.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent event) {
				keyController.getBindingModel().remove();
			}
		});

		final Button restore = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		restore.setText(NewKeysPreferenceMessages.RestoreBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, restore.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		restore.setLayoutData(gridData);
		restore.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent event) {
				try {
					fFilteredTree.setRedraw(false);
					BindingModel bindingModel = keyController.getBindingModel();
					bindingModel
							.restoreBinding(keyController.getContextModel());
				} finally {
					fFilteredTree.setRedraw(true);
				}
			}
		});

		return treeControls;
	}

	/**
	 * 
	 */
	private void fill() {
		fSchemeCombo.setInput(keyController.getSchemeModel());
		fSchemeCombo.setSelection(new StructuredSelection(keyController
				.getSchemeModel().getSelectedElement()));

		// Apply context filters
		keyController.filterContexts(fFilterActionSetContexts,
				fFilterInternalContexts);
		fWhenCombo.setInput(keyController.getContextModel());

		fFilteredTree.filterCategories(fPatternFilter.isFilteringCategories());
		fFilteredTree.getViewer().setInput(keyController.getBindingModel());
	}

	private void createSchemeControls(Composite parent) {
		final Composite schemeControls = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		schemeControls.setLayout(layout);

		final Label schemeLabel = new Label(schemeControls, SWT.NONE);
		schemeLabel.setText(NewKeysPreferenceMessages.SchemeLabel_Text);

		fSchemeCombo = new ComboViewer(schemeControls);
		fSchemeCombo.setLabelProvider(new ListLabelProvider());
		fSchemeCombo.setContentProvider(new ModelContentProvider());
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		gridData.horizontalAlignment = SWT.FILL;
		fSchemeCombo.getCombo().setLayoutData(gridData);
		fSchemeCombo
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public final void selectionChanged(
							final SelectionChangedEvent event) {
						BusyIndicator.showWhile(fFilteredTree.getViewer()
								.getTree().getDisplay(), new Runnable() {
							public void run() {
								SchemeElement scheme = (SchemeElement) ((IStructuredSelection) event
										.getSelection()).getFirstElement();
								keyController.getSchemeModel()
										.setSelectedElement(scheme);
							}
						});
					}
				});
		IPropertyChangeListener listener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == keyController.getSchemeModel()
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event
								.getProperty())) {
					Object newVal = event.getNewValue();
					StructuredSelection structuredSelection = newVal == null ? null
							: new StructuredSelection(newVal);
					fSchemeCombo.setSelection(structuredSelection, true);
				}
			}
		};

		keyController.addPropertyChangeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		keyController = new KeyController();
		keyController.init(workbench);

		commandService = (ICommandService) workbench
				.getService(ICommandService.class);
		fDefaultCategory = commandService.getCategory(null);
		fBindingService = (IBindingService) workbench
				.getService(IBindingService.class);

		commandImageService = (ICommandImageService) workbench
				.getService(ICommandImageService.class);
	}

	public void applyData(Object data) {
		if (data instanceof ModelElement) {
			keyController.getBindingModel().setSelectedElement(
					(ModelElement) data);
		}
		if (data instanceof Binding && fFilteredTree != null) {
			BindingElement be = (BindingElement) keyController
					.getBindingModel().getBindingToElement().get(data);
			fFilteredTree.getViewer().setSelection(new StructuredSelection(be),
					true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		keyController.saveBindings(fBindingService);
		saveState(getDialogSettings());
		return super.performOk();
	}

	/**
	 * Save the state of the receiver.
	 * 
	 * @param dialogSettings
	 */
	public void saveState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			return;
		}
		dialogSettings.put(TAG_FILTER_ACTION_SETS, fFilterActionSetContexts);
		dialogSettings.put(TAG_FILTER_INTERNAL, fFilterInternalContexts);
		dialogSettings.put(TAG_FILTER_UNCAT, fFilteredTree
				.isFilteringCategories());
	}

	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();

		IDialogSettings settings = workbenchSettings
				.getSection(TAG_DIALOG_SECTION);

		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}

	protected final void performDefaults() {

		// Ask the user to confirm
		final String title = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxText;
		final String message = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxMessage;
		final boolean confirmed = MessageDialog.openConfirm(getShell(), title,
				message);

		if (confirmed) {
			long startTime = 0L;
			if (DEBUG) {
				startTime = System.currentTimeMillis();
			}

			fFilteredTree.setRedraw(false);
			BusyIndicator.showWhile(fFilteredTree.getViewer().getTree()
					.getDisplay(), new Runnable() {
				public void run() {
					keyController.setDefaultBindings(fBindingService);
				}
			});
			fFilteredTree.setRedraw(true);
			if (DEBUG) {
				final long elapsedTime = System.currentTimeMillis() - startTime;
				Tracing.printTrace(TRACING_COMPONENT,
						"performDefaults:model in " //$NON-NLS-1$
								+ elapsedTime + "ms"); //$NON-NLS-1$
			}
		}

		super.performDefaults();
	}
}
