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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.Expression;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Owns the Find/Replace overlay's command infrastructure: handler activation and
 * key-binding hint updates.
 * <p>
 * The overlay's own commands are activated as handlers once, rather than
 * imperatively activated and deactivated on every focus change. They are scoped
 * by {@link FindReplaceOverlayContextSupport#overlayFocusedExpression()}, which
 * both limits them to the time an input field has focus and tells them apart
 * from the handlers of the overlays of other editors, since all of those are
 * activated at the workbench for the same commands.
 * <p>
 * Everything context related, both the overlay's key binding scopes and keeping
 * the editor's own commands from consuming keys meant for the input fields, is
 * owned by {@link FindReplaceOverlayContextSupport}. This class only forwards
 * the overlay's focus changes to it, because the shortcut hints have to be
 * refreshed whenever the active scopes change.
 */
class FindReplaceOverlayCommandSupport {

	static final String CMD_CLOSE =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.close"; //$NON-NLS-1$
	static final String CMD_TOGGLE_REPLACE =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.toggleReplace"; //$NON-NLS-1$
	static final String CMD_TOGGLE_CASE_SENSITIVE =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.toggleCaseSensitive"; //$NON-NLS-1$
	static final String CMD_TOGGLE_WHOLE_WORD =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.toggleWholeWord"; //$NON-NLS-1$
	static final String CMD_TOGGLE_REGEX =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.toggleRegex"; //$NON-NLS-1$
	static final String CMD_TOGGLE_SEARCH_IN_SELECTION =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.toggleSearchInSelection"; //$NON-NLS-1$
	static final String CMD_SEARCH_FORWARD =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.searchForward"; //$NON-NLS-1$
	static final String CMD_SEARCH_BACKWARD =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.searchBackward"; //$NON-NLS-1$
	static final String CMD_SELECT_ALL =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.selectAll"; //$NON-NLS-1$
	static final String CMD_REPLACE_FORWARD =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.replaceOne"; //$NON-NLS-1$
	static final String CMD_REPLACE_ALL =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.replaceAll"; //$NON-NLS-1$

	private final Expression overlayFocusedExpression;
	private final FindReplaceOverlayContextSupport contextSupport;

	private final List<FindReplaceOverlayAction> registeredActions = new ArrayList<>();
	private final List<IHandlerActivation> actionActivations = new ArrayList<>();

	FindReplaceOverlayCommandSupport(IWorkbenchPart targetPart) {
		this.contextSupport = new FindReplaceOverlayContextSupport(targetPart);
		this.overlayFocusedExpression = contextSupport.overlayFocusedExpression();
	}

	void dispose() {
		deregisterActionActivations();
		contextSupport.dispose();
	}

	void registerAction(FindReplaceOverlayAction action) {
		IHandlerActivation activation = activateAction(action);
		if (activation != null) {
			actionActivations.add(activation);
		}
		registeredActions.add(action);
		action.updateHint();
	}

	private IHandlerActivation activateAction(FindReplaceOverlayAction action) {
		String commandId = action.getCommandId();
		IHandlerService handlerService = getWorkbenchHandlerService();
		if (commandId == null || handlerService == null) {
			return null;
		}
		return handlerService.activateHandler(commandId, action, overlayFocusedExpression);
	}

	private void deregisterActionActivations() {
		IHandlerService handlerService = getWorkbenchHandlerService();
		if (handlerService != null) {
			handlerService.deactivateHandlers(actionActivations);
		}
		actionActivations.clear();
	}

	private static IHandlerService getWorkbenchHandlerService() {
		return PlatformUI.getWorkbench().getService(IHandlerService.class);
	}

	void searchBarActivated() {
		contextSupport.searchBarFocused();
		refreshShortcutHints();
	}

	void replaceBarActivated() {
		contextSupport.replaceBarFocused();
		refreshShortcutHints();
	}

	void searchOrReplaceBarDeactivated() {
		contextSupport.fieldsLostFocus();
		refreshShortcutHints();
	}

	private void refreshShortcutHints() {
		for (FindReplaceOverlayAction action : registeredActions) {
			action.updateHint();
		}
	}

}
