/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Control;

/**
 * The ProgressViewerLabelProvider is the label provider for progress viewers.
 */
public class ProgressViewerLabelProvider extends LabelProvider {
	private Control control;

	@Override
	public String getText(Object element) {
		JobTreeElement info = (JobTreeElement) element;
		return ProgressManagerUtil.shortenText(
				info.getCondensedDisplayString(), control);
	}

	/**
	 * Create a new instance of the receiver within the control.
	 *
	 * @param progressControl The control that the label is
	 * being created for.
	 */
	public ProgressViewerLabelProvider(Control progressControl) {
		super();
		control = progressControl;
	}
}
