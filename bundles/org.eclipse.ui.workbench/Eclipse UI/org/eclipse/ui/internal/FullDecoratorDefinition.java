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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The RunnableDecoratorDefinition is the definition for 
 * decorators that have an ILabelDecorator class to instantiate.
 */

class FullDecoratorDefinition extends DecoratorDefinition {
	
	private ILabelDecorator decorator;

	/**
	 * Create a new instance of the receiver with the
	 * supplied values.
	 */

	FullDecoratorDefinition(
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
			initEnabled,
			element);
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
		if (labelProviderCreationFailed)
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
			this.labelProviderCreationFailed = true;
			setEnabled(false);
		}

		if (exceptions[0] != null)
			throw exceptions[0];

		return decorator;
	}
	/**
	 * Refresh the current decorator based on our enable
	 * state.
	 */

	private void refreshDecorator() throws CoreException {
		DecoratorManager manager =
			(DecoratorManager) WorkbenchPlugin
				.getDefault()
				.getDecoratorManager();

		if (this.enabled) {
			//Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				currentDecorator.addListener(manager);
		} else {
			if (decorator != null) {
				ILabelDecorator cached = decorator;
				cached.removeListener(manager);
				//Clear the decorator before disposing
				decorator = null;
				cached.dispose();
			}
		}

	}

	/**
	 * Sets the enabled flag and adds or removes the decorator
	 * manager as a listener as appropriate.
	 * @param enabled The enabled to set
	 */
	public void setEnabled(boolean newState) {

		//Only refresh if there has been a change
		if (this.enabled != newState) {
			this.enabled = newState;
			try {
				refreshDecorator();
			} catch (CoreException exception) {
				handleCoreException(exception);
			}

		}
	}
	/**
	 * Decorate the image provided for the element type.
	 * This method should not be called unless a check for
	 * isEnabled() has been done first.
	 * Return null if there is no image or if an error occurs.
	 */
	Image decorateImage(Image image, Object element) {
		try {
			//Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				return currentDecorator.decorateImage(image, element);

		} catch (CoreException exception) {
			handleCoreException(exception);
		}
		return null;
	}

	/**
	 * Decorate the text provided for the element type.
	 * This method should not be called unless a check for
	 * isEnabled() has been done first.
	 * Return null if there is no text or if there is an exception.
	 */
	String decorateText(String text, Object element) {
		try {
			//Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				return currentDecorator.decorateText(text, element);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
		return null;
	}

	/**
	 * Gets the decorator.
	 * @return Returns a ILabelDecorator
	 * @throws CoreException. This will be removed and is only
	 * here for backwards compatability.
	 */
	public ILabelDecorator getDecorator() throws CoreException {
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
		return true;
	}

}
