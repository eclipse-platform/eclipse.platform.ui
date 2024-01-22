/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

	@Override
	protected Control createContents(Composite parent) {
		return new Composite(parent, SWT.NULL);
	}
}
