package org.eclipse.ui.views.navigator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.*;

public class DecoratorActionFactory extends ActionFactory {

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
	
	/*
	 * @see ActionFactor.fillActionBarMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillActionBarMenu(IMenuManager menu, IStructuredSelection selection) {
		
		IMenuManager submenu =
			new MenuManager(ResourceNavigatorMessages.getString("DecoratorMenu.title"));
		//$NON-NLS-1$
		menu.add(submenu);
		
		for(int i = 0; i < actions.length; i ++){
			submenu.add(actions[i]);
		}
		
	}

}

