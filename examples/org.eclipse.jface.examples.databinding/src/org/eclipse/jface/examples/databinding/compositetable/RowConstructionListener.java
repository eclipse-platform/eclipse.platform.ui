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
 * Class IRowConstructionListener. An "interface" for objects that need to
 * listen to row object construction events.
 * 
 * @author djo
 */
public abstract class RowConstructionListener {
	/**
	 * Method rowConstructed. Called when the CompositeTable creates a new row
	 * object. CompositeTable only creates a new row object when it needs on in
	 * order to fill vacant space. During its life cycle, it never disposes a
	 * row object, but rather caches unused row objects for later reuse if
	 * needed. All row objects are disposed when the CompositeTable itself is
	 * disposed.
	 * 
	 * @param newRow
	 *            The new row object that was just constructed.
	 */
	public abstract void rowConstructed(Control newRow);

	/**
	 * Method headerConstructed. Called when the CompositeTable creates a new
	 * header object. CompositeTable only creates a new header when the runTime
	 * property is set to true. This method permits clients to modify the header
	 * control that was created from the prototype header control as needed.
	 * 
	 * @param newHeader
	 *            The new header object that was just constructed.
	 */
	public abstract void headerConstructed(Control newHeader);
}
