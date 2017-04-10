/*******************************************************************************
 * Copyright (c) 2011, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Maxime Porhel <maxime.porhel@obeo.fr> Obeo - Bug 430116
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 457237, 472654
 *     Andrey Loskutov <loskutov@gmx.de> - Bugs 383569, 420956, 457198, 395601, 445538
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 409633
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.menus.ActionSet;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.menus.CommandContributionItem;

/**
 * @since 3.5
 *
 */
public class CoolBarToTrimManager extends ContributionManager implements ICoolBarManager2 {

	private final class ToolBarContributionItemExtension extends ToolBarContributionItem {
		private final MToolBar tb;

		private ToolBarContributionItemExtension(IToolBarManager toolBarManager, MToolBar tb) {
			super(toolBarManager, tb.getElementId());
			this.tb = tb;
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			tb.setVisible(visible);
		}
	}

	private static final String TOOLBAR_SEPARATOR = "toolbarSeparator"; //$NON-NLS-1$
	private static final String MAIN_TOOLBAR_ID = ActionSet.MAIN_TOOLBAR;
	public static final String OBJECT = "coolbar.object"; //$NON-NLS-1$
	private static final String PREV_CHILD_VISIBLE = "prevChildVisible"; //$NON-NLS-1$
	private MTrimBar topTrim;
	private List<MTrimElement> workbenchTrimElements;
	private IRendererFactory rendererFactory;
	private ToolBarManagerRenderer renderer;
	private MApplication application;
	private MTrimmedWindow window;
	private IContributionManagerOverrides toolbarOverrides;

	/**
	 * Field to indicate whether the trim bars have been added to the window's
	 * model or not. They should only ever be added once.
	 */
	private boolean trimBarsAdded;
	private EModelService modelService;

	public CoolBarToTrimManager(MApplication app, MTrimmedWindow window,
			List<MTrimElement> workbenchTrimElements, IRendererFactory rf) {
		application = app;
		this.window = window;
		rendererFactory = rf;
		this.workbenchTrimElements = workbenchTrimElements;

		modelService = window.getContext().get(EModelService.class);
		topTrim = (MTrimBar) modelService.find(MAIN_TOOLBAR_ID, window);
		if (topTrim == null) {
			topTrim = modelService.getTrim(window, SideValue.TOP);
			topTrim.setElementId(MAIN_TOOLBAR_ID);
		}
		topTrim.setToBeRendered(false);
		MToolBar mToolBar = modelService.createModelElement(MToolBar.class);
		renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(mToolBar, null);
	}

