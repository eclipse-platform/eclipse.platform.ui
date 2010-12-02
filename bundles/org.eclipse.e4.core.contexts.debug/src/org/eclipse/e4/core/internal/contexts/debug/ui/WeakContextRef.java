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
package org.eclipse.e4.core.internal.contexts.debug.ui;

import java.lang.ref.WeakReference;
import org.eclipse.e4.core.internal.contexts.EclipseContext;

public class WeakContextRef extends WeakReference<EclipseContext> {

	private int hashCode;

	public WeakContextRef(EclipseContext referent) {
		super(referent);
		hashCode = referent.hashCode(); // store it in case referent is GCed
	}

	@Override
	public boolean equals(Object obj) {
		if (!WeakContextRef.class.equals(obj.getClass()))
			return false;

		EclipseContext context1 = get();
		EclipseContext context2 = ((WeakContextRef) obj).get();
		if (context1 == null || context2 == null)
			return hashCode == ((WeakContextRef) obj).hashCode;
		return context1 == context2;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
