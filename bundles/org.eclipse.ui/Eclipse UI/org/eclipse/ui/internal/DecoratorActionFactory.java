package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.internal.LabelDecoratorAction;
import org.eclipse.ui.views.navigator.ResourceNavigatorMessages;

/**
 * The DecoratorActionFactory builds the actions for the 
 * decorator functions.
 * 
 * @since 2.0
 */
public class DecoratorActionFactory {

	LabelDecoratorAction[] actions;
	
	/**
	 * Makes the actions for the registered decorators.
	 */
	public void makeActions() {
		//Make an action for each decorator definition
		DecoratorDefinition[] definitions =
			WorkbenchPlugin.getDefault().getDecoratorManager().getDecoratorDefinitions();
		actions = new LabelDecoratorAction[definitions.length];
		for(int i = 0; i < definitions.length; i++){
			DecoratorDefinition definition = definitions[i];
			actions[i] =
				new LabelDecoratorAction(definition);
		}
		
	}
	
	/**
	 * Adds a submenu for decorators to the supplied menu.
	 */
	public void fillMenu(IMenuManager menu) {
		
		// don't contribute the menu if there are no decorators.
		if (actions.length == 0) {
			return;
		}
		
		String title = ResourceNavigatorMessages.getString("DecoratorMenu.title"); //$NON-NLS-1$
		IMenuManager submenu =	new MenuManager(title);
		menu.add(submenu);
		
		for (int i = 0; i < actions.length; i++){
			submenu.add(actions[i]);
		}
		
	}

}

