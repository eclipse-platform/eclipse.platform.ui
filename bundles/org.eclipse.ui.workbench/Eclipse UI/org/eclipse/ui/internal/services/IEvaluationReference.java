/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * This is not meant to be implemented or extended by clients.
 * @since 3.3
 * 
 */
public interface IEvaluationReference extends IEvaluationResultCache {
	public IPropertyChangeListener getListener();
	public String getProperty();
}
