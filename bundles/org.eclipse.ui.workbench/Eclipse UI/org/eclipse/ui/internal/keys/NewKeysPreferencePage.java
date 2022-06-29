/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> -
 *     		Bug 186522 - [KeyBindings] New Keys preference page does not resort by binding with conflicts
 *     		Bug 226342 - [KeyBindings] Keys preference page conflict table is hard to read
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440810, 491393
 *     Cornel Izbasa <cizbasa@info.uvt.ro> - Bug 442215
 *     Christian Georgi (SAP SE) - Bug 540440
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
import org.eclipse.ui.internal.IPreferenceConstants;
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
import org.eclipse.ui.internal.keys.show.ShowKeysToggleHandler;
import org.eclipse.ui.internal.keys.show.ShowKeysUI;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;
import org.osgi.framework.FrameworkUtil;

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
public class NewKeysPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	private static final String TRACING_COMPONENT = "NewKeysPref"; //$NON-NLS-1$

	public static final String TAG_DIALOG_SECTION = "org.eclipse.ui.preferences.keysPreferencePage"; //$NON-NLS-1$

	private static final String TAG_FILTER_ACTION_SETS = "actionSetFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_INTERNAL = "internalFilter"; //$NON-NLS-1$

	private static final String TAG_FILTER_UNCAT = "uncategorizedFilter"; //$NON-NLS-1$

	/**
	 * The number of items to show in the bindings table tree.
	 */
	private static final int ITEMS_TO_SHOW = 12;

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

	private TableViewer conflictViewer;

	private Button fShowCommandKey;
	private Button fShowCommandKeyForMouseEvents;

	private ICommandImageService commandImageService;

	private ICommandService commandService;

	private IWorkbench fWorkbench;

	/**
	 * A FilteredTree that provides a combo which is used to organize and display
	 * elements in the tree according to the selected criteria.
	 *
	 */
	protected static class CategoryFilterTree extends FilteredTree {

		private CategoryPatternFilter filter;

		/**
		 * Constructor for PatternFilteredTree.
		 *
		 * @param parent
		 * @param treeStyle
		 * @param filter
		 */
		protected CategoryFilterTree(Composite parent, int treeStyle, CategoryPatternFilter filter) {
			super(parent, treeStyle, filter, true);
			this.filter = filter;
			setQuickSelectionMode(true);
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
		private LinkedList<Integer> sortColumns = new LinkedList<>();
		private boolean ascending = true;

		public BindingModelComparator() {
			for (int i = 0; i < NUM_OF_COLUMNS; i++) {
				sortColumns.add(Integer.valueOf(i));
			}
		}

		public int getSortColumn() {
			return sortColumns.getFirst().intValue();
		}

		public void setSortColumn(int column) {
			if (column == getSortColumn()) {
				return;
			}
			Integer sortColumn = Integer.valueOf(column);
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
		 * @param ascending The ascending to set.
		 */
		public void setAscending(boolean ascending) {
			this.ascending = ascending;
		}

		@Override
		public int compare(final Viewer viewer, final Object a, final Object b) {
			int result = 0;
			Iterator<Integer> i = sortColumns.iterator();
			while (i.hasNext() && result == 0) {
				int column = i.next().intValue();
				result = compareColumn(viewer, a, b, column);
			}
			return ascending ? result : (-1) * result;
		}

		private int compareColumn(final Viewer viewer, final Object a, final Object b, final int columnNumber) {
			if (columnNumber == USER_DELTA_COLUMN) {
				return sortUser(a, b);
			}
			IBaseLabelProvider baseLabel = ((TreeViewer) viewer).getLabelProvider();
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
			return Comparator.comparing(o -> getBindingUserText((BindingElement) o),
					Comparator.nullsFirst(Comparator.naturalOrder())).reversed().compare(a,
					b);
		}

	}

	private static final class ResortColumn extends SelectionAdapter {
		private final BindingModelComparator comparator;
		private final TreeColumn treeColumn;
		private final TreeViewer viewer;
		private final int column;

		private ResortColumn(BindingModelComparator comparator, TreeColumn treeColumn, TreeViewer viewer, int column) {
			this.comparator = comparator;
			this.treeColumn = treeColumn;
			this.viewer = viewer;
			this.column = column;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (comparator.getSortColumn() == column) {
				comparator.setAscending(!comparator.isAscending());
				viewer.getTree().setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
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
		@Override
		public String getText(Object element) {
			return ((ModelElement) element).getName();
		}
	}

	private class BindingElementLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		 * A resource manager for this preference page.
		 */
		private final LocalResourceManager localResourceManager = new LocalResourceManager(
				JFaceResources.getResources());

		@Override
		public final void dispose() {
			super.dispose();
			localResourceManager.dispose();
		}

		@Override
		public String getText(Object element) {
			String rc = getColumnText(element, 0);
			if (rc == null) {
				rc = super.getText(element);
			}
			StringBuilder buf = new StringBuilder(rc);
			for (int i = 1; i < USER_DELTA_COLUMN; i++) {
				String text = getColumnText(element, i);
				if (text != null) {
					buf.append(' ');
					buf.append(text);
				}
			}
			return buf.toString();
		}

		@Override
		public String getColumnText(Object element, int index) {
			BindingElement bindingElement = ((BindingElement) element);
			switch (index) {
			case COMMAND_NAME_COLUMN: // name
				return LegacyActionTools.removeMnemonics(bindingElement.getName());
			case KEY_SEQUENCE_COLUMN: // keys
				TriggerSequence seq = bindingElement.getTrigger();
				return seq == null ? Util.ZERO_LENGTH_STRING : seq.format();
			case CONTEXT_COLUMN: // when
				ModelElement context = bindingElement.getContext();
				return context == null ? Util.ZERO_LENGTH_STRING : context.getName();
			case CATEGORY_COLUMN: // category
				return bindingElement.getCategory();
			case USER_DELTA_COLUMN: // user
				return getBindingUserText(bindingElement);
			}
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int index) {
			BindingElement be = (BindingElement) element;
			switch (index) {
			case COMMAND_NAME_COLUMN:
				final String commandId = be.getId();
				final ImageDescriptor imageDescriptor = commandImageService.getImageDescriptor(commandId);
				if (imageDescriptor == null) {
					return null;
				}
				try {
					return localResourceManager.createImage(imageDescriptor);
				} catch (final DeviceResourceException e) {
					final String message = "Problem retrieving image for a command '" //$NON-NLS-1$
							+ commandId + '\'';
					final IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
					WorkbenchPlugin.log(message, status);
				}
				return null;

			}

			return null;
		}
	}

	private static String getBindingUserText(BindingElement bindingElement) {
		if (bindingElement.getUserDelta().intValue() == Binding.USER) {
			if (bindingElement.getConflict().equals(Boolean.TRUE)) {
				return "CU"; //$NON-NLS-1$
			}
			return " U"; //$NON-NLS-1$
		}
		if (bindingElement.getConflict().equals(Boolean.TRUE)) {
			return "C "; //$NON-NLS-1$
		}
		return " "; //$NON-NLS-1$
	}

	static class ModelContentProvider implements ITreeContentProvider {
		@Override
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

		@Override
		public Object getParent(Object element) {
			return ((ModelElement) element).getParent();
		}

		@Override
		public boolean hasChildren(Object element) {
			return (element instanceof BindingModel) || (element instanceof ContextModel)
					|| (element instanceof SchemeModel);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

	}

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.KEYS_PREFERENCE_PAGE);
		final Composite page = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		page.setLayout(layout);

		IDialogSettings settings = getDialogSettings();

		fPatternFilter = new CategoryPatternFilter(true, commandService.getCategory(null));
		if (settings.get(TAG_FILTER_UNCAT) != null) {
			fPatternFilter.filterCategories(settings.getBoolean(TAG_FILTER_UNCAT));
		}

		createSchemeControls(page);
		createTree(page);
		createTreeControls(page);
		createDataControls(page);
		createShowKeysControls(page);

		fill();

		applyDialogFont(page);

		// we want the description text control to span four lines, but because
		// we need the dialog's font for this information, we have to set it here
		// after the dialog font has been applied
		GC gc = new GC(fDescriptionText);
		gc.setFont(fDescriptionText.getFont());
		FontMetrics metrics = gc.getFontMetrics();
		gc.dispose();
		int height = metrics.getHeight() * 5 / 2;

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.heightHint = height;
		fDescriptionText.setLayoutData(gridData);

		return page;
	}

	/**
	 * Creates the button bar with "Filters..." and "Export CVS..." buttons.
	 *
	 * @param parent The composite in which the button bar should be placed; never
	 *               <code>null</code>.
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
		gridData.grabExcessHorizontalSpace = true;
		buttonBar.setLayoutData(gridData);

		// Advanced button.
		final Button filtersButton = new Button(buttonBar, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		filtersButton.setText(NewKeysPreferenceMessages.FiltersButton_Text);
		gridData.widthHint = Math.max(widthHint, filtersButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		filtersButton.setLayoutData(gridData);
		filtersButton.addSelectionListener(widgetSelectedAdapter(e -> {
			KeysPreferenceFiltersDialog dialog = new KeysPreferenceFiltersDialog(getShell());
			dialog.setFilterActionSet(fFilterActionSetContexts);
			dialog.setFilterInternal(fFilterInternalContexts);
			dialog.setFilterUncategorized(fFilteredTree.isFilteringCategories());
			if (dialog.open() == Window.OK) {
				fFilterActionSetContexts = dialog.getFilterActionSet();
				fFilterInternalContexts = dialog.getFilterInternal();
				fFilteredTree.filterCategories(dialog.getFilterUncategorized());

				// Apply context filters
				keyController.filterContexts(fFilterActionSetContexts, fFilterInternalContexts);

				ISelection currentContextSelection = fWhenCombo.getSelection();
				fWhenCombo.setInput(keyController.getContextModel());
				fWhenCombo.setSelection(currentContextSelection);
			}
		}));

		// Export bindings to CSV
		final Button exportButton = new Button(buttonBar, SWT.PUSH);
		// gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		exportButton.setText(NewKeysPreferenceMessages.ExportButton_Text);
		gridData.widthHint = Math.max(widthHint, exportButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		exportButton.setLayoutData(gridData);
		exportButton.addSelectionListener(
				widgetSelectedAdapter(e -> keyController.exportCSV(((Button) e.getSource()).getShell())));

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
		commandNameLabel.setText(NewKeysPreferenceMessages.CommandNameLabel_Text);

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

		fDescriptionText = new Text(leftDataArea, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY);

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
			@Override
			public void focusGained(FocusEvent e) {
				fBindingService.setKeyFilterEnabled(false);
			}

			@Override
			public void focusLost(FocusEvent e) {
				fBindingService.setKeyFilterEnabled(true);
			}
		});
		fBindingText.addDisposeListener(e -> {
			if (!fBindingService.isKeyFilterEnabled()) {
				fBindingService.setKeyFilterEnabled(true);
			}
		});

		fKeySequenceText = new KeySequenceText(fBindingText);
		fKeySequenceText.setKeyStrokeLimit(4);
		fKeySequenceText.addPropertyChangeListener(event -> {
			if (!event.getOldValue().equals(event.getNewValue())) {
				final KeySequence keySequence = fKeySequenceText.getKeySequence();
				if (!keySequence.isComplete()) {
					return;
				}

				BindingElement activeBinding = (BindingElement) keyController.getBindingModel().getSelectedElement();
				if (activeBinding != null) {
					activeBinding.setTrigger(keySequence);
				}
				fBindingText.setSelection(fBindingText.getTextLimit());
			}
		});

		// Button for adding trapped key strokes
		final Button addKeyButton = new Button(leftDataArea, SWT.LEFT | SWT.ARROW);
		addKeyButton.setToolTipText(NewKeysPreferenceMessages.AddKeyButton_ToolTipText);
		gridData = new GridData();
		gridData.heightHint = fSchemeCombo.getCombo().getTextHeight();
		addKeyButton.setLayoutData(gridData);

		// Arrow buttons aren't normally added to the tab list. Let's fix that.
		final Control[] tabStops = dataArea.getTabList();
		final ArrayList<Control> newTabStops = new ArrayList<>();
		for (Control tabStop : tabStops) {
			newTabStops.add(tabStop);
			if (fBindingText.equals(tabStop)) {
				newTabStops.add(addKeyButton);
			}
		}
		final Control[] newTabStopArray = newTabStops.toArray(new Control[newTabStops.size()]);
		dataArea.setTabList(newTabStopArray);

		// Construct the menu to attach to the above button.
		final Menu addKeyMenu = new Menu(addKeyButton);
		final Iterator<KeyStroke> trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
		while (trappedKeyItr.hasNext()) {
			final KeyStroke trappedKey = trappedKeyItr.next();
			final MenuItem menuItem = new MenuItem(addKeyMenu, SWT.PUSH);
			menuItem.setText(trappedKey.format());
			menuItem.addSelectionListener(widgetSelectedAdapter(e -> {
				fKeySequenceText.insert(trappedKey);
				fBindingText.setFocus();
				fBindingText.setSelection(fBindingText.getTextLimit());
			}));
		}
		addKeyButton.addSelectionListener(widgetSelectedAdapter(selectionEvent -> {
			Point buttonLocation = addKeyButton.getLocation();
			buttonLocation = dataArea.toDisplay(buttonLocation.x, buttonLocation.y);
			Point buttonSize = addKeyButton.getSize();
			addKeyMenu.setLocation(buttonLocation.x, buttonLocation.y + buttonSize.y);
			addKeyMenu.setVisible(true);
		}));

		// The when label.
		final Label whenLabel = new Label(leftDataArea, SWT.NONE);
		whenLabel.setText(NewKeysPreferenceMessages.WhenLabel_Text);

		// The when combo.
		fWhenCombo = new ComboViewer(leftDataArea);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		ViewerComparator comparator = new ViewerComparator();
		fWhenCombo.setComparator(comparator);
		fWhenCombo.getCombo().setLayoutData(gridData);
		fWhenCombo.setContentProvider(new ModelContentProvider());
		fWhenCombo.setLabelProvider(new ListLabelProvider());
		fWhenCombo.addSelectionChangedListener(event -> {
			ContextElement context = (ContextElement) event.getStructuredSelection().getFirstElement();
			if (context != null) {
				keyController.getContextModel().setSelectedElement(context);
			}
		});
		IPropertyChangeListener whenListener = event -> {
			if (event.getSource() == keyController.getContextModel()
					&& CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				Object newVal = event.getNewValue();
				StructuredSelection structuredSelection = newVal == null ? null : new StructuredSelection(newVal);
				fWhenCombo.setSelection(structuredSelection, true);
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

		new Label(rightDataArea, SWT.NONE); // filler

		// The description label.
		final Label descriptionLabel = new Label(rightDataArea, SWT.NONE);
		descriptionLabel.setText(NewKeysPreferenceMessages.ConflictsLabel_Text);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		descriptionLabel.setLayoutData(gridData);

		conflictViewer = new TableViewer(rightDataArea, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		Table table = conflictViewer.getTable();
		table.setHeaderVisible(true);
		TableColumn bindingNameColumn = new TableColumn(table, SWT.LEAD);
		bindingNameColumn.setText(NewKeysPreferenceMessages.CommandNameColumn_Text);
		bindingNameColumn.setWidth(150);
		TableColumn bindingContextNameColumn = new TableColumn(table, SWT.LEAD);
		bindingContextNameColumn.setText(NewKeysPreferenceMessages.WhenColumn_Text);
		bindingContextNameColumn.setWidth(150);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// gridData.horizontalIndent = 10;
		table.setLayoutData(gridData);
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(60));
		tableLayout.addColumnData(new ColumnWeightData(40));
		table.setLayout(tableLayout);
		conflictViewer.setContentProvider((IStructuredContentProvider) inputElement -> {
			if (inputElement instanceof Collection<?>) {
				return ((Collection<?>) inputElement).toArray(new Object[0]);
			}
			return new Object[0];
		});
		conflictViewer.setLabelProvider(new BindingElementLabelProvider() {
			@Override
			public String getColumnText(Object o, int index) {
				BindingElement element = (BindingElement) o;
				if (index == 0) {
					return element.getName();
				}
				return element.getContext().getName();
			}
		});
		conflictViewer.addSelectionChangedListener(event -> {
			ModelElement binding = (ModelElement) event.getStructuredSelection().getFirstElement();
			BindingModel bindingModel = keyController.getBindingModel();
			if (binding != null && binding != bindingModel.getSelectedElement()) {
				StructuredSelection selection = new StructuredSelection(binding);

				bindingModel.setSelectedElement(binding);
				conflictViewer.setSelection(selection);

				boolean selectionVisible = false;
				TreeItem[] items = fFilteredTree.getViewer().getTree().getItems();
				for (TreeItem item : items) {
					if (item.getData().equals(binding)) {
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
		});

		IPropertyChangeListener conflictsListener = event -> {
			if (event.getSource() == keyController.getConflictModel()
					&& CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				if (keyController.getConflictModel().getConflicts() != null) {
					Object newVal = event.getNewValue();
					StructuredSelection structuredSelection = newVal == null ? null : new StructuredSelection(newVal);
					conflictViewer.setSelection(structuredSelection, true);
				}
			} else if (ConflictModel.PROP_CONFLICTS.equals(event.getProperty())) {
				conflictViewer.setInput(event.getNewValue());
			} else if (ConflictModel.PROP_CONFLICTS_ADD.equals(event.getProperty())) {
				conflictViewer.add(event.getNewValue());
			} else if (ConflictModel.PROP_CONFLICTS_REMOVE.equals(event.getProperty())) {
				conflictViewer.remove(event.getNewValue());
			}
		};
		keyController.addPropertyChangeListener(conflictsListener);

		IPropertyChangeListener dataUpdateListener = event -> {
			BindingElement bindingElement = null;
			boolean weCare = false;
			if (event.getSource() == keyController.getBindingModel()
					&& CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				bindingElement = (BindingElement) event.getNewValue();
				weCare = true;
			} else if (event.getSource() == keyController.getBindingModel().getSelectedElement()
					&& ModelElement.PROP_MODEL_OBJECT.equals(event.getProperty())) {
				bindingElement = (BindingElement) event.getSource();
				weCare = true;
			}
			if (bindingElement == null && weCare) {
				commandNameValueLabel.setText(""); //$NON-NLS-1$
				fDescriptionText.setText(""); //$NON-NLS-1$
				fBindingText.setText(""); //$NON-NLS-1$
			} else if (bindingElement != null) {
				commandNameValueLabel.setText(bindingElement.getName());
				String desc = bindingElement.getDescription();
				fDescriptionText.setText(desc == null ? "" : desc); //$NON-NLS-1$
				KeySequence trigger = (KeySequence) bindingElement.getTrigger();
				fKeySequenceText.setKeySequence(trigger);
			}
		};
		keyController.addPropertyChangeListener(dataUpdateListener);

	}

	private void createTree(Composite parent) {
		fPatternFilter = new CategoryPatternFilter(true, fDefaultCategory);
		fPatternFilter.filterCategories(true);

		GridData gridData;

		fFilteredTree = new CategoryFilterTree(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER, fPatternFilter);
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

		final TreeColumn commandNameColumn = new TreeColumn(tree, SWT.LEFT, COMMAND_NAME_COLUMN);
		commandNameColumn.setText(NewKeysPreferenceMessages.CommandNameColumn_Text);
		tree.setSortColumn(commandNameColumn);
		tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
		commandNameColumn
				.addSelectionListener(new ResortColumn(comparator, commandNameColumn, viewer, COMMAND_NAME_COLUMN));

		final TreeColumn triggerSequenceColumn = new TreeColumn(tree, SWT.LEFT, KEY_SEQUENCE_COLUMN);
		triggerSequenceColumn.setText(NewKeysPreferenceMessages.TriggerSequenceColumn_Text);
		triggerSequenceColumn
				.addSelectionListener(new ResortColumn(comparator, triggerSequenceColumn, viewer, KEY_SEQUENCE_COLUMN));

		final TreeColumn whenColumn = new TreeColumn(tree, SWT.LEFT, CONTEXT_COLUMN);
		whenColumn.setText(NewKeysPreferenceMessages.WhenColumn_Text);
		whenColumn.addSelectionListener(new ResortColumn(comparator, whenColumn, viewer, CONTEXT_COLUMN));

		final TreeColumn categoryColumn = new TreeColumn(tree, SWT.LEFT, CATEGORY_COLUMN);
		categoryColumn.setText(NewKeysPreferenceMessages.CategoryColumn_Text);
		categoryColumn.addSelectionListener(new ResortColumn(comparator, categoryColumn, viewer, CATEGORY_COLUMN));

		final TreeColumn userMarker = new TreeColumn(tree, SWT.LEFT, USER_DELTA_COLUMN);
		userMarker.setText(NewKeysPreferenceMessages.UserColumn_Text);
		userMarker.addSelectionListener(new ResortColumn(comparator, userMarker, viewer, USER_DELTA_COLUMN));

		viewer.setContentProvider(new ModelContentProvider());
		viewer.setLabelProvider(new BindingElementLabelProvider());

		fFilteredTree.getPatternFilter().setIncludeLeadingWildcard(true);
		final TreeColumn[] columns = viewer.getTree().getColumns();

		columns[COMMAND_NAME_COLUMN].setWidth(240);
		columns[KEY_SEQUENCE_COLUMN].setWidth(130);
		columns[CONTEXT_COLUMN].setWidth(130);
		columns[CATEGORY_COLUMN].setWidth(130);
		columns[USER_DELTA_COLUMN].setWidth(50);

		viewer.addSelectionChangedListener(event -> {
			ModelElement binding = (ModelElement) event.getStructuredSelection().getFirstElement();
			keyController.getBindingModel().setSelectedElement(binding);
		});

		IPropertyChangeListener treeUpdateListener = event -> {
			if (event.getSource() == keyController.getBindingModel()
					&& CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				Object newVal = event.getNewValue();
				StructuredSelection structuredSelection = newVal == null ? null : new StructuredSelection(newVal);
				viewer.setSelection(structuredSelection, true);
			} else if (event.getSource() instanceof BindingElement
					&& ModelElement.PROP_MODEL_OBJECT.equals(event.getProperty())) {
				viewer.update(event.getSource(), null);
			} else if (BindingElement.PROP_CONFLICT.equals(event.getProperty())) {
				viewer.update(event.getSource(), null);
			} else if (BindingModel.PROP_BINDINGS.equals(event.getProperty())) {
				viewer.refresh();
			} else if (BindingModel.PROP_BINDING_ADD.equals(event.getProperty())) {
				viewer.add(keyController.getBindingModel(), event.getNewValue());
			} else if (BindingModel.PROP_BINDING_REMOVE.equals(event.getProperty())) {
				viewer.remove(event.getNewValue());
			} else if (BindingModel.PROP_BINDING_FILTER.equals(event.getProperty())) {
				viewer.refresh();
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
		addBindingButton.setText(NewKeysPreferenceMessages.AddBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, addBindingButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		addBindingButton.setLayoutData(gridData);
		addBindingButton.addSelectionListener(widgetSelectedAdapter(event -> keyController.getBindingModel().copy()));

		final Button removeBindingButton = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		removeBindingButton.setText(NewKeysPreferenceMessages.RemoveBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, removeBindingButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		removeBindingButton.setLayoutData(gridData);
		removeBindingButton
				.addSelectionListener(widgetSelectedAdapter(event -> keyController.getBindingModel().remove()));

		final Button restore = new Button(treeControls, SWT.PUSH);
		gridData = new GridData();
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		restore.setText(NewKeysPreferenceMessages.RestoreBindingButton_Text);
		gridData.widthHint = Math.max(widthHint, restore.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		restore.setLayoutData(gridData);
		restore.addSelectionListener(widgetSelectedAdapter(event -> {
			try {
				fFilteredTree.setRedraw(false);
				BindingModel bindingModel = keyController.getBindingModel();
				bindingModel.restoreBinding(keyController.getContextModel());
			} finally {
				fFilteredTree.setRedraw(true);
			}
		}));

		createButtonBar(treeControls);
		return treeControls;
	}

	/**
	 *
	 */
	private void fill() {
		fSchemeCombo.setInput(keyController.getSchemeModel());
		fSchemeCombo.setSelection(new StructuredSelection(keyController.getSchemeModel().getSelectedElement()));

		// Apply context filters
		keyController.filterContexts(fFilterActionSetContexts, fFilterInternalContexts);
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
		fSchemeCombo.addSelectionChangedListener(
				event -> BusyIndicator.showWhile(fFilteredTree.getViewer().getTree().getDisplay(), () -> {
					SchemeElement scheme = (SchemeElement) event.getStructuredSelection().getFirstElement();
					keyController.getSchemeModel().setSelectedElement(scheme);
				}));
		IPropertyChangeListener listener = event -> {
			if (event.getSource() == keyController.getSchemeModel()
					&& CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				Object newVal = event.getNewValue();
				StructuredSelection structuredSelection = newVal == null ? null : new StructuredSelection(newVal);
				fSchemeCombo.setSelection(structuredSelection, true);
			}
		};

		keyController.addPropertyChangeListener(listener);
	}

	private void createShowKeysControls(Composite parent) {
		ShowKeysUI showKeysUI = new ShowKeysUI(fWorkbench, getPreferenceStore());

		final Group group = new Group(parent, SWT.NONE);
		group.setText(NewKeysPreferenceMessages.ShowCommandKeysGroup_Title);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		fShowCommandKey = new Button(group, SWT.CHECK);
		fShowCommandKey.setText(NewKeysPreferenceMessages.ShowCommandKeysForKeyboard_Text);
		fShowCommandKey.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_KEYBOARD));
		fShowCommandKey.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			// show a preview of the shortcut popup
			if (fShowCommandKey.getSelection()) {
				showKeysUI.openForPreview(ShowKeysToggleHandler.COMMAND_ID, null);
			}
		}));
		fShowCommandKeyForMouseEvents = new Button(group, SWT.CHECK);
		fShowCommandKeyForMouseEvents.setText(NewKeysPreferenceMessages.ShowCommandKeysForMouse_Text);
		fShowCommandKeyForMouseEvents
				.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_MOUSE_EVENTS));
		fShowCommandKeyForMouseEvents.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			// show a preview of the shortcut popup
			if (fShowCommandKeyForMouseEvents.getSelection()) {
				showKeysUI.openForPreview(ShowKeysToggleHandler.COMMAND_ID, null);
			}
		}));
	}

	@Override
	public void init(IWorkbench workbench) {
		fWorkbench = workbench;
		keyController = new KeyController();
		keyController.init(workbench);

		commandService = workbench.getService(ICommandService.class);
		fDefaultCategory = commandService.getCategory(null);
		fBindingService = workbench.getService(IBindingService.class);

		commandImageService = workbench.getService(ICommandImageService.class);
	}

	@Override
	public void applyData(Object data) {
		if (data instanceof ModelElement) {
			keyController.getBindingModel().setSelectedElement((ModelElement) data);
		}
		if (data instanceof Binding && fFilteredTree != null) {
			BindingElement be = keyController.getBindingModel().getBindingToElement().get(data);
			fFilteredTree.getViewer().setSelection(new StructuredSelection(be), true);
		}
		if (data instanceof ParameterizedCommand) {
			Map<ParameterizedCommand, BindingElement> commandToElement = keyController.getBindingModel().getCommandToElement();

			BindingElement be = commandToElement.get(data);
			if (be != null) {
				fFilteredTree.getViewer().setSelection(new StructuredSelection(be), true);
			}
		}
	}

	@Override
	public boolean performOk() {
		keyController.saveBindings(fBindingService);
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_KEYBOARD, fShowCommandKey.getSelection());
		preferenceStore.setValue(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_MOUSE_EVENTS,
				fShowCommandKeyForMouseEvents.getSelection());

		saveState(getDialogSettings());
		return super.performOk();
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
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
		dialogSettings.put(TAG_FILTER_UNCAT, fFilteredTree.isFilteringCategories());
	}

	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(NewKeysPreferencePage.class)).getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}

	@Override
	protected final void performDefaults() {

		// Ask the user to confirm
		final String title = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxText;
		final String message = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxMessage;
		final boolean confirmed = MessageDialog.open(MessageDialog.CONFIRM, getShell(), title, message, SWT.SHEET);

		if (confirmed) {
			long startTime = 0L;
			if (DEBUG) {
				startTime = System.currentTimeMillis();
			}

			fFilteredTree.setRedraw(false);
			BusyIndicator.showWhile(fFilteredTree.getViewer().getTree().getDisplay(),
					() -> keyController.setDefaultBindings(fBindingService));
			fFilteredTree.setRedraw(true);
			if (DEBUG) {
				final long elapsedTime = System.currentTimeMillis() - startTime;
				Tracing.printTrace(TRACING_COMPONENT, "performDefaults:model in " + elapsedTime + "ms"); //$NON-NLS-1$ //$NON-NLS-2$

			}

			fShowCommandKey
					.setSelection(getPreferenceStore().getDefaultBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_KEYBOARD));
			fShowCommandKeyForMouseEvents.setSelection(
					getPreferenceStore().getDefaultBoolean(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_MOUSE_EVENTS));
		}

		super.performDefaults();
	}
}
