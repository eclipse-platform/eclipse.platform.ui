/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.modeling.IModelExtension;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * @since 3.5
 * 
 */
public class DefaultPerspectiveProcessor implements IModelExtension {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.IModelExtension#processElement(org.
	 * eclipse.emf.ecore.EObject)
	 */
	public void processElement(EObject parent) {
		MApplication app = (MApplication) parent;
		final MWindow win = app.getChildren().get(0);
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(WorkbenchPlugin
				.getDefault().getBundleContext());
		final IEventBroker broker = (IEventBroker) serviceContext.get(IEventBroker.class.getName());
		broker.subscribe(UIEvents.buildTopic(UIEvents.Context.TOPIC, UIEvents.Context.CONTEXT),
				new EventHandler() {
					public void handleEvent(Event event) {
						Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
						if (element == win) {
							MContext window = (MContext) element;
							if (window.getContext() != null) {
								broker.unsubscribe(this);
								SetPerspective runnable = new SetPerspective(window.getContext());
								window.getContext().runAndTrack(runnable, null);
							}
						}
					}
				});
	}

	static class SetPerspective implements IRunAndTrack {
		final private IEclipseContext context;

		public SetPerspective(IEclipseContext c) {
			context = c;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.core.services.context.IRunAndTrack#notify(org.eclipse
		 * .e4.core.services.context.ContextChangeEvent)
		 */
		public boolean notify(ContextChangeEvent event) {
			if (event.getEventType() == ContextChangeEvent.DISPOSE)
				return false;
			IWorkbench wb = (IWorkbench) context.get(IWorkbench.class.getName());
			IWorkbenchPage page = (IWorkbenchPage) context.get(IWorkbenchPage.class.getName());
			if (wb == null || page == null) {
				return true;
			}
			IPerspectiveRegistry perspectiveRegistry = wb.getPerspectiveRegistry();
			page.setPerspective(perspectiveRegistry.findPerspectiveWithId(perspectiveRegistry
					.getDefaultPerspective()));
			return false;
		}

	}
}
