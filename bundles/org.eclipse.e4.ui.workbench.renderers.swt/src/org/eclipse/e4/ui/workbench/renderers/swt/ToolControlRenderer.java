/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class ToolControlRenderer extends SWTPartRenderer {

	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolControl)
				|| !(parent instanceof ToolBar || parent instanceof Composite))
			return null;
		Composite parentComp = (Composite) parent;
		MToolControl toolControl = (MToolControl) element;

		if (((Object) toolControl.getParent()) instanceof MToolBar) {
			IRendererFactory factory = context.get(IRendererFactory.class);
			AbstractPartRenderer renderer = factory.getRenderer(
					toolControl.getParent(), parent);
			if (renderer instanceof ToolBarManagerRenderer) {
				return null;
			}
		}

		Widget parentWidget = (Widget) parent;
		IEclipseContext parentContext = getContextForParent(element);

		ToolItem sep = null;
		if (parent instanceof ToolBar) {
			sep = new ToolItem((ToolBar) parentWidget, SWT.SEPARATOR);
		}

		// final Composite newComposite = new Composite((Composite)
		// parentWidget,
		// SWT.NONE);
		// newComposite.setLayout(new FillLayout());
		// bindWidget(element, newComposite);

		// Create a context just to contain the parameters for injection
		IContributionFactory contributionFactory = parentContext
				.get(IContributionFactory.class);

		IEclipseContext localContext = EclipseContextFactory.create();

		localContext.set(Composite.class.getName(), parentComp);
		localContext.set(MToolControl.class.getName(), toolControl);

		Object tcImpl = contributionFactory.create(
				toolControl.getContributionURI(), parentContext, localContext);
		toolControl.setObject(tcImpl);
		Control[] kids = parentComp.getChildren();

		// No kids means that the trim failed curing creation
		if (kids.length == 0)
			return null;

		// The new control is assumed to be the last child created
		// We could safe this up even more by asserting that the
		// number of children should go up by *one* during injection
		Control newCtrl = kids[kids.length - 1];

		if (sep != null && newCtrl != null) {
			sep.setControl(newCtrl);
			newCtrl.pack();
			sep.setWidth(newCtrl.getSize().x);
		}

		setCSSInfo(toolControl, newCtrl);

		boolean vertical = false;
		MUIElement parentElement = element.getParent();
		if (parentElement instanceof MTrimBar) {
			MTrimBar bar = (MTrimBar) parentElement;
			vertical = bar.getSide() == SideValue.LEFT
					|| bar.getSide() == SideValue.RIGHT;
		}
		CSSRenderingUtils cssUtils = parentContext.get(CSSRenderingUtils.class);
		newCtrl = cssUtils.frameMeIfPossible(newCtrl, null, vertical, true);
		return newCtrl;
	}

}
