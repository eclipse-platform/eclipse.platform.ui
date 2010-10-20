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
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class ContributedPartRenderer extends SWTPartRenderer {
	private MUIElement partToActivate;

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MPart) || !(parent instanceof Composite))
			return null;

		Widget parentWidget = (Widget) parent;
		Widget newWidget = null;

		final Composite newComposite = new Composite((Composite) parentWidget,
				SWT.NONE);
		newComposite.setLayout(new FillLayout(SWT.VERTICAL));

		newWidget = newComposite;
		bindWidget(element, newWidget);
		final MPart part = (MPart) element;

		// Create a context for this part
		IEclipseContext localContext = part.getContext();
		localContext.set(Composite.class.getName(), newComposite);

		IContributionFactory contributionFactory = (IContributionFactory) localContext
				.get(IContributionFactory.class.getName());
		Object newPart = contributionFactory.create(part.getContributionURI(),
				localContext);
		part.setObject(newPart);

		return newWidget;
	}

	public static void setDescription(MPart part, String description) {
		if (!(part.getWidget() instanceof Composite))
			return;

		Composite c = (Composite) part.getWidget();

		// Do we already have a label?
		if (c.getChildren().length == 2) {
			Label label = (Label) c.getChildren()[0];
			if (description == null)
				description = ""; //$NON-NLS-1$
			label.setText(description);
			label.setToolTipText(description);
			c.layout();
		} else if (c.getChildren().length == 1) {
			c.setLayout(new Layout() {

				@Override
				protected Point computeSize(Composite composite, int wHint,
						int hHint, boolean flushCache) {
					return new Point(0, 0);
				}

				@Override
				protected void layout(Composite composite, boolean flushCache) {
					Rectangle bounds = composite.getBounds();
					if (composite.getChildren().length == 1) {
						composite.getChildren()[0].setBounds(composite
								.getBounds());
					} else if (composite.getChildren().length == 2) {
						Label label = (Label) composite.getChildren()[0];
						Control partCtrl = composite.getChildren()[1];

						int labelHeight = label.computeSize(bounds.width,
								SWT.DEFAULT).y;
						label.setBounds(0, 0, bounds.width, labelHeight);

						partCtrl.setBounds(0, labelHeight, bounds.width,
								bounds.height - labelHeight);
					}
				}
			});

			Control partCtrl = c.getChildren()[0];
			Label label = new Label(c, SWT.NONE);
			label.setText(description);
			label.setToolTipText(description);
			label.moveAbove(partCtrl);
			c.layout();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#requiresFocus
	 * (org.eclipse.e4.ui.model.application.ui.basic.MPart)
	 */
	@Override
	protected boolean requiresFocus(MPart element) {
		if (element == partToActivate) {
			return true;
		}
		return super.requiresFocus(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.PartFactory#hookControllerLogic
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
					try {
						partToActivate = me;
						activate((MPart) me);
					} finally {
						partToActivate = null;
					}
				}
			});
		}

	}
}
