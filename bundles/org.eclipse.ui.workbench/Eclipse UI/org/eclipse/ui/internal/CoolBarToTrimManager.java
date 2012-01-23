/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.List;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ActionContributionItem;
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
	private MTrimBar trimBar;
	private List<MTrimElement> workbenchTrimElements;
	private IRendererFactory rendererFactory;
	private ToolBarManagerRenderer renderer;
	private MApplication application;
	private MTrimmedWindow window;

	/**
	 * Field to indicate whether the trim bars have been added to the window's
	 * model or not. They should only ever be added once.
	 */
	private boolean trimBarsAdded = false;

	public CoolBarToTrimManager(MApplication app, MTrimmedWindow window,
			List<MTrimElement> workbenchTrimElements, IRendererFactory rf) {
		application = app;
		this.window = window;
		rendererFactory = rf;
		this.workbenchTrimElements = workbenchTrimElements;
		for (MTrimBar tb : window.getTrimBars()) {
			if (MAIN_TOOLBAR_ID.equals(tb.getElementId())) {
				trimBar = tb;
				trimBarsAdded = true;
				break;
			}
		}
		if (trimBar == null) {
			trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
			trimBar.setElementId(MAIN_TOOLBAR_ID);
		}

		renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(
				MenuFactoryImpl.eINSTANCE.createToolBar(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#add(org.eclipse.jface.action
	 * .IAction)
	 */
	public void add(IAction action) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#add(org.eclipse.jface.action
	 * .IContributionItem)
	 */
	public void add(IContributionItem item) {
		add(-1, item);
	}

	private void add(int idx, IContributionItem item) {
		if (item instanceof IToolBarContributionItem) {
			IToolBarManager mgr = ((IToolBarContributionItem) item).getToolBarManager();
			if (!(mgr instanceof ToolBarManager)) {
				return;
			}
			ToolBarManager manager = (ToolBarManager) mgr;

			if (renderer.getToolBarModel(manager) != null) {
				return;
			}
			MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
			toolBar.setElementId(item.getId());
			toolBar.getTransientData().put(OBJECT, item);
			if (manager instanceof ToolBarManager) {
				renderer.linkModelToManager(toolBar, (ToolBarManager) manager);
			}
			toolBar.setToBeRendered(true);
			if (idx < 0) {
				trimBar.getChildren().add(toolBar);
			} else {
				trimBar.getChildren().add(idx, toolBar);
			}
			workbenchTrimElements.add(toolBar);
		} else if (item instanceof IContributionManager) {
			new Exception("Have to deal with " + item).printStackTrace(); //$NON-NLS-1$
		} else if (item instanceof AbstractGroupMarker) {
			if (item.getId() == null) {
				return;
			}
			for (MTrimElement toolBar : trimBar.getChildren()) {
				if (item.getId().equals(toolBar.getElementId())
						&& toolBar.getTags().contains(TOOLBAR_SEPARATOR)) {
					// already in the coolbar
					return;
				}
			}
			MToolBarSeparator separator = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
			separator.setToBeRendered(false);
			separator.setElementId(item.getId());

			MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
			toolBar.getTransientData().put(OBJECT, item);
			toolBar.getTags().add(TOOLBAR_SEPARATOR);
			toolBar.setElementId(item.getId());
			toolBar.getChildren().add(separator);
			toolBar.setToBeRendered(false);
			if (idx < 0) {
				trimBar.getChildren().add(toolBar);
			} else {
				trimBar.getChildren().add(idx, toolBar);
			}
			workbenchTrimElements.add(toolBar);
		}

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ICoolBarManager#add(org.eclipse.jface.action
	 * .IToolBarManager)
	 */
	public void add(final IToolBarManager toolBarManager) {
		if (toolBarManager instanceof ToolBarManager) {
			add(new ToolBarContributionItem(toolBarManager));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#appendToGroup(java.lang
	 * .String, org.eclipse.jface.action.IAction)
	 */
	public void appendToGroup(String groupName, IAction action) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#appendToGroup(java.lang
	 * .String, org.eclipse.jface.action.IContributionItem)
	 */
	public void appendToGroup(String groupName, IContributionItem item) {
		boolean foundGroup = false;
		final List<MTrimElement> children = trimBar.getChildren();
		for (int i = 0; i < children.size(); i++) {
			MTrimElement el = children.get(i);
			if (groupName.equals(el.getElementId()) && el.getTags().contains(TOOLBAR_SEPARATOR)) {
				foundGroup = true;
			} else if (foundGroup && el.getTags().contains(TOOLBAR_SEPARATOR)) {
				add(i, item);
				return;
			}
		}
		add(-1, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.internal.provisional.action.ICoolBarManager2#createControl2
	 * (org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl2(Composite parent) {
		new Exception("CBTTM:createControl2()").printStackTrace(); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.internal.provisional.action.ICoolBarManager2#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#find(java.lang.String)
	 */
	public IContributionItem find(String id) {
		for (MTrimElement el : trimBar.getChildren()) {
			if (id.equals(el.getElementId())) {
				if (el instanceof MToolBar) {
					final MToolBar model = (MToolBar) el;
					if (model.getTransientData().get(OBJECT) != null) {
						return (IContributionItem) model.getTransientData().get(OBJECT);
					}
					ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory
							.getRenderer(model, null);
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
					} else {
						return null;
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ICoolBarManager#getContextMenuManager()
	 */
	public IMenuManager getContextMenuManager() {
		new Exception("CBTTM:getContextMenuManager()").printStackTrace(); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.internal.provisional.action.ICoolBarManager2#getControl2
	 * ()
	 */
	public Control getControl2() {
		new Exception("CBTTM:getControl2()").printStackTrace(); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#getItems()
	 */
	public IContributionItem[] getItems() {
		new Exception("CBTTM:getItems()").printStackTrace(); //$NON-NLS-1$
		return new IContributionItem[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ICoolBarManager#getLockLayout()
	 */
	public boolean getLockLayout() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#getOverrides()
	 */
	public IContributionManagerOverrides getOverrides() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ICoolBarManager#getStyle()
	 */
	public int getStyle() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#insertAfter(java.lang.String
	 * , org.eclipse.jface.action.IAction)
	 */
	public void insertAfter(String id, IAction action) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#insertAfter(java.lang.String
	 * , org.eclipse.jface.action.IContributionItem)
	 */
	public void insertAfter(String id, IContributionItem item) {
		final List<MTrimElement> children = trimBar.getChildren();
		for (int i = 0; i < children.size(); i++) {
			MTrimElement el = children.get(i);
			if (id.equals(el.getElementId())) {
				add(i + 1 < children.size() ? i + 1 : -1, item);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#insertBefore(java.lang.
	 * String, org.eclipse.jface.action.IAction)
	 */
	public void insertBefore(String id, IAction action) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#insertBefore(java.lang.
	 * String, org.eclipse.jface.action.IContributionItem)
	 */
	public void insertBefore(String id, IContributionItem item) {
		final List<MTrimElement> children = trimBar.getChildren();
		for (int i = 0; i < children.size(); i++) {
			MTrimElement el = children.get(i);
			if (id.equals(el.getElementId())) {
				add(i, item);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#isDirty()
	 */
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#isEmpty()
	 */
	public boolean isEmpty() {
		return trimBar.getChildren().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#markDirty()
	 */
	public void markDirty() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#prependToGroup(java.lang
	 * .String, org.eclipse.jface.action.IAction)
	 */
	public void prependToGroup(String groupName, IAction action) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#prependToGroup(java.lang
	 * .String, org.eclipse.jface.action.IContributionItem)
	 */
	public void prependToGroup(String groupName, IContributionItem item) {
		final List<MTrimElement> children = trimBar.getChildren();
		for (int i = 0; i < children.size(); i++) {
			MTrimElement el = children.get(i);
			if (groupName.equals(el.getElementId()) && el.getTags().contains(TOOLBAR_SEPARATOR)) {
				add(i + 1 < children.size() ? i + 1 : -1, item);
				return;
			}
		}
		add(-1, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.internal.provisional.action.ICoolBarManager2#refresh()
	 */
	public void refresh() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#remove(org.eclipse.jface
	 * .action.IContributionItem)
	 */
	public IContributionItem remove(IContributionItem item) {
		final List<MTrimElement> children = trimBar.getChildren();
		for (int i = 0; i < children.size(); i++) {
			final MTrimElement child = children.get(i);
			final Object obj = child.getTransientData().get(OBJECT);
			if (obj != null && obj.equals(item)) {
				if (child instanceof MToolBarElement) {
					renderer.clearModelToContribution((MToolBarElement) child, item);
				}

				if (child instanceof MToolBar && item instanceof IToolBarContributionItem) {
					IToolBarManager parent = ((IToolBarContributionItem) item).getToolBarManager();
					if (parent instanceof ToolBarManager) {
						renderer.clearModelToManager((MToolBar) child, (ToolBarManager) parent);
					}
				}
				
				children.get(i).setToBeRendered(false);
				children.remove(i);
				return (IContributionItem) obj;
			}
			if (item.getId() != null && item.getId().equals(child.getElementId())) {
				new Exception("CBTTM:remove(IContributionItem item) " + item //$NON-NLS-1$
						+ "\n\t" + child).printStackTrace(); //$NON-NLS-1$
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IContributionManager#remove(java.lang.String)
	 */
	public IContributionItem remove(String id) {
		new Exception("CBTTM:remove(String id) " + id).printStackTrace(); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#removeAll()
	 */
	public void removeAll() {
		new Exception("CBTTM:removeAll").printStackTrace(); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.internal.provisional.action.ICoolBarManager2#resetItemOrder
	 * ()
	 */
	public void resetItemOrder() {
		new Exception("CBTTM:resetItemOrder").printStackTrace(); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ICoolBarManager#setContextMenuManager(org.eclipse
	 * .jface.action.IMenuManager)
	 */
	public void setContextMenuManager(IMenuManager menuManager) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.internal.provisional.action.ICoolBarManager2#setItems
	 * (org.eclipse.jface.action.IContributionItem[])
	 */
	public void setItems(IContributionItem[] newItems) {
		new Exception("CBTTM:setItems(IContributionItem[] newItems)").printStackTrace(); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ICoolBarManager#setLockLayout(boolean)
	 */
	public void setLockLayout(boolean value) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.internal.provisional.action.ICoolBarManager2#setOverrides
	 * (org.eclipse.jface.action.IContributionManagerOverrides)
	 */
	public void setOverrides(IContributionManagerOverrides newOverrides) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	public void update(boolean force) {
		for (MTrimElement el : trimBar.getChildren()) {
			if (el instanceof MToolBar) {
				ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory
						.getRenderer(el, null);
				final ToolBarManager manager = renderer.getManager((MToolBar) el);
				if (manager != null) {
					// if (!el.isVisible() || !el.isToBeRendered()) {
					//						System.out.println("update(boolean force): " + el); //$NON-NLS-1$
					// }
					fill((MToolBar) el, manager);
				}
			}
		}
		// and now add it to the model, start the rendering
		if (!trimBarsAdded) {
			List<MTrimBar> trimBars = window.getTrimBars();
			trimBars.add(0, trimBar);
			trimBarsAdded = true;
		}
	}

	private void fill(MToolBar container, IContributionManager manager) {
		ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) rendererFactory.getRenderer(
				container, null);

		for (IContributionItem item : manager.getItems()) {
			if (item == null) {
				continue;
			}
			if (renderer.getToolElement(item) != null) {
				continue;
			}
			if (item instanceof IToolBarContributionItem) {
				IToolBarManager manager2 = ((IToolBarContributionItem) item).getToolBarManager();
				new Exception("fill(MToolBar container, IContributionManager manager) with " //$NON-NLS-1$
						+ item + " to " + manager2).printStackTrace(); //$NON-NLS-1$
				fill(container, manager2);
			} else if (item instanceof IContributionManager) {
				new Exception(
						"fill(MToolBar container, IContributionManager manager) with rogue contribution manager: " //$NON-NLS-1$
								+ item).printStackTrace();
				fill(container, (IContributionManager) item);
			} else if (item instanceof CommandContributionItem) {
				CommandContributionItem cci = (CommandContributionItem) item;
				MToolItem toolItem = MenuHelper.createToolItem(application, cci);
				manager.remove(item);
				if (toolItem != null) {
					container.getChildren().add(toolItem);
				}
			} else if (item instanceof AbstractGroupMarker) {
				MToolBarSeparator separator = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
				separator.setToBeRendered(item.isVisible());
				separator.setVisible(false);
				separator.setElementId(item.getId());
				container.getChildren().add(separator);
				manager.remove(item);
			} else if (item instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem) item).getAction();
				if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
					MOpaqueToolItem toolItem = MenuFactoryImpl.eINSTANCE.createOpaqueToolItem();
					toolItem.setElementId(item.getId());
					container.getChildren().add(toolItem);
					renderer.linkModelToContribution(toolItem, item);
				} else {
					MToolItem toolItem = MenuHelper.createToolItem(application,
							(ActionContributionItem) item);
					manager.remove(item);
					if (toolItem != null) {
						container.getChildren().add(toolItem);
					}
				}
			} else {
				MOpaqueToolItem toolItem = MenuFactoryImpl.eINSTANCE.createOpaqueToolItem();
				toolItem.setElementId(item.getId());
				container.getChildren().add(toolItem);
				renderer.linkModelToContribution(toolItem, item);
			}
		}
	}
}
