package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.views.internal.framelist.*;

/**
 * The GotoActionContributionFactory is the class
 * that adds the goto actions for a menu.
 */

public class GotoActionContributionFactory extends ActionContributionFactory {

	protected FrameList frameList;
	protected IResourceNavigatorPart resourceNavigator;
	protected BackAction backAction;
	protected ForwardAction forwardAction;
	protected GoIntoAction goIntoAction;
	protected UpAction upAction;
	protected GotoResourceAction gotoResourceAction;

	public GotoActionContributionFactory(
		FrameList list,
		IResourceNavigatorPart navigator) {
		frameList = list;
		resourceNavigator = navigator;
	}

	/*
	 * @see ActionContributionFactory#updateActions(IStructuredSelection)
	 */
	public void updateActions(IStructuredSelection selection) {
		//Do nothing by default here
	}

	/*
	 * @see ActionContributionFactory#makeActions()
	 */
	public void makeActions() {

		goIntoAction = new GoIntoAction(frameList);
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);
		gotoResourceAction =
			new GotoResourceAction(
				resourceNavigator,
				ResourceNavigatorMessages.getString("ResourceNavigator.resourceText"));
		//$NON-NLS-1$
	}

	/*
	* @see ActionContributionFactory#fillToolBar(IToolBarManager)
	*/
	public void fillToolBar(IToolBarManager toolBar) {
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
	}

	/*
	 * @see ActionContributionFactory#fillMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillMenu(IMenuManager menu, IStructuredSelection selection) {

		if (selection.size() == 1
			&& SelectionUtil.allResourcesAreOfType(
				selection,
				IResource.PROJECT | IResource.FOLDER)) {
			menu.add(goIntoAction);
		}
		MenuManager gotoMenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.goto"));
		//$NON-NLS-1$
		menu.add(gotoMenu);
		gotoMenu.add(backAction);
		gotoMenu.add(forwardAction);
		gotoMenu.add(upAction);
		gotoMenu.add(gotoResourceAction);
	}

	/*
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		goIntoAction.update();
	}

}