/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;

/**
 * @since 3.1
 */
public class NullContributionManagerOverrides implements
		IContributionManagerOverrides {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManagerOverrides#getEnabled(org.eclipse.jface.action.IContributionItem)
	 */
	public Boolean getEnabled(IContributionItem item) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManagerOverrides#getAccelerator(org.eclipse.jface.action.IContributionItem)
	 */
	public Integer getAccelerator(IContributionItem item) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManagerOverrides#getAcceleratorText(org.eclipse.jface.action.IContributionItem)
	 */
	public String getAcceleratorText(IContributionItem item) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManagerOverrides#getText(org.eclipse.jface.action.IContributionItem)
	 */
	public String getText(IContributionItem item) {
		return null;
	}

}
