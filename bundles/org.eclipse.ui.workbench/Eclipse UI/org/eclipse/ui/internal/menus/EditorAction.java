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
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

public class EditorAction extends ActionSet {

	private IConfigurationElement parent;

	public EditorAction(MApplication application, IEclipseContext appContext,
			IConfigurationElement parent, IConfigurationElement element) {
		super(application, appContext, element);
		this.parent = parent;
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

		addContribution(idContrib, menuContributions, configElement, false, MAIN_MENU);
		addToolBarContribution(idContrib, toolBarContributions, trimContributions, configElement,
				MAIN_TOOLBAR);
	}

	@Override
	protected Expression createExpression(IConfigurationElement configElement) {
		String idContrib = MenuHelper.getId(configElement);
		return new ActiveEditorContextExpression(
				parent.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID), idContrib);
	}

	static class ActiveEditorContextExpression extends Expression {
		private String editorId;
		private String actionId;

		public ActiveEditorContextExpression(String editorId, String actionId) {
			this.editorId = editorId;
			this.actionId = actionId;
		}

		@Override
		public void collectExpressionInfo(ExpressionInfo info) {
			info.addVariableNameAccess(ISources.ACTIVE_EDITOR_ID_NAME);
		}

		@Override
		public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
			Object activeEditorId = context.getVariable(ISources.ACTIVE_EDITOR_ID_NAME);
			if (activeEditorId instanceof String) {
				return EvaluationResult.valueOf(editorId.equals(activeEditorId));
			}
			return EvaluationResult.FALSE;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ActiveEditorContextExpression)) {
				return false;
			}

			ActiveEditorContextExpression other = (ActiveEditorContextExpression) obj;
			return actionId.equals(other.actionId) && editorId.equals(other.editorId);
		}

		@Override
		public int hashCode() {
			return actionId.hashCode() * editorId.hashCode();
		}
	}

}
