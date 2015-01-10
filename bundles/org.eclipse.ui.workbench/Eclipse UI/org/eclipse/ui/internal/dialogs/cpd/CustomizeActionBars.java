/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Erik Chou <ekchou@ymail.com> - Bug 378849
 *     Paul Webster <pwebster@ca.ibm.com> - Bug 378849
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 420956 - Fix perspective customization on 4.x
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs.cpd;

import java.util.ArrayList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.internal.provisional.action.ToolBarContributionItem2;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.CoolBarToTrimManager;
import org.eclipse.ui.internal.menus.ActionSet;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Fake action bars to build the menus and toolbar contributions for the
 * workbench. We cannot use the actual workbench action bars, since doing so
 * would make the action set items visible.
 *
 * @since 3.5
 */
public class CustomizeActionBars implements IActionBarConfigurer2, IActionBars2 {

	private final IWorkbenchWindowConfigurer configurer;
	private final StatusLineManager statusLineManager;
	private final MApplication app;

	final MenuManager menuManager;
	final CoolBarToTrimManager coolBarManager;
	final MTrimmedWindow windowModel;
	final MMenu mainMenu;
	final MenuManagerRenderer menuRenderer;

	/**
	 * Create a new instance of this class.
	 *
	 * @param configurer
	 *            non null
	 * @param context
	 *            non null
	 */
	public CustomizeActionBars(IWorkbenchWindowConfigurer configurer, IEclipseContext context) {
		this.configurer = configurer;
		statusLineManager = new StatusLineManager();
		menuManager = new MenuManager("MenuBar", ActionSet.MAIN_MENU); //$NON-NLS-1$

		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		EModelService modelService = context.get(EModelService.class);

		windowModel = modelService.createModelElement(MTrimmedWindow.class);
		app = context.get(MApplication.class);
		IEclipseContext eclipseContext = app.getContext().createChild("window - CustomizeActionBars"); //$NON-NLS-1$
		windowModel.setContext(eclipseContext);
		eclipseContext.set(MWindow.class, windowModel);

		Shell shell = new Shell();
		windowModel.setWidget(shell);
		windowModel.setToBeRendered(false);
		app.getChildren().add(windowModel);
		shell.setData(org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer.OWNING_ME, windowModel);

		// See WorkbenchWindow.setup()
		mainMenu = modelService.createModelElement(MMenu.class);
		mainMenu.setElementId(ActionSet.MAIN_MENU);

		menuRenderer = (MenuManagerRenderer) rendererFactory.getRenderer(mainMenu, null);
		menuRenderer.linkModelToManager(mainMenu, menuManager);
		windowModel.setMainMenu(mainMenu);

		coolBarManager = new CoolBarToTrimManager(app, windowModel, new ArrayList<MTrimElement>(), rendererFactory);
	}

	@Override
	public IWorkbenchWindowConfigurer getWindowConfigurer() {
		return configurer;
	}

	@Override
	public IMenuManager getMenuManager() {
		return menuManager;
	}

	@Override
	public IStatusLineManager getStatusLineManager() {
		return statusLineManager;
	}

	@Override
	public CoolBarToTrimManager getCoolBarManager() {
		return coolBarManager;
	}

	@Override
	public IToolBarManager getToolBarManager() {
		return null;
	}

	@Override
	public void setGlobalActionHandler(String actionID, IAction handler) {
	}

	@Override
	public void updateActionBars() {
	}

	@Override
	public void clearGlobalActionHandlers() {
	}

	@Override
	public IAction getGlobalActionHandler(String actionId) {
		return null;
	}

	@Override
	public void registerGlobalAction(IAction action) {
	}

	/**
	 * Clean up the action bars.
	 */
	public void dispose() {
		coolBarManager.dispose();
		menuManager.dispose();
		statusLineManager.dispose();
		windowModel.getContext().deactivate();
		windowModel.getContext().dispose();
		((Shell) windowModel.getWidget()).dispose();
		app.getChildren().remove(windowModel);
	}

	@Override
	public final IServiceLocator getServiceLocator() {
		return configurer.getWindow();
	}

	@Override
	public IToolBarContributionItem createToolBarContributionItem(IToolBarManager toolBarManager, String id) {
		return new ToolBarContributionItem2(toolBarManager, id);
	}

	@Override
	public IToolBarManager createToolBarManager() {
		return new ToolBarManager();
	}
}
