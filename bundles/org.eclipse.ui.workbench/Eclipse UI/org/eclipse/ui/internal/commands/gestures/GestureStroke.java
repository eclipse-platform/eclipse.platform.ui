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

package org.eclipse.ui.internal.commands.gestures;

public class GestureStroke {
	
	public static GestureStroke create(Gesture gesture) {
		return new GestureStroke(gesture);
	}

	private Gesture gesture;
	
	private GestureStroke(Gesture gesture) {
		super();
		this.gesture = gesture;
	}

	public Gesture getGesture() {
		return gesture;
	}
}
