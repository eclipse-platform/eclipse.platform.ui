/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.swt.SWT;
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
		IEclipseContext localContext = E4Workbench.initializeContext(
				parentContext, part);
		E4Workbench.processHierarchy(part);

		for (String variables : part.getVariables()) {
			int delimiterIndex = variables.indexOf("::"); //$NON-NLS-1$
			if (delimiterIndex == -1) {
				continue;
			}

			String key = variables.substring(0, delimiterIndex);
			String value = variables.substring(delimiterIndex + 2);

			if (!value.startsWith("platform:/")) { //$NON-NLS-1$
				localContext.set(key, value);
			}
		}

		IContributionFactory contributionFactory = (IContributionFactory) localContext
				.get(IContributionFactory.class.getName());

		for (String variables : part.getVariables()) {
			int delimiterIndex = variables.indexOf("::"); //$NON-NLS-1$
			if (delimiterIndex == -1) {
				continue;
			}

			String key = variables.substring(0, delimiterIndex);
			String value = variables.substring(delimiterIndex + 2);

			if (value.startsWith("platform:/")) { //$NON-NLS-1$
				Object result = contributionFactory.create(value, localContext);
				localContext.set(key, result);
			}
		}

		localContext.set(Composite.class.getName(), newComposite);
		localContext.set(MPart.class.getName(), part);

		Object newPart = contributionFactory
				.create(part.getURI(), localContext);
		part.setObject(newPart);

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
		if (!(me instanceof MPart)) {
			return;
		}
		Widget widget = (Widget) me.getWidget();
		if (widget instanceof Composite) {
			((Composite) widget).addListener(SWT.Activate, new Listener() {
				public void handleEvent(Event event) {
					activate((MPart) me);
				}
			});
		}
	}

}