	@Override
	public void add(IAction action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(IContributionItem item) {
		add(topTrim, -1, item);
	}

	private void add(MTrimBar trimBar, int idx, IContributionItem item) {
		// Special check to make sure that new additions are *before* the SPACER
		if (idx == -1) {
			MUIElement spacer = modelService.find(WorkbenchWindow.PERSPECTIVE_SPACER_ID, trimBar);
			if (spacer != null) {
				idx = trimBar.getChildren().indexOf(spacer);
			}
		}

		if (item instanceof IToolBarContributionItem) {
			IToolBarContributionItem tbc = (IToolBarContributionItem) item;
			IToolBarManager mgr = tbc.getToolBarManager();
			if (!(mgr instanceof ToolBarManager)) {
				return;
			}
			ToolBarManager manager = (ToolBarManager) mgr;

			if (renderer.getToolBarModel(manager) != null) {
				return;
			}

			MToolBar toolBar = (MToolBar) modelService.find(item.getId(), window);
			boolean tbFound = toolBar != null;
			if (!tbFound) {
				toolBar = modelService.createModelElement(MToolBar.class);
			} else {
				toolBar.getChildren().clear();
			}
			toolBar.setElementId(item.getId());
			toolBar.getTransientData().put(OBJECT, item);
			String toolbarLabel = getToolbarLabel(application, item.getId());
			if (toolbarLabel != null) {
				toolBar.getTransientData().put("Name", toolbarLabel); //$NON-NLS-1$
			}
			renderer.linkModelToManager(toolBar, manager);
			toolBar.setToBeRendered(true);
			if (!tbFound) {
				if (idx < 0) {
					trimBar.getChildren().add(toolBar);
				} else {
					trimBar.getChildren().add(idx, toolBar);
				}
			}
			workbenchTrimElements.add(toolBar);
			manager.setOverrides(toolbarOverrides);
		} else if (item instanceof IContributionManager) {
			throw new IllegalStateException();
		} else if (item instanceof AbstractGroupMarker) {
			if (item.getId() == null) {
				return;
			}
			for (MTrimElement toolBar : topTrim.getChildren()) {
				if (item.getId().equals(toolBar.getElementId())
						&& toolBar.getTags().contains(TOOLBAR_SEPARATOR)) {
					// already in the coolbar
					return;
				}
			}
			MToolBarSeparator separator = modelService.createModelElement(MToolBarSeparator.class);
			separator.setToBeRendered(false);
			separator.setElementId(item.getId());

			List<MToolBar> toolbars = modelService.findElements(window, item.getId(), MToolBar.class, null);
			MToolBar toolBar = toolbars.isEmpty() ? null : toolbars.get(0);
			boolean tbFound = toolBar != null;
			if (!tbFound) {
				toolBar = modelService.createModelElement(MToolBar.class);
			} else {
				toolBar.getChildren().clear();
			}
			toolBar.getTransientData().put(OBJECT, item);
			toolBar.getTags().add(TOOLBAR_SEPARATOR);
			toolBar.setElementId(item.getId());
			toolBar.getChildren().add(separator);
			toolBar.setToBeRendered(false);
			if (!tbFound) {
				if (idx < 0) {
					topTrim.getChildren().add(toolBar);
				} else {
					topTrim.getChildren().add(idx, toolBar);
				}
			}
			workbenchTrimElements.add(toolBar);
		}

	}

	public static String getToolbarLabel(MApplication application, MUIElement elt) {
		String name = getTransientName(elt);
		if (name != null) {
			return name;
		}
		String elementId = elt.getElementId();
		return getToolbarLabel(application, elementId);
	}

	// See MenuAdditionCacheEntry
	private static String getToolbarLabel(MApplication application, String elementId) {
		String name;
		if (IWorkbenchActionConstants.TOOLBAR_FILE.equalsIgnoreCase(elementId)) {
			return WorkbenchMessages.WorkbenchWindow_FileToolbar;
		}
		if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE.equalsIgnoreCase(elementId)) {
			return WorkbenchMessages.WorkbenchWindow_NavigateToolbar;
		}
		if (IWorkbenchActionConstants.TOOLBAR_HELP.equalsIgnoreCase(elementId)) {
			return WorkbenchMessages.WorkbenchWindow_HelpToolbar;
		}
		List<MTrimContribution> trimContributions = application.getTrimContributions();
		for (MTrimContribution mtb : trimContributions) {
			for (MTrimElement e : mtb.getChildren()) {
				if (e.getElementId().equals(elementId)) {
					name = getTransientName(e);
					if (name != null) {
						return name;
					}
				}
			}
		}
		return null;
	}

	static String getTransientName(MUIElement elt) {
		Object name = elt.getTransientData().get("Name"); //$NON-NLS-1$
		if (name instanceof String) {
			return (String) name;
		}
		return null;
	}

	@Override
	public void add(final IToolBarManager toolBarManager) {
		if (toolBarManager instanceof ToolBarManager) {
			add(new ToolBarContributionItem(toolBarManager));
		}
	}

