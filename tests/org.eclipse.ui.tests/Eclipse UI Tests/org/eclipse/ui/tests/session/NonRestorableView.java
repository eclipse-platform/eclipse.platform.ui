/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public void createPartControl(Composite parent) {}

	@Override
	public void setFocus() {	}
}
