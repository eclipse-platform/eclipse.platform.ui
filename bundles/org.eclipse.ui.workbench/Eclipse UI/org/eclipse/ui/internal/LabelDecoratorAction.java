/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.decorators.*;

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
