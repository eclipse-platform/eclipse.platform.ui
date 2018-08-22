/*******************************************************************************
 * Copyright (c) 2018 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.core.internal.contexts.IEclipseContextDebugger;
import org.osgi.service.component.annotations.Component;

/**
 * A simple listener to the context changes, tracing the data to the eclipse log
 * (if trancing is enabled).
 *
 * Changes from anonymous contexts are not traced.
 */
@Component
public class ContextListener implements IEclipseContextDebugger {

	@Override
	public void notify(EclipseContext context, EventType type, Object data) {
		if (!Policy.DEBUG_CONTEXTS) {
			return;
		}
		String msg = context.toString();
		if (EclipseContext.ANONYMOUS_CONTEXT_NAME.equals(msg)) {
			// Don't bother with contexts without names. Unfortunately all contexts are
			// anonymous at the creation time, but this event isn't very interesting either
			return;
		}
		msg = type + " : " + describe(context);
		WorkbenchSWTActivator.trace(Policy.DEBUG_CONTEXTS_FLAG, msg, null);
	}

	private String describe(IEclipseContext context) {
		StringBuilder sb = new StringBuilder(); // $NON-NLS-1$
		IEclipseContext activeContext = context;
		IEclipseContext parent = context.getParent();
		while (parent != null) {
			sb.append(activeContext).append(" <- "); //$NON-NLS-1$
			activeContext = parent;
			parent = parent.getParent();
		}
		sb.append(activeContext);
		return sb.toString();
	}

}
