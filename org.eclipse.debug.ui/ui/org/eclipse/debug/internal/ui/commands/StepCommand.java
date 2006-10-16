/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IStep;

/**
 * Common function for step commands.
 * 
 * @since 3.3
 */
public abstract class StepCommand extends DebugCommand {

	protected Object getTarget(Object element) {
		if (element instanceof IStep) {
			return element;
		} else if (element instanceof IAdaptable) {
			return ((IAdaptable) element).getAdapter(IStep.class);
		}
		return null;
	}
}
