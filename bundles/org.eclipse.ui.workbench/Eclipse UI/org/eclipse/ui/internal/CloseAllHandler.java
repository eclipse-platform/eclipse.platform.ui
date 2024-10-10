/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Closes all active editors
 * <p>
 * Replacement for CloseAllAction
 * </p>
 *
 * @since 3.3
 */
public class CloseAllHandler extends AbstractEvaluationHandler {
	private Expression enabledWhen;

	public CloseAllHandler() {
		registerEnablement();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			page.closeAllEditors(true);

			// close parts representing editors which were contributed via
			// eg. model fragment(s)
			Collection<MPart> partsTaggedAsEditor = getContributedPartsTaggedAsEditor();
			if (!partsTaggedAsEditor.isEmpty()) {
				MApplication application = getApplicationModel();
				EPartService partService = application.getContext().get(EPartService.class);
				if (partService != null) {
					for (MPart part : partsTaggedAsEditor) {
						if (partService.savePart(part, true)) {
							partService.hidePart(part);
						}
					}
					// ensure the EnabledWhenExpression evaluation is performed
					// otherwise the 'Close All Editors' will still appear enabled until
					// the user clicks/selects a different part
					getEvaluationService().requestEvaluation(ISources.ACTIVE_PART_NAME);
				}
			}
		}

		return null;
	}

	@Override
	protected Expression getEnabledWhenExpression() {
		if (enabledWhen == null) {
			enabledWhen = new Expression() {
				@Override
				public EvaluationResult evaluate(IEvaluationContext context) {
					IWorkbenchPart part = InternalHandlerUtil.getActivePart(context);
					Object perspective = InternalHandlerUtil.getVariable(context,
							ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
					if (part != null && perspective != null && part.getSite() != null) {
						IWorkbenchPage page = part.getSite().getPage();
						if (page != null) {
							IEditorReference[] refArray = page.getEditorReferences();
							if (refArray != null && refArray.length > 0) {
								return EvaluationResult.TRUE;
							}

							// determine if we have any part contributions via model fragment
							// which were tagged as being an 'Editor' (and which are to be rendered)
							if (!getContributedPartsTaggedAsEditor().isEmpty()) {
								return EvaluationResult.TRUE;
							}
						}
					}
					return EvaluationResult.FALSE;
				}

				@Override
				public void collectExpressionInfo(ExpressionInfo info) {
					info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
					info.addVariableNameAccess(ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
				}
			};
		}
		return enabledWhen;
	}

	/**
	 * Collects part contributions from the application model which are not
	 * associated with compatibility layer editors, and are instead parts
	 * contributed via eg. model fragment, and which were tagged as representing an
	 * Editor, via the {@link Workbench#EDITOR_TAG} tag.
	 *
	 * @return a collection of (closable) part contributions from the application
	 *         model, tagged as 'Editor' and not containing the parts associated
	 *         with compatibility layer editors. Returns an empty collection if none
	 *         are found
	 */
	private Collection<MPart> getContributedPartsTaggedAsEditor() {
		MApplication application = getApplicationModel();
		EModelService modelService = application.getContext().get(EModelService.class);

		List<MPart> partsTaggedAsEditor = modelService != null
				? modelService.findElements(application, null, MPart.class, Arrays.asList(Workbench.EDITOR_TAG))
				: Collections.emptyList();

		// remove parts which we wish to ignore: compatibility layer editors,
		// non-closable parts, non-rendered parts
		return partsTaggedAsEditor.stream().filter(p -> !CompatibilityEditor.MODEL_ELEMENT_ID.equals(p.getElementId())
				&& p.isCloseable() && p.isToBeRendered()).collect(Collectors.toSet());
	}

	private MApplication getApplicationModel() {
		BundleContext bundleContext = FrameworkUtil.getBundle(IWorkbench.class).getBundleContext();
		ServiceReference<IWorkbench> reference = bundleContext.getServiceReference(IWorkbench.class);
		return bundleContext.getService(reference).getApplication();
	}
}
