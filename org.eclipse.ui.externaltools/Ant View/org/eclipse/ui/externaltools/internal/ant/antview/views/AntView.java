/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.views;

import java.util.Hashtable;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.externaltools.internal.ant.antview.actions.ClearAction;
import org.eclipse.ui.externaltools.internal.ant.antview.actions.MsgLvlAction;
import org.eclipse.ui.externaltools.internal.ant.antview.actions.RefreshAction;
import org.eclipse.ui.externaltools.internal.ant.antview.actions.RemoveProjectAction;
import org.eclipse.ui.externaltools.internal.ant.antview.actions.RunAction;
import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;
import org.eclipse.ui.externaltools.internal.ant.antview.events.OpenListener;
import org.eclipse.ui.externaltools.internal.ant.antview.preferences.Preferences;
import org.eclipse.ui.part.ViewPart;

public class AntView extends ViewPart {
	{
		ResourceMgr.init();
		Preferences.setDefaults();
	}
	private Hashtable actionMap;
	private TreeViewer viewer = null;
	private AntViewContentProvider viewContentProvider = null;

	/**
	 * The constructor.
	 */
	public AntView() {
	}
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		ResourceMgr.dispose();
		super.dispose();
	}
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		// Build Components
		makeActions();
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		viewer.setUseHashlookup(true);
		viewContentProvider = new AntViewContentProvider();
		AntViewLabelProvider viewLabelProvider = new AntViewLabelProvider();
		//DrillDownAdapter drillDownAdapter = new DrillDownAdapter(viewer);
		// Connect Components
		viewer.setContentProvider(viewContentProvider);
		viewer.setLabelProvider(new DecoratingLabelProvider(viewLabelProvider, viewLabelProvider));
		viewer.setSorter(new AntViewSorter());
		viewer.setInput(ResourcesPlugin.getWorkspace());
		viewer.addOpenListener(new OpenListener());		
		viewer.addFilter(new AntViewFilter());
		hookContextMenu();
		contributeToActionBars();
        // Check Preference DisplayLevel Action	
		String action = Preferences.getString(IAntViewConstants.PREF_ANT_DISPLAY);
		((Action) (actionMap.get(action))).run();
	}
	/**
	 * Method hookContextMenu.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AntView.this.mainContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}	
	/**
	 * Method contributeToActionBars.
	 */
	private void contributeToActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();

		IToolBarManager toolBarMgr = actionBars.getToolBarManager();
		toolBarMgr.add((Action)actionMap.get(IAntViewConstants.IMAGE_RUN));
		toolBarMgr.add((Action)actionMap.get(IAntViewConstants.IMAGE_REMOVE));
		toolBarMgr.add((Action)actionMap.get(IAntViewConstants.IMAGE_CLEAR));
		toolBarMgr.add((Action)actionMap.get(IAntViewConstants.IMAGE_REFRESH));

		IMenuManager menuMgr = actionBars.getMenuManager();
		MenuManager submenu =
			new MenuManager(ResourceMgr.getString("Label.DisplayLevel"));
		menuMgr.add(submenu);
		submenu.setRemoveAllWhenShown(true);
		submenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				msgLvlContextMenu(mgr);
			}
		});
		msgLvlContextMenu(submenu);

		actionBars.updateActionBars();
	}
	/**
	 * msgLvlContextMenu.
	 * @param manager
	 */
	private void msgLvlContextMenu(IMenuManager manager) {
		manager.add((Action)actionMap.get(IAntViewConstants.ANT_DISPLAYLVL_ERROR));
		manager.add((Action)actionMap.get(IAntViewConstants.ANT_DISPLAYLVL_WARN));
		manager.add((Action)actionMap.get(IAntViewConstants.ANT_DISPLAYLVL_INFO));
		manager.add((Action)actionMap.get(IAntViewConstants.ANT_DISPLAYLVL_VERBOSE));
		manager.add((Action)actionMap.get(IAntViewConstants.ANT_DISPLAYLVL_DEBUG));
	}
	/**
	 * Method mainContextMenu.
	 * @param manager
	 */
	private void mainContextMenu(IMenuManager manager) {
		manager.add((Action)actionMap.get(IAntViewConstants.IMAGE_RUN));
		manager.add((Action)actionMap.get(IAntViewConstants.IMAGE_REFRESH));
		manager.add((Action)actionMap.get(IAntViewConstants.IMAGE_CLEAR));
		manager.add((Action)actionMap.get(IAntViewConstants.IMAGE_REMOVE));

		MenuManager submenu =
			new MenuManager(ResourceMgr.getString("Label.DisplayLevel"));
		manager.add(submenu);
		msgLvlContextMenu(submenu);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator("Additions"));
	}
	/**
	 * Method makeActions.
	 */
	private void makeActions() {			
		actionMap = new Hashtable();

		actionMap.put(
			IAntViewConstants.IMAGE_REMOVE,
			new RemoveProjectAction(
				"Remove",
				ResourceMgr.getImageDescriptor(IAntViewConstants.IMAGE_REMOVE)));
		actionMap.put(
			IAntViewConstants.IMAGE_RUN,
			new RunAction(
				ResourceMgr.getString("Label.Run"),
				ResourceMgr.getImageDescriptor(IAntViewConstants.IMAGE_RUN)));
		actionMap.put(
			IAntViewConstants.IMAGE_REFRESH,
			new RefreshAction(
				ResourceMgr.getString("Label.Refresh"),
				ResourceMgr.getImageDescriptor(IAntViewConstants.IMAGE_REFRESH)));
		actionMap.put(
			IAntViewConstants.IMAGE_CLEAR,
			new ClearAction(
				ResourceMgr.getString("Label.Clear"),
				ResourceMgr.getImageDescriptor(IAntViewConstants.IMAGE_CLEAR)));
		actionMap.put(
			IAntViewConstants.ANT_DISPLAYLVL_ERROR,
			new MsgLvlAction(
				ResourceMgr.getString("DisplayLevel.Error"),
				IAntViewConstants.ANT_DISPLAYLVL_ERROR));
		actionMap.put(
			IAntViewConstants.ANT_DISPLAYLVL_WARN,
			new MsgLvlAction(
				ResourceMgr.getString("DisplayLevel.Warn"),
				IAntViewConstants.ANT_DISPLAYLVL_WARN));
		actionMap.put(
			IAntViewConstants.ANT_DISPLAYLVL_INFO,
			new MsgLvlAction(
				ResourceMgr.getString("DisplayLevel.Info"),
				IAntViewConstants.ANT_DISPLAYLVL_INFO));
		actionMap.put(
			IAntViewConstants.ANT_DISPLAYLVL_VERBOSE,
			new MsgLvlAction(
				ResourceMgr.getString("DisplayLevel.Verbose"),
				IAntViewConstants.ANT_DISPLAYLVL_VERBOSE));
		actionMap.put(
			IAntViewConstants.ANT_DISPLAYLVL_DEBUG,
			new MsgLvlAction(
				ResourceMgr.getString("DisplayLevel.Debug"),
				IAntViewConstants.ANT_DISPLAYLVL_DEBUG));			
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	/**
	 * Method getViewContentProvider.
	 * @return AntViewContentProvider
	 */
	public AntViewContentProvider getViewContentProvider() { 
		return viewContentProvider;
	}
	/**
	 * Method getMsgLevelAction.
	 * @param action
	 * @return Action
	 */
	public Action getMsgLevelAction(String action) { 
		return (Action) (actionMap.get(action));
	}
	/**
	 * Method refresh.
	 */
	public void refresh() { 
		viewer.refresh();
	}
	/**
	 * Method getTreeViewer.
	 * @return TreeViewer
	 */
	public TreeViewer getTreeViewer() { 
		return viewer;
	}
}