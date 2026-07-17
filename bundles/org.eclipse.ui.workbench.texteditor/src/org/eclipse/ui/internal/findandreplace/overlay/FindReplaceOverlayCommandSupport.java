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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.ILog;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.ui.swt.IFocusService;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Owns the Find/Replace overlay's command infrastructure, including context
 * activation, handler activation, and key-binding hint updates.
 * <p>
 * The overlay's own commands are activated as handlers once, scoped by
 * {@link #overlayFocusedExpression}, rather than imperatively
 * activated/deactivated on every focus change. That expression relies on
 * {@link IFocusService} tracking the search/replace bar controls so that
 * {@code ACTIVE_FOCUS_CONTROL} reflects them. Context activation (which
 * drives key binding resolution and has no expression-based equivalent)
 * remains imperative and is updated directly from the overlay's focus
 * listeners.
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

	private static final String OVERLAY_CONTEXT_ID =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay"; //$NON-NLS-1$
	private static final String OVERLAY_SEARCH_CONTEXT_ID =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.searchFocused"; //$NON-NLS-1$
	private static final String OVERLAY_REPLACE_CONTEXT_ID =
			"org.eclipse.ui.workbench.texteditor.findReplaceOverlay.replaceFocused"; //$NON-NLS-1$

	private Composite containerControl;
	private final IWorkbenchPart targetPart;
	private DeactivateGlobalActionHandlers globalActionHandlerDeaction;

	private final List<IContextActivation> contextActivations = new ArrayList<>();
	private final Expression overlayFocusedExpression;

	private final List<FindReplaceOverlayAction> permanentActions = new ArrayList<>();
	private final List<IHandlerActivation> permanentActionActivations = new ArrayList<>();
	private final List<FindReplaceOverlayAction> replaceActions = new ArrayList<>();
	private final List<IHandlerActivation> replaceActionActivations = new ArrayList<>();

	FindReplaceOverlayCommandSupport(IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
		this.overlayFocusedExpression = createOverlayFocusedExpression();
	}

	private Expression createOverlayFocusedExpression() {
		return new Expression() {
			@Override
			public EvaluationResult evaluate(IEvaluationContext context) {
				Object focusControl = context.getVariable(ISources.ACTIVE_FOCUS_CONTROL_NAME);
				if (focusControl instanceof Control control) {
					Control current = control;
					while (current != null) {
						if (current == containerControl) {
							return EvaluationResult.TRUE;
						}
						current = current.getParent();
					}
				}
				return EvaluationResult.FALSE;
			}

			@Override
			public void collectExpressionInfo(ExpressionInfo info) {
				info.addVariableNameAccess(ISources.ACTIVE_FOCUS_CONTROL_NAME);
			}
		};
	}

	void trackFocusControl(Text text) {
		IFocusService focusService = PlatformUI.getWorkbench().getService(IFocusService.class);
		if (focusService != null) {
			focusService.addFocusTracker(text, "" + text.hashCode()); //$NON-NLS-1$
		}
	}

	void setContainerControl(Composite containerControl) {
		this.containerControl = containerControl;
		containerControl.addDisposeListener(__ -> {
			deregisterActionActivations();
			// Safety net: normally already done by the focus-lost handling that runs
			// while the overlay is closed via close(), but disposal is not guaranteed
			// to be preceded by a focus-lost event, so repeat it here defensively. Both
			// calls are idempotent if that cleanup already ran.
			deactivateContexts();
			setTextEditorActionsActivated(true);
		});
	}

	void registerAction(FindReplaceOverlayAction action) {
		IHandlerActivation activation = activateAction(action);
		if (activation != null) {
			permanentActionActivations.add(activation);
		}
		permanentActions.add(action);
		action.updateHint();
	}

	void registerReplaceAction(FindReplaceOverlayAction action) {
		IHandlerActivation activation = activateAction(action);
		if (activation != null) {
			replaceActionActivations.add(activation);
		}
		replaceActions.add(action);
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

	void unregisterReplaceActions() {
		IHandlerService handlerService = getWorkbenchHandlerService();
		if (handlerService != null) {
			handlerService.deactivateHandlers(replaceActionActivations);
		}
		replaceActionActivations.clear();
		replaceActions.clear();
	}

	private void deregisterActionActivations() {
		IHandlerService handlerService = getWorkbenchHandlerService();
		if (handlerService != null) {
			handlerService.deactivateHandlers(permanentActionActivations);
			handlerService.deactivateHandlers(replaceActionActivations);
		}
		permanentActionActivations.clear();
		replaceActionActivations.clear();
	}

	private static IHandlerService getWorkbenchHandlerService() {
		return PlatformUI.getWorkbench().getService(IHandlerService.class);
	}

	void searchBarActivated() {
		searchOrReplaceBarActivated(OVERLAY_SEARCH_CONTEXT_ID);
	}

	void replaceBarActivated() {
		searchOrReplaceBarActivated(OVERLAY_REPLACE_CONTEXT_ID);
	}

	private void searchOrReplaceBarActivated(String barContextId) {
		setTextEditorActionsActivated(false);
		// Defensively clear any contexts still active from a previous activation,
		// making this method idempotent instead of relying on a focus-lost event
		// always having deactivated them first.
		deactivateContexts();
		activateContext(OVERLAY_CONTEXT_ID);
		activateContext(barContextId);
		refreshShortcutHints();
	}

	private void activateContext(String context) {
		IContextService contextService = getWorkbenchContextService();
		if (contextService != null) {
			contextActivations.add(contextService.activateContext(context));
		}
	}

	private static IContextService getWorkbenchContextService() {
		return PlatformUI.getWorkbench().getService(IContextService.class);
	}

	void searchOrReplaceBarDeactivated() {
		deactivateContexts();
		setTextEditorActionsActivated(true);
		refreshShortcutHints();
	}

	private void deactivateContexts() {
		IContextService contextService = getWorkbenchContextService();
		if (contextService != null) {
			for (IContextActivation activation : contextActivations.reversed()) {
				contextService.deactivateContext(activation);
			}
		}
		contextActivations.clear();
	}

	private void refreshShortcutHints() {
		for (FindReplaceOverlayAction action : permanentActions) {
			action.updateHint();
		}
		for (FindReplaceOverlayAction action : replaceActions) {
			action.updateHint();
		}
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
