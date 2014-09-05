/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multieditor;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.ui.part.EditorActionBarContributor;

public class TestActionBarContributor extends EditorActionBarContributor {

	public TestActionBarContributor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToCoolBar(org.eclipse.jface.action.ICoolBarManager)
	 */
	@Override
	public void contributeToCoolBar(ICoolBarManager coolBarManager) {
		super.contributeToCoolBar(coolBarManager);
	}
}
