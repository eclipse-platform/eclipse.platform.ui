/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.tools.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.services.internal.context.EclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Generates a snapshot of the current main application context state
 */
public class GenerateSnapshotHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MApplication app = (MApplication) HandlerUtil.getVariableChecked(event,
				MApplication.class.getName());
		((EclipseContext) app.getContext()).debugSnap();
		return null;
	}

}
