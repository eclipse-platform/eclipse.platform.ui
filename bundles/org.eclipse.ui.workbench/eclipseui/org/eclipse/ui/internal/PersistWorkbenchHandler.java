/*******************************************************************************
 * Copyright (c) 2025, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PlatformUI;

public class PersistWorkbenchHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		if (PlatformUI.getWorkbench() instanceof Workbench workbench) {
			workbench.persist(false);
			return IStatus.OK;
		}
		return IStatus.ERROR;
	}

}
