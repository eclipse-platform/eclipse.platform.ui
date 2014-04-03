/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Maxime Porhel <maxime.porhel@obeo.fr> Obeo - Bug 430116
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.menus.CommandContributionItem;

/**
 * @since 3.5
 * 
 */
public class CoolBarToTrimManager extends ContributionManager implements ICoolBarManager2 {

	private static final String TOOLBAR_SEPARATOR = "toolbarSeparator"; //$NON-NLS-1$
	private static final String MAIN_TOOLBAR_ID = "org.eclipse.ui.main.toolbar"; //$NON-NLS-1$
	private static final String OBJECT = "coolbar.object"; //$NON-NLS-1$
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
	private boolean trimBarsAdded = false;
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

		renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(
				MenuFactoryImpl.eINSTANCE.createToolBar(), null);
	}

	@Override
	public void add(IAction action) {
		// TODO Auto-generated method stub

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
			IToolBarManager mgr = ((IToolBarContributionItem) item).getToolBarManager();
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
				toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
			} else {
				toolBar.getChildren().clear();
			}
			toolBar.setElementId(item.getId());
			toolBar.getTransientData().put(OBJECT, item);
			if (manager instanceof ToolBarManager) {
				renderer.linkModelToManager(toolBar, (ToolBarManager) manager);
			}
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
			new Exception("Have to deal with " + item).printStackTrace(); //$NON-NLS-1$
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
			MToolBarSeparator separator = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
			separator.setToBeRendered(false);
			separator.setElementId(item.getId());

			List<MToolBar> toolbars = modelService.findElements(window, item.getId(),
					MToolBar.class, null);
			MToolBar toolBar = toolbars.isEmpty() ? null : toolbars.get(0);
			boolean tbFound = toolBar != null;
			if (!tbFound) {
				toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
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

	@Override
	public void add(final IToolBarManager toolBarManager) {
		if (toolBarManager instanceof ToolBarManager) {
			add(new ToolBarContributionItem(toolBarManager));
		}
	}

	@Override
	public void appendToGroup(String groupName, IAction action) {
	}

	@Override
	public void appendToGroup(String groupName, IContributionItem item) {
		List<MToolBar> toolBars = modelService
				.findElements(window, groupName, MToolBar.class, null);
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
		new Exception("CBTTM:createControl2()").printStackTrace(); //$NON-NLS-1$
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

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
		ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(
				model, null);
		final ToolBarManager manager = renderer.getManager(model);
		if (manager != null) {
			final ToolBarContributionItem toolBarContributionItem = new ToolBarContributionItem(
					manager, model.getElementId()) {
				@Override
				public void setVisible(boolean visible) {
					super.setVisible(visible);
					model.setVisible(visible);
				}
			};
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
		new Exception("CBTTM:getContextMenuManager()").printStackTrace(); //$NON-NLS-1$
		return null;
	}

	@Override
	public Control getControl2() {
		new Exception("CBTTM:getControl2()").printStackTrace(); //$NON-NLS-1$
		return null;
	}

	@Override
	public IContributionItem[] getItems() {
		ArrayList<IContributionItem> items = new ArrayList<IContributionItem>();

		List<MToolBar> toolBars = modelService.findElements(window, null, MToolBar.class, null);
		for (MToolBar el : toolBars) {
			final MToolBar tb = el;
			if (tb.getTransientData().get(OBJECT) != null) {
				items.add((IContributionItem) tb.getTransientData().get(OBJECT));
			} else {
				ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory
						.getRenderer(tb, null);
				final ToolBarManager manager = renderer.getManager(tb);
				if (manager != null) {
					final ToolBarContributionItem toolBarContributionItem = new ToolBarContributionItem(
							manager, tb.getElementId()) {
						@Override
						public void setVisible(boolean visible) {
							super.setVisible(visible);
							tb.setVisible(visible);
						}
					};
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IContributionManagerOverrides getOverrides() {
		return toolbarOverrides;
	}

	@Override
	public int getStyle() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void insertAfter(String id, IAction action) {
		// TODO Auto-generated method stub

	}

	private MTrimBar getTrim(MTrimElement te) {
		if (te == null)
			return null;

		MUIElement parentElement = te.getParent();
		return (MTrimBar) (parentElement instanceof MTrimBar ? parentElement : null);
	}

	private MToolBar getToolBar(String id) {
		List<MToolBar> toolbars = modelService.findElements(window, id, MToolBar.class, null);
		if (toolbars.size() == 1)
			return toolbars.get(0);

		return null;
	}

	@Override
	public void insertAfter(String id, IContributionItem item) {
		MToolBar afterElement = getToolBar(id);
		if (afterElement == null || getTrim(afterElement) == null)
			return;

		MTrimBar trimBar = getTrim(afterElement);
		int index = trimBar.getChildren().indexOf(afterElement);
		index = index + 1 < trimBar.getChildren().size() ? index + 1 : -1;
		add(trimBar, index, item);
	}

	@Override
	public void insertBefore(String id, IAction action) {
	}

	@Override
	public void insertBefore(String id, IContributionItem item) {
		MToolBar beforeElement = getToolBar(id);
		if (beforeElement == null || getTrim(beforeElement) == null)
			return;

		MTrimBar trimBar = getTrim(beforeElement);
		int index = trimBar.getChildren().indexOf(beforeElement);
		add(trimBar, index, item);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		return topTrim.getChildren().isEmpty();
	}

	@Override
	public void markDirty() {
		// TODO Auto-generated method stub

	}

	@Override
	public void prependToGroup(String groupName, IAction action) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public IContributionItem remove(IContributionItem item) {
		final List<MToolBar> children = modelService.findElements(window, null, MToolBar.class,
				null);
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
						renderer.clearModelToManager((MToolBar) child, (ToolBarManager) parent);
					}
				}
				workbenchTrimElements.remove(child);

				child.setToBeRendered(false);
				child.getParent().getChildren().remove(child);
				return (IContributionItem) obj;
			}
			if (item.getId() != null && item.getId().equals(child.getElementId())) {
				new Exception("CBTTM:remove(IContributionItem item) " + item //$NON-NLS-1$
						+ "\n\t" + child).printStackTrace(); //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public IContributionItem remove(String id) {
		new Exception("CBTTM:remove(String id) " + id).printStackTrace(); //$NON-NLS-1$
		return null;
	}

	@Override
	public void removeAll() {
		new Exception("CBTTM:removeAll").printStackTrace(); //$NON-NLS-1$
	}

	@Override
	public void resetItemOrder() {
		updateAll(true);
	}

	@Override
	public void setContextMenuManager(IMenuManager menuManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setItems(IContributionItem[] newItems) {
		new Exception("CBTTM:setItems(IContributionItem[] newItems)").printStackTrace(); //$NON-NLS-1$
	}

	@Override
	public void setLockLayout(boolean value) {
		// TODO Auto-generated method stub

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
		final List<MToolBar> children = modelService.findElements(window, null, MToolBar.class,
				null);

		for (MToolBar el : children) {
			ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(
					el, null);
			final ToolBarManager manager = renderer.getManager((MToolBar) el);
			if (manager != null) {
				// if (!el.isVisible() || !el.isToBeRendered()) {
				//						System.out.println("update(boolean force): " + el); //$NON-NLS-1$
				// }
				fill(el, manager);
				// TODO: Hack to work around Bug 370961
				ToolBar tb = manager.getControl();
				if (tb != null && !tb.isDisposed()) {
					tb.getShell().layout(new Control[] { tb }, SWT.DEFER);
				}
			}
		}
		// and now add it to the model, start the rendering
		if (!trimBarsAdded) {
			topTrim.setToBeRendered(true);
			trimBarsAdded = true;
		}
	}

	/**
	 * @param force
	 */
	public void updateAll(boolean force) {
		final List<MToolBar> children = modelService.findElements(window, null, MToolBar.class,
				null);
		for (MToolBar el : children) {
			if (el instanceof MToolBar) {
				MToolBar toolbar = (MToolBar) el;
				ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory
						.getRenderer(el, null);
				final ToolBarManager manager = renderer.getManager(toolbar);
				if (manager != null) {
					manager.update(true);
					// TODO: Hack to work around Bug 370961
					ToolBar tb = manager.getControl();
					if (tb != null && !tb.isDisposed()) {
						tb.getShell().layout(new Control[] { tb }, SWT.DEFER);
					}
				}
			}
		}
	}

	private void fill(MToolBar container, IContributionManager manager) {
		ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(
				container, null);

		for (IContributionItem item : manager.getItems()) {
			if (item == null) {
				continue;
			}
			MToolBarElement toolBarElem = renderer.getToolElement(item);
			if (toolBarElem != null) {
				if (container.isVisible()) {
					setChildVisible(toolBarElem, item, manager);
				}
				continue;
			}
			if (item instanceof IToolBarContributionItem) {
				IToolBarManager manager2 = ((IToolBarContributionItem) item).getToolBarManager();
				//new Exception("fill(MToolBar container, IContributionManager manager) with " //$NON-NLS-1$
				//		+ item + " to " + manager2).printStackTrace(); //$NON-NLS-1$
				fill(container, manager2);
			} else if (item instanceof IMenuManager) {
				// No element to add in toolbar:
				// let the menu manager control its contributions.
				continue;
			} else if (item instanceof IContributionManager) {
				// new Exception(
				//		"fill(MToolBar container, IContributionManager manager) with rogue contribution manager: " //$NON-NLS-1$
				// + item).printStackTrace();
				fill(container, (IContributionManager) item);
			} else if (item instanceof CommandContributionItem) {
				CommandContributionItem cci = (CommandContributionItem) item;
				MToolItem toolItem = MenuHelper.createToolItem(application, cci);
				manager.remove(item);
				if (toolItem != null) {
					container.getChildren().add(toolItem);
				}
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
	}

	private void setChildVisible(MToolBarElement modelItem, IContributionItem item,
			IContributionManager manager) {
		Boolean currentChildVisible = isChildVisible(item, manager);
		Boolean prevChildVisible = (Boolean) modelItem.getTransientData().get(PREV_CHILD_VISIBLE);

		if (currentChildVisible != null) {
			if (prevChildVisible == null) {
				modelItem.getTransientData().put(PREV_CHILD_VISIBLE, modelItem.isVisible());
				modelItem.setVisible(currentChildVisible);
			}
		} else if (prevChildVisible != null) {
			modelItem.setVisible(prevChildVisible);
			modelItem.getTransientData().remove(PREV_CHILD_VISIBLE);
		}
	}

	private Boolean isChildVisible(IContributionItem item, IContributionManager manager) {
		Boolean v;
		IContributionManagerOverrides overrides = manager.getOverrides();
		if (overrides == null) {
			v = null;
		} else {
			v = overrides.getVisible(item);
		}

		if (v != null) {
			return v.booleanValue();
		}
		return null;
	}
}
