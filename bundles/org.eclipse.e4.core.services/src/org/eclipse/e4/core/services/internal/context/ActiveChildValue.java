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

// TBD find a better place for this
public final class ActiveChildValue implements IComputedValue {
	private final String attr;

	public ActiveChildValue(String attr) {
		this.attr = attr;
	}

	public Object compute(IEclipseContext context) {
		if (context.isSet("activeChild")) {
			IEclipseContext childContext = (IEclipseContext) context.get("activeChild");
			return childContext.get(attr);
		}
		return null;
	}
}