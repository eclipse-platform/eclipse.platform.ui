package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.DecoratorDefinition;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.navigator.*;

/**
 * The LabelDecoratorAction is an action that toggles the 
 * enabled state of a decorator.
 * 
 * @since 2.0
 * @deprecated this action is no longer in use
 */
public class LabelDecoratorAction extends Action {

	private DecoratorDefinition decorator;

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
	public void run() {}

}