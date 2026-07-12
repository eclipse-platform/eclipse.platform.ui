/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILog;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.MultiPageEditorSite;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Owns the Find/Replace overlay's command-related infrastructure. Currently
 * encapsulates the workaround that disables the target text editor's global
 * action handlers while the overlay has focus, preventing them from consuming
 * key events that are meant for the overlay's text fields.
 */
class FindReplaceOverlayCommandSupport {

	private final IWorkbenchPart targetPart;
	private DeactivateGlobalActionHandlers globalActionHandlerDeaction;

	FindReplaceOverlayCommandSupport(IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	void overlayActivated() {
		setTextEditorActionsActivated(false);
	}

	void overlayDeactivated() {
		setTextEditorActionsActivated(true);
	}

	/*
	 * Adapted from
	 * org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#setActionsActivated(boolean)
	 */
	private void setTextEditorActionsActivated(boolean state) {
		if (!(targetPart instanceof AbstractTextEditor) || targetPart.getSite().getWorkbenchWindow().isClosing()) {
			return;
		}
		if (targetPart.getSite() instanceof MultiPageEditorSite multiEditorSite) {
			if (!state && globalActionHandlerDeaction == null) {
				globalActionHandlerDeaction = new DeactivateGlobalActionHandlers(multiEditorSite.getActionBars());
			} else if (state && globalActionHandlerDeaction != null) {
				globalActionHandlerDeaction.reactivate();
				globalActionHandlerDeaction = null;
			}
		}
		try {
			Method method = AbstractTextEditor.class.getDeclaredMethod("setActionActivation", boolean.class); //$NON-NLS-1$
			method.setAccessible(true);
			method.invoke(targetPart, Boolean.valueOf(state));
		} catch (IllegalArgumentException | ReflectiveOperationException ex) {
			ILog.of(FindReplaceOverlayCommandSupport.class).error("cannot (de-)activate actions for text editor", ex); //$NON-NLS-1$
		}
	}

	private static final class DeactivateGlobalActionHandlers {
		private static final List<String> ACTIONS = List.of(ITextEditorActionConstants.CUT,
				ITextEditorActionConstants.COPY, ITextEditorActionConstants.PASTE,
				ITextEditorActionConstants.DELETE, ITextEditorActionConstants.SELECT_ALL,
				ITextEditorActionConstants.FIND);

		private final Map<String, IAction> deactivatedActions = new HashMap<>();
		private final IActionBars actionBars;

		DeactivateGlobalActionHandlers(IActionBars actionBars) {
			this.actionBars = actionBars;
			for (String actionID : ACTIONS) {
				deactivatedActions.putIfAbsent(actionID, actionBars.getGlobalActionHandler(actionID));
				actionBars.setGlobalActionHandler(actionID, null);
			}
		}

		void reactivate() {
			for (String actionID : deactivatedActions.keySet()) {
				actionBars.setGlobalActionHandler(actionID, deactivatedActions.get(actionID));
			}
		}
	}

}
