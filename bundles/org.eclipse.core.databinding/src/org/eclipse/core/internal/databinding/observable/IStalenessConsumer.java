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

package org.eclipse.core.internal.databinding.observable;

/**
 * @since 1.0
 * 
 */
public interface IStalenessConsumer {
	/**
	 * @param stale
	 * 
	 */
	public void setStale(boolean stale);
}
