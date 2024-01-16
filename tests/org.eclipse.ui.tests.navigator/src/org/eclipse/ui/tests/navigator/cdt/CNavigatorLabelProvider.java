/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.cdt;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * A label provider suitable for the Common Navigator providing also status
 * message text for the current selected item.
 * <p>
 * See {@code org.eclipse.cdt.internal.ui.cview.CView#createLabelProvider}<br>
 * See {@code org.eclipse.cdt.internal.ui.cview.CView#getStatusLineMessage}
 */
public class CNavigatorLabelProvider extends LabelProvider implements
		ICommonLabelProvider {

	public CNavigatorLabelProvider() {
	}

	@Override
	public void init(ICommonContentExtensionSite extensionSite) {
	}

	@Override
	public void restoreState(IMemento memento) {
	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public String getText(Object element) {
		return getDescription(element);
	}

	@Override
	public String getDescription(Object element) {
		String desc = "<notfound>";
		if (element instanceof IResource) {
			desc = ((IResource) element).getFullPath().makeRelative()
					.toString();
		} else if (element instanceof CElement) {
			CElement celement = (CElement) element;
			desc = celement.getElementName();
		} else if (element instanceof IWorkbenchAdapter) {
			IWorkbenchAdapter wAdapter = (IWorkbenchAdapter) element;
			desc = wAdapter.getLabel(element);
		}
		// Show that it came from this label provider
		return "CL: " + desc;
	}

}
