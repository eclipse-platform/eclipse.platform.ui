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
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The DeclarativeDecoratorDefinition is a decorator 
 * definition that is defined entirely from xml and
 * will not require the activation of its defining 
 * plug-in.
 */
class LightweightDecoratorDefinition extends DecoratorDefinition {

	/**
	 * The DeclarativeDecorator is the internal decorator
	 * supplied by the decorator definition.
	 */
	private ILightweightLabelDecorator decorator;
	private String quadrant;
	private String iconLocation;
	private String decoratorClass;

	LightweightDecoratorDefinition(
		String identifier,
		String label,
		String decoratorDescription,
		ActionExpression expression,
		boolean isAdaptable,
		boolean initEnabled,
		String quadrantValue,
		String iconPath,
		IConfigurationElement element) {
		super(
			identifier,
			label,
			decoratorDescription,
			expression,
			isAdaptable,
			initEnabled,
			element);
		this.iconLocation = iconPath;
		this.quadrant = quadrantValue;
	}
	
	/**
	 * Gets the decorator and creates it if it does
	 * not exist yet. Throws a CoreException if there is a problem
	 * creating the decorator.
	 * This method should not be called unless a check for
	 * enabled to be true is done first.
	 * @return Returns a ILabelDecorator
	 */
	protected ILightweightLabelDecorator internalGetDecorator() throws CoreException {
		if (labelProviderCreationFailed)
			return null;

		final CoreException[] exceptions = new CoreException[1];

		if (decorator == null) {
			Platform.run(new SafeRunnable(WorkbenchMessages.format("DecoratorManager.ErrorActivatingDecorator", new String[] { getName()})) { //$NON-NLS-1$
				public void run() {
					try {
						decorator =
							(ILightweightLabelDecorator) WorkbenchPlugin.createExtension(
								definingElement,
								DecoratorRegistryReader.ATT_DECORATOR_CLASS);
					} catch (CoreException exception) {
						exceptions[0] = exception;
					}
				}
			});
		}

		if (decorator == null) {
			this.labelProviderCreationFailed = true;
			setEnabled(false);
		}

		if (exceptions[0] != null)
			throw exceptions[0];

		return decorator;
	}

	/**
	 * @see org.eclipse.ui.internal.DecoratorDefinition#internalGetLabelProvider()
	 */
	protected IBaseLabelProvider internalGetLabelProvider()
		throws CoreException {
		return internalGetDecorator();
	}

	/**
	 * @see org.eclipse.ui.internal.DecoratorDefinition#isFull()
	 */
	public boolean isFull() {
		return false;
	}

}
