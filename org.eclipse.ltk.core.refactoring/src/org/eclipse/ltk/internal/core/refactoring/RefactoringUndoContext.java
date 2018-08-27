/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.commands.operations.IUndoContext;

import org.eclipse.core.resources.ResourcesPlugin;

public class RefactoringUndoContext implements IUndoContext {

	@Override
	public String getLabel() {
		return RefactoringCoreMessages.RefactoringUndoContext_label;
	}

	@Override
	public boolean matches(IUndoContext context) {
		if (this == context)
			return true;
		IUndoContext workspaceContext= ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
		if (workspaceContext == null)
			return false;
		return workspaceContext.matches(context);
	}
}
