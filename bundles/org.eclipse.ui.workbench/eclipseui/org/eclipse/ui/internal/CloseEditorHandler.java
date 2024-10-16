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

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Closes the active editor.
 * <p>
 * Replacement for CloseEditorAction
 * </p>
 *
 * @since 3.3
 */
public class CloseEditorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IEditorPart) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			window.getActivePage().closeEditor((IEditorPart) activePart, true);
		} else {
			// we may have an E4PartWrapper for a part which has been contributed eg. via a
			// PartDescriptor in a model fragment, and which has been tagged as
			// representing an Editor
			if (activePart instanceof E4PartWrapper) {
				// derive the IEclipseContext & EPartService
				BundleContext context = FrameworkUtil.getBundle(IWorkbench.class).getBundleContext();
				ServiceReference<IWorkbench> reference = context.getServiceReference(IWorkbench.class);
				IEclipseContext eclipseContext = context.getService(reference).getApplication().getContext();
				EPartService partService = eclipseContext.get(EPartService.class);

				// access the wrapped part => save & close it
				MPart wrappedPart = ((E4PartWrapper) activePart).wrappedPart;
				if (wrappedPart != null && partService != null) {
					// ensure the active part does indeed represent an editor
					// (and not eg. a view) - checking here is just for extra
					// redundancy
					if (representsEditor(wrappedPart)) {
						if (partService.savePart(wrappedPart, true)) {
							partService.hidePart(wrappedPart);
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Checks whether the specified part represents an editor instance.
	 *
	 * @param part the part to query
	 * @return true if the specified part represents an editor, false otherwise
	 */
	private boolean representsEditor(MPart part) {
		List<String> partTags = part.getTags();
		return partTags == null || partTags.isEmpty() ? false
				: partTags.stream().anyMatch(tag -> Workbench.EDITOR_TAG.equals(tag));
	}
}