/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.Position;

/**
 * Internal class. Do not use. Only public for testing purposes.
 * 
 * @since 3.0
 */
public class Segment extends Position {
	
	public Fragment fragment;
	public boolean isMarkedForStretch;
	public boolean isMarkedForShift;
	
	public Segment(int offset, int length) {
		super(offset, length);
	}
	
	public void markForStretch() {
		isMarkedForStretch= true;
	}
	
	public boolean isMarkedForStretch() {
		return isMarkedForStretch;
	}
	
	public void markForShift() {
		isMarkedForShift= true;
	}
	
	public boolean isMarkedForShift() {
		return isMarkedForShift;
	}

	public void clearMark() {
		isMarkedForStretch= false;
		isMarkedForShift= false;
	}
}