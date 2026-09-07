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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EContextService;

import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Owns the Find/Replace overlay's contexts, of which there are two kinds: an
 * {@link IEclipseContext} that makes the overlay rather than its host editor the
 * active part while the focus is anywhere in the overlay, and the overlay's key
 * binding scopes. The second kind lives inside the first, because scopes are
 * collected along the chain between the active leaf and the root: a scope
 * activated here is active exactly while this context is the active leaf, so the
 * shared scope is activated once and only the per-field scope is switched.
 * <p>
 * The shared scope follows the focus on the whole overlay, so that its shortcuts
 * also work from the tool bars. The per-field scopes bind Enter and its
 * variations and thus follow the input fields only, since on a tool bar button
 * Enter belongs to the button.
 * <p>
 * Two conditions decide whether one of the editor's handlers wins, and the
 * editor's commands are spread over both. Its key binding scopes and part-level
 * handlers are only <em>reachable</em> through the editor's part context, which
 * activating a sibling of that context takes off the chain. Its retargetable
 * actions live in the window context instead, guarded by an <em>expression</em>
 * over {@link ISources#ACTIVE_PART_ID_NAME}, which publishing this context's own
 * id as the active part id makes false. Together they leave no editor handler in
 * the resolution path, so no command has to be suppressed individually. The
 * context sits below the window context rather than the application context,
 * which would detach window-scoped commands and services as well.
 * <p>
 * The {@link MPart} looks superfluous, since nothing reads it, but removing it
 * changes behaviour: it is what lets {@code ActivePartLookupFunction} resolve an
 * active part here. Without it that lookup yields {@code null}, and
 * {@code PartServiceImpl} answers a null active part by firing part deactivation
 * and clearing the active selection. With it, the same code path finds a part
 * outside the application model and returns early. That early return is also why
 * the part is neither added to the model nor rendered nor activated through
 * {@code EPartService}.
 * <p>
 * The alternatives considered, and what was measured about them, are recorded in
 * {@code docs/adr/0001-find-replace-overlay-key-handling.md}.
 */
class FindReplaceOverlayContextSupport {

	static final String OVERLAY_PART_ID_PREFIX = "org.eclipse.ui.workbench.texteditor.findReplaceOverlay.part."; //$NON-NLS-1$

	/**
	 * Tells the overlays of different editors apart: their command handlers are all
	 * activated at the workbench, so the id must not be shared.
	 */
	private static final AtomicInteger PART_ID_SEQUENCE = new AtomicInteger();

	private static final String OVERLAY_CONTEXT_ID = "org.eclipse.ui.workbench.texteditor.findReplaceOverlay"; //$NON-NLS-1$

	private static final String OVERLAY_SEARCH_CONTEXT_ID = "org.eclipse.ui.workbench.texteditor.findReplaceOverlay.searchFocused"; //$NON-NLS-1$

	private static final String OVERLAY_REPLACE_CONTEXT_ID = "org.eclipse.ui.workbench.texteditor.findReplaceOverlay.replaceFocused"; //$NON-NLS-1$

	private final IWorkbenchPart targetPart;

	private final String overlayPartId = OVERLAY_PART_ID_PREFIX + PART_ID_SEQUENCE.incrementAndGet();

	private IEclipseContext overlayContext;

	/**
	 * The leaf that was active before the overlay took over, restored when it gives
	 * focus back. Remembered rather than derived from the target part, which also
	 * works without a part and does not need the part's site to still be there. Held
	 * only while the overlay owns the active leaf, so that handing it back does not
	 * keep the context of an editor closed in the meantime reachable.
	 */
	private IEclipseContext contextToRestore;

	private boolean overlayContextActive;

	private final Expression overlayFocusedExpression = createOverlayFocusedExpression();

	/**
	 * The per-field scope currently activated in {@link #overlayContext}, or
	 * {@code null} while the focus is on a control that is not an input field. Only
	 * ever switched: while that context is off the active chain its scopes are
	 * inert.
	 */
	private String activeFieldContextId;

	FindReplaceOverlayContextSupport(IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
		this.overlayContext = createOverlayContext();
	}

	private IEclipseContext createOverlayContext() {
		IEclipseContext windowContext = getWindowContext();
		if (windowContext == null) {
			return null;
		}
		MPart overlayPart = MBasicFactory.INSTANCE.createPart();
		overlayPart.setElementId(overlayPartId);

		IEclipseContext context = windowContext.createChild(overlayPartId);
		context.set(MPart.class, overlayPart);
		// Only the id is overridden, not ACTIVE_PART_NAME: the overlay operates on the
		// editor, and so does anything invoked while the overlay has focus, so the
		// active part itself must keep pointing at the editor.
		context.set(ISources.ACTIVE_PART_ID_NAME, overlayPartId);
		overlayPart.setContext(context);

		context.get(EContextService.class).activateContext(OVERLAY_CONTEXT_ID);
		return context;
	}

	/**
	 * Holds exactly while the focus is somewhere in this overlay, for scoping
	 * activations to this overlay alone. It tests the active part id this context
	 * publishes, which is unique per overlay. Declaring that variable in
	 * {@code collectExpressionInfo} is what gives such activations their source
	 * priority, so the expression belongs next to the code setting the variable.
	 */
	Expression overlayFocusedExpression() {
		return overlayFocusedExpression;
	}

	private Expression createOverlayFocusedExpression() {
		return new Expression() {
			@Override
			public EvaluationResult evaluate(IEvaluationContext context) {
				return EvaluationResult
						.valueOf(overlayPartId.equals(context.getVariable(ISources.ACTIVE_PART_ID_NAME)));
			}

			@Override
			public void collectExpressionInfo(ExpressionInfo info) {
				info.addVariableNameAccess(ISources.ACTIVE_PART_ID_NAME);
			}
		};
	}

	void searchBarFocused() {
		overlayFocused(OVERLAY_SEARCH_CONTEXT_ID);
	}

	void replaceBarFocused() {
		overlayFocused(OVERLAY_REPLACE_CONTEXT_ID);
	}

	/**
	 * Any control other than the input fields, which gets the shared scope but no
	 * per-field scope.
	 */
	void otherOverlayControlFocused() {
		overlayFocused(null);
	}

	/**
	 * @param fieldContextId the per-field scope to activate, or {@code null} for
	 *                       none
	 */
	private void overlayFocused(String fieldContextId) {
		if (overlayContext == null) {
			return;
		}
		if (!Objects.equals(fieldContextId, activeFieldContextId)) {
			EContextService contextService = overlayContext.get(EContextService.class);
			if (activeFieldContextId != null) {
				contextService.deactivateContext(activeFieldContextId);
			}
			if (fieldContextId != null) {
				contextService.activateContext(fieldContextId);
			}
			activeFieldContextId = fieldContextId;
		}
		if (!overlayContextActive) {
			contextToRestore = overlayContext.getParent().getActiveLeaf();
			overlayContextActive = true;
		}
		overlayContext.activate();
	}

	void overlayLostFocus() {
		if (!overlayContextActive) {
			return;
		}
		overlayContextActive = false;
		IEclipseContext toRestore = contextToRestore;
		contextToRestore = null;
		if (toRestore != null) {
			handBackActiveLeaf(toRestore);
		}
	}

	/**
	 * Gives the active leaf back to what held it before the overlay took over, but
	 * only while the overlay still holds it. Losing the focus to another part does
	 * not come through here as a hand-back: the workbench activates the part under
	 * the mouse before the focus leaves the overlay, so that part already owns the
	 * leaf by now and has to keep it.
	 * <p>
	 * Handing it to the editor anyway would put the editor's key binding scope up
	 * beside the one the part coming up brings, and while both are up every key the
	 * two scopes have in common is an unresolvable binding conflict. Whether that
	 * surfaces depends on how the other part holds its scope, which is why it is
	 * observed with the console but not with an ordinary view.
	 * <p>
	 * Activating the overlay's context replaced the window context's active child, so
	 * the whole chain down to the context to restore has to be re-established, not
	 * just its last link.
	 */
	private void handBackActiveLeaf(IEclipseContext toRestore) {
		if (overlayContext != null && overlayContext.getParent().getActiveLeaf() == overlayContext) {
			toRestore.activateBranch();
		}
	}

	void dispose() {
		if (overlayContext != null) {
			// The context is about to go, and disposing it while it holds the active leaf
			// would leave the window context itself as the leaf, with the editor's scopes
			// on no chain at all.
			overlayLostFocus();
			overlayContext.dispose();
			overlayContext = null;
		}
	}

	/**
	 * The window the overlay belongs to. Only when there is no part at all, which
	 * the find/replace UI tests exercise, does the overlay fall back to the active
	 * window rather than guessing one for a part whose site is unavailable.
	 */
	private IEclipseContext getWindowContext() {
		IWorkbenchWindow window;
		if (targetPart == null) {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		} else {
			IWorkbenchPartSite site = targetPart.getSite();
			window = site == null ? null : site.getWorkbenchWindow();
		}
		return window == null ? null : window.getService(IEclipseContext.class);
	}

}
