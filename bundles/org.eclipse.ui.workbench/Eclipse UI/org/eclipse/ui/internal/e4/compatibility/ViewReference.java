/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;

public class ViewReference extends WorkbenchPartReference implements IViewReference {

	ViewReference(IWorkbenchPage page, MPart part) {
		super(page, part);
	}

	public String getSecondaryId() {
		// TODO Auto-generated method stub
		return null;
	}

	public IViewPart getView(boolean restore) {
		return (IViewPart) getPart(restore);
	}

	public boolean isFastView() {
		// TODO Auto-generated method stub
		return false;
	}

}
