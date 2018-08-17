/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
package org.eclipse.ui.examples.jobs.views;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class ProgressExampleAdapterFactory implements IAdapterFactory {
	private SlowElementAdapter slowElementAdapter = new SlowElementAdapter();

	@Override
	public <T> T getAdapter(Object object, Class<T> type) {
		if(object instanceof SlowElement) {
			if(type == SlowElement.class || type == IDeferredWorkbenchAdapter.class || type == IWorkbenchAdapter.class)
				return type.cast(slowElementAdapter);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] {
				SlowElement.class, IDeferredWorkbenchAdapter.class, IWorkbenchAdapter.class
				};
	}
}
