/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonas Helming <jhelming@eclipsesource.com>
 ******************************************************************************/

package org.eclipse.e4.tools.compat.parts;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

public abstract class DIMultiPageEditorPart extends MultiPageEditorPart
		implements IDirtyProviderService {

	public <T> int addPage(Class<T> clazz) throws PartInitException {
		DIEditorPart<T> part = new DIEditorPart<T>(clazz) {};
		return addPage(part, getEditorInput());

	}

	public DIMultiPageEditorPart() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		for (int i = 0; i < getPageCount(); i++) {
			IEditorPart e = getEditor(i);
			e.doSave(monitor);
		}
	}

}