	@Override
	public void appendToGroup(String groupName, IAction action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void appendToGroup(String groupName, IContributionItem item) {
		List<MToolBar> toolBars = modelService.findElements(window, groupName, MToolBar.class, null);
		if (toolBars.size() == 1) {
			MToolBar el = toolBars.get(0);
			MTrimBar trimBar = getTrim(el);
			int index = trimBar.getChildren().indexOf(el);
			index = index + 1 < trimBar.getChildren().size() ? index : -1;
			add(trimBar, index, item);
		}

		add(topTrim, -1, item);
	}

	@Override
	public Control createControl2(Composite parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dispose() {
		ArrayList<MToolBarElement> toRemove = new ArrayList<>();
		for (MTrimElement child : topTrim.getChildren()) {
			if (child instanceof MToolBar) {
				MToolBar toolbar = (MToolBar) child;
				for (MToolBarElement element : toolbar.getChildren()) {
					if (OpaqueElementUtil.isOpaqueToolItem(element)) {
						toRemove.add(element);
					}
				}
				if (!toRemove.isEmpty()) {
					toolbar.getChildren().removeAll(toRemove);
					toRemove.clear();
				}
			}
		}

	}

	@Override
	public IContributionItem find(String id) {
		List<MToolBar> toolbars = modelService.findElements(window, id, MToolBar.class, null);
		if (toolbars.isEmpty()) {
			return null;
		}

		final MToolBar model = toolbars.get(0);
		if (model.getTransientData().get(OBJECT) != null) {
			return (IContributionItem) model.getTransientData().get(OBJECT);
		}
		ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(model, null);
		final ToolBarManager manager = renderer.getManager(model);
		if (manager != null) {
			final ToolBarContributionItem toolBarContributionItem = new ToolBarContributionItemExtension(manager, model);
			model.getTransientData().put(OBJECT, toolBarContributionItem);
			return toolBarContributionItem;
		} else if (model.getTags().contains(TOOLBAR_SEPARATOR)) {
			if (model.getTransientData().get(OBJECT) != null) {
				return (IContributionItem) model.getTransientData().get(OBJECT);
			}
			return new GroupMarker(model.getElementId());
		}
		return null;
	}

	@Override
	public IMenuManager getContextMenuManager() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Control getControl2() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IContributionItem[] getItems() {
		ArrayList<IContributionItem> items = new ArrayList<>();

		List<MToolBar> toolBars = modelService.findElements(window, null, MToolBar.class, null);
		for (final MToolBar tb : toolBars) {
			if (tb.getTransientData().get(OBJECT) != null) {
				items.add((IContributionItem) tb.getTransientData().get(OBJECT));
			} else {
				ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(tb, null);
				final ToolBarManager manager = renderer.getManager(tb);
				if (manager != null) {
					ToolBarContributionItem toolBarContributionItem = new ToolBarContributionItemExtension(manager, tb);
					tb.getTransientData().put(OBJECT, toolBarContributionItem);
					items.add(toolBarContributionItem);
				} else if (tb.getTags().contains(TOOLBAR_SEPARATOR)) {
					if (tb.getTransientData().get(OBJECT) != null) {
						items.add((IContributionItem) tb.getTransientData().get(OBJECT));
					}
					items.add(new GroupMarker(tb.getElementId()));
				}
			}
		}

		return items.toArray(new IContributionItem[items.size()]);
	}

	@Override
	public boolean getLockLayout() {
		return false;
	}

	@Override
	public IContributionManagerOverrides getOverrides() {
		return toolbarOverrides;
	}

	@Override
	public int getStyle() {
		return 0;
	}

	@Override
	public void insertAfter(String id, IAction action) {
		throw new UnsupportedOperationException();
	}

	private MTrimBar getTrim(MTrimElement te) {
		if (te == null) {
			return null;
		}

		MUIElement parentElement = te.getParent();
		return (MTrimBar) (parentElement instanceof MTrimBar ? parentElement : null);
	}

	private MToolBar getToolBar(String id) {
		List<MToolBar> toolbars = modelService.findElements(window, id, MToolBar.class, null);
		if (toolbars.size() == 1) {
			return toolbars.get(0);
		}

		return null;
	}

	@Override
	public void insertAfter(String id, IContributionItem item) {
		MToolBar afterElement = getToolBar(id);
		if (afterElement == null || getTrim(afterElement) == null) {
			return;
		}

		MTrimBar trimBar = getTrim(afterElement);
		int index = trimBar.getChildren().indexOf(afterElement);
		index = index + 1 < trimBar.getChildren().size() ? index + 1 : -1;
		add(trimBar, index, item);
	}

	@Override
	public void insertBefore(String id, IAction action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void insertBefore(String id, IContributionItem item) {
		MToolBar beforeElement = getToolBar(id);
		if (beforeElement == null || getTrim(beforeElement) == null) {
			return;
		}

		MTrimBar trimBar = getTrim(beforeElement);
		int index = trimBar.getChildren().indexOf(beforeElement);
		add(trimBar, index, item);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return topTrim.getChildren().isEmpty();
	}

	@Override
	public void markDirty() {
	}

	@Override
	public void prependToGroup(String groupName, IAction action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void prependToGroup(String groupName, IContributionItem item) {
		MUIElement gnElement = modelService.find(groupName, window);
		if (gnElement instanceof MToolBar) {
			MTrimBar trimBar = getTrim((MTrimElement) gnElement);
			int index = trimBar.getChildren().indexOf(gnElement);
			add(trimBar, index, item);
		}
		add(topTrim, -1, item);
	}

	@Override
	public void refresh() {
	}

	@Override
	public IContributionItem remove(IContributionItem item) {
		final List<MToolBar> children = modelService.findElements(window, null, MToolBar.class, null);
		for (int i = 0; i < children.size(); i++) {
			final MToolBar child = children.get(i);
			final Object obj = child.getTransientData().get(OBJECT);
			if (obj != null && obj.equals(item)) {
				if (child instanceof MToolBarElement) {
					renderer.clearModelToContribution((MToolBarElement) child, item);
				}

				if (item instanceof IToolBarContributionItem) {
					IToolBarManager parent = ((IToolBarContributionItem) item).getToolBarManager();
					if (parent instanceof ToolBarManager) {
						renderer.clearModelToManager(child, (ToolBarManager) parent);
					}
				}
				workbenchTrimElements.remove(child);

				child.setToBeRendered(false);
				child.getParent().getChildren().remove(child);
				return (IContributionItem) obj;
			}
			if (item.getId() != null && item.getId().equals(child.getElementId())) {
				throw new IllegalStateException();
			}
		}
		return null;
	}

	@Override
	public IContributionItem remove(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetItemOrder() {
		updateAll(true);
	}

	@Override
	public void setContextMenuManager(IMenuManager menuManager) {
	}

	@Override
	public void setItems(IContributionItem[] newItems) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLockLayout(boolean value) {
		// 409633 Not implemented, see LockToolBarHandler
	}

	@Override
	public void setOverrides(IContributionManagerOverrides newOverrides) {
		this.toolbarOverrides = newOverrides;
		// this is required when we need to set the overrides for the
		// new ToolbarManager when it is created in ToolbarManagerRenderer
		topTrim.getTransientData().put(IContributionManagerOverrides.class.getName(), newOverrides);
	}

	@Override
	public void update(boolean force) {
		final List<MToolBar> children = modelService.findElements(window, null, MToolBar.class, null);

		for (MToolBar el : children) {
			ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(el, null);
			final ToolBarManager manager = renderer.getManager(el);
			if (manager != null) {
				boolean wasVisible = el.isVisible();
				boolean needUpdate = fill(el, manager);
				// fix for bug 383569#25: if the toolbar model changed the
				// visibility we must create (or remove) SWT toolbar widgets
				if (needUpdate || el.isVisible() != wasVisible) {
					manager.markDirty();
					manager.update(true);
				}
				// TODO: Hack to work around Bug 370961
				ToolBar toolbar = manager.getControl();
				if (toolbar != null && !toolbar.isDisposed()) {
					toolbar.requestLayout();
				}
			}
		}
		// and now add it to the model, start the rendering
		if (!trimBarsAdded) {
			boolean hidden = !topTrim.isVisible();
			if (hidden) {
				topTrim.setVisible(true);
			}
			topTrim.setToBeRendered(true);
			if (hidden) {
				topTrim.setVisible(false);
			}
			trimBarsAdded = true;
		}
	}

	/**
	 * @param force
	 */
	public void updateAll(boolean force) {
		final List<MToolBar> children = modelService.findElements(window, null, MToolBar.class, null);
		for (MToolBar mToolbar : children) {
			if (mToolbar == null) {
				continue;
			}
			ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(mToolbar, null);
			final ToolBarManager manager = renderer.getManager(mToolbar);
			if (manager != null) {
				manager.update(true);
				// TODO: Hack to work around Bug 370961
				ToolBar toolbar = manager.getControl();
				if (toolbar != null && !toolbar.isDisposed()) {
					toolbar.requestLayout();
				}
			}
		}
	}

	/**
	 * @return true if the contribution manager needs to be updated because item
	 *         visibility is changed
	 */
	private boolean fill(MToolBar container, IContributionManager manager) {
		boolean needUpdate = false;
		ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(container, null);

		IContributionItem[] items = manager.getItems();
		for (int index = 0; index < items.length; index++) {
			IContributionItem item = items[index];
			if (item == null) {
				continue;
			}
			MToolBarElement toolBarElem = renderer.getToolElement(item);
			if (toolBarElem != null) {
				if (container.isVisible()) {
					needUpdate |= applyOverridenVisibility(toolBarElem, item, manager);
					continue;
				}
				if (item.isSeparator() || item.isGroupMarker()) {
					continue;
				}
				// partial fix for bug 383569, introduced via fix for bug 402429
				// If the toolbar is hidden but one of the children is not,
				// make both the child and the toolbar visible
				if (isChildVisible(item, manager)) {
					needUpdate |= applyOverridenVisibility(toolBarElem, item, manager);
					container.setVisible(true);
				}
				continue;
			}
			if (item instanceof IToolBarContributionItem) {
				IToolBarManager manager2 = ((IToolBarContributionItem) item).getToolBarManager();
				needUpdate |= fill(container, manager2);
			} else if (item instanceof IMenuManager) {
				// No element to add in toolbar:
				// let the menu manager control its contributions.
				continue;
			} else if (item instanceof IContributionManager) {
				needUpdate |= fill(container, (IContributionManager) item);
			} else if (item instanceof CommandContributionItem) {
				MHandledToolItem toolItem = MenuHelper.createToolItem(application, (CommandContributionItem) item);
				if (toolItem == null) {
					continue;
				}
				// this section below should match what's in
				// org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer.processHandledItem(ToolBarManager,
				// MHandledToolItem)
				toolItem.setRenderer(renderer);
				HandledContributionItem ci = ContextInjectionFactory.make(HandledContributionItem.class,
						window.getContext());

				if (manager instanceof ContributionManager) {
					// set basic attributes to the item before adding to the manager
					ci.setId(toolItem.getElementId());
					ci.setVisible(toolItem.isVisible());

					ContributionManager cm = (ContributionManager) manager;
					cm.insert(index, ci);
					cm.remove(item);

					// explicitly dispose contribution since it is now
					// disconnected from manager
					item.dispose();
				}
				ci.setModel(toolItem);
				renderer.linkModelToContribution(toolItem, ci);
				container.getChildren().add(toolItem);
			} else {
				MToolItem toolItem = OpaqueElementUtil.createOpaqueToolItem();
				toolItem.setElementId(item.getId());
				OpaqueElementUtil.setOpaqueItem(toolItem, item);
				if (item instanceof AbstractGroupMarker) {
					toolItem.setVisible(item.isVisible());
				}
				// make sure the renderer knows this has already been processed
				renderer.linkModelToContribution(toolItem, item);
				container.getChildren().add(toolItem);
			}
		}
		return needUpdate;
	}

	/**
	 * @return true if the contribution manager needs to be updated because item
	 *         visibility is changed
	 */
	private boolean applyOverridenVisibility(MToolBarElement modelItem, IContributionItem item,
			IContributionManager manager) {
		boolean needUpdate = false;
		Boolean overridenVisibility = getOverridenVisibility(item, manager);
		Boolean prevChildVisible = (Boolean) modelItem.getTransientData().get(PREV_CHILD_VISIBLE);

		if (overridenVisibility != null) {
			if (prevChildVisible == null) {
				boolean modelVisible = modelItem.isVisible();
				boolean itemVisible = item.isVisible();
				if (modelVisible != overridenVisibility || itemVisible != overridenVisibility) {
					needUpdate = true;
				}
				modelItem.getTransientData().put(PREV_CHILD_VISIBLE, itemVisible);
				modelItem.setVisible(overridenVisibility);
			} else {
				return needUpdate;
			}
		} else if (prevChildVisible != null) {
			boolean oldVisible = modelItem.isVisible();
			if (oldVisible != prevChildVisible) {
				needUpdate = true;
			}
			modelItem.setVisible(prevChildVisible);
			modelItem.getTransientData().remove(PREV_CHILD_VISIBLE);
		} else {
			return needUpdate;
		}
		return needUpdate;
	}

	/**
	 * Checks if the item's visibility is overridden by the given manager
	 *
	 * @return non null overridden visibility value (if it is overridden), null
	 *         otherwise
	 */
	private Boolean getOverridenVisibility(IContributionItem item, IContributionManager manager) {
		IContributionManagerOverrides overrides = manager.getOverrides();
		return overrides == null ? null : overrides.getVisible(item);
		}

	/**
	 * Computes real item visibility considering possibly overridden state from
	 * manager
	 */
	private boolean isChildVisible(IContributionItem item, IContributionManager manager) {
		Boolean v = getOverridenVisibility(item, manager);
		return v == null ? item.isVisible() : v.booleanValue();
	}

	public MTrimBar getTopTrim() {
		return topTrim;
	}
}
