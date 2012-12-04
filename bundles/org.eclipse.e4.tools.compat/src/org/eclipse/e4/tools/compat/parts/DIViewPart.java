/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.compat.parts;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.tools.compat.internal.PartHelper;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public abstract class DIViewPart<C> extends ViewPart {
	private IEclipseContext context;
	private Class<C> clazz;
	private C component;

	public DIViewPart(Class<C> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		context = PartHelper.createPartContext(this);

		context.declareModifiable(IViewPart.class);

		context.set(IViewPart.class, this);
	}

	@Override
	public void createPartControl(Composite parent) {
		component = PartHelper.createComponent(parent, context, clazz, this);
	}

	protected IEclipseContext getContext() {
		return context;
	}

	public C getComponent() {
		return component;
	}

	@Override
	public void setFocus() {
		ContextInjectionFactory.invoke(component, Focus.class, context);
	}
}