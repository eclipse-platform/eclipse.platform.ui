/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;

/**
 * The FullDecoratorRunnable is the ISafeRunnable that runs the full decorators
 */
abstract class FullDecoratorRunnable implements ISafeRunnable {
	protected Object element;

	protected FullDecoratorDefinition decorator;

	/**
	 * Set the values for the element and the decorator.
	 * 
	 * @param object
	 * @param definition
	 */
	protected void setValues(Object object, FullDecoratorDefinition definition) {
		element = object;
		decorator = definition;

	}

	/*
	 * @see ISafeRunnable.handleException(Throwable).
	 */
	@Override
	public void handleException(Throwable exception) {
		IStatus status = StatusUtil.newStatus(IStatus.ERROR, exception.getMessage(), exception);
		String message = NLS.bind(WorkbenchMessages.DecoratorWillBeDisabled, decorator.getName());
		WorkbenchPlugin.log(message, status);
		decorator.crashDisable();
	}

}
