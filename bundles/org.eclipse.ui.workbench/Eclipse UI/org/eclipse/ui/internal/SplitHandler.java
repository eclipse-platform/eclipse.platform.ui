/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 494680
 ******************************************************************************/
package org.eclipse.ui.internal;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SplitHandler extends AbstractHandler {
	private EModelService modelService;
	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Only works for the active editor
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor == null)
			return null;

		MPart editorPart = activeEditor.getSite().getService(MPart.class);
		if (editorPart == null)
			return null;

		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		// Get services
		modelService =  editorPart.getContext().get(EModelService.class);

		MPartStack stack = getStackFor(editorPart);
		if (stack == null)
			return null;

		window.getShell().setRedraw(false);
		try {
			// Determine which part has the tags
			MStackElement stackSelElement = stack.getSelectedElement();
			MPart taggedEditor = editorPart;
			if (stackSelElement instanceof MCompositePart) {
				List<MPart> innerElements = modelService.findElements(stackSelElement, null, MPart.class, null);
				taggedEditor = innerElements.get(1); // '0' is the composite part
			}

			if ("false".equals(event.getParameter("Splitter.isHorizontal"))) { //$NON-NLS-1$ //$NON-NLS-2$
				if (taggedEditor.getTags().contains(IPresentationEngine.SPLIT_VERTICAL)) {
					taggedEditor.getTags().remove(IPresentationEngine.SPLIT_VERTICAL);
				} else {
					editorPart.getTags().remove(IPresentationEngine.SPLIT_HORIZONTAL);
					editorPart.getTags().add(IPresentationEngine.SPLIT_VERTICAL);
				}
			} else {
				if (taggedEditor.getTags().contains(IPresentationEngine.SPLIT_HORIZONTAL)) {
					taggedEditor.getTags().remove(IPresentationEngine.SPLIT_HORIZONTAL);
				} else {
					editorPart.getTags().remove(IPresentationEngine.SPLIT_VERTICAL);
					editorPart.getTags().add(IPresentationEngine.SPLIT_HORIZONTAL);
				}
			}
		} finally {
			window.getShell().setRedraw(true);
		}

		return null;
	}

	private MPartStack getStackFor(MPart part) {
		MUIElement presentationElement = part.getCurSharedRef() == null ? part : part.getCurSharedRef();
		MUIElement parent = presentationElement.getParent();
		while (parent != null && !(parent instanceof MPartStack))
			parent = parent.getParent();

		return (MPartStack) parent;
	}
}
