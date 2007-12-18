/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.expressions.ActivePartExpression;
import org.eclipse.ui.internal.expressions.WorkbenchWindowExpression;
import org.eclipse.ui.internal.services.ISourceProviderService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.4
 * 
 */
public class HandlerServiceFactory extends AbstractServiceFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.AbstractServiceFactory#create(java.lang.Class,
	 *      org.eclipse.ui.services.IServiceLocator,
	 *      org.eclipse.ui.services.IServiceLocator)
	 */
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if (!IHandlerService.class.equals(serviceInterface)) {
			return null;
		}
		final IWorkbench wb = (IWorkbench) locator.getService(IWorkbench.class);
		if (wb == null) {
			return null;
		}

		Object parent = parentLocator.getService(serviceInterface);
		if (parent == null) {
			ICommandService commands = (ICommandService) locator
					.getService(ICommandService.class);
			IEvaluationService evals = (IEvaluationService) locator
					.getService(IEvaluationService.class);
			HandlerService handlerService = new HandlerService(commands, evals);
			handlerService.readRegistry();
			fillInSources(locator, handlerService);
			return handlerService;
		}

		final IWorkbenchWindow window = (IWorkbenchWindow) locator
				.getService(IWorkbenchWindow.class);
		final IWorkbenchPartSite site = (IWorkbenchPartSite) locator
				.getService(IWorkbenchPartSite.class);
		if (site == null) {
			Expression exp = new WorkbenchWindowExpression(window);
			return new SlaveHandlerService((IHandlerService) parent, exp);
		}
		
		if (parent instanceof SlaveHandlerService) {
			Expression parentExp = ((SlaveHandlerService) parent)
					.getDefaultExpression();
			if (parentExp instanceof ActivePartExpression) {
				return new NestableHandlerService((IHandlerService) parent,
						parentExp);
			}
		}
		
		Expression exp = new ActivePartExpression(site.getPart());
		return new SlaveHandlerService((IHandlerService) parent, exp);
	}

	/**
	 * @param handlerService
	 */
	private void fillInSources(IServiceLocator locator,
			HandlerService handlerService) {
		ISourceProviderService sps = (ISourceProviderService) locator
				.getService(ISourceProviderService.class);
		ISourceProvider[] sourceProviders = sps.getSourceProviders();
		for (int i = 0; i < sourceProviders.length; i++) {
			handlerService.addSourceProvider(sourceProviders[i]);
		}
	}
}
