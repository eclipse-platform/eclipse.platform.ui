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

import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.internal.UIContextScheduler;
import org.eclipse.e4.workbench.ui.renderers.PartHandlerService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class ContributedPartFactory extends SWTPartFactory {

	public Object createWidget(final MPart<?> part) {
		Widget parentWidget = getParentWidget(part);
		IEclipseContext parentContext = getContextForParent(part);
		Widget newWidget = null;

		if (part instanceof MContributedPart<?>) {
			final Composite newComposite = new Composite(
					(Composite) parentWidget, SWT.NONE);
			newWidget = newComposite;
			bindWidget(part, newWidget);
			final MContributedPart<?> contributedPart = (MContributedPart<?>) part;
			final IHandlerService hs = new PartHandlerService(part);
			final IEclipseContext localContext = EclipseContextFactory.create(
					parentContext, UIContextScheduler.instance);
			localContext.set(IContextConstants.DEBUG_STRING, "ContributedPart"); //$NON-NLS-1$
			final IEclipseContext outputContext = EclipseContextFactory.create(
					null, UIContextScheduler.instance);
			outputContext.set(IContextConstants.DEBUG_STRING,
					"ContributedPart-output"); //$NON-NLS-1$
			contributedPart.setContext(localContext);
			localContext.set(Composite.class.getName(), newComposite);
			localContext.set(IHandlerService.class.getName(), hs);
			localContext.set(IServiceConstants.OUTPUTS, outputContext);
			localContext.set(IEclipseContext.class.getName(), outputContext);
			localContext.set(MContributedPart.class.getName(), contributedPart);
			localContext.set(IServiceConstants.PERSISTED_STATE, contributedPart
					.getPersistedState());
			outputContext.runAndTrack(new Runnable() {
				public void run() {
					Object state = outputContext
							.get(IServiceConstants.PERSISTED_STATE);
					if (state != null) {
						contributedPart.setPersistedState((String) state);
					}
				}
			}, ""); //$NON-NLS-1$
			parentContext.set(IServiceConstants.ACTIVE_CHILD, localContext);
			Object newPart = contributionFactory.create(contributedPart
					.getURI(), localContext);
			contributedPart.setObject(newPart);
		}

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
	public void hookControllerLogic(final MPart<?> me) {
		// TODO Auto-generated method stub
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
