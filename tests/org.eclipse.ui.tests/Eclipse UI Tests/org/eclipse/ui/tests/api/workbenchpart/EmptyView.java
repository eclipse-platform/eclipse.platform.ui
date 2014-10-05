/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 3.0
 */
public class EmptyView extends ViewPart {

    /**
     * 
     */
    public EmptyView() {
        super();
    }

    @Override
	public void createPartControl(Composite parent) {

    }

    @Override
	public void setFocus() {

    }

    @Override
	public void setContentDescription(String description) {
        super.setContentDescription(description);
    }

    @Override
	public void setPartName(String partName) {
        super.setPartName(partName);
    }

    @Override
	public void setTitle(String title) {
        super.setTitle(title);
    }
}
