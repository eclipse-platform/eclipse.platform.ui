/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.Set;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.internal.services.ContextContextFunction;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 * @since 1.0
 */
public final class ContextServiceAddon {
	@PostConstruct
	public void init(IEclipseContext context) {
		// global context service.
		ContextManager manager = context.get(ContextManager.class);
		if (manager == null) {
			manager = new ContextManager();
			context.set(ContextManager.class, manager);
		}

		context.set(EContextService.class.getName(), new ContextContextFunction());
		context.set(IServiceConstants.ACTIVE_CONTEXTS, new ActiveContextsFunction());
		context.runAndTrack(new RunAndTrack() {
			@Override
			public boolean changed(IEclipseContext context) {
				ContextManager manager = context.get(ContextManager.class);
				if (manager != null) {
					Object s = context.get(IServiceConstants.ACTIVE_CONTEXTS);
					if (s instanceof Set) {
						manager.setActiveContextIds((Set<?>) s);
					} else {
						manager.setActiveContextIds(Collections.EMPTY_SET);
					}
				}
				return true;
			}
		});
	}
}
