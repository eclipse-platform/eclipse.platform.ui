package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.views.framelist.*;
import org.eclipse.ui.views.navigator.*;

/**
 * The GotoActionFactory is the class
 * that adds the goto actions for a menu.
 * 
 * @deprecated use GotoActionGroup
 */
public class GotoActionFactory extends ActionFactory {

	protected FrameList frameList;
	protected IResourceNavigatorPart resourceNavigator;
	protected BackAction backAction;
	protected ForwardAction forwardAction;
	protected GoIntoAction goIntoAction;
	protected UpAction upAction;
	protected GotoResourceAction gotoResourceAction;

	/**
	 * @deprecated
	 */
	public GotoActionFactory(
		FrameList list,
		IResourceNavigatorPart navigator) {
		frameList = list;
		resourceNavigator = navigator;
	}

	/*
	 * @see ActionFactory#makeActions()
	 */
	public void makeActions() {

		goIntoAction = new GoIntoAction(frameList);
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);
		gotoResourceAction =
			new GotoResourceAction(
				resourceNavigator,
				ResourceNavigatorMessages.getString("ResourceNavigator.resourceText")); //$NON-NLS-1$
		//$NON-NLS-1$
	}

	/*
	* @see ActionFactory#fillToolBar(IToolBarManager)
	*/
	public void fillToolBar(IToolBarManager toolBar) {
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
	}

	/*
	 * @see ActionFactory#fillPopUpMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillPopUpMenu(IMenuManager menu, IStructuredSelection selection) {

		if (selection.size() == 1
			&& ResourceSelectionUtil.allResourcesAreOfType(
				selection,
				IResource.PROJECT | IResource.FOLDER)) {
			menu.add(goIntoAction);
		}
		MenuManager gotoMenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.goto")); //$NON-NLS-1$
		//$NON-NLS-1$
		menu.add(gotoMenu);
		gotoMenu.add(backAction);
		gotoMenu.add(forwardAction);
		gotoMenu.add(upAction);
		gotoMenu.add(gotoResourceAction);
	}

	/**
	 * Update the selection for new selection.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		goIntoAction.update();
	}

}