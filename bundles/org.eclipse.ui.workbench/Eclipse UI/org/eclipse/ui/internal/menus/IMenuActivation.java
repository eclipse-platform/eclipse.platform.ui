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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.internal.services.IEvaluationResultCache;

/**
 * @since 3.3
 *
 */
public interface IMenuActivation extends IEvaluationResultCache {
	/**
	 * @return the IContributionItem for the cache.
	 */
	public IContributionItem getContribution();
}
