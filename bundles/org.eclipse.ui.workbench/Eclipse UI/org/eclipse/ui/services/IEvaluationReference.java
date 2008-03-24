/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.services;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.internal.services.IEvaluationResultCache;

/**
 * A token representing a core expression and property change listener currently
 * working in the <code>IEvaluationService</code>.
 * <p>
 * Note:This is not meant to be implemented or extended by clients.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team. This might disappear in 3.4 M5.
 * </p>
 * 
 * @since 3.4
 */
public interface IEvaluationReference extends IEvaluationResultCache {
	/**
	 * The property change listener associated with the evaluated expression.
	 * 
	 * @return the listener for updates.
	 */
	public IPropertyChangeListener getListener();

	/**
	 * The property used in change notifications.
	 * 
	 * @return the property name.
	 */
	public String getProperty();

}
