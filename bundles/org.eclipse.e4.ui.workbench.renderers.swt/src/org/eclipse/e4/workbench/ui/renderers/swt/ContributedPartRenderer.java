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
package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.internal.UISchedulerStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class ContributedPartRenderer extends SWTPartRenderer {

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MPart) || !(parent instanceof Composite))
			return null;

		Widget parentWidget = (Widget) parent;
		IEclipseContext parentContext = getContextForParent(element);
		Widget newWidget = null;

		final Composite newComposite = new Composite((Composite) parentWidget,
				SWT.NONE);
		newComposite.setLayout(new FillLayout());

		newWidget = newComposite;
		bindWidget(element, newWidget);
		final MPart part = (MPart) element;

		// Create a context for this part
		IEclipseContext localContext = EclipseContextFactory.create(
				parentContext, UISchedulerStrategy.getInstance());
		localContext.set(IContextConstants.DEBUG_STRING,
				"PartContext(" + element + ')'); //$NON-NLS-1$
		part.setContext(localContext);

		localContext.set(Composite.class.getName(), newComposite);
		localContext.set(MPart.class.getName(), part);

		parentContext.set(IServiceConstants.ACTIVE_CHILD, localContext);

		Object newPart = contributionFactory
				.create(part.getURI(), localContext);
		part.setObject(newPart);
		final IEclipseContext eventLclContext = localContext;
		newWidget.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (eventLclContext instanceof IDisposable)
					((IDisposable) eventLclContext).dispose();
			}
		});

		activate(element);
		return newWidget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.PartFactory#hookControllerLogic
	 * (org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void hookControllerLogic(final MUIElement me) {
		super.hookControllerLogic(me);
		Widget widget = (Widget) me.getWidget();
		if (widget instanceof Composite) {
			((Composite) widget).addListener(SWT.Activate, new Listener() {
				public void handleEvent(Event event) {
					activate(me);
				}
			});
		}
	}

}
