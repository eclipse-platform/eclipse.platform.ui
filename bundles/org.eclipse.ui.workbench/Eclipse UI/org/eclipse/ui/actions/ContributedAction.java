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

package org.eclipse.ui.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.handlers.HandlerService;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.services.IServiceLocator;

/**
 * For a declarative editor action, see if we can link it to a command.
 * <p>
 * This is a legacy bridge class, and should not be used outside of the Eclipse
 * SDK. Please use menu contributions to display a command in a menu or toolbar.
 * </p>
 * <p>
 * <b>Note:</b> Clients may instantiate.
 * </p>
 * 
 * @since 3.3
 */
public final class ContributedAction extends CommandAction {
	private IEvaluationContext appContext;
	private IHandler partHandler;

	/**
	 * Create an action that can call a command.
	 * 
	 * @param locator
	 *            The appropriate service locator to use. If you use a part site
	 *            as your locator, this action will be tied to your part.
	 * @param element
	 *            the contributed action element
	 */
	public ContributedAction(IServiceLocator locator,
			IConfigurationElement element) throws CommandNotMappedException {

		String actionId = element
				.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		String commandId = element
				.getAttribute(IWorkbenchRegistryConstants.ATT_DEFINITION_ID);

		// TODO throw some more exceptions here :-)

		String contributionId = null;
		if (commandId == null) {

			Object obj = element.getParent();
			if (obj instanceof IConfigurationElement) {
				contributionId = ((IConfigurationElement) obj)
						.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
				if (contributionId == null) {
					throw new CommandNotMappedException("Action " //$NON-NLS-1$
							+ actionId + " configuration element invalid"); //$NON-NLS-1$
				}
			}
			// legacy bridge part
			IActionCommandMappingService mapping = (IActionCommandMappingService) locator
					.getService(IActionCommandMappingService.class);
			if (mapping == null) {
				throw new CommandNotMappedException(
						"No action mapping service available"); //$NON-NLS-1$
			}

			commandId = mapping.getCommandId(mapping.getGeneratedCommandId(
					contributionId, actionId));
		}
		// what, still no command?
		if (commandId == null) {
			throw new CommandNotMappedException("Action " + actionId //$NON-NLS-1$
					+ " in contribution " + contributionId //$NON-NLS-1$
					+ " not mapped to a command"); //$NON-NLS-1$
		}

		if (locator instanceof PartSite) {
			updateSiteAssociations(locator, commandId);
		}

		init(locator, commandId, null);
		setId(actionId);
	}

	private void updateSiteAssociations(IServiceLocator locator,
			String commandId) {
		PartSite site = (PartSite) locator;
		IWorkbench workbench = (IWorkbench) locator
				.getService(IWorkbench.class);
		IWorkbenchWindow window = (IWorkbenchWindow) locator
				.getService(IWorkbenchWindow.class);
		IHandlerService serv = (IHandlerService) workbench
				.getService(IHandlerService.class);
		appContext = serv.getCurrentState();

		// set up the appContext as we would want it.
		appContext.addVariable(ISources.ACTIVE_PART_NAME, site.getPart());
		appContext.addVariable(ISources.ACTIVE_PART_ID_NAME, site.getId());
		appContext.addVariable(ISources.ACTIVE_SITE_NAME, site);
		if (site instanceof IEditorSite) {
			appContext.addVariable(ISources.ACTIVE_EDITOR_NAME, site.getPart());
			appContext
					.addVariable(ISources.ACTIVE_EDITOR_ID_NAME, site.getId());
		}
		appContext.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, window);
		appContext.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME,
				window.getShell());

		HandlerService realService = (HandlerService) serv;
		partHandler = realService.findHandler(commandId, appContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.actions.CommandAction#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		if (partHandler != null) {
			IHandler oldHandler = getParameterizedCommand().getCommand()
					.getHandler();
			try {
				getParameterizedCommand().getCommand().setHandler(partHandler);
				getParameterizedCommand().executeWithChecks(event, appContext);
			} catch (ExecutionException e) {
				// TODO some logging, perhaps?
			} catch (NotDefinedException e) {
				// TODO some logging, perhaps?
			} catch (NotEnabledException e) {
				// TODO some logging, perhaps?
			} catch (NotHandledException e) {
				// TODO some logging, perhaps?
			} finally {
				getParameterizedCommand().getCommand().setHandler(oldHandler);
			}
		} else {
			super.runWithEvent(event);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	public boolean isEnabled() {
		if (partHandler!=null) {
			return partHandler.isEnabled();
		}
		return super.isEnabled();
	}
}
