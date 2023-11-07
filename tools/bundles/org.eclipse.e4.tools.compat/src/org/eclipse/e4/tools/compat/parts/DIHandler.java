/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource München GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonas Helming <jhelming@eclipsesource.com> - initial API and implementation
 * Lars Vogel <Lars.Vogel@gmail.com> - Bug 421453
 ******************************************************************************/
package org.eclipse.e4.tools.compat.parts;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.PlatformUI;

/**
 * This is a preliminary implementation of a Handler wrapper. It does not support @CanExecute yet
 *
 * @author Jonas
 */
public class DIHandler<C> extends AbstractHandler {

	private final C component;

	public DIHandler(Class<C> clazz) {
		final IEclipseContext context = getActiveContext();
		component = ContextInjectionFactory.make(clazz, context);
	}

	private static IEclipseContext getActiveContext() {
		final IEclipseContext parentContext = getParentContext();
		return parentContext.getActiveLeaf();
	}

	private static IEclipseContext getParentContext() {
		return PlatformUI.getWorkbench().getService(IEclipseContext.class);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return ContextInjectionFactory.invoke(component, Execute.class, getActiveContext());
	}

}