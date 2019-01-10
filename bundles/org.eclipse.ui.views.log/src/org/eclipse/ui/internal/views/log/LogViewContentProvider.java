/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583,207344
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class LogViewContentProvider implements ITreeContentProvider {
	private LogView logView;

	public LogViewContentProvider(LogView logView) {
		this.logView = logView;
	}


	@Override
	public Object[] getChildren(Object element) {
		return ((AbstractEntry) element).getChildren(element);
	}

	@Override
	public Object[] getElements(Object element) {
		return logView.getElements();
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof LogSession) {
			return null;
		}
		return ((AbstractEntry) element).getParent(element);
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((AbstractEntry) element).getChildren(element).length > 0;
	}

	public boolean isDeleted(Object element) {
		return false;
	}
}
