package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.internal.framelist.FrameList;

/**
 * Factory to create actions for the resource navigator
 * 
 * @since 2.0
 * 
 * @deprecated use ResourceNavigatorActionGroup
 */
public class ResourceNavigatorActionFactory extends ActionFactory {

	//The action factories
	protected GotoActionFactory gotoFactory;
	protected OpenActionFactory openActionFactory;
	protected RefactorActionFactory refactorFactory;
	protected WorkbenchStateActionFactory workbenchFactory;
	protected SortAndFilterActionFactory sortAndFilterMenuFactory;

	protected IResourceNavigatorPart part;

	/**
	 * Create a new instance of the receiver with the required 
	 * parameters.
	 * 
	 * @deprecated
	 */
	public ResourceNavigatorActionFactory(
		FrameList frameList,
		Shell shell,
		Clipboard clipboard,
		IResourceTreeNavigatorPart navigatorPart) {

		gotoFactory = new GotoActionFactory(frameList, navigatorPart);
		openActionFactory =
			new OpenActionFactory(navigatorPart.getSite(), shell);
		refactorFactory =
			new RefactorActionFactory(
				navigatorPart.getTreeViewer(), 
				navigatorPart.getViewSite(),
				clipboard);
		workbenchFactory =
			new WorkbenchStateActionFactory(
				navigatorPart.getResourceViewer().getControl());
		sortAndFilterMenuFactory = new SortAndFilterActionFactory(navigatorPart);
		this.part = navigatorPart;

	}

	/*
	 * @see ActionFactory#makeActions()
	 */
	public void makeActions() {

		gotoFactory.makeActions();
		openActionFactory.makeActions();
		refactorFactory.makeActions();
		workbenchFactory.makeActions();
		sortAndFilterMenuFactory.makeActions();
	}

	/**
	 * Updates the checked state of the sort actions.
	 */
	public void updateSortActions() {
		sortAndFilterMenuFactory.updateSortActions();
	}

	/**
	 * Updates the global actions with the given selection.
	 * Be sure to invoke after actions objects have updated, since can* methods delegate to action objects.
	 */
	public void updateGlobalActions(IStructuredSelection selection) {

		IActionBars actionBars = part.getViewSite().getActionBars();
		refactorFactory.updateGlobalActions(selection, actionBars);
		workbenchFactory.updateGlobalActions(selection);
	}

	/**
	 * Contributes actions to the local tool bar and local pulldown menu.
	 * @since 2.0
	 */
	public void fillActionBars(IStructuredSelection selection) {
		IActionBars actionBars = part.getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		gotoFactory.fillToolBar(toolBar);
		actionBars.updateActionBars();
		sortAndFilterMenuFactory.fillActionBarMenu(actionBars.getMenuManager(), selection);
	}

	/*
	 * @see ActionFactory#fillPopUpMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillPopUpMenu(IMenuManager menu, IStructuredSelection selection) {

		gotoFactory.fillPopUpMenu(menu, selection);
		openActionFactory.fillPopUpMenu(menu, selection);
		menu.add(new Separator());
		refactorFactory.fillPopUpMenu(menu, selection);
		menu.add(new Separator());
		workbenchFactory.fillPopUpMenu(menu, selection);
	}
	
	/**
	 * Update the selection for new selection.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		//Update the selections of those who need a refresh before filling
		openActionFactory.selectionChanged(selection);
		gotoFactory.selectionChanged(selection);
	}
	
	/**
	 * Handles double clicks in viewer.
	 */
	public void handleDoubleClick(IStructuredSelection selection) {
		openActionFactory.handleDoubleClick(selection);

	}
	
	/**
	 * Handles key release in viewer.
	 */
	public void handleKeyReleased(KeyEvent event) {
		refactorFactory.handleKeyReleased(event);
		workbenchFactory.handleKeyReleased(event);
	}

}