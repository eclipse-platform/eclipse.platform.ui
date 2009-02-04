/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;

/**
 * A computed value that delegates lookup to a child context.
 * TODO: There might be different bundles with their own notion of "active child",
 * so to make this more reusable the "activeChild" constant should likely be pulled
 * out and live in the UI ("activeChildControl" or some such), allowing others to store 
 * their own notion of child under a different key.
 */
public final class ActiveChildValue implements IComputedValue {
	private final String attr;

	public ActiveChildValue(String attr) {
		this.attr = attr;
	}

	public Object compute(IEclipseContext context, String[] arguments) {
		if (context.isSet("activeChild")) {
			IEclipseContext childContext = (IEclipseContext) context.get("activeChild");
			return childContext.get(attr);
		}
		return null;
	}
}