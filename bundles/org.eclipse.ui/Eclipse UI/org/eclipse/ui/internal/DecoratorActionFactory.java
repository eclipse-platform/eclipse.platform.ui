package org.eclipse.ui.internal;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.views.navigator.LabelDecoratorAction;
import org.eclipse.ui.views.navigator.ResourceNavigatorMessages;

/**
 * The DecoratorActionFactory builds the actions for the 
 * decorator functions.
 */

public class DecoratorActionFactory {

	LabelDecoratorAction[] actions;
	/*
	 * @see ActionFactory#makeActions()
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
	 * Fill the supplied menu with entries for the decorators.
	 */
	public void fillMenu(IMenuManager menu) {
		
		IMenuManager submenu =
			new MenuManager(ResourceNavigatorMessages.getString("DecoratorMenu.title"));
		//$NON-NLS-1$
		menu.add(submenu);
		
		for(int i = 0; i < actions.length; i ++){
			submenu.add(actions[i]);
		}
		
	}

}

