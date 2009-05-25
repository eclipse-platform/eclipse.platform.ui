/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Versant Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class NonRestorableView extends ViewPart {
	public static final String ID ="org.eclipse.ui.tests.session.NonRestorableView";

	public NonRestorableView() {	}

	public void createPartControl(Composite parent) {}

	public void setFocus() {	}
}
