/*******************************************************************************
 * Copyright (c) 2008 Aleksandra Wozniak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aleksandra Wozniak (aleksandra.k.wozniak@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import org.eclipse.jface.viewers.ISelection;

/**
 * The "Compare with other resource" action
 * 
 * @since 3.4
 */
public class CompareWithOtherResourceAction extends CompareAction {

	public void run(ISelection selection) {
		// Show CompareWithOtherResourceDialog which return resources to compare
		// and ancestor if specified. Don't need to display the other dialog
		showSelectAncestorDialog = false;
		super.run(selection);
	}

	protected boolean isEnabled(ISelection selection) {
		// ignore returned value
		super.isEnabled(selection);
		return true;
	}

}
