/*******************************************************************************
 * Copyright (c) 2010, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 372799
 ******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.AbstractEvaluationHandler;
import org.eclipse.ui.internal.InternalHandlerUtil;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * @since 3.7
 */
public abstract class AbstractSaveHandler extends AbstractEvaluationHandler {

	protected static DirtyStateTracker dirtyStateTracker;
	private Expression enabledWhen;

	public AbstractSaveHandler() {
		if (dirtyStateTracker == null) {
			dirtyStateTracker = new DirtyStateTracker();
		}
	}

	@Override
	protected Expression getEnabledWhenExpression() {
		if (enabledWhen == null) {
			enabledWhen = new Expression() {
				@Override
				public EvaluationResult evaluate(IEvaluationContext context) {
					return AbstractSaveHandler.this.evaluate(context);
				}

				@Override
				public void collectExpressionInfo(ExpressionInfo info) {
					info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
				}
			};
		}
		return enabledWhen;
	}

	protected abstract EvaluationResult evaluate(IEvaluationContext context);

	protected ISaveablePart getSaveablePart(IEvaluationContext context) {
		IWorkbenchPart activePart = InternalHandlerUtil.getActivePart(context);
		ISaveablePart part = SaveableHelper.getSaveable(activePart);
		if (part != null) {
			return part;
		}
		return InternalHandlerUtil.getActiveEditor(context);
	}

	protected ISaveablePart getSaveablePart(ExecutionEvent event) {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		ISaveablePart part = SaveableHelper.getSaveable(activePart);
		if (part != null) {
			return part;
		}
		return HandlerUtil.getActiveEditor(event);
	}

	public static DirtyStateTracker getDirtyStateTracker() {
		return dirtyStateTracker;
	}

	protected MPart getActivePart(IWorkbenchWindow window) {
		MPart part = null;
		if (window instanceof WorkbenchWindow) {
			EPartService partService = null;
			try {
				partService = ((WorkbenchWindow) window).getModel().getContext().get(EPartService.class);
			} catch (Exception e) {
				// do nothing
			}
			if (partService != null) {
				part = partService.getActivePart();
			}
		}
		return part;
	}
}