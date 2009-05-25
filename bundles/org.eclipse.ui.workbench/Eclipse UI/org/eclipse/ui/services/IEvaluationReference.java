/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
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
