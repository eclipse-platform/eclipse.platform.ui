/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPlaceholderResolver;

/**
 * Default implementation of a resolver that expects the id of the placeholder to match the id of a
 * PartDescriptor. It will create an MPart from the descriptor and add it to the appropriate
 * window'w 'sharedElementList' if necessary.
 */
public class PlaceholderResolver implements EPlaceholderResolver {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EPlaceholderResolver#resolvePlaceholderRef(org.eclipse
	 * .e4.ui.model.application.ui.advanced.MPlaceholder,
	 * org.eclipse.e4.ui.model.application.ui.basic.MWindow)
	 */
	@Override
	public void resolvePlaceholderRef(MPlaceholder ph, MWindow refWin) {
		if (ph.getRef() != null)
			return;

		// Must give us the window that you are resolving for...
		Assert.isLegal(refWin != null);

		// ..and it must be a 'top level' window
		MUIElement refParent = refWin.getParent();
		Assert.isLegal(refParent instanceof MApplication);

		// So we already have a matching shared element ?
		List<MUIElement> sharedElements = refWin.getSharedElements();
		for (MUIElement se : sharedElements) {
			if (ph.getElementId().equals(se.getElementId())) {
				ph.setRef(se);
				return;
			}
		}

		// Hack to make the 3.x compatibility layer work
		if (ph.getElementId().equals("org.eclipse.ui.editorss")) { //$NON-NLS-1$
			// This code is for the eclipse compatibility layer...
			MArea sharedArea = AdvancedFactoryImpl.eINSTANCE.createArea();
			// sharedArea.setLabel("Editor Area"); //$NON-NLS-1$

			MPartStack editorStack = BasicFactoryImpl.eINSTANCE.createPartStack();
			// temporary HACK for bug 303982
			editorStack.getTags().add("newtablook"); //$NON-NLS-1$
			editorStack.getTags().add("org.eclipse.e4.primaryDataStack"); //$NON-NLS-1$
			editorStack.getTags().add("EditorStack"); //$NON-NLS-1$
			editorStack.setElementId("org.eclipse.e4.primaryDataStack"); //$NON-NLS-1$
			sharedArea.getChildren().add(editorStack);
			sharedArea.setElementId(ph.getElementId());

			refWin.getSharedElements().add(sharedArea);
			ph.setRef(sharedArea);
			return;
		}

		// Assume that the placeholder is to a shared 'part'
		EPartService ps = refWin.getContext().get(EPartService.class);
		MPart newReferencedPart = ps.createPart(ph.getElementId());
		if (newReferencedPart != null) {
			refWin.getSharedElements().add(newReferencedPart);
			ph.setRef(newReferencedPart);
			return;
		}

		// SHould we log / show an exception here ?
	}

}
