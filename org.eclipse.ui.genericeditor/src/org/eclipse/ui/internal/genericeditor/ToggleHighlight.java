/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.handlers.HandlerUtil;

public class ToggleHighlight extends AbstractHandler {

	public static final String TOGGLE_HIGHLIGHT_PREFERENCE = "org.eclipse.ui.genericeditor.togglehighlight"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean oldValue = HandlerUtil.toggleCommandState(command);

		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GenericEditorPlugin.BUNDLE_ID);
		preferences.putBoolean(TOGGLE_HIGHLIGHT_PREFERENCE, !oldValue);
		return null;
	}
}
