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

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

public class ViewAction extends ActionSet {

	private IConfigurationElement parent;
	private boolean isMenu;

	public ViewAction(MApplication application, IEclipseContext appContext,
			IConfigurationElement parent, IConfigurationElement element, boolean isMenu) {
		super(application, appContext, element);
		this.parent = parent;
		this.isMenu = isMenu;
	}

	@Override
	public void addToModel(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions,
			ArrayList<MTrimContribution> trimContributions) {
		String idContrib = MenuHelper.getId(configElement);
		visibleWhen = createExpression(configElement);

		EContextService contextService = application.getContext().get(EContextService.class);
		Context actionSetContext = contextService.getContext(idContrib);
		if (!actionSetContext.isDefined()) {
			actionSetContext.define(MenuHelper.getLabel(configElement),
					MenuHelper.getDescription(configElement), "org.eclipse.ui.contexts.actionSet"); //$NON-NLS-1$
		}

		String parentId = parent.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
		addContribution(idContrib, menuContributions, configElement, isMenu, parentId);
		if (!isMenu) {
			addToolBarContribution(idContrib, toolBarContributions, trimContributions,
					configElement, parentId);
		}
	}

	protected Expression createExpression(IConfigurationElement configElement) {
		return null;
	}
}
