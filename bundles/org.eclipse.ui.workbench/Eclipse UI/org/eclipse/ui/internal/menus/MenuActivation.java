/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.internal.services.EvaluationResultCache;

/**
 * @since 3.3
 * 
 */
public class MenuActivation extends EvaluationResultCache implements
		IMenuActivation {

	private IContributionItem fItem;

	/**
	 * @param item
	 *            this contribution ite
	 * @param visibleWhen
	 *            when it's visible
	 * @param auth
	 *            the menu authority responsible for this cache
	 */
	public MenuActivation(IContributionItem item, Expression visibleWhen) {
		super(visibleWhen);
		fItem = item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuActivation#getContribution()
	 */
	public IContributionItem getContribution() {
		return fItem;
	}
}
