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

package org.eclipse.ui.internal.commands.util.old;

import java.util.List;

public class Util extends org.eclipse.ui.internal.util.Util {

	public static boolean validateSequence(Sequence sequence) {
		List strokes = sequence.getStrokes();
		int size = strokes.size();
			
		if (size == 0)
			return false;
		else 
			for (int i = 0; i < size; i++) {
				Stroke stroke = (Stroke) strokes.get(i);	
	
				if (!validateStroke(stroke))
					return false;
			}
			
		return true;
	}

	public static boolean validateStroke(Stroke stroke) {
		return stroke.getValue() != 0;
	}

	private Util() {
		super();
	}
}
