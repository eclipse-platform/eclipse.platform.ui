/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/*
 * A page used as a filler for nodes in the property page dialog
 * for which no page is suppplied.
 */
public class EmptyPropertyPage extends PropertyPage {
    /**
     * Creates empty composite for this page content.
     */

    protected Control createContents(Composite parent) {
        return new Composite(parent, SWT.NULL);
    }
}
