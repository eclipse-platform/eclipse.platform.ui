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

package org.eclipse.e4.core.services;

public final class ActiveChildValue extends ComputedValue {
	private final String attr;

	public ActiveChildValue(String attr) {
		this.attr = attr;
	}

	protected Object compute(Context context) {
		if (context.isSet("activeChild")) {
			Context childContext = (Context) context.get("activeChild");
			return childContext.get(attr);
		}
		return null;
	}
}