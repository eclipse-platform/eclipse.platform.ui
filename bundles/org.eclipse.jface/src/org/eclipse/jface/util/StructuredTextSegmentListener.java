/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.bidi.StructuredTextTypeHandlerFactory;
import org.eclipse.equinox.bidi.advanced.IStructuredTextExpert;
import org.eclipse.equinox.bidi.advanced.StructuredTextEnvironment;
import org.eclipse.equinox.bidi.advanced.StructuredTextExpertFactory;
import org.eclipse.equinox.bidi.custom.StructuredTextTypeHandler;
import org.eclipse.swt.events.SegmentEvent;
import org.eclipse.swt.events.SegmentListener;

/**
 * Segment listener that implements bidi-structured text reordering.
 * The reordering is specified by the structured text type that is passed to the constructor.
 *
 * <p>
 * <strong>Note:</strong> This class only works if the <code>org.eclipse.equinox.bidi</code>
 * bundle is on the classpath!
 * </p>
 *
 * @since 3.9
 * @noextend This class is not intended to be subclassed by clients.
 */
public class StructuredTextSegmentListener implements SegmentListener {

	private final IStructuredTextExpert expert;
	private boolean logExceptions = true;

	/**
	 * Creates a new structured text segment listener.
	 *
	 * @param textType
	 *            the structured text type. Possible values are the structured
	 *            text type ids supported by
	 *            {@link StructuredTextTypeHandlerFactory#getHandler(String)}.
	 * @throws IllegalArgumentException
	 *             if <code>textType</code> is not a known type identifier
	 */
	public StructuredTextSegmentListener(String textType) {
		expert = StructuredTextExpertFactory.getExpert(textType);
	}

	/**
	 * Creates a new structured text segment listener.
	 *
	 * @param textTypeHandler the structured text type handler
	 * @throws IllegalArgumentException if the <code>handler</code> is <code>null</code>
	 */
	public StructuredTextSegmentListener(StructuredTextTypeHandler textTypeHandler) {
		expert = StructuredTextExpertFactory.getStatefulExpert(textTypeHandler, StructuredTextEnvironment.DEFAULT);
	}

	@Override
	public void getSegments(SegmentEvent event) {
		int length = event.lineText.length();
		if (length > 0) {
			try {
				int segments[] = expert.leanBidiCharOffsets(event.lineText);
				event.segments = new int[segments.length + 2];
				event.segments[0] = 0;
				System.arraycopy(segments, 0, event.segments, 1, segments.length);
				event.segments[segments.length + 1] = length;
				event.segmentsChars = new char[event.segments.length];
				event.segmentsChars[0] = BidiUtils.LRE;
				for (int i = 1; i < event.segments.length - 1; i++)
					event.segmentsChars[i] = BidiUtils.LRM;
				event.segmentsChars[event.segments.length - 1] = BidiUtils.PDF;
			} catch (RuntimeException ex) {
				// Only log the first exception. Logging every exception would make the system unusable.
				if (logExceptions) {
					Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE,
							"An error occurred while processing \"" + event.lineText + "\" with " + expert, ex)); //$NON-NLS-1$//$NON-NLS-2$
					logExceptions = false;
				}
			}
		}
	}
}