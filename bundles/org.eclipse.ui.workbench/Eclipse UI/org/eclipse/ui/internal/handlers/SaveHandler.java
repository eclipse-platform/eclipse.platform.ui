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

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.InternalHandlerUtil;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.WorkbenchPage;

/**
 * <p>
 * Replacement for SaveAction
 * </p>
 * 
 * @since 3.7
 * 
 */
public class SaveHandler extends AbstractSaveHandler {

	public SaveHandler() {
		registerEnablement();
	}

	public Object execute(ExecutionEvent event) {

		ISaveablePart saveablePart = getSaveablePart(event);

		// no saveable
		if (saveablePart == null)
			return null;

		// if editor
		if (saveablePart instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) saveablePart;
			IWorkbenchPage page = editorPart.getSite().getPage();
			page.saveEditor(editorPart, false);
			return null;
		}

		// if view
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		WorkbenchPage page = (WorkbenchPage) activePart.getSite().getPage();
		page.savePart(saveablePart, activePart, false);

		return null;

	}

	protected EvaluationResult evaluate(IEvaluationContext context) {

		IWorkbenchWindow window = InternalHandlerUtil.getActiveWorkbenchWindow(context);
		// no window? not active
		if (window == null)
			return EvaluationResult.FALSE;
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();

		// no page? not active
		if (page == null)
			return EvaluationResult.FALSE;

		// get saveable part
		ISaveablePart saveablePart = getSaveablePart(context);
		if (saveablePart == null)
			return EvaluationResult.FALSE;

		if (saveablePart instanceof ISaveablesSource) {
			ISaveablesSource modelSource = (ISaveablesSource) saveablePart;
			if (SaveableHelper.needsSave(modelSource))
				return EvaluationResult.TRUE;
			return EvaluationResult.FALSE;
		}

		if (saveablePart != null && saveablePart.isDirty())
			return EvaluationResult.TRUE;

		return EvaluationResult.FALSE;
	}
}
