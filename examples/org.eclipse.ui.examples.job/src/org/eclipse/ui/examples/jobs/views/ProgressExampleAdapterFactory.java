/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class ProgressExampleAdapterFactory implements IAdapterFactory {
	private SlowElementAdapter slowElementAdapter = new SlowElementAdapter();

	@Override
	public Object getAdapter(Object object, Class type) {
		if(object instanceof SlowElement) {
			if(type == SlowElement.class || type == IDeferredWorkbenchAdapter.class || type == IWorkbenchAdapter.class)
				return slowElementAdapter;
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] {
				SlowElement.class, IDeferredWorkbenchAdapter.class, IWorkbenchAdapter.class
				};
	}
}
