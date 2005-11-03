/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable;

import org.eclipse.swt.widgets.Control;

/**
 * Interface IRowConstructionListener.  An interface for objects that need to listen to row object
 * construction events.
 * 
 * @author djo
 */
public interface IRowConstructionListener {
	/**
	 * Method rowConstructed.  Called when the CompositeTable creates a new row object.  
	 * CompositeTable only creates a new row object when it needs on in order to fill vacant
	 * space.  During its life cycle, it never disposes a row object, but rather caches
	 * unused row objects for later reuse if needed.  All row objects are disposed when the
	 * CompositeTable itself is disposed.
	 * 
	 * @param newRow The new row object that was just constructed.
	 */
	public void rowConstructed(Control newRow);
}
