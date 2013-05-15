/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.services;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class ActiveContextsFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		// 1) get active child
		IEclipseContext current = context.getActiveLeaf();
		//2 form an answer going up
		boolean inDialog = false;
		Set<String> rc = new HashSet<String>();
		while (current != null) {
			LinkedList<String> locals = (LinkedList<String>) current.getLocal(ContextContextService.LOCAL_CONTEXTS);
			if (locals != null) {
				if (!inDialog || !locals.contains("org.eclipse.ui.contexts.window")) {
					rc.addAll(locals);
				}
				if (!inDialog && locals.contains("org.eclipse.ui.contexts.dialog")) {
					inDialog = true;
				}
			}
			current = current.getParent();
		}
		return rc;
	}

}
