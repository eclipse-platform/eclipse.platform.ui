/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.swt.events.SegmentListener;
import org.eclipse.swt.events.SegmentEvent;
import org.eclipse.equinox.bidi.advanced.ISTextExpert;
import org.eclipse.equinox.bidi.advanced.STextExpertFactory;

/**
 * This class defines the segment listener in order to enforce Structured Text (STT) support
 * 
 * @since 3.9
 */
public final class StructuredTextSegmentListener implements SegmentListener {

	private String textType;

	/**
	 *  Creates a new segment listener instance in order to enforce Structured Text (STT) support.
	 * 
	 * @param sttType the type for the structured Text 
	 * Possible values are:
	 * <ul>
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#EMAIL}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#FILE}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#JAVA}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#REGEXP}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#COMMA_DELIMITED}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#PROPERTY}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#RTL_ARITHMETIC}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#SQL}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#UNDERSCORE}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#XPATH}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#SYSTEM_USER}
	 * <li> {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#URL}
	 * </ul>      
	 */ 
	protected StructuredTextSegmentListener(String sttType) {
		super();
		textType = sttType;
	}

	public void getSegments(SegmentEvent event) {
		int length = event.lineText.length();
		int segments[];
		if (length > 0) {
			try {
				ISTextExpert expert = STextExpertFactory.getExpert(textType);
				segments = expert.leanBidiCharOffsets(event.lineText);
				event.segments = new int[segments.length + 2];
				event.segments[0] = 0;
				System.arraycopy(segments, 0, event.segments, 1,
						segments.length);
				event.segments[segments.length + 1] = length;
				event.segmentsChars = new char[event.segments.length];
				event.segmentsChars[0] = BidiUtils.LRE;
				for (int i = 1; i < event.segments.length - 1; i++)
					event.segmentsChars[i] = BidiUtils.LRM;
				event.segmentsChars[event.segments.length - 1] = BidiUtils.PDF;
			} catch (Exception ex) {
				// Ignore for now
			}
		}
	}
}