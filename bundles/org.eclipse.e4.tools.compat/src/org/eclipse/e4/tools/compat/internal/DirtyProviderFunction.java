/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.compat.internal;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.ui.part.EditorPart;

public class DirtyProviderFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context) {
		final DIEditorPart<?> part = (DIEditorPart<?>) context.get(EditorPart.class);
		if( part == null ) {
			return null;
		}
		
		return new IDirtyProviderService() {

			public void setDirtyState(boolean dirtyState) {
				part.setDirtyState(dirtyState);
			}
		};
	}

}
