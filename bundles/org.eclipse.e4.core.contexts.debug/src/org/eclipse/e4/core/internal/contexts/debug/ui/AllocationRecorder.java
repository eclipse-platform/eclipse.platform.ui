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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;

public class AllocationRecorder {

	static private AllocationRecorder defaultRecorder;

	private Map<EclipseContext, Throwable> traces = Collections.synchronizedMap(new WeakHashMap<EclipseContext, Throwable>());

	static public AllocationRecorder getDefault() {
		if (defaultRecorder == null)
			defaultRecorder = new AllocationRecorder();
		return defaultRecorder;
	}

	public AllocationRecorder() {
		// placeholder
	}

	public void allocated(EclipseContext context, Throwable exception) {
		traces.put(context, exception);
	}

	public void disposed(IEclipseContext context) {
		traces.remove(context);
	}

	public Throwable getTrace(IEclipseContext context) {
		return traces.get(context);
	}

	public Set<EclipseContext> getContexts() {
		return traces.keySet();
	}
}
