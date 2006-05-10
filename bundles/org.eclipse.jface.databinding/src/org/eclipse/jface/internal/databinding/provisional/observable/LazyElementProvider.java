/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.observable;

/**
 * An API used by ILazyDataRequestors to retrieve elements inside their lists.
 * 
 * @since 3.3
 */
public abstract class LazyElementProvider {
	
	/**
	 * @param position
	 * @return
	 */
	abstract public Object getElement(int position);
	
}
