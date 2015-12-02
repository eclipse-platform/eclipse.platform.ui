/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    Image image;

    @Override
	public Image getImage(Object element) {
        return ((JobTreeElement) element).getDisplayImage();
    }

    @Override
	public String getText(Object element) {
        return ((JobTreeElement) element).getDisplayString();
    }

}
