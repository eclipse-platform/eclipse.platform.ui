/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
package org.eclipse.e4.ui.internal.workbench;

import java.util.List;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;

/**
 *
 */
public class ActivePartLookupFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		/**
		 * This is the specific implementation. TODO: generalize it
		 */
		MContext window = context.get(MWindow.class);
		if (window == null) {
			window = context.get(MApplication.class);
			if (window == null) {
				return null;
			}
		}
		IEclipseContext current = window.getContext();
		if (current == null) {
			return null;
		}
		MPart part = current.getActiveLeaf().get(MPart.class);
		if (part == null)
			return null;
		MUIElement parent = part.getCurSharedRef() != null ? part.getCurSharedRef().getParent() : part.getParent();
		if (parent == null)
			return part;
		List<String> parentTags = parent.getTags();
		if (parentTags.contains(IPresentationEngine.MINIMIZED) && !parentTags.contains(IPresentationEngine.ACTIVE)
				&& !part.getTags().contains(IPresentationEngine.ACTIVE)) {
			return null;
		}
		return part;

	}
}
