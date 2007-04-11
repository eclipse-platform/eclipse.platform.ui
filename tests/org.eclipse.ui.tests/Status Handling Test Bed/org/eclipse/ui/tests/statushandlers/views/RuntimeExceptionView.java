/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.statushandlers.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * A view throwing RuntimeException during control creation.
 */
public class RuntimeExceptionView extends ViewPart {

	public void createPartControl(Composite parent) {
		throw new RuntimeException(
				"A sample RuntimeException thrown during control creation.");
	}

	public void setFocus() {

	}

}
