/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.ui.internal.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.help.internal.base.MissingContentManager;


public class IgnoreMissingPlaceholderHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		MissingContentManager.getInstance().ignoreAllMissingPlaceholders();
		return null;
	}

}
