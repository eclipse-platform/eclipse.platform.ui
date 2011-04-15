/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.internal.provisional.action.ToolBarContributionItem2;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.OpenPerspectiveAction;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.ActionSetActionBars;
import org.eclipse.ui.internal.ActionSetContributionItem;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.PluginActionCoolBarContributionItem;
import org.eclipse.ui.internal.PluginActionSet;
import org.eclipse.ui.internal.PluginActionSetBuilder;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.dialogs.TreeManager.CheckListener;
import org.eclipse.ui.internal.dialogs.TreeManager.TreeItem;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.internal.registry.ActionSetDescriptor;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Dialog to allow users the ability to customize the perspective. This includes
 * customizing menus and toolbars by adding, removing, or re-arranging commands
 * or groups of commands.
 * 
 */
public class CustomizePerspectiveDialog extends TrayDialog {

	private static final String TOOLBAR_ICON = "$nl$/icons/full/obj16/toolbar.gif"; //$NON-NLS-1$
	private static final String SUBMENU_ICON = "$nl$/icons/full/obj16/submenu.gif"; //$NON-NLS-1$
	private static final String MENU_ICON = "$nl$/icons/full/obj16/menu.gif"; //$NON-NLS-1$
	private static final String WARNING_ICON = "$nl$/icons/full/obj16/warn_tsk.gif"; //$NON-NLS-1$

	private static final String SHORTCUT_CONTRIBUTION_ITEM_ID_OPEN_PERSPECTIVE = "openPerspective"; //$NON-NLS-1$
	private static final String SHORTCUT_CONTRIBUTION_ITEM_ID_SHOW_VIEW = "showView"; //$NON-NLS-1$

	private static final String KEYS_PREFERENCE_PAGE_ID = "org.eclipse.ui.preferencePages.Keys"; //$NON-NLS-1$

	private static final String NEW_LINE = System.getProperty("line.separator"); //$NON-NLS-1$

	private static final int MIN_TOOLTIP_WIDTH = 160;

	private WorkbenchWindow window;

	private Perspective perspective;

	private TabFolder tabFolder;

	private final static int TAB_WIDTH_IN_DLUS = 490;

	private final static int TAB_HEIGHT_IN_DLUS = 230;

	private final String shortcutMenuColumnHeaders[] = {
			WorkbenchMessages.ActionSetSelection_menuColumnHeader,
			WorkbenchMessages.ActionSetSelection_descriptionColumnHeader };

	private int[] shortcutMenuColumnWidths = { 125, 300 };

	ImageDescriptor menuImageDescriptor = null;

	ImageDescriptor submenuImageDescriptor = null;

	ImageDescriptor toolbarImageDescriptor = null;

	ImageDescriptor warningImageDescriptor = null;

	private TreeManager treeManager;

	private DisplayItem menuItems;

	private DisplayItem toolBarItems;

	private Category shortcuts;

	private DisplayItem wizards;

	private DisplayItem perspectives;

	private DisplayItem views;

	private Map idToActionSet = new HashMap();

	private final List actionSets = new ArrayList();

	private IWorkbenchWindowConfigurer configurer;

	private TabItem actionSetTab;

	private CheckboxTableViewer actionSetAvailabilityTable;

	private CheckboxTreeViewer menuStructureViewer1;

	private CheckboxTreeViewer menuStructureViewer2;

	private CheckboxTreeViewer toolbarStructureViewer1;

	private CheckboxTreeViewer toolbarStructureViewer2;

	private Set toDispose;

	private CustomizeActionBars customizeActionBars;

	private Font tooltipHeading;

	/**
	 * A Listener for a list of command groups, that updates the viewer and
	 * filter who are dependent on the action set selection.
	 * 
	 * @since 3.5
	 */
	private static final class ActionSetSelectionChangedListener implements
			ISelectionChangedListener {
		private final TreeViewer filterViewer;
		private final ActionSetFilter filter;

		public ActionSetSelectionChangedListener(TreeViewer viewer,
				ActionSetFilter menuStructureFilterByActionSet) {
			this.filterViewer = viewer;
			this.filter = menuStructureFilterByActionSet;
		}

		public void selectionChanged(SelectionChangedEvent event) {
			Object element = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			filter.setActionSet((ActionSet) element);
			filterViewer.refresh();
			filterViewer.expandAll();
		}
	}

	/**
	 * A filter which will only show action sets which contribute items in the
	 * given tree structure.
	 * 
	 * @since 3.5
	 */
	private static final class ShowUsedActionSetsFilter extends ViewerFilter {
		private DisplayItem rootItem;

