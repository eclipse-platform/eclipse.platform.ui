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
package org.eclipse.ui.internal.decorators;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.WorkbenchPlugin;
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
	private int quadrant;
	private String iconLocation;

	LightweightDecoratorDefinition(
		String identifier,
		String label,
		String decoratorDescription,
		ActionExpression expression,
		boolean isAdaptable,
		boolean initEnabled,
		int quadrantValue,
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
	protected ILightweightLabelDecorator internalGetDecorator()
		throws CoreException {
		if (labelProviderCreationFailed)
			return null;

		final CoreException[] exceptions = new CoreException[1];

		if (decorator == null) {

			if (definingElement.getAttribute(WizardsRegistryReader.ATT_CLASS)
				== null)
				decorator =
					new DeclarativeDecorator(definingElement, iconLocation);
			else {

				Platform.run(new ISafeRunnable() {
					public void run() {
						try {
							decorator =
								(
									ILightweightLabelDecorator) WorkbenchPlugin
										.createExtension(
									definingElement,
									WizardsRegistryReader.ATT_CLASS);
							decorator.addListener(
								WorkbenchPlugin
									.getDefault()
									.getDecoratorManager());
						} catch (CoreException exception) {
							exceptions[0] = exception;
						}
					}

					/* (non-Javadoc)
					 * Method declared on ISafeRunnable.
					 */
					public void handleException(Throwable e) {
						//Do nothing as Core will handle the logging
					}
				});
			}
		} else
			return decorator;

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

	/**
	 * Returns the quadrant.One of the following constants in
	 * DecoratorRegistryReader:
	 * 	TOP_LEFT 
	 *  TOP_RIGHT
	 *  BOTTOM_LEFT
	 *  BOTTOM_RIGHT
	 *  UNDERLAY
	 * @return int
	 */
	public int getQuadrant() {
		return quadrant;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#getOverlay(java.lang.Object)
	 */
	public void decorate(Object element, IDecoration decoration) {
		try {
			//Internal decorator might be null so be prepared
			ILightweightLabelDecorator currentDecorator =
				internalGetDecorator();
			if (currentDecorator != null)
				currentDecorator.decorate(element, decoration);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}

	}

	/**
	 * Returns the lightweight decorator, or <code>null</code> if not enabled.
	 * 
	 * @return the lightweight decorator, or <code>null</code> if not enabled
	 */
	public ILightweightLabelDecorator getDecorator() {
		return decorator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#refreshDecorator()
	 */

	protected void refreshDecorator() throws CoreException {
		//Only do something if disabled so as to prevent
		//gratutitous activation
		if (!this.enabled && decorator != null) {
			IBaseLabelProvider cached = decorator;
			decorator = null;
			disposeCachedDecorator(cached);
		}
	}
}
