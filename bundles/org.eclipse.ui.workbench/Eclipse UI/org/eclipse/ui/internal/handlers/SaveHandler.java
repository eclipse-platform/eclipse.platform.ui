/*******************************************************************************
 * Copyright (c) 2013, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
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
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * <p>
 * Replacement for SaveAction
 * </p>
 *
 * @since 3.7
 */
public class SaveHandler extends AbstractSaveHandler {

	public SaveHandler() {
		registerEnablement();
	}

	@Override
	public Object execute(ExecutionEvent event) {

		ISaveablePart saveablePart = getSaveablePart(event);

		MPart activeMPart = null;
		EPartService partService = null;
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			partService = ((WorkbenchWindow) window).getModel().getContext().get(EPartService.class);
			activeMPart = getActivePart(window);
		} catch (Exception e1) {
			// do nothing
		}

		// no saveable
		if (saveablePart == null) {
			if (activeMPart != null && activeMPart.isDirty() && partService != null) {
				partService.savePart(activeMPart, false);
			}
			return null;
		}

		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		ISaveablePart part = SaveableHelper.getSaveable(activePart);
		if (part == null && activeMPart != null && activeMPart.isDirty() && partService != null) {
			partService.savePart(activeMPart, false);
			return null;
		}

		// if editor
		if (saveablePart instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) saveablePart;
			IWorkbenchPage page = editorPart.getSite().getPage();
			page.saveEditor(editorPart, false);
			return null;
		}

		// if view
		WorkbenchPage page = (WorkbenchPage) activePart.getSite().getPage();
		page.saveSaveable(saveablePart, activePart, false, false);

		return null;

	}

	@Override
	protected EvaluationResult evaluate(IEvaluationContext context) {

		IWorkbenchWindow window = InternalHandlerUtil.getActiveWorkbenchWindow(context);
		// no window? not active
		if (window == null)
			return EvaluationResult.FALSE;

		boolean enabled = evaluateEnabled(context, window);
		if (window != null) {
			Shell shell = window.getShell();
			if (shell != null && !shell.isDisposed()) {
				shell.setModified(enabled);
			}
		}

		return enabled ? EvaluationResult.TRUE : EvaluationResult.FALSE;
	}

	private boolean evaluateEnabled(IEvaluationContext context, IWorkbenchWindow window) {

		WorkbenchPage page = (WorkbenchPage) window.getActivePage();

		// no page? not active
		if (page == null)
			return false;

		MPart activeMPart = getActivePart(window);

		IWorkbenchPart activePart = InternalHandlerUtil.getActivePart(context);
		ISaveablePart part = SaveableHelper.getSaveable(activePart);
		if (part == null && activeMPart != null && activeMPart.isDirty()) {
			return true;
		}

		// get saveable part
		ISaveablePart saveablePart = getSaveablePart(context);
		if (saveablePart == null && activeMPart == null)
			return false;

		if (saveablePart instanceof ISaveablesSource) {
			ISaveablesSource modelSource = (ISaveablesSource) saveablePart;
			if (SaveableHelper.needsSave(modelSource))
				return true;
			if (activeMPart == null)
				return false;
		}

		if (saveablePart != null && saveablePart.isDirty())
			return true;

		if (activeMPart != null && activeMPart.isDirty()) {
			return true;
		}

		return false;
	}
}