		public ShowUsedActionSetsFilter(DisplayItem rootItem) {
			this.rootItem = rootItem;
		}

		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			return (includeInSetStructure(rootItem, (ActionSet) element));
		}
	}

	/**
	 * Represents a menu item or a tool bar item.
	 * 
	 * @since 3.5
	 */
	private class DisplayItem extends TreeItem {
		/** The logic item represented */
		private IContributionItem item;

		/** The action set this item belongs to (optional) */
		private ActionSet actionSet;

		public DisplayItem(String label, IContributionItem item) {
			treeManager.super(label == null ? null : DialogUtil
					.removeAccel(removeShortcut(label)));
			this.item = item;
		}

		public void setActionSet(ActionSet actionSet) {
			this.actionSet = actionSet;
			if (actionSet != null)
				actionSet.addItem(this);
		}

		public ActionSet getActionSet() {
			return actionSet;
		}

		public IContributionItem getIContributionItem() {
			return item;
		}
	}

	/**
	 * Represents a menu item whose content is dynamic. Contains a list of the
	 * current items being displayed.
	 * 
	 * @since 3.5
	 */
	private class DynamicContributionItem extends DisplayItem {
		private List preview;

		public DynamicContributionItem(IContributionItem item) {
			super(WorkbenchMessages.HideItems_dynamicItemName, item);
			preview = new ArrayList();
		}

		public void addCurrentItem(MenuItem item) {
			preview.add(item);
		}

		public List getCurrentItems() {
			return preview;
		}
	}

	/**
	 * @param descriptor
	 * @param window
	 * @return the appropriate {@link IContributionItem} for the given wizard
	 */
	private static ActionContributionItem getIContributionItem(
			IWizardDescriptor descriptor, IWorkbenchWindow window) {
		IAction action = new NewWizardShortcutAction(window, descriptor);
		return new ActionContributionItem(action);
	}

	/**
	 * @param descriptor
	 * @param window
	 * @return the appropriate {@link IContributionItem} for the given
	 *         perspective
	 */
	private static ActionContributionItem getIContributionItem(
			IPerspectiveDescriptor descriptor, IWorkbenchWindow window) {
		IAction action = new OpenPerspectiveAction(window, descriptor, null);
		return new ActionContributionItem(action);
	}

	/**
	 * @param window
	 * @return the appropriate {@link IContributionItem} for showing views
	 */
	private static ActionContributionItem getIContributionItem(
			IWorkbenchWindow window) {
		IAction action = ActionFactory.SHOW_VIEW_MENU.create(window);
		return new ActionContributionItem(action);
	}

	/**
	 * Represents a menu item which needs to be shown in the Shortcuts tab.
	 * 
	 * @since 3.5
	 */
	private class ShortcutItem extends DisplayItem {
		/** The description to show in the table */
		private String description;

		/** The category this shortcut is in (should be set) */
		private Category category;

		private Object descriptor;

		public ShortcutItem(String label, IWizardDescriptor descriptor) {
			super(label, CustomizePerspectiveDialog.getIContributionItem(
					descriptor, window));
			this.descriptor = descriptor;
		}

		public ShortcutItem(String label, IPerspectiveDescriptor descriptor) {
			super(label, CustomizePerspectiveDialog.getIContributionItem(
					descriptor, window));
			this.descriptor = descriptor;
		}

		public ShortcutItem(String label, IViewDescriptor descriptor) {
			super(label, CustomizePerspectiveDialog
					.getIContributionItem(window));
			this.descriptor = descriptor;
		}

		public Object getDescriptor() {
			return descriptor;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setCategory(Category category) {
			this.category = category;
		}

		public Category getCategory() {
			return category;
		}
	}

	/**
	 * Represents a category in the shortcuts menu. Since categories can have a
	 * tree-structure, the functionality provided by the TreeManager and
	 * TreeItem classes is used, however the logic for visibility changes and
	 * gray states is more sophisticated.
	 * 
	 * @since 3.5
	 */
	private class Category extends TreeItem {

		/** ShortcutItems which are contributed in this Category */
		private List contributionItems;

		public Category(String label) {
			treeManager.super(label == null ? null : DialogUtil
					.removeAccel(removeShortcut(label)));
			this.contributionItems = new ArrayList();
		}

		public List getContributionItems() {
			return contributionItems;
		}

		/**
		 * Adds another ShortcutItem to this Category's list of ShortcutItems
		 * and creates a pseudo-child/parent relationship.
		 * 
		 * @param item
		 *            the item to add
		 */
		public void addShortcutItem(ShortcutItem item) {
			contributionItems.add(item);
			item.setCategory(this);
		}

		/**
		 * While the child/parent state in the Category hierarchy is
		 * automatically maintained, the pseudo-child/parent relationship must
		 * be explicitly updated. This method will update Categories if their
		 * states need to change as a result of their ShortcutItems.
		 */
		public void update() {
			for (Iterator i = contributionItems.iterator(); i.hasNext();) {
				DisplayItem item = (DisplayItem) i.next();
				if (item.getState()) {
					this.setCheckState(true);
					return;
				}
			}

			this.setCheckState(false);
		}

		/**
		 * Changes the state of all pseudo-descendant ShortcutItems, causing the
		 * effective state of this Category and all its sub-Categories to match.
		 * 
		 * @param state
		 *            The state to set this branch to.
		 */
		public void setItemsState(boolean state) {
			for (Iterator i = contributionItems.iterator(); i.hasNext();) {
				DisplayItem item = (DisplayItem) i.next();
				item.setCheckState(state);
			}
			for (Iterator i = getChildren().iterator(); i.hasNext();) {
				Category category = (Category) i.next();
				category.setItemsState(state);
			}
		}
	}

	/**
	 * Represents an action set, under which ContributionItems exist. There is
	 * no inherent hierarchy in action sets - they exist independent of one
	 * another, simply contribution menu items and tool bar items.
	 * 
	 * @since 3.5
	 */
	private class ActionSet {
		/** The descriptor which describes the action set represented */
		private ActionSetDescriptor descriptor;

		/** ContributionItems contributed by this action set */
		private List contributionItems;

		private boolean active;
		
		private boolean wasChanged = false;

		public ActionSet(ActionSetDescriptor descriptor, boolean active) {
			this.descriptor = descriptor;
			this.active = active;
			this.contributionItems = new ArrayList();
		}

		public void addItem(DisplayItem item) {
			contributionItems.add(item);
		}

		public String toString() {
			return descriptor.getLabel();
		}

		public boolean isActive() {
			return active;
		}

		public boolean wasChanged() {
			return wasChanged;
		}
		
		public void setActive(boolean active) {
			boolean wasActive = this.active;
			this.active = active;
			if (!active) {
				for (Iterator i = contributionItems.iterator(); i.hasNext();) {
					DisplayItem item = (DisplayItem) i.next();
					item.setCheckState(false);
				}
			}
			if (wasActive != active) {
				actionSetAvailabilityChanged();
			}
			
			wasChanged = true;
		}
	}

	/**
	 * A label provider to include the description field of ShortcutItems in the
	 * table.
	 * 
	 * @since 3.5
	 */
	private class ShortcutLabelProvider extends
			TreeManager.TreeItemLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0)
				return this.getImage(element);
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 1)
				return ((ShortcutItem) element).getDescription();
			return this.getText(element);
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

	/**
	 * Provides the check logic for the categories viewer in the shortcuts tab.
	 * Categories have a dual concept of children - their proper children
	 * (sub-Categories, as in the wizards), and the actual elements they
	 * contribute to the menu system. The check state must take this into
	 * account.
	 * 
	 * @since 3.5
	 */
	private static class CategoryCheckProvider implements ICheckStateProvider {
		public boolean isChecked(Object element) {
			Category category = (Category) element;

			if (category.getChildren().isEmpty()
					&& category.getContributionItems().isEmpty())
				return false;

			// To be checked, any sub-Category can be checked.
			for (Iterator i = category.getChildren().iterator(); i.hasNext();) {
				Category child = (Category) i.next();
				if (isChecked(child))
					return true;
			}

			// To be checked, any ShortcutItem can be checked.
			for (Iterator i = category.getContributionItems().iterator(); i
					.hasNext();) {
				DisplayItem item = (DisplayItem) i.next();
				if (item.getState())
					return true;
			}

			return false;
		}

		public boolean isGrayed(Object element) {
			boolean hasChecked = false;
			boolean hasUnchecked = false;
			Category category = (Category) element;

			// Search in sub-Categories and ShortcutItems for one that is
			// checked and one that is unchecked.

			for (Iterator i = category.getChildren().iterator(); i.hasNext();) {
				Category child = (Category) i.next();
				if (isGrayed(child))
					return true;
				if (isChecked(child))
					hasChecked = true;
				else
					hasUnchecked = true;
				if (hasChecked && hasUnchecked)
					return true;
			}

			for (Iterator i = category.getContributionItems().iterator(); i
					.hasNext();) {
				DisplayItem item = (DisplayItem) i.next();
				if (item.getState())
					hasChecked = true;
				else
					hasUnchecked = true;
				if (hasChecked && hasUnchecked)
					return true;
			}

			return false;
		}
	}

	/**
	 * A tooltip which, given a model element, will display its icon (if there
	 * is one), name, and a description (if there is one).
	 * 
	 * @since 3.5
	 */
	private abstract class NameAndDescriptionToolTip extends ToolTip {
		public NameAndDescriptionToolTip(Control control, int style) {
			super(control, style, false);
		}

		protected abstract Object getModelElement(Event event);

		/**
		 * Adds logic to only show a tooltip if a meaningful item is under the
		 * cursor.
		 */
		protected boolean shouldCreateToolTip(Event event) {
			return super.shouldCreateToolTip(event)
					&& getModelElement(event) != null;
		}

		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			Object modelElement = getModelElement(event);

			Image iconImage = null;
			String nameString = null;

			if (modelElement instanceof DisplayItem) {
				iconImage = ((DisplayItem) modelElement).getImage();
				nameString = ((DisplayItem) modelElement).getLabel();
			} else if (modelElement instanceof ActionSet) {
				nameString = ((ActionSet) modelElement).descriptor.getLabel();
			}

			// Create the content area
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setBackground(parent.getDisplay().getSystemColor(
					SWT.COLOR_INFO_BACKGROUND));
			composite.setLayout(new GridLayout(2, false));

			// The title area with the icon (if there is one) and label.
			Label title = createEntry(composite, iconImage, nameString);
			title.setFont(tooltipHeading);
			GridDataFactory.createFrom((GridData)title.getLayoutData())
				.hint(SWT.DEFAULT, SWT.DEFAULT)
				.minSize(MIN_TOOLTIP_WIDTH, 1)
				.applyTo(title);

			// The description (if there is one)
			String descriptionString = getDescription(modelElement);
			if (descriptionString != null) {
				createEntry(composite, null, descriptionString);
			}

			// Other Content to add
			addContent(composite, modelElement);

			return composite;
		}

		/**
		 * Adds a line of information to <code>parent</code>. If
		 * <code>icon</code> is not <code>null</code>, an icon is placed on the
		 * left, and then a label with <code>text</code>.
		 * 
		 * @param parent
		 *            the composite to add the entry to
		 * @param icon
		 *            the icon to place next to the text. <code>null</code> for
		 *            none.
		 * @param text
		 *            the text to display
		 * @return the created label
		 */
		protected Label createEntry(Composite parent, Image icon, String text) {
			if (icon != null) {
				Label iconLabel = new Label(parent, SWT.NONE);
				iconLabel.setImage(icon);
				iconLabel.setBackground(parent.getDisplay().getSystemColor(
						SWT.COLOR_INFO_BACKGROUND));
				iconLabel.setData(new GridData());
			}

			Label textLabel = new Label(parent, SWT.WRAP);
			
			if(icon == null) {
				GridDataFactory.generate(textLabel, 2, 1);
			} else {
				GridDataFactory.generate(textLabel, 1, 1);
			}
			
			textLabel.setText(text);
			textLabel.setBackground(parent.getDisplay().getSystemColor(
					SWT.COLOR_INFO_BACKGROUND));
			return textLabel;
		}

		/**
		 * Adds a line of information to <code>parent</code>. If
		 * <code>icon</code> is not <code>null</code>, an icon is placed on the
		 * left, and then a label with <code>text</code>, which supports using
		 * anchor tags to creates links
		 * 
		 * @param parent
		 *            the composite to add the entry to
		 * @param icon
		 *            the icon to place next to the text. <code>null</code> for
		 *            none.
		 * @param text
		 *            the text to display
		 * @return the created link
		 */
		protected Link createEntryWithLink(Composite parent, Image icon,
				String text) {
			if (icon != null) {
				Label iconLabel = new Label(parent, SWT.NONE);
				iconLabel.setImage(icon);
				iconLabel.setBackground(parent.getDisplay().getSystemColor(
						SWT.COLOR_INFO_BACKGROUND));
				iconLabel.setData(new GridData());
			}
			
			Link textLink = new Link(parent, SWT.WRAP);

			if(icon == null) {
				GridDataFactory.generate(textLink, 2, 1);
			}
			
			textLink.setText(text);
			textLink.setBackground(parent.getDisplay().getSystemColor(
					SWT.COLOR_INFO_BACKGROUND));
			return textLink;
		}

		protected void addContent(Composite destination, Object modelElement) {
		}
	}

	/**
	 * A tooltip with useful information based on the type of ContributionItem
	 * the cursor hovers over in a Table.
	 * 
	 * @since 3.5
	 */
	private class TableToolTip extends NameAndDescriptionToolTip {
		private Table table;

		public TableToolTip(Table table) {
			super(table, RECREATE);
			this.table = table;
		}

		protected Object getModelElement(Event event) {
			TableItem tableItem = table.getItem(new Point(event.x, event.y));
			if (tableItem == null)
				return null;
			return tableItem.getData();
		}
	}

	/**
	 * A tooltip with useful information based on the type of ContributionItem
	 * the cursor hovers over in a Tree. In addition to the content provided by
	 * the {@link NameAndDescriptionToolTip} this includes action set
	 * information and key binding data.
	 * 
	 * @since 3.5
	 */
	private class ItemDetailToolTip extends NameAndDescriptionToolTip {
		private Tree tree;
		private boolean showActionSet;
		private boolean showKeyBindings;
		private ViewerFilter filter;
		private TreeViewer v;
		
		/**
		 * @param tree
		 *            The tree for the tooltip to hover over
		 */
		private ItemDetailToolTip(TreeViewer v, Tree tree, boolean showActionSet,
				boolean showKeyBindings, ViewerFilter filter) {
			super(tree,NO_RECREATE);
			this.tree = tree;
			this.v = v;
			this.showActionSet = showActionSet;
			this.showKeyBindings = showKeyBindings;
			this.filter = filter;
			this.setHideOnMouseDown(false);
		}
		
		public Point getLocation(Point tipSize, Event event) {
			// try to position the tooltip at the bottom of the cell
			ViewerCell cell = v.getCell(new Point(event.x, event.y));
			
			if( cell != null ) {
				return tree.toDisplay(event.x,cell.getBounds().y+cell.getBounds().height);
			}
			
			return super.getLocation(tipSize, event);
		}

		protected Object getToolTipArea(Event event) {
			// Ensure that the tooltip is hidden when the cell is left
			return v.getCell(new Point(event.x, event.y));
		}
		
		protected void addContent(Composite destination, Object modelElement) {
			final DisplayItem item = (DisplayItem) modelElement;

			// Show any relevant action set info
			if (showActionSet) {
				String text = null;
				Image image = null;
				
				if(isEffectivelyAvailable(item, filter)) {
					if(item.actionSet != null) {
						//give information on which command group the item is in
						
						final String actionSetName = item.getActionSet().descriptor
								.getLabel();
						
						text = NLS.bind(
								WorkbenchMessages.HideItems_itemInActionSet,
								actionSetName);
					}
				} else {
					//give feedback on why item is unavailable
					
					image = warningImageDescriptor.createImage();
					
					if(item.getChildren().isEmpty()) {
						//i.e. is a leaf
						
						final String actionSetName = item.getActionSet().
								descriptor.getLabel();
						
						text = NLS.bind(
								WorkbenchMessages.HideItems_itemInUnavailableActionSet,
								actionSetName);
						
					} else {
						//i.e. has children

						Set actionGroup = new LinkedHashSet();
						collectDescendantCommandGroups(actionGroup, item,
								filter);
						
						if (actionGroup.size() == 1) {
							//i.e. only one child
							ActionSet actionSet = (ActionSet) actionGroup.
									iterator().next();
							text = NLS.bind(
									WorkbenchMessages.HideItems_unavailableChildCommandGroup,
									actionSet.descriptor.getId(),
									actionSet.descriptor.getLabel());
						} else {
							//i.e. multiple children
							String commandGroupList = null;
		
							for (Iterator i = actionGroup.iterator(); i.hasNext();) {
								ActionSet actionSet = (ActionSet) i.next();
		
								// For each action set, make a link for it, set
								// the href to its id
								String commandGroupLink = MessageFormat.format(
										"<a href=\"{0}\">{1}</a>", //$NON-NLS-1$
										new Object[] { actionSet.descriptor.getId(),
												actionSet.descriptor.getLabel() });
		
								if (commandGroupList == null)
									commandGroupList = commandGroupLink;
								else
									commandGroupList = Util.createList(
											commandGroupList, commandGroupLink);
							}
							
							commandGroupList = NLS.bind(
									"{0}{1}", new Object[] { NEW_LINE, commandGroupList }); //$NON-NLS-1$
							text = NLS.bind(
									WorkbenchMessages.HideItems_unavailableChildCommandGroups,
									commandGroupList);
						}
					}
				}
				
				if(text != null) {
					Link link = createEntryWithLink(destination, image, text);
					link.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {
							widgetSelected(e);
						}
	
						public void widgetSelected(SelectionEvent e) {
							ActionSet actionSet = (ActionSet) idToActionSet
									.get(e.text);
							if (actionSet == null) {
								hide();
								viewActionSet(item);
							} else {
								hide();
								viewActionSet(actionSet);
							}
						}
					});
				}
			}

			// Show key binding info
			if (showKeyBindings && getCommandID(item) != null) {
				// See if there is a command associated with the command id
				ICommandService commandService = (ICommandService) window
						.getService(ICommandService.class);
				Command command = commandService.getCommand(getCommandID(item));

				if (command != null && command.isDefined()) {
					// Find the bindings and list them as a string
					Binding[] bindings = getKeyBindings(item);
					String keybindings = keyBindingsAsString(bindings);

					String text = null;

					// Is it possible for this item to be visible?
					final boolean available = (item.getActionSet() == null)
							|| (item.getActionSet().isActive());

					if (bindings.length > 0) {
						if (available)
							text = NLS.bind(
									WorkbenchMessages.HideItems_keyBindings,
									keybindings);
						else
							text = NLS
									.bind(
											WorkbenchMessages.HideItems_keyBindingsActionSetUnavailable,
											keybindings);
					} else {
						if (available)
							text = WorkbenchMessages.HideItems_noKeyBindings;
						else
							text = WorkbenchMessages.HideItems_noKeyBindingsActionSetUnavailable;
					}

					// Construct link to go to the preferences page for key
					// bindings
					final Object highlight;
					if (bindings.length == 0) {
						Map parameters = new HashMap();

						// If item is a shortcut, need to add a parameter to go
						// to
						// the correct item
						if (item instanceof ShortcutItem) {
							if (isNewWizard(item)) {
								parameters.put(
												IWorkbenchCommandConstants.FILE_NEW_PARM_WIZARDID,
										getParamID(item));
							} else if (isShowPerspective(item)) {
								parameters
										.put(
												IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID,
												getParamID(item));
							} else if (isShowView(item)) {
								parameters.put(
										IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID,
										getParamID(item));
							}
						}

						ParameterizedCommand pc = ParameterizedCommand
								.generateCommand(command, parameters);
						highlight = pc;
					} else {
						highlight = bindings[0];
					}

					Link bindingLink = createEntryWithLink(destination, null,
							text);

					bindingLink.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {
							widgetDefaultSelected(e);
						}

						public void widgetSelected(SelectionEvent e) {
							PreferenceDialog dialog = PreferencesUtil
									.createPreferenceDialogOn(getShell(),
											KEYS_PREFERENCE_PAGE_ID,
											new String[0], highlight);
							hide();
							dialog.open();
						}
					});
				}
			}

			// Show dynamic menu item info
			if (item instanceof DynamicContributionItem) {
				DynamicContributionItem dynamic = ((DynamicContributionItem) item);
				StringBuffer text = new StringBuffer();
				final List currentItems = dynamic.getCurrentItems();

				if (currentItems.size() > 0) {
					// Create a list of the currently displayed items
					text.append(WorkbenchMessages.HideItems_dynamicItemList);
					for (Iterator i = currentItems.iterator(); i.hasNext();) {
						MenuItem menuItem = (MenuItem) i.next();
						text.append(NEW_LINE).append("- ") //$NON-NLS-1$
								.append(menuItem.getText());
					}
				} else {
					text
							.append(WorkbenchMessages.HideItems_dynamicItemEmptyList);
				}
				createEntry(destination, null, text.toString());
			}
		}

		protected Object getModelElement(Event event) {
			org.eclipse.swt.widgets.TreeItem treeItem = tree.getItem(new Point(
					event.x, event.y));
			if (treeItem == null)
				return null;
			return treeItem.getData();
		}
	}

	/**
	 * Filters out contribution items which are not in a given action set.
	 * 
	 * @since 3.5
	 */
	private static class ActionSetFilter extends ViewerFilter {
		private ActionSet actionSet;

		public void setActionSet(ActionSet actionSet) {
			this.actionSet = actionSet;
		}

		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (!(element instanceof DisplayItem))
				return false;
			if (actionSet == null)
				return false;
			return includeInSetStructure((DisplayItem) element, actionSet);
		}
	}

	/**
	 * A check provider which calculates checked state based on leaf states in
	 * the tree (as opposed to children in a model).
	 * 
	 * @since 3.5
	 */
	private static class FilteredTreeCheckProvider implements
			ICheckStateProvider {
		private ITreeContentProvider contentProvider;
		private ViewerFilter filter;

		public FilteredTreeCheckProvider(ITreeContentProvider contentProvider,
				ViewerFilter filter) {
			this.contentProvider = contentProvider;
			this.filter = filter;
		}

		public boolean isChecked(Object element) {
			TreeItem treeItem = (TreeItem) element;
			return getLeafStates(treeItem, contentProvider, filter) != TreeManager.CHECKSTATE_UNCHECKED;
		}

		public boolean isGrayed(Object element) {
			TreeItem treeItem = (TreeItem) element;
			return getLeafStates(treeItem, contentProvider, filter) == TreeManager.CHECKSTATE_GRAY;
		}
	}

	/**
	 * A check listener to bring about the expected change in a model based on a
	 * check event in a filtered viewer. Since the checked state of a parent in
	 * a filtered viewer is not based on its model state, but rather its leafs'
	 * states, when a non-leaf element's check state changes, its model state
	 * does not necessarily change, but its leafs' model states do.
	 * 
	 * @since 3.5
	 */
	private static class FilteredViewerCheckListener implements
			ICheckStateListener {
		private ITreeContentProvider contentProvider;
		private ViewerFilter filter;

		public FilteredViewerCheckListener(
				ITreeContentProvider contentProvider, ViewerFilter filter) {
			this.contentProvider = contentProvider;
			this.filter = filter;
		}

		public void checkStateChanged(CheckStateChangedEvent event) {
			setAllLeafs((DisplayItem) event.getElement(), event
					.getChecked(), contentProvider, filter);
		}
	}

	/**
	 * On a model change, update a filtered listener. While the check listener
	 * provided by the model will take care of the elements which change, since
	 * we simulate our own check state of parents, the parents may need to be
	 * updated.
	 * 
	 * @since 3.5
	 */
	private final class FilteredModelCheckListener implements CheckListener {
		private final ActionSetFilter filter;
		private final StructuredViewer viewer;

		private FilteredModelCheckListener(ActionSetFilter filter,
				StructuredViewer viewer) {
			this.filter = filter;
			this.viewer = viewer;
		}

		public void checkChanged(TreeItem changedItem) {
			TreeItem item = changedItem;
			boolean update = false;

			// Force an update on all parents.
			while (item != null) {
				update = update || filter.select(null, null, item);
				if (update) {
					viewer.update(item, null);
				}
				item = item.getParent();
			}
		}
	}

	/**
	 * A check listener which, upon changing the check state of a contribution
	 * item, checks if that item is eligible to be checked (i.e. it is in an
	 * available action set), and if not, informs the user of the illegal
	 * operation. If the operation is legal, the event is forwarded to the check
	 * listener to actually perform a useful action.
	 * 
	 * @since 3.5
	 */
	private class UnavailableContributionItemCheckListener implements
			ICheckStateListener {
		private CheckboxTreeViewer viewer;
		private ICheckStateListener originalListener;

		/**
		 * @param viewer
		 *            the viewer being listened to
		 * @param originalListener
		 *            the listener to invoke upon a legal action
		 */
		public UnavailableContributionItemCheckListener(
				CheckboxTreeViewer viewer, ICheckStateListener originalListener) {
			this.viewer = viewer;
			this.originalListener = originalListener;
		}

		public void checkStateChanged(CheckStateChangedEvent event) {
			DisplayItem item = (DisplayItem) event.getElement();
			ViewerFilter[] filters = viewer.getFilters();
			boolean isEffectivelyAvailable = isEffectivelyAvailable(item, filters.length > 0 ? filters[0] : null);

			if (isEffectivelyAvailable) {
				// legal action - invoke the listener which will do actual work
				originalListener.checkStateChanged(event);
				return;
			}

			boolean isAvailable = isAvailable(item);
			viewer.update(event.getElement(), null);

			if (isAvailable) {
				// the case where this item is unavailable because of its
				// children
				if (viewer.getExpandedState(item)) {
					MessageBox mb = new MessageBox(viewer.getControl()
							.getShell(), SWT.OK | SWT.ICON_WARNING | SWT.SHEET);
					mb
							.setText(WorkbenchMessages.HideItemsCannotMakeVisible_dialogTitle);
					mb
							.setMessage(NLS
									.bind(
											WorkbenchMessages.HideItemsCannotMakeVisible_unavailableChildrenText,
											item.getLabel()));
					mb.open();
				} else {
					MessageBox mb = new MessageBox(viewer.getControl()
							.getShell(), SWT.OK | SWT.ICON_WARNING | SWT.SHEET);
					mb
							.setText(WorkbenchMessages.HideItemsCannotMakeVisible_dialogTitle);
					mb
							.setMessage(NLS
									.bind(
											WorkbenchMessages.HideItemsCannotMakeVisible_unavailableChildrenText,
											item.getLabel()));
					mb.open();
				}
			} else {
				// the case where this item is unavailable because it belongs to
				// an unavailable action set
				MessageBox mb = new MessageBox(viewer.getControl().getShell(),
						SWT.YES | SWT.NO | SWT.ICON_WARNING | SWT.SHEET);
				mb
						.setText(WorkbenchMessages.HideItemsCannotMakeVisible_dialogTitle);
				final String errorExplanation = NLS
						.bind(
								WorkbenchMessages.HideItemsCannotMakeVisible_unavailableCommandGroupText,
								item.getLabel(), item.getActionSet());
				final String message = NLS
						.bind(
								"{0}{1}{1}{2}", new Object[] { errorExplanation, NEW_LINE, WorkbenchMessages.HideItemsCannotMakeVisible_switchToCommandGroupTab }); //$NON-NLS-1$
				mb.setMessage(message);
				if (mb.open() == SWT.YES) {
					viewActionSet(item);
				}
			}
		}
	}

	/**
	 * A label provider which takes the default label provider in the
	 * TreeManager, and adds on functionality to gray out text and icons of
	 * contribution items whose action sets are unavailable.
	 * 
	 * @since 3.5
	 * 
	 */
	private class GrayOutUnavailableLabelProvider extends
			TreeManager.TreeItemLabelProvider implements IColorProvider {
		private Display display;
		private ViewerFilter filter;

		public GrayOutUnavailableLabelProvider(Display display, ViewerFilter filter) {
			this.display = display;
			this.filter = filter;
		}

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			if (!isEffectivelyAvailable((DisplayItem) element, filter)) {
				return display.getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}

		public Image getImage(Object element) {
			Image actual = super.getImage(element);

			if (element instanceof DisplayItem && actual != null) {
				DisplayItem item = (DisplayItem) element;
				if (!isEffectivelyAvailable(item, filter)) {
					ImageDescriptor original = ImageDescriptor
							.createFromImage(actual);
					ImageDescriptor disable = ImageDescriptor.createWithFlags(
							original, SWT.IMAGE_DISABLE);
					Image newImage = disable.createImage();
					toDispose.add(newImage);
					return newImage;
				}
			}

			return actual;
		}
	}

	/**
	 * The proxy IActionBarConfigurer that gets passed to the application's
	 * ActionBarAdvisor. This is used to construct a representation of the
	 * window's hardwired menus and toolbars in order to display their structure
	 * properly in the preview panes.
	 * 
	 * @since 3.5
	 */
	public class CustomizeActionBars implements IActionBarConfigurer2,
			IActionBars2 {

		IWorkbenchWindowConfigurer configurer;

		/**
		 * Fake action bars to use to build the menus and toolbar contributions
		 * for the workbench. We cannot use the actual workbench action bars,
		 * since doing so would make the action set items visible.
		 */
		private MenuManager menuManager = new MenuManager();
		private CoolBarManager coolBarManager = new CoolBarManager();
		private StatusLineManager statusLineManager = new StatusLineManager();

		/**
		 * Create a new instance of this class.
		 * 
		 * @param configurer
		 *            the configurer
		 */
		public CustomizeActionBars(IWorkbenchWindowConfigurer configurer) {
			this.configurer = configurer;
		}

		public IWorkbenchWindowConfigurer getWindowConfigurer() {
			return configurer;
		}

		public IMenuManager getMenuManager() {
			return menuManager;
		}

		public IStatusLineManager getStatusLineManager() {
			return statusLineManager;
		}

		public ICoolBarManager getCoolBarManager() {
			return coolBarManager;
		}

		public IToolBarManager getToolBarManager() {
			return null;
		}

		public void setGlobalActionHandler(String actionID, IAction handler) {
		}

		public void updateActionBars() {
		}

		public void clearGlobalActionHandlers() {
		}

		public IAction getGlobalActionHandler(String actionId) {
			return null;
		}

		public void registerGlobalAction(IAction action) {
		}

		/**
		 * Clean up the action bars.
		 */
		public void dispose() {
			coolBarManager.dispose();
			menuManager.dispose();
			statusLineManager.dispose();
		}

		public final IServiceLocator getServiceLocator() {
			return configurer.getWindow();
		}

		public IToolBarContributionItem createToolBarContributionItem(
				IToolBarManager toolBarManager, String id) {
			return new ToolBarContributionItem2(toolBarManager, id);
		}

		public IToolBarManager createToolBarManager() {
			return new ToolBarManager();
		}
	}

	/**
	 * Create an instance of this Dialog.
	 * 
	 * @param configurer
	 *            the configurer
	 * @param persp
	 *            the perspective
	 */
	public CustomizePerspectiveDialog(IWorkbenchWindowConfigurer configurer,
			Perspective persp) {
		super(configurer.getWindow().getShell());
		this.treeManager = new TreeManager();
		this.configurer = configurer;
		perspective = persp;
		window = (WorkbenchWindow) configurer.getWindow();

		toDispose = new HashSet();

		initializeIcons();

		initializeActionSetInput();
		loadMenuAndToolbarStructure();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		String title = perspective.getDesc().getLabel();

		title = NLS.bind(WorkbenchMessages.ActionSetSelection_customize, title);
		shell.setText(title);
		window.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.ACTION_SET_SELECTION_DIALOG);
	}

	protected Control createDialogArea(Composite parent) {
		// Create a font for titles in the tooltips
		FontData[] defaultFont = JFaceResources.getDefaultFont().getFontData();
		FontData boldFontData = new FontData(defaultFont[0].getName(),
				defaultFont[0].getHeight(), SWT.BOLD);
		tooltipHeading = new Font(parent.getDisplay(), boldFontData);

		Composite composite = (Composite) super.createDialogArea(parent);

		// tab folder
		tabFolder = new TabFolder(composite, SWT.NONE);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = convertHorizontalDLUsToPixels(TAB_WIDTH_IN_DLUS);
		gd.heightHint = convertVerticalDLUsToPixels(TAB_HEIGHT_IN_DLUS);
		tabFolder.setLayoutData(gd);

		// Tool Bar Item Hiding Page
		TabItem tab = new TabItem(tabFolder, SWT.NONE);
		tab.setText(WorkbenchMessages.HideToolBarItems_toolBarItemsTab);
		tab.setControl(createToolBarVisibilityPage(tabFolder));

		// Menu Item Hiding Page
		tab = new TabItem(tabFolder, SWT.NONE);
		tab.setControl(createMenuVisibilityPage(tabFolder));
		tab.setText(WorkbenchMessages.HideMenuItems_menuItemsTab);

		// Action Set Availability Page
		actionSetTab = new TabItem(tabFolder, SWT.NONE);
		actionSetTab
				.setText(WorkbenchMessages.ActionSetSelection_actionSetsTab);
		actionSetTab.setControl(createActionSetAvailabilityPage(tabFolder));

		// Shortcuts Page
		if (showShortcutTab()) {
			TabItem item1 = new TabItem(tabFolder, SWT.NONE);
			item1.setText(WorkbenchMessages.Shortcuts_shortcutTab);
			item1.setControl(createShortCutsPage(tabFolder));
		}

		applyDialogFont(tabFolder);

		return composite;
	}

	private Composite createShortCutsPage(Composite parent) {
		GridData data;

		Composite menusComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		menusComposite.setLayout(layout);

		// Select... label
		Label label = new Label(menusComposite, SWT.WRAP);
		label.setText(NLS.bind(
				WorkbenchMessages.Shortcuts_selectShortcutsLabel, perspective
						.getDesc().getLabel()));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		label.setLayoutData(data);

		Label sep = new Label(menusComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		SashForm sashComposite = new SashForm(menusComposite, SWT.HORIZONTAL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		sashComposite.setLayoutData(data);

		// Menus List
		Composite menusGroup = new Composite(sashComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		menusGroup.setLayout(layout);
		menusGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		label = new Label(menusGroup, SWT.WRAP);
		label.setText(WorkbenchMessages.Shortcuts_availableMenus);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Combo menusCombo = new Combo(menusGroup, SWT.READ_ONLY);
		menusCombo
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ComboViewer menusViewer = new ComboViewer(menusCombo);
		menusViewer.setContentProvider(TreeManager.getTreeContentProvider());
		menusViewer.setLabelProvider(TreeManager.getLabelProvider());

		// Categories Tree
		label = new Label(menusGroup, SWT.WRAP);
		label.setText(WorkbenchMessages.Shortcuts_availableCategories);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final CheckboxTreeViewer menuCategoriesViewer = new CheckboxTreeViewer(
				menusGroup);
		menuCategoriesViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		menuCategoriesViewer.setLabelProvider(TreeManager.getLabelProvider());
		menuCategoriesViewer.setContentProvider(TreeManager
				.getTreeContentProvider());
		menuCategoriesViewer.setComparator(new WorkbenchViewerComparator());
		menuCategoriesViewer.setCheckStateProvider(new CategoryCheckProvider());
		menuCategoriesViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Category category = (Category) event.getElement();
				category.setItemsState(event.getChecked());
				updateCategoryAndParents(menuCategoriesViewer, category);
			}
		});

		treeManager.addListener(new CheckListener() {
			public void checkChanged(TreeItem changedItem) {
				if (changedItem instanceof Category) {
					menuCategoriesViewer.update(changedItem, null);
				} else if (changedItem instanceof ShortcutItem) {
					ShortcutItem item = (ShortcutItem) changedItem;
					if (item.getCategory() != null) {
						item.getCategory().update();
						updateCategoryAndParents(menuCategoriesViewer, item
								.getCategory());
					}
				}
			}
		});

		// Menu items list
		Composite menuItemsGroup = new Composite(sashComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		menuItemsGroup.setLayout(layout);
		menuItemsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		label = new Label(menuItemsGroup, SWT.WRAP);
		label.setText(WorkbenchMessages.Shortcuts_allShortcuts);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final CheckboxTableViewer menuItemsViewer = CheckboxTableViewer
				.newCheckList(menuItemsGroup, SWT.BORDER | SWT.H_SCROLL
						| SWT.V_SCROLL);
		Table menuTable = menuItemsViewer.getTable();
		menuTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		menuItemsViewer.setLabelProvider(new ShortcutLabelProvider());
		menuItemsViewer.setCheckStateProvider(TreeManager
				.getCheckStateProvider());
		menuItemsViewer.addCheckStateListener(treeManager
				.getViewerCheckStateListener());
		treeManager.getCheckListener(menuItemsViewer);

		menuItemsViewer
				.setContentProvider(new TreeManager.TreeItemContentProvider() {
					public Object[] getChildren(Object parentElement) {
						if (parentElement instanceof Category)
							return ((Category) parentElement)
									.getContributionItems().toArray();
						return super.getChildren(parentElement);
					}
				});
		menuItemsViewer.setComparator(new WorkbenchViewerComparator());

		// update menuCategoriesViewer, and menuItemsViewer on a change to
		// menusViewer
		menusViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						Category category = (Category) ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						menuCategoriesViewer.setInput(category);
						menuItemsViewer.setInput(category);
						if (category.getChildren().size() != 0) {
							setSelectionOn(menuCategoriesViewer, category
									.getChildren().get(0));
						}
					}
				});

		// update menuItemsViewer on a change to menuCategoriesViewer
		menuCategoriesViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						Category category = (Category) ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						menuItemsViewer.setInput(category);
					}
				});

		menuTable.setHeaderVisible(true);
		int[] columnWidths = new int[shortcutMenuColumnWidths.length];
		for (int i = 0; i < shortcutMenuColumnWidths.length; i++) {
			columnWidths[i] = convertHorizontalDLUsToPixels(shortcutMenuColumnWidths[i]);
		}
		for (int i = 0; i < shortcutMenuColumnHeaders.length; i++) {
			TableColumn tc = new TableColumn(menuTable, SWT.NONE, i);
			tc.setResizable(true);
			tc.setText(shortcutMenuColumnHeaders[i]);
			tc.setWidth(columnWidths[i]);
		}
		sashComposite.setWeights(new int[] { 30, 70 });

		menusViewer.setInput(shortcuts);

		if (shortcuts.getChildren().size() > 0) {
			setSelectionOn(menusViewer, shortcuts.getChildren().get(0));
		}

		return menusComposite;
	}

	private Composite createActionSetAvailabilityPage(Composite parent) {
		GridData data;

		Composite actionSetsComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		actionSetsComposite.setLayout(layout);

		// Select... label
		Label label = new Label(actionSetsComposite, SWT.WRAP);
		label.setText(NLS.bind(
				WorkbenchMessages.ActionSetSelection_selectActionSetsLabel,
				perspective.getDesc().getLabel()));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		label.setLayoutData(data);

		Label sep = new Label(actionSetsComposite, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		SashForm sashComposite = new SashForm(actionSetsComposite,
				SWT.HORIZONTAL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		sashComposite.setLayoutData(data);

		// Action Set List Composite
		Composite actionSetGroup = new Composite(sashComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		actionSetGroup.setLayout(layout);
		actionSetGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		label = new Label(actionSetGroup, SWT.WRAP);
		label.setText(WorkbenchMessages.ActionSetSelection_availableActionSets);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final CheckboxTableViewer actionSetsViewer = CheckboxTableViewer
				.newCheckList(actionSetGroup, SWT.BORDER | SWT.H_SCROLL
						| SWT.V_SCROLL);
		actionSetAvailabilityTable = actionSetsViewer;
		actionSetsViewer.getTable().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		actionSetsViewer.setLabelProvider(new LabelProvider());
		actionSetsViewer.setContentProvider(new ArrayContentProvider());
		actionSetsViewer.setComparator(new WorkbenchViewerComparator());
		actionSetsViewer.setCheckStateProvider(new ICheckStateProvider() {
			public boolean isChecked(Object element) {
				return ((ActionSet) element).isActive();
			}

			public boolean isGrayed(Object element) {
				return false;
			}
		});
		actionSetsViewer.setInput(actionSets.toArray());

		Table table = actionSetsViewer.getTable();
		new TableToolTip(table);

		final ActionSet[] selectedActionSet = { null };

		// Filter to show only branches necessary for the selected action set.
		final ViewerFilter setFilter = new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (selectedActionSet[0] == null)
					return false;
				return includeInSetStructure((DisplayItem) element,
						selectedActionSet[0]);
			}
		};

		// Updates the check state of action sets
		actionSetsViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				final ActionSet actionSet = (ActionSet) event.getElement();
				if (event.getChecked()) {
					actionSet.setActive(true);
					for (Iterator i = actionSet.contributionItems.iterator(); i
							.hasNext();) {
						DisplayItem item = (DisplayItem) i.next();
						item.setCheckState(true);
					}
				} else {
					actionSet.setActive(false);
				}
			}
		});

		// Menu and toolbar composite
		Composite actionGroup = new Composite(sashComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		actionGroup.setLayout(layout);
		actionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite menubarGroup = new Composite(actionGroup, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		menubarGroup.setLayout(layout);
		menubarGroup
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		label = new Label(menubarGroup, SWT.WRAP);
		label.setText(WorkbenchMessages.ActionSetSelection_menubarActions);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final TreeViewer actionSetMenuViewer = new TreeViewer(menubarGroup);
		actionSetMenuViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		actionSetMenuViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		actionSetMenuViewer.setUseHashlookup(true);
		actionSetMenuViewer.setContentProvider(TreeManager
				.getTreeContentProvider());
		actionSetMenuViewer.setLabelProvider(TreeManager.getLabelProvider());
		actionSetMenuViewer.addFilter(setFilter);
		actionSetMenuViewer.setInput(menuItems);

		Tree tree = actionSetMenuViewer.getTree();
		new ItemDetailToolTip(actionSetMenuViewer, tree, false, true, setFilter);

		Composite toolbarGroup = new Composite(actionGroup, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		toolbarGroup.setLayout(layout);
		toolbarGroup
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		label = new Label(toolbarGroup, SWT.WRAP);
		label.setText(WorkbenchMessages.ActionSetSelection_toolbarActions);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final TreeViewer actionSetToolbarViewer = new TreeViewer(toolbarGroup);
		actionSetToolbarViewer
				.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		actionSetToolbarViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		actionSetToolbarViewer.setContentProvider(TreeManager
				.getTreeContentProvider());
		actionSetToolbarViewer.setLabelProvider(TreeManager.getLabelProvider());
		actionSetToolbarViewer.addFilter(setFilter);
		actionSetToolbarViewer.setInput(toolBarItems);

		tree = actionSetToolbarViewer.getTree();
		new ItemDetailToolTip(actionSetToolbarViewer, tree, false, true, setFilter);

		// Updates the menu item and toolbar items tree viewers when the
		// selection changes
		actionSetsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						selectedActionSet[0] = (ActionSet) ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						actionSetMenuViewer.setInput(menuItems);
						actionSetToolbarViewer.setInput(toolBarItems);
					}
				});

		sashComposite.setWeights(new int[] { 30, 70 });

		return actionSetsComposite;
	}

	/**
	 * Creates the page used to allow users to choose menu items to hide.
	 */
	private Composite createMenuVisibilityPage(Composite parent) {
		GridData data;

		Composite hideMenuItemsComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		hideMenuItemsComposite.setLayout(layout);

		// Label for entire tab
		Label label = new Label(hideMenuItemsComposite, SWT.WRAP);
		label.setText(WorkbenchMessages.HideMenuItems_chooseMenuItemsLabel);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		label.setLayoutData(data);

		Label sep = new Label(hideMenuItemsComposite, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Main contents of tab
		final PageBook book = new PageBook(hideMenuItemsComposite, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		book.setLayoutData(data);

		// Simple view: just the menu structure
		final Composite simpleComposite = createItemStructureGroup(book,
				WorkbenchMessages.HideMenuItems_menuStructure);
		menuStructureViewer1 = initStructureViewer(simpleComposite,
				new TreeManager.ViewerCheckStateListener(), null);

		// Update the viewer when the model changes
		treeManager.getCheckListener(menuStructureViewer1); // To update ctv on
															// model changes

		// Simply grab the checkstate out of the model
		menuStructureViewer1.setCheckStateProvider(TreeManager
				.getCheckStateProvider());

		// Init with input
		menuStructureViewer1.setInput(menuItems);

		// Advanced view: action set with filtered menu structure
		final SashForm advancedComposite = new SashForm(book, SWT.HORIZONTAL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		advancedComposite.setLayoutData(data);

		// Action set list
		final TableViewer actionSetViewer = initActionSetViewer(createActionSetGroup(advancedComposite));

		// Filter to only show action sets which have useful menu items
		actionSetViewer.addFilter(new ShowUsedActionSetsFilter(menuItems));

		// Init with input
		actionSetViewer.setInput(actionSets.toArray());

		// Filter to only show items in the current action set
		final ActionSetFilter menuStructureFilterByActionSet = new ActionSetFilter();

		final Composite menuStructureComposite = createItemStructureGroup(
				advancedComposite,
				WorkbenchMessages.HideMenuItems_menuStructure);
		final ICheckStateListener menuStructureFilter = new FilteredViewerCheckListener(
				TreeManager.getTreeContentProvider(),
				menuStructureFilterByActionSet);
		menuStructureViewer2 = initStructureViewer(menuStructureComposite,
				menuStructureFilter, menuStructureFilterByActionSet);

		treeManager.addListener(new FilteredModelCheckListener(
				menuStructureFilterByActionSet, menuStructureViewer2));

		menuStructureViewer2.addFilter(menuStructureFilterByActionSet);

		// Update filter when a new action set is selected
		actionSetViewer
				.addSelectionChangedListener(new ActionSetSelectionChangedListener(
						menuStructureViewer2, menuStructureFilterByActionSet));

		// Check state provider to emulate standard SWT
		// behaviour on visual tree
		menuStructureViewer2
				.setCheckStateProvider(new FilteredTreeCheckProvider(
						TreeManager.getTreeContentProvider(),
						menuStructureFilterByActionSet));

		// Init input
		menuStructureViewer2.setInput(menuItems);

		// Override any attempts to set an item to visible
		// which exists in an unavailable action set
		treeManager.addListener(new CheckListener() {
			public void checkChanged(TreeItem changedItem) {
				if (!(changedItem instanceof DisplayItem))
					return;
				if (!changedItem.getState())
					return;
				if (isAvailable((DisplayItem) changedItem))
					return;
				changedItem.setCheckState(false);
			}
		});

		final Button showCommandGroupFilterButton = new Button(
				hideMenuItemsComposite, SWT.CHECK);
		showCommandGroupFilterButton
				.setText(WorkbenchMessages.HideItems_turnOnActionSets);
		showCommandGroupFilterButton
				.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						if (showCommandGroupFilterButton.getSelection()) {
							Object o = ((StructuredSelection) menuStructureViewer1
									.getSelection()).getFirstElement();
							ActionSet initSelectAS = null;
							DisplayItem initSelectCI = null;
							if (o instanceof DisplayItem) {
								initSelectCI = ((DisplayItem) o);
								initSelectAS = initSelectCI.getActionSet();
							}
							if (initSelectAS == null) {
								initSelectAS = (ActionSet) actionSetViewer
										.getElementAt(0);
							}
							setSelectionOn(actionSetViewer, initSelectAS);
							actionSetViewer.reveal(initSelectAS);
							if (initSelectCI != null) {
								setSelectionOn(menuStructureViewer2,
										initSelectCI);
								menuStructureViewer2.reveal(initSelectCI);
							}
							book.showPage(advancedComposite);
						} else {
							book.showPage(simpleComposite);
						}
					}
				});

		book.showPage(simpleComposite);
		advancedComposite.setWeights(new int[] { 30, 70 });

		return hideMenuItemsComposite;
	}

	/**
	 * Creates the page used to allow users to choose menu items to hide.
	 */
	private Composite createToolBarVisibilityPage(Composite parent) {
		GridData data;

		Composite hideToolbarItemsComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		hideToolbarItemsComposite.setLayout(layout);

		// Label for entire tab
		Label label = new Label(hideToolbarItemsComposite, SWT.WRAP);
		label
				.setText(WorkbenchMessages.HideToolBarItems_chooseToolBarItemsLabel);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		label.setLayoutData(data);

		Label sep = new Label(hideToolbarItemsComposite, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Main contents of tab
		final PageBook book = new PageBook(hideToolbarItemsComposite, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		book.setLayoutData(data);

		// Simple view: just the toolbar structure
		final Composite simpleComposite = createItemStructureGroup(book,
				WorkbenchMessages.HideToolBarItems_toolBarStructure);
		toolbarStructureViewer1 = initStructureViewer(simpleComposite,
				new TreeManager.ViewerCheckStateListener(), null);

		// Update the viewer when the model changes
		treeManager.getCheckListener(toolbarStructureViewer1); // To update ctv
																// on model
																// changes

		// Simply grab the check state out of the model
		toolbarStructureViewer1.setCheckStateProvider(TreeManager
				.getCheckStateProvider());

		// Init with input
		toolbarStructureViewer1.setInput(toolBarItems);

		// Advanced view: action set with filtered toolbar structure
		final SashForm advancedComposite = new SashForm(book, SWT.HORIZONTAL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		advancedComposite.setLayoutData(data);

		// Action set list
		final TableViewer actionSetViewer = initActionSetViewer(createActionSetGroup(advancedComposite));

		// Filter to only show action sets which have useful toolbar items
		actionSetViewer.addFilter(new ShowUsedActionSetsFilter(toolBarItems));

		// Init with input
		actionSetViewer.setInput(actionSets.toArray());

		// Filter to only show items in the current action set
		final ActionSetFilter toolbarStructureFilterByActionSet = new ActionSetFilter();

		final Composite toolbarStructureComposite = createItemStructureGroup(
				advancedComposite,
				WorkbenchMessages.HideToolBarItems_toolBarStructure);
		final ICheckStateListener toolbarStructureFilter = new FilteredViewerCheckListener(
				TreeManager.getTreeContentProvider(),
				toolbarStructureFilterByActionSet);
		toolbarStructureViewer2 = initStructureViewer(
				toolbarStructureComposite, toolbarStructureFilter,
				toolbarStructureFilterByActionSet);

		toolbarStructureViewer2.addFilter(toolbarStructureFilterByActionSet);

		treeManager.addListener(new FilteredModelCheckListener(
				toolbarStructureFilterByActionSet, toolbarStructureViewer2));

		// Update filter when a new action set is selected
		actionSetViewer
				.addSelectionChangedListener(new ActionSetSelectionChangedListener(
						toolbarStructureViewer2,
						toolbarStructureFilterByActionSet));

		// Check state provider to emulate standard SWT
		// behaviour on visual tree
		toolbarStructureViewer2
				.setCheckStateProvider(new FilteredTreeCheckProvider(
						TreeManager.getTreeContentProvider(),
						toolbarStructureFilterByActionSet));

		// Init input
		toolbarStructureViewer2.setInput(toolBarItems);

		// Override any attempts to set an item to visible
		// which exists in an unavailable action set
		treeManager.addListener(new CheckListener() {
			public void checkChanged(TreeItem changedItem) {
				if (!(changedItem instanceof DisplayItem))
					return;
				if (!changedItem.getState())
					return;
				if (isAvailable((DisplayItem) changedItem))
					return;
				changedItem.setCheckState(false);
			}
		});

		final Button showCommandGroupFilterButton = new Button(
				hideToolbarItemsComposite, SWT.CHECK);
		showCommandGroupFilterButton
				.setText(WorkbenchMessages.HideItems_turnOnActionSets);
		showCommandGroupFilterButton
				.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						if (showCommandGroupFilterButton.getSelection()) {
							Object o = ((StructuredSelection) toolbarStructureViewer1
									.getSelection()).getFirstElement();
							ActionSet initSelectAS = null;
							DisplayItem initSelectCI = null;
							if (o instanceof DisplayItem) {
								initSelectCI = ((DisplayItem) o);
								initSelectAS = initSelectCI.getActionSet();
							}
							if (initSelectAS == null) {
								initSelectAS = (ActionSet) actionSetViewer
										.getElementAt(0);
							}
							setSelectionOn(actionSetViewer, initSelectAS);
							actionSetViewer.reveal(initSelectAS);
							if (initSelectCI != null) {
								setSelectionOn(toolbarStructureViewer2,
										initSelectCI);
								toolbarStructureViewer2.reveal(initSelectCI);
							}
							book.showPage(advancedComposite);
						} else {
							book.showPage(simpleComposite);
						}
					}
				});

		book.showPage(simpleComposite);
		advancedComposite.setWeights(new int[] { 30, 70 });

		return hideToolbarItemsComposite;
	}

	/**
	 * Creates a table to display action sets.
	 * 
	 * @param parent
	 * @return a viewer to display action sets
	 */
	private TableViewer initActionSetViewer(Composite parent) {
		// List of categories
		final TableViewer actionSetViewer = new TableViewer(parent, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		actionSetViewer.getTable().setLayoutData(
				new GridData(GridData.FILL_BOTH));
		actionSetViewer.setLabelProvider(new LabelProvider());
		actionSetViewer.setComparator(new WorkbenchViewerComparator());
		actionSetViewer.setContentProvider(new ArrayContentProvider());

		// Tooltip on tree items
		Table table = actionSetViewer.getTable();
		new TableToolTip(table);
		return actionSetViewer;
	}

	/**
	 * Creates a CheckboxTreeViewer to display menu or toolbar structure.
	 * 
	 * @param parent
	 * @param checkStateListener
	 *            the listener which listens to the viewer for check changes
	 * @param filter the filter used in the viewer (null for none)
	 * @return A viewer within <code>parent</code> which will show menu or
	 *         toolbar structure. It comes setup, only missing a
	 *         CheckStateProvider and its input.
	 */
	private CheckboxTreeViewer initStructureViewer(Composite parent,
			ICheckStateListener checkStateListener, ViewerFilter filter) {
		CheckboxTreeViewer ctv = new CheckboxTreeViewer(parent, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		ctv.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		ctv.setUseHashlookup(true);
		ctv.setContentProvider(TreeManager.getTreeContentProvider());
		// use an UnavailableContributionItemCheckListener to filter check
		// events: if it is legal, forward it to the actual checkStateListener,
		// if not, inform the user
		ctv.addCheckStateListener(new UnavailableContributionItemCheckListener(
				ctv, checkStateListener));
		ctv.setLabelProvider(new GrayOutUnavailableLabelProvider(parent
				.getDisplay(), filter));
		new ItemDetailToolTip(ctv, ctv.getTree(), true, true, filter);
		return ctv;
	}

	/**
	 * Creates a composite to put a tree viewer in to display menu or toolbar
	 * items.
	 */
	private static Composite createItemStructureGroup(
			final Composite composite, String labelText) {
		GridLayout layout;
		Label label;
		layout = new GridLayout();
		Composite menubarGroup = new Composite(composite, SWT.NONE);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		menubarGroup.setLayout(layout);
		menubarGroup
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		label = new Label(menubarGroup, SWT.WRAP);
		label.setText(labelText);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		return menubarGroup;
	}

	/**
	 * Creates a composite to put a viewer in to display action sets.
	 */
	private static Composite createActionSetGroup(final Composite composite) {
		GridLayout layout;
		Label label;
		Composite actionSetGroup = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		actionSetGroup.setLayout(layout);
		actionSetGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		label = new Label(actionSetGroup, SWT.WRAP);
		label.setText(WorkbenchMessages.HideItems_commandGroupTitle);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		return actionSetGroup;
	}

	/**
	 * Set the selection on a structured viewer.
	 * 
	 * @param viewer
	 * @param selected
	 */
	private void setSelectionOn(Viewer viewer, final Object selected) {
		viewer.setSelection(new StructuredSelection(selected), true);
	}

	/**
	 * Searches deeply to see if <code>item</code> is a node in a branch
	 * containing a ContributionItem contributed by <code>set</code>.
	 * 
	 * @param item
	 *            the item in question
	 * @param set
	 *            the action set to look for
	 * @return true iff <code>item</code> is required in build a tree including
	 *         elements in <code>set</code>
	 */
	private static boolean includeInSetStructure(DisplayItem item,
			ActionSet set) {
		if (item.actionSet != null && item.actionSet.equals(set))
			return true;
		for (Iterator i = item.getChildren().iterator(); i.hasNext();) {
			DisplayItem child = (DisplayItem) i.next();
			if (includeInSetStructure(child, set))
				return true;
		}
		return false;
	}

	/**
	 * @param item
	 * @return true iff the item is available - i.e. if it belongs to an action
	 *         set, that that action set is available
	 */
	private static boolean isAvailable(DisplayItem item) {
		if (item.getActionSet() == null)
			return true;
		if (item.getActionSet().isActive())
			return true;
		for (Iterator i = item.getChildren().iterator(); i.hasNext();) {
			DisplayItem child = (DisplayItem) i.next();
			if (isAvailable(child))
				return true;
		}
		return false;
	}

	/**
	 * @param item
	 * @return true iff the item will show up in a menu or tool bar structure -
	 *         i.e. it is available, or has a child which is available thus must
	 *         be displayed in order to display the child
	 */
	private static boolean isEffectivelyAvailable(DisplayItem item, ViewerFilter filter) {
		if (!isAvailable(item))
			return false;
		final List children = item.getChildren();
		if (children.isEmpty())
			return true;
		for (Iterator i = children.iterator(); i.hasNext();) {
			DisplayItem child = (DisplayItem) i.next();
			if(filter != null && !filter.select(null, null, child))
				continue;
			if (isAvailable(child)) {
				return true;
			}
		}
		for (Iterator i = children.iterator(); i.hasNext();) {
			DisplayItem child = (DisplayItem) i.next();
			if(filter != null && !filter.select(null, null, child))
				continue;
			if (isEffectivelyAvailable(child, filter)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param collection
	 *            a collection, into which all command groups (action sets)
	 *            which contribute <code>item</code> or its descendants will be
	 *            placed
	 * @param item	the item to collect descendants of
	 * @param filter the filter currently being used
	 * @param item
	 */
	private static void collectDescendantCommandGroups(Collection collection,
			DisplayItem item, ViewerFilter filter) {
		List children = item.getChildren();
		for (Iterator i = children.iterator(); i.hasNext();) {
			DisplayItem child = (DisplayItem) i.next();
			if ((filter == null || filter.select(null, null, child))
					&& child.getActionSet() != null) {
				collection.add(child.getActionSet());
			}
			collectDescendantCommandGroups(collection, child, filter);
		}
	}

	/**
	 * Gets the keybindings associated with a ContributionItem.
	 */
	private Binding[] getKeyBindings(DisplayItem item) {
		IBindingService bindingService = (IBindingService) window
				.getService(IBindingService.class);

		if (!(bindingService instanceof BindingService))
			return new Binding[0];

		String id = getCommandID(item);
		String param = getParamID(item);

		BindingManager bindingManager = ((BindingService) bindingService)
				.getBindingManager();

		Collection allBindings = bindingManager
				.getActiveBindingsDisregardingContextFlat();

		List foundBindings = new ArrayList(2);

		for (Iterator i = allBindings.iterator(); i.hasNext();) {
			Binding binding = (Binding) i.next();
			if (binding.getParameterizedCommand() == null)
				continue;
			if (binding.getParameterizedCommand().getId() == null)
				continue;
			if (binding.getParameterizedCommand().getId().equals(id)) {
				if (param == null) {
					// We found it!
					foundBindings.add(binding);
				} else {
					// command parameters are only used in the shortcuts
					Map m = binding.getParameterizedCommand().getParameterMap();
					String key = null;
					if (isNewWizard(item)) {
						key = IWorkbenchCommandConstants.FILE_NEW_PARM_WIZARDID;
					} else if (isShowView(item)) {
						key = IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID;
					} else if (isShowPerspective(item)) {
						key = IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID;
					}

					if (key != null) {
						if (param.equals(m.get(key))) {
							foundBindings.add(binding);
						}
					}
				}
			}
		}

		Binding[] bindings = (Binding[]) foundBindings
				.toArray(new Binding[foundBindings.size()]);

		return bindings;
	}

	/**
	 * @param bindings
	 * @return a String representing the key bindings in <code>bindings</code>
	 */
	private String keyBindingsAsString(Binding[] bindings) {
		String keybindings = null;
		for (int i = 0; i < bindings.length; i++) {
			// Unfortunately, bindings may be reported more than once:
			// check to see if this one has already been recorded.
			boolean alreadyRecorded = false;
			for (int j = 0; j < i && !alreadyRecorded; j++) {
				if (bindings[i].getTriggerSequence().equals(
						bindings[j].getTriggerSequence())) {
					alreadyRecorded = true;
				}
			}
			if (!alreadyRecorded) {
				String keybinding = bindings[i].getTriggerSequence().format();
				if (i == 0) {
					keybindings = keybinding;
				} else {
					keybindings = Util.createList(keybindings, keybinding);
				}
			}
		}
		return keybindings;
	}

	/**
	 * On a change to availability, updates the appropriate widgets.
	 */
	private void actionSetAvailabilityChanged() {
		menuStructureViewer1.refresh();
		menuStructureViewer2.refresh();
		toolbarStructureViewer1.refresh();
		toolbarStructureViewer2.refresh();
	}

	private void initializeActionSetInput() {
		// Just get the action sets at this point. Do not load the action set
		// until it is actually selected in the dialog.
		ActionSetRegistry reg = WorkbenchPlugin.getDefault()
				.getActionSetRegistry();
		IActionSetDescriptor[] sets = reg.getActionSets();
		IActionSetDescriptor[] actionSetDescriptors = ((WorkbenchPage) window
				.getActivePage()).getActionSets();
		List initiallyAvailableActionSets = Arrays.asList(actionSetDescriptors);

		for (int i = 0; i < sets.length; i++) {
			ActionSetDescriptor actionSetDesc = (ActionSetDescriptor) sets[i];
			if (WorkbenchActivityHelper.filterItem(actionSetDesc)) {
				continue;
			}
			ActionSet actionSet = new ActionSet(actionSetDesc,
					initiallyAvailableActionSets.contains(actionSetDesc));
			idToActionSet.put(actionSetDesc.getId(), actionSet);
			actionSets.add(actionSet);
		}
	}

	private void initializeIcons() {
		String iconPath = MENU_ICON;
		URL url = BundleUtility.find(PlatformUI.PLUGIN_ID, iconPath);
		menuImageDescriptor = ImageDescriptor.createFromURL(url);

		iconPath = SUBMENU_ICON;
		url = BundleUtility.find(PlatformUI.PLUGIN_ID, iconPath);
		submenuImageDescriptor = ImageDescriptor.createFromURL(url);

		iconPath = TOOLBAR_ICON;
		url = BundleUtility.find(PlatformUI.PLUGIN_ID, iconPath);
		toolbarImageDescriptor = ImageDescriptor.createFromURL(url);

		iconPath = WARNING_ICON;
		url = BundleUtility.find(PlatformUI.PLUGIN_ID, iconPath);
		warningImageDescriptor = ImageDescriptor.createFromURL(url);
	}

	private void initializeNewWizardsMenu(DisplayItem menu,
			Category parentCategory, IWizardCategory element, List activeIds) {
		Category category = new Category(element.getLabel());
		parentCategory.addChild(category);

		Object[] wizards = element.getWizards();
		for (int i = 0; i < wizards.length; i++) {
			WorkbenchWizardElement wizard = (WorkbenchWizardElement) wizards[i];

			ShortcutItem item = new ShortcutItem(wizard.getLabel(), wizard);
			item.setLabel(wizard.getLabel());
			item.setDescription(wizard.getDescription());
			if (wizard.getImageDescriptor() != null) {
				item.setImageDescriptor(wizard.getImageDescriptor());
			}
			item.setCheckState(activeIds.contains(wizard.getId()));
			menu.addChild(item);
			category.addShortcutItem(item);
		}
		// @issue should not pass in null
		IWizardCategory[] children = element.getCategories();
		for (int i = 0; i < children.length; i++) {
			initializeNewWizardsMenu(menu, category, children[i], activeIds);
		}
	}

	private void initializeNewWizardsMenu(DisplayItem menu) {
		Category rootForNewWizards = new Category(
				WorkbenchMessages.ActionSetDialogInput_wizardCategory);
		shortcuts.addChild(rootForNewWizards);

		IWizardCategory wizardCollection = WorkbenchPlugin.getDefault()
				.getNewWizardRegistry().getRootCategory();
		IWizardCategory[] wizardCategories = wizardCollection.getCategories();
		List activeIDs = Arrays.asList(perspective.getNewWizardShortcuts());

		for (int i = 0; i < wizardCategories.length; i++) {
			IWizardCategory element = wizardCategories[i];
			if (WorkbenchActivityHelper.filterItem(element)) {
				continue;
			}

			initializeNewWizardsMenu(menu, rootForNewWizards, element,
					activeIDs);
		}
	}

	private void initializePerspectivesMenu(DisplayItem menu) {
		Category rootForPerspectives = new Category(
				WorkbenchMessages.ActionSetDialogInput_perspectiveCategory);
		shortcuts.addChild(rootForPerspectives);

		IPerspectiveRegistry perspReg = WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry();
		IPerspectiveDescriptor[] persps = perspReg.getPerspectives();

		List activeIds = Arrays.asList(perspective.getPerspectiveShortcuts());

		for (int i = 0; i < persps.length; i++) {
			IPerspectiveDescriptor perspective = persps[i];
			if (WorkbenchActivityHelper.filterItem(perspective)) {
				continue;
			}

			ShortcutItem child = new ShortcutItem(perspective.getLabel(),
					perspective);
			child.setImageDescriptor(perspective.getImageDescriptor());
			child.setDescription(perspective.getDescription());
			child.setCheckState(activeIds.contains(perspective.getId()));
			menu.addChild(child);

			rootForPerspectives.addShortcutItem(child);
		}
	}

	private void initializeViewsMenu(DisplayItem menu) {
		Category rootForViews = new Category(
				WorkbenchMessages.ActionSetDialogInput_viewCategory);

		shortcuts.addChild(rootForViews);

		IViewRegistry viewReg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewCategory[] categories = viewReg.getCategories();

		List activeIds = Arrays.asList(perspective.getShowViewShortcuts());

		for (int i = 0; i < categories.length; i++) {
			IViewCategory category = categories[i];
			if (WorkbenchActivityHelper.filterItem(category)) {
				continue;
			}

			Category viewCategory = new Category(category.getLabel());
			rootForViews.addChild(viewCategory);

			IViewDescriptor[] views = category.getViews();

			if (views != null) {
				for (int j = 0; j < views.length; j++) {
					IViewDescriptor view = views[j];
					if (view.getId().equals(IIntroConstants.INTRO_VIEW_ID)) {
						continue;
					}
					if (WorkbenchActivityHelper.filterItem(view)) {
						continue;
					}

					ShortcutItem child = new ShortcutItem(view.getLabel(), view);
					child.setImageDescriptor(view.getImageDescriptor());
					child.setDescription(view.getDescription());
					child.setCheckState(activeIds.contains(view.getId()));
					menu.addChild(child);
					viewCategory.addShortcutItem(child);
				}
			}
		}
	}

	/**
	 * Loads the current perspective's menu structure and also loads which menu
	 * items are visible and not.
	 */
	private void loadMenuAndToolbarStructure() {
		WorkbenchWindow workbenchWindow = (WorkbenchWindow) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow();

		customizeActionBars = new CustomizeActionBars(configurer);

		// Fill fake action bars with static menu information.
		window.fillActionBars(customizeActionBars, ActionBarAdvisor.FILL_PROXY
				| ActionBarAdvisor.FILL_MENU_BAR
				| ActionBarAdvisor.FILL_COOL_BAR);

		// 3.3 start
		final IMenuService menuService = (IMenuService) window
				.getService(IMenuService.class);
		menuService.populateContributionManager(
				(ContributionManager) customizeActionBars.getMenuManager(),
				MenuUtil.MAIN_MENU);
		ICoolBarManager coolbar = customizeActionBars.getCoolBarManager();
		if (coolbar != null) {
			menuService.populateContributionManager(
					(ContributionManager) coolbar, MenuUtil.MAIN_TOOLBAR);
		}
		// 3.3 end

		// Populate the action bars with the action sets' data
		for (Iterator i = actionSets.iterator(); i.hasNext();) {
			ActionSet actionSet = (ActionSet) i.next();
			ActionSetDescriptor descriptor = actionSet.descriptor;
			PluginActionSet pluginActionSet = buildMenusAndToolbarsFor(
					customizeActionBars, descriptor);

			if (pluginActionSet != null) {
				pluginActionSet.dispose();
			}
		}

		// Make all menu items visible so they are included in the list.
		customizeActionBars.menuManager.setVisible(true);

		makeAllContributionsVisible(customizeActionBars.menuManager);

		// Get the menu from the action bars
		Menu menu = customizeActionBars.menuManager
				.createMenuBar((Decorations) workbenchWindow.getShell());

		CoolBar cb = customizeActionBars.coolBarManager
				.createControl(workbenchWindow.getShell());
		cb.equals(cb);

		// Ensure the menu is completely built by updating the menu manager.
		// (This method call requires a menu already be created)
		customizeActionBars.menuManager.updateAll(true);
		customizeActionBars.coolBarManager.update(true);

		shortcuts = new Category(""); //$NON-NLS-1$
		toolBarItems = createToolBarStructure(cb);
		menuItems = createMenuStructure(menu);
	}

	private PluginActionSet buildMenusAndToolbarsFor(
			CustomizeActionBars customizeActionBars,
			ActionSetDescriptor actionSetDesc) {
		String id = actionSetDesc.getId();
		ActionSetActionBars bars = new ActionSetActionBars(customizeActionBars,
				window, customizeActionBars, id);
		bars.getMenuManager().setVisible(true);
		PluginActionSetBuilder builder = new PluginActionSetBuilder();
		PluginActionSet actionSet = null;
		try {
			actionSet = (PluginActionSet) actionSetDesc.createActionSet();
			actionSet.init(null, bars);
		} catch (CoreException ex) {
			WorkbenchPlugin.log(
					"Unable to create action set " + actionSetDesc.getId(), ex); //$NON-NLS-1$
			return null;
		}
		builder.buildMenuAndToolBarStructure(actionSet, window);
		return actionSet;
	}

	private static String getCommandID(DisplayItem item) {
		Object object = item.getIContributionItem();

		if (item instanceof ShortcutItem && isShowView(item)) {
			return IWorkbenchCommandConstants.VIEWS_SHOW_VIEW;
		}

		return getIDFromIContributionItem(object);
	}

	/**
	 * Given an object, tries to find an id which will uniquely identify it.
	 * 
	 * @param object
	 *            an instance of {@link IContributionItem},
	 *            {@link IPerspectiveDescriptor}, {@link IViewDescriptor} or
	 *            {@link WorkbenchWizardElement}.
	 * @return an id
	 * @throws IllegalArgumentException
	 *             if object is not one of the listed types
	 */
	public static String getIDFromIContributionItem(Object object) {
		if (object instanceof ActionContributionItem) {
			ActionContributionItem item = (ActionContributionItem) object;
			IAction action = item.getAction();
			if (action == null)
				return null;
			if (action instanceof NewWizardShortcutAction) {
				return IWorkbenchCommandConstants.FILE_NEW;
			}
			if (action instanceof OpenPerspectiveAction) {
				return IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE;
			}
			String id = action.getActionDefinitionId();
			if (id != null) {
				return id;
			}
			return action.getId();
		}
		if (object instanceof ActionSetContributionItem) {
			ActionSetContributionItem item = (ActionSetContributionItem) object;
			IContributionItem subitem = item.getInnerItem();
			return getIDFromIContributionItem(subitem);
		}
		if (object instanceof CommandContributionItem) {
			CommandContributionItem item = (CommandContributionItem) object;
			ParameterizedCommand command = item.getCommand();
			if (command == null) {
				return null;
			}
			return command.getId();
		}
		if (object instanceof IPerspectiveDescriptor) {
			return ((IPerspectiveDescriptor) object).getId();
		}
		if (object instanceof IViewDescriptor) {
			return ((IViewDescriptor) object).getId();
		}
		if (object instanceof WorkbenchWizardElement) {
			return ((WorkbenchWizardElement) object).getLocalId();
		}
		if (object instanceof IContributionItem) {
			String id = ((IContributionItem) object).getId();
			if (id != null)
				return id;
			return object.getClass().getName();
		}
		return null;	//couldn't determine the id
	}

	private static String getDescription(Object object) {
		if (object instanceof DisplayItem) {
			DisplayItem item = (DisplayItem) object;

			if (isNewWizard(item)) {
				ShortcutItem shortcut = (ShortcutItem) item;
				IWizardDescriptor descriptor = (IWizardDescriptor) shortcut
						.getDescriptor();
				return descriptor.getDescription();
			}

			if (isShowPerspective(item)) {
				ShortcutItem shortcut = (ShortcutItem) item;
				IPerspectiveDescriptor descriptor = (IPerspectiveDescriptor) shortcut
						.getDescriptor();
				return descriptor.getDescription();
			}

			if (isShowView(item)) {
				ShortcutItem shortcut = (ShortcutItem) item;
				IViewDescriptor descriptor = (IViewDescriptor) shortcut
						.getDescriptor();
				return descriptor.getDescription();
			}

			if (item instanceof DynamicContributionItem) {
				return WorkbenchMessages.HideItems_dynamicItemDescription;
			}

			IContributionItem contrib = item.getIContributionItem();
			return getDescription(contrib);
		}

		if (object instanceof ActionSet) {
			ActionSet actionSet = (ActionSet) object;
			return actionSet.descriptor.getDescription();
		}

		return null;
	}

	private static String getDescription(IContributionItem item) {
		if (item instanceof ActionContributionItem) {
			ActionContributionItem aci = (ActionContributionItem) item;
			IAction action = aci.getAction();
			if (action == null)
				return null;
			return action.getDescription();
		}
		if (item instanceof ActionSetContributionItem) {
			ActionSetContributionItem asci = (ActionSetContributionItem) item;
			IContributionItem subitem = asci.getInnerItem();
			return getDescription(subitem);
		}
		return null;
	}

	private static String getParamID(DisplayItem object) {
		if (object instanceof ShortcutItem) {
			ShortcutItem shortcutItem = (ShortcutItem) object;

			if (isNewWizard(shortcutItem)) {
				ActionContributionItem item = (ActionContributionItem) object
						.getIContributionItem();
				NewWizardShortcutAction nwsa = (NewWizardShortcutAction) item
						.getAction();
				return nwsa.getLocalId();
			}

			if (isShowPerspective(shortcutItem)) {
				ActionContributionItem item = (ActionContributionItem) object
						.getIContributionItem();
				OpenPerspectiveAction opa = (OpenPerspectiveAction) item
						.getAction();
				return opa.getLocalId();
			}

			if (isShowView(shortcutItem)) {
				IViewDescriptor descriptor = (IViewDescriptor) shortcutItem
						.getDescriptor();
				return descriptor.getId();
			}
		}

		return null;
	}

	private static boolean isNewWizard(DisplayItem item) {
		if (!(item instanceof ShortcutItem))
			return false;
		return ((ShortcutItem) item).getDescriptor() instanceof IWizardDescriptor;
	}

	private static boolean isShowPerspective(DisplayItem item) {
		if (!(item instanceof ShortcutItem))
			return false;
		return ((ShortcutItem) item).getDescriptor() instanceof IPerspectiveDescriptor;
	}

	private static boolean isShowView(DisplayItem item) {
		if (!(item instanceof ShortcutItem))
			return false;
		return ((ShortcutItem) item).getDescriptor() instanceof IViewDescriptor;
	}

	private static String getActionSetID(IContributionItem item) {
		if (item instanceof ActionSetContributionItem) {
			ActionSetContributionItem asci = (ActionSetContributionItem) item;
			return asci.getActionSetId();
		}
		if (item instanceof PluginActionCoolBarContributionItem) {
			PluginActionCoolBarContributionItem pacbci = (PluginActionCoolBarContributionItem) item;
			return pacbci.getActionSetId();
		}
		return null;
	}

	/**
	 * Causes all items under the manager to be visible, so they can be read.
	 * 
	 * @param manager
	 */
	private static void makeAllContributionsVisible(IContributionManager manager) {
		IContributionItem[] items = manager.getItems();

		for (int i = 0; i < items.length; i++) {
			makeContributionVisible(items[i]);
		}
	}

	/**
	 * Makes all items under the item to be visible, so they can be read.
	 * 
	 * @param item
	 */
	private static void makeContributionVisible(IContributionItem item) {
		item.setVisible(true);

		if (item instanceof IContributionManager) {
			makeAllContributionsVisible((IContributionManager) item);
		}
		if (item instanceof SubContributionItem) {
			makeContributionVisible(((SubContributionItem) item).getInnerItem());
		}
	}

	private DisplayItem createMenuStructure(Menu menu) {
		DisplayItem root = new DisplayItem("", null); //$NON-NLS-1$
		createMenuEntries(menu, root, true);
		return root;
	}

	private void createMenuEntries(Menu menu, DisplayItem parent,
			boolean trackDynamics) {
		if (menu == null)
			return;
		MenuItem[] menuItems = menu.getItems();

		Map findDynamics = new HashMap();
		DynamicContributionItem dynamicEntry = null;

		if (trackDynamics && menu.getParentItem() != null) {
			//Search for any dynamic menu entries which will be handled later
			Object data = menu.getParentItem().getData();
			if (data instanceof IContributionManager) {
				IContributionManager manager = (IContributionManager) data;
				IContributionItem[] items = manager.getItems();
				for (int i = 0; i < items.length; i++) {
					if (items[i].isDynamic()) {
						findDynamics.put(i > 0 ? items[i - 1] : null, items[i]);
					}
				}

				//If there is an item with no preceeding item, set it up to be
				//added first.
				if (findDynamics.containsKey(null)) {
					IContributionItem item = (IContributionItem) findDynamics
							.get(null);
					dynamicEntry = new DynamicContributionItem(item);
					parent.addChild(dynamicEntry);
				}
			}
		}

		for (int i = 0; i < menuItems.length; i++) {
			if (!menuItems[i].getText().equals("")) { //$NON-NLS-1$
				IContributionItem contributionItem =
						(IContributionItem) menuItems[i].getData();
				if (dynamicEntry != null
						&& contributionItem.equals(dynamicEntry
								.getIContributionItem())) {
					//If the last item added is the item meant to go before the
					//given dynamic entry, add the dynamic entry so it is in the
					//correct order.
					dynamicEntry.addCurrentItem(menuItems[i]);
				} else {
					DisplayItem menuEntry = new DisplayItem(
							menuItems[i].getText(), contributionItem);

					Image image = menuItems[i].getImage();
					if (image != null) {
						menuEntry.setImageDescriptor(ImageDescriptor
								.createFromImage(image));
					}
					menuEntry.setActionSet((ActionSet) idToActionSet
							.get(getActionSetID(contributionItem)));
					parent.addChild(menuEntry);

					if (ActionFactory.NEW.getId()
							.equals(((IContributionItem) menuItems[i].getData())
									.getId())) {
						initializeNewWizardsMenu(menuEntry);
						wizards = menuEntry;
					} else if (SHORTCUT_CONTRIBUTION_ITEM_ID_OPEN_PERSPECTIVE
							.equals(((IContributionItem) menuItems[i].getData())
									.getId())) {
						initializePerspectivesMenu(menuEntry);
						perspectives = menuEntry;
					} else if (SHORTCUT_CONTRIBUTION_ITEM_ID_SHOW_VIEW
							.equals(((IContributionItem) menuItems[i].getData())
									.getId())) {
						initializeViewsMenu(menuEntry);
						views = menuEntry;
					} else {
						createMenuEntries(menuItems[i].getMenu(), menuEntry,
								trackDynamics);
					}

					if (menuEntry.getChildren().isEmpty()) {
						menuEntry
								.setCheckState(getMenuItemIsVisible(menuEntry));
					}

					if (image == null) {
						if (parent != null && parent.getParent() == null) {
							menuEntry.setImageDescriptor(menuImageDescriptor);
						} else if (menuEntry.getChildren().size() > 0) {
							menuEntry
									.setImageDescriptor(submenuImageDescriptor);
						}
					}
				}
				if (trackDynamics
						&& findDynamics.containsKey(menuItems[i].getData())) {
					IContributionItem item = (IContributionItem) findDynamics
							.get(menuItems[i].getData());
					dynamicEntry = new DynamicContributionItem(item);
					dynamicEntry
							.setCheckState(getMenuItemIsVisible(dynamicEntry));
					parent.addChild(dynamicEntry);
				}
			}
		}
	}

	private boolean getMenuItemIsVisible(DisplayItem item) {
		return isAvailable(item)
				&& !(perspective.getHiddenMenuItems()
						.contains(getCommandID(item)));
	}

	private boolean getToolbarItemIsVisible(DisplayItem item) {
		return isAvailable(item)
				&& !(perspective.getHiddenToolbarItems()
						.contains(getCommandID(item)));
	}

	/**
	 * Causes a viewer to update the state of a category and all its ancestors.
	 * 
	 * @param viewer
	 * @param category
	 */
	private void updateCategoryAndParents(StructuredViewer viewer,
			Category category) {
		while (category.getParent() != shortcuts) {
			viewer.update(category, null);
			category = (Category) category.getParent();
		}
	}

	private DisplayItem createToolBarStructure(CoolBar coolbar) {
		DisplayItem root = new DisplayItem(null, null); // Create a
																	// root
		createToolbarEntries(coolbar, root);
		return root;
	}

	private void createToolbarEntries(CoolBar coolbar, DisplayItem parent) {
		if (coolbar == null)
			return;
		CoolItem[] items = coolbar.getItems();
		List entries = new ArrayList(items.length);

		for (int i = 0; i < items.length; i++) {
			IContributionItem contributionItem = (IContributionItem) items[i]
					.getData();
			String text;
			if (contributionItem instanceof IToolBarContributionItem) {
				IToolBarContributionItem item = (IToolBarContributionItem) contributionItem;
				text = window.getToolbarLabel(item.getId());
				if (text == null || text.equals("")) //$NON-NLS-1$
					text = items[i].getText();

			} else {
				text = items[i].getText();
			}
			DisplayItem toolBarEntry = new DisplayItem(text,
					contributionItem);
			if (items[i].getImage() == null) {
				toolBarEntry.setImageDescriptor(toolbarImageDescriptor);
			}
			toolBarEntry.setActionSet((ActionSet) idToActionSet
					.get(getActionSetID(contributionItem)));
			parent.addChild(toolBarEntry);

			Control control = items[i].getControl();

			if (control instanceof ToolBar) {
				ToolItem[] toolitems = ((ToolBar) control).getItems();
				createToolbarEntries(toolitems, toolBarEntry);
			}

			entries.add(toolBarEntry);
		}
	}

	private void createToolbarEntries(ToolItem[] toolitems,
			DisplayItem parent) {
		if (toolitems == null)
			return;

		for (int i = 0; i < toolitems.length; i++) {
			IContributionItem contributionItem = (IContributionItem) toolitems[i]
					.getData();
			if (contributionItem.isGroupMarker()
					|| contributionItem.isSeparator()) {
				continue;
			}
			DisplayItem toolBarEntry = new DisplayItem(toolitems[i]
					.getToolTipText(), contributionItem);
			Image image = toolitems[i].getImage();
			if (image != null) {
				toolBarEntry.setImageDescriptor(ImageDescriptor
						.createFromImage(image));
			}
			toolBarEntry.setActionSet((ActionSet) idToActionSet
					.get(getActionSetID(contributionItem)));
			contributionItem.setVisible(true);// force parents to update
			toolBarEntry.setCheckState(getToolbarItemIsVisible(toolBarEntry));
			parent.addChild(toolBarEntry);
		}
	}

	/**
	 * Returns whether the shortcut tab should be shown.
	 * 
	 * @return <code>true</code> if the shortcut tab should be shown, and
	 *         <code>false</code> otherwise
	 * @since 3.0
	 */
	private boolean showShortcutTab() {
		return window.containsSubmenu(WorkbenchWindow.NEW_WIZARD_SUBMENU)
				|| window
						.containsSubmenu(WorkbenchWindow.OPEN_PERSPECTIVE_SUBMENU)
				|| window.containsSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU);
	}

	private ArrayList getVisibleIDs(TreeItem root) {
		if (root == null) {
			return new ArrayList();
		}
		ArrayList ids = new ArrayList(root.getChildren().size());
		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
			DisplayItem object = (DisplayItem) i.next();
			if (object instanceof ShortcutItem && object.getState()) {
				ids.add(getParamID(object));
			}
		}
		return ids;
	}

	private void getChangedIds(DisplayItem item, List invisible, List visible) {
		if (item instanceof ShortcutItem)
			return;

		if (item == wizards || item == perspectives || item == views) {
			// We always want the top-level wizard/perspective/view shortcuts to
			// be visible, see bug 293448
			return;
		} else if (item.getChildren().size() > 0) {
			if (item.isChangedByUser()) {
				String id = getCommandID(item);
				if (item.getState())
					visible.add(id);
				else
					invisible.add(id);
			}
			for (Iterator i = item.getChildren().iterator(); i.hasNext();) {
				getChangedIds((DisplayItem) i.next(), invisible, visible);
			}
		} else if (item.isChangedByUser()) {
			String id = getCommandID(item);
			if (item.getState())
				visible.add(id);
			else
				invisible.add(id);
		}
	}

	private boolean updateHiddenElements(DisplayItem items, Collection currentHidden) {
		boolean hasChanges = false;
		
		List changedAndVisible = new ArrayList();
		List changedAndInvisible = new ArrayList();
		getChangedIds(items, changedAndInvisible, changedAndVisible);
		
		// Remove explicitly 'visible' elements from the current list
		for (Iterator iterator = changedAndVisible.iterator(); iterator.hasNext();) {
			Object id = iterator.next();
			if (currentHidden.contains(id)) {
				hasChanges = true;
				currentHidden.remove(id);
			}
		}
		
		// Add explicitly 'hidden' elements to the current list
		for (Iterator iterator = changedAndInvisible.iterator(); iterator.hasNext();) {
			Object id = iterator.next();
			if (!currentHidden.contains(id)) {
				hasChanges = true;
				currentHidden.add(id);
			}
		}
		
		return hasChanges;
	}
	
	protected void okPressed() {
		// Shortcuts
		if (showShortcutTab()) {
			perspective.setNewWizardActionIds(getVisibleIDs(wizards));
			perspective.setPerspectiveActionIds(getVisibleIDs(perspectives));
			perspective.setShowViewActionIds(getVisibleIDs(views));
		}

		// Determine if anything has changed and, if so, update the menu & tb's
		boolean requiresUpdate = false;
		
		// Action Sets
		ArrayList toAdd = new ArrayList();
		ArrayList toRemove = new ArrayList();

		for (Iterator i = actionSets.iterator(); i.hasNext();) {
			ActionSet actionSet = (ActionSet) i.next();
			if (!actionSet.wasChanged())
				continue;
			
			// Something has changed
			requiresUpdate = true;
			
			if (actionSet.isActive()) {
				toAdd.add(actionSet.descriptor);
			} else {
				toRemove.add(actionSet.descriptor);
			}
		}

		perspective.turnOnActionSets((IActionSetDescriptor[]) toAdd
				.toArray(new IActionSetDescriptor[toAdd.size()]));
		perspective.turnOffActionSets((IActionSetDescriptor[]) toRemove
				.toArray(new IActionSetDescriptor[toRemove.size()]));

		// Menu  and Toolbar Items
		requiresUpdate |= updateHiddenElements(menuItems, perspective.getHiddenMenuItems());
		requiresUpdate |= updateHiddenElements(toolBarItems, perspective.getHiddenToolbarItems());
		
		if (requiresUpdate) {
			perspective.updateActionBars();
		}
		
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TrayDialog#close()
	 */
	public boolean close() {
		tooltipHeading.dispose();

		for (Iterator i = toDispose.iterator(); i.hasNext();) {
			Resource resource = (Resource) i.next();
			resource.dispose();
		}

		treeManager.dispose();
		customizeActionBars.dispose();

		return super.close();
	}

	private String removeShortcut(String label) {
		if (label == null) {
			return label;
		}
		int end = label.lastIndexOf('@');
		if (end >= 0) {
			label = label.substring(0, end);
		}

		end = label.lastIndexOf('\t');
		if (end >= 0) {
			label = label.substring(0, end);
		}

		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#applyDialogFont()
	 */
	protected boolean applyDialogFont() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}

	private void viewActionSet(final DisplayItem item) {
		viewActionSet(item.getActionSet());
	}

	/**
	 * @param item
	 */
	private void viewActionSet(final ActionSet actionSet) {
		tabFolder.setSelection(actionSetTab);
		actionSetAvailabilityTable.reveal(actionSet);
		setSelectionOn(actionSetAvailabilityTable, actionSet);
		actionSetAvailabilityTable.getControl().setFocus();
	}

	/**
	 * Determines the state <code>item</code> should be (checked, gray or
	 * unchecked) based only on the leafs underneath it (unless it is indeed a
	 * leaf).
	 * 
	 * @param item
	 *            the item to find the state of
	 * @param provider
	 *            the content provider which will provide <code>item</code>'s
	 *            children
	 * @param filter
	 *            the filter that will only select elements in the currently
	 *            chosen action set
	 * @return {@link TreeManager#CHECKSTATE_CHECKED},
	 *         {@link TreeManager#CHECKSTATE_GRAY} or
	 *         {@link TreeManager#CHECKSTATE_UNCHECKED}
	 */
	private static int getLeafStates(TreeItem item,
			ITreeContentProvider provider, ViewerFilter filter) {
		Object[] children = provider.getChildren(item);

		boolean checkedFound = false;
		boolean uncheckedFound = false;

		for (int i = 0; i < children.length; i++) {
			if (filter.select(null, null, children[i])) {
				TreeItem child = (TreeItem) children[i];
				switch (getLeafStates(child, provider, filter)) {
				case TreeManager.CHECKSTATE_CHECKED: {
					checkedFound = true;
					break;
				}
				case TreeManager.CHECKSTATE_GRAY: {
					checkedFound = uncheckedFound = true;
					break;
				}
				case TreeManager.CHECKSTATE_UNCHECKED: {
					uncheckedFound = true;
					break;
				}
				}
				if (checkedFound && uncheckedFound) {
					return TreeManager.CHECKSTATE_GRAY;
				}
			}
		}

		if (!checkedFound && !uncheckedFound)
			return item.getState() ? TreeManager.CHECKSTATE_CHECKED
					: TreeManager.CHECKSTATE_UNCHECKED;
		return checkedFound ? TreeManager.CHECKSTATE_CHECKED
				: TreeManager.CHECKSTATE_UNCHECKED;
	}

	/**
	 * Sets all leafs under a {@link DisplayItem} to either visible or
	 * invisible. This is for use with the action set trees, where the only
	 * state used is that of leafs, and the rest is rolled up to the parents.
	 * Thus, this method effectively sets the state of the entire branch.
	 * 
	 * @param item
	 *            the item whose leafs underneath (or itself, if it is a leaf)
	 *            to <code>value</code>
	 * @param value
	 *            <code>true</code>for visible, <code>false</code> for invisible
	 * @param provider
	 *            the content provider which will provide <code>item</code>'s
	 *            children
	 * @param filter
	 *            the filter that will only select elements in the currently
	 *            chosen action set
	 */
	private static void setAllLeafs(DisplayItem item, boolean value,
			ITreeContentProvider provider, ViewerFilter filter) {
		Object[] children = provider.getChildren(item);
		boolean isLeaf = true;

		for (int i = 0; i < children.length; i++) {
			isLeaf = false;
			if (filter.select(null, null, children[i])) {
				DisplayItem child = (DisplayItem) children[i];
				setAllLeafs(child, value, provider, filter);
			}
		}

		if (isLeaf) {
			item.setCheckState(value);
		}
	}
}
