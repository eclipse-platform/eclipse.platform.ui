/*******************************************************************************
 * Copyright (c) 2012 EclipseSource M�nchen GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonas Helming <jhelming@eclipsesource.com> - initial API and implementation
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
 * @author Jonas
 *
 * @param <C>
 */
public class DIHandler<C> extends AbstractHandler {

	private Class<C> clazz;
	private C component;

	public DIHandler(Class<C> clazz) {
		this.clazz = clazz;
		IEclipseContext context = getActiveContext();
		component = ContextInjectionFactory.make(clazz, context);
	}

	private static IEclipseContext getActiveContext() {
		IEclipseContext parentContext = getParentContext();
		return parentContext.getActiveLeaf();
	}

	private static IEclipseContext getParentContext() {
		return (IEclipseContext) PlatformUI.getWorkbench().getService(
				IEclipseContext.class);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return ContextInjectionFactory.invoke(component, Execute.class,
				getActiveContext());
	}

}