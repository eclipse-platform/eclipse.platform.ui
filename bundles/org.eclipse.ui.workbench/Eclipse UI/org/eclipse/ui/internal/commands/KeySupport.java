/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;

public final class KeySupport {

	private final static String STROKE_SEPARATOR = " "; //$NON-NLS-1$
	
	public static String formatSequence(Sequence sequence)
		throws IllegalArgumentException {
		if (sequence == null)
			throw new IllegalArgumentException();
			
		int i = 0;
		Iterator iterator = sequence.getStrokes().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(STROKE_SEPARATOR);

			stringBuffer.append(formatStroke((Stroke) iterator.next()));
			i++;
		}

		return stringBuffer.toString();
	}

	public static String formatStroke(Stroke stroke) {
		return Action.convertAccelerator(stroke.getValue());
	}
	
	public static Sequence parseSequence(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();

		List strokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string);
				
		while (stringTokenizer.hasMoreTokens())
			strokes.add(parseStroke(stringTokenizer.nextToken()));
			
		return Sequence.create(strokes);
	}	
	
	public static Stroke parseStroke(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();
		
		int value = Action.convertAccelerator(string);
		
		//TODO uncomment
		//if (value == 0)
		//	throw new IllegalArgumentException();
			
		return Stroke.create(value);
	}

	private KeySupport() {
		super();
	}
}
