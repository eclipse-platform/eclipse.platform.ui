/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * The ProgressLabelProvider is a label provider used for viewers
 * that need anILabelprovider to show JobInfos.
 */
public class ProgressLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		return ((JobTreeElement) element).getDisplayImage();
	}

	@Override
	public String getText(Object element) {
		return ((JobTreeElement) element).getDisplayString();
	}

}
