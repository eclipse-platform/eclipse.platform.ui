package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.DecoratorDefinition;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The LabelDecoratorAction is an action that toggles the 
 * enabled state of a decorator.
 */

public class LabelDecoratorAction extends Action {

	DecoratorDefinition decorator;

	/**
	 * Constructor for LabelDecoratorAction.
	 * @param text
	 */
	public LabelDecoratorAction(DecoratorDefinition definition) {
		super(definition.getName());
		decorator = definition;
		setChecked(decorator.isEnabled());
	}

	/*
	 * see @Action.run()
	*/
	public void run() {
		//Toggle the enabled state of the decorator and then update the manager
		boolean enabledState = decorator.isEnabled();
		decorator.setEnabled(!enabledState);
		setChecked(!enabledState);
		WorkbenchPlugin.getDefault().getDecoratorManager().reset();
	}

}