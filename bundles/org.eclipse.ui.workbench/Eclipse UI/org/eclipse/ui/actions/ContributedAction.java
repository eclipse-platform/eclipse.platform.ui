/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.actions.CommandAction;
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
		}
		// what, still no command?
		if (commandId == null) {
			throw new CommandNotMappedException("Action " + actionId //$NON-NLS-1$
					+ " in contribution " + contributionId //$NON-NLS-1$
					+ " not mapped to a command"); //$NON-NLS-1$
		}

		init(locator, commandId, null);

		setId(actionId);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.actions.CommandAction#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		if (partHandler != null && getParameterizedCommand() != null) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	public boolean isEnabled() {
		if (partHandler != null) {
			if (partHandler instanceof IHandler2) {
				((IHandler2) partHandler).setEnabled(appContext);
			}
			return partHandler.isEnabled();
		}
		return false;
	}


}
