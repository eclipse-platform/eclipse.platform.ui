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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.actions.CommandAction;
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

		init(locator, commandId, null);
		setId(actionId);
	}
}
