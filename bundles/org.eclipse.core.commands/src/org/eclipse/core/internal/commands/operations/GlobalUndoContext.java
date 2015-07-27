/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.commands.operations;

import org.eclipse.core.commands.operations.IUndoContext;

/**
 * <p>
 * An operation context that matches to any context.  It can be used to
 * get an unfiltered (global) history.
 * </p>
 *
 * @since 3.1
 */
public class GlobalUndoContext implements IUndoContext {

	@Override
	public String getLabel() {
		return "Global Undo Context"; //$NON-NLS-1$
	}

	@Override
	public boolean matches(IUndoContext context) {
		return true;
	}
}
