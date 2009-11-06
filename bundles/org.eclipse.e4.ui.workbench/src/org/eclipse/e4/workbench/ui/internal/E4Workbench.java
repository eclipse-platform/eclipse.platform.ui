/*******************************************************************************
 * Copyright (c) 2008, 2009 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.core.commands.Category;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;

public class E4Workbench {
	public static final String LOCAL_ACTIVE_SHELL = "localActiveShell"; //$NON-NLS-1$
	public static final String XMI_URI_ARG = "applicationXMI"; //$NON-NLS-1$
	public static final String CSS_URI_ARG = "applicationCSS"; //$NON-NLS-1$
	public static final String CSS_RESOURCE_URI_ARG = "applicationCSSResources"; //$NON-NLS-1$
	public static final String PRESENTATION_URI_ARG = "presentationURI"; //$NON-NLS-1$

	IEclipseContext appContext;

	public IEclipseContext getContext() {
		return appContext;
	}

	public E4Workbench(MApplicationElement uiRoot, IEclipseContext applicationContext) {
		appContext = applicationContext;

		if (uiRoot instanceof MApplication) {
			init((MApplication) uiRoot);
		}

		// Hook the global notifications
		((Notifier) uiRoot).eAdapters().add(new UIEventPublisher(appContext));

		// Create and run the UI (if any)
		String presentationURI = (String) appContext.get(PRESENTATION_URI_ARG);

		// HACK!!
		presentationURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt/" //$NON-NLS-1$
				+ "org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine"; //$NON-NLS-1$
		appContext.set(PRESENTATION_URI_ARG, presentationURI);
		if (presentationURI != null) {
			createAndRunUI(uiRoot, appContext);
		}
	}

	/**
	 * @param renderingEngineURI
	 * @param cssURI
	 * @param cssResourcesURI
	 */
	private void createAndRunUI(MApplicationElement uiRoot, IEclipseContext appContext) {
		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		if (renderer == null) {
			String presentationURI = (String) appContext.get(PRESENTATION_URI_ARG);
			if (presentationURI != null) {
				IContributionFactory factory = (IContributionFactory) appContext
						.get(IContributionFactory.class.getName());
				renderer = (IPresentationEngine) factory.create(presentationURI, appContext);
				appContext.set(IPresentationEngine.class.getName(), renderer);
			}
		}

		if (renderer != null) {
			renderer.run(uiRoot, appContext);
		}
	}

	private void init(MApplication appElement) {
		Activator.trace(Policy.DEBUG_WORKBENCH, "init() workbench", null); //$NON-NLS-1$

		// fill in commands
		Activator.trace(Policy.DEBUG_CMDS, "Initialize service from model", null); //$NON-NLS-1$
		ECommandService cs = (ECommandService) appContext.get(ECommandService.class.getName());
		Category cat = cs
				.defineCategory(MApplication.class.getName(), "Application Category", null); //$NON-NLS-1$
		EList<MCommand> commands = appElement.getCommands();
		for (MCommand cmd : commands) {
			String id = cmd.getId();
			String name = cmd.getCommandName();
			cs.defineCommand(id, name, null, cat, null);
		}

		// Do a top level processHierarchy for the application?
		Workbench.processHierarchy(appElement);
	}

	/**
	 * @return
	 */
	public Object getReturnValue() {
		return 0;
	}
}
