package org.eclipse.ui.internal;

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The RunnableDecoratorDefinition is the definition for 
 * decorators that have an ILabelDecorator class to instantiate. */

class RunnableDecoratorDefinition extends DecoratorDefinition {

	private IConfigurationElement definingElement;
	//A flag that is set if there is an error creating the decorator
	private boolean decoratorCreationFailed = false;

	/**
	 * Create a new instance of the receiver with the
	 * supplied values.
	 */

	RunnableDecoratorDefinition(
		String identifier,
		String label,
		String decoratorDescription,
		ActionExpression expression,
		boolean isAdaptable,
		boolean initEnabled,
		IConfigurationElement element) {
		super(
			identifier,
			label,
			decoratorDescription,
			expression,
			isAdaptable,
			initEnabled);
		this.definingElement = element;
	}
	/**
	 * Gets the decorator and creates it if it does
	 * not exist yet. Throws a CoreException if there is a problem
	 * creating the decorator.
	 * This method should not be called unless a check for
	 * enabled to be true is done first.
	 * @return Returns a ILabelDecorator
	 */
	protected ILabelDecorator internalGetDecorator() throws CoreException {
		if (decoratorCreationFailed)
			return null;

		final CoreException[] exceptions = new CoreException[1];

		if (decorator == null) {
			Platform.run(new SafeRunnable(WorkbenchMessages.format("DecoratorManager.ErrorActivatingDecorator", new String[] { getName()})) { //$NON-NLS-1$
				public void run() {
					try {
						decorator =
							(ILabelDecorator) WorkbenchPlugin.createExtension(
								definingElement,
								WizardsRegistryReader.ATT_CLASS);
					} catch (CoreException exception) {
						exceptions[0] = exception;
					}
				}
			});
		}

		if (decorator == null) {
			this.decoratorCreationFailed = true;
			setEnabled(false);
		}

		if (exceptions[0] != null)
			throw exceptions[0];

		return decorator;
	}

}
