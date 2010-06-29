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
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class ToolControlRenderer extends SWTPartRenderer {

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolControl)
				|| !(parent instanceof ToolBar || parent instanceof Composite))
			return null;

		MToolControl toolControl = (MToolControl) element;
		Widget parentWidget = (Widget) parent;
		IEclipseContext parentContext = getContextForParent(element);

		ToolItem sep = null;
		if (parent instanceof ToolBar) {
			sep = new ToolItem((ToolBar) parentWidget, SWT.SEPARATOR);
		}

		final Composite newComposite = new Composite((Composite) parentWidget,
				SWT.NONE);
		newComposite.setLayout(new FillLayout());

		// Create a context just to contain the parameters for injection
		IContributionFactory contributionFactory = parentContext
				.get(IContributionFactory.class);

		IEclipseContext localContext = EclipseContextFactory.create();

		localContext.set(Composite.class.getName(), newComposite);
		localContext.set(MToolControl.class.getName(), toolControl);

		Object tcImpl = contributionFactory.create(
				toolControl.getContributionURI(), parentContext, localContext);
		toolControl.setObject(tcImpl);

		if (sep != null) {
			sep.setControl(newComposite);
			newComposite.pack();
			sep.setWidth(newComposite.getSize().x);
		}

		return newComposite;
	}

}
