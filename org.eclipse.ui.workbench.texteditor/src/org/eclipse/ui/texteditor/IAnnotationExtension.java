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

package org.eclipse.ui.texteditor;


/**
 * Extension interface for <code>Annotation</code>. It adds the following
 * functions:
 * <ul>
 * <li> marker type
 * <li> annotation severity
 * <li> notion of temporary annotation
 * <li> an optional message
 * </ul>
 * <p>
 * Note: This is work in progress and can change anytime until API for 3.0 is frozen.
 * </p>
 * 
 * @see org.eclipse.core.resources.IMarker
 * @see org.eclipse.jface.text.source.Annotation
 * @since 3.0
 */
public interface IAnnotationExtension {

	/**
	 * Returns the marker type of the given annotation.
	 * 
	 * @return the marker type of the given annotation or <code>null</code> if it has none.
	 */
	String getMarkerType();
	
	/** 
	 * Severity marker attribute.  A number from the set of error, warning and info
	 * severities defined by the plaform.
	 * 
	 * XXX: This needs to be redefined when RCP work is being done.
	 *
	 * @see #SEVERITY_ERROR
	 * @see #SEVERITY_WARNING
	 * @see #SEVERITY_INFO
	 * @see #getAttribute
	 */
	int getSeverity();

	/**
	 * Returns whether the given annotation is temporary rather than persistent.
	 * 
	 * @return <code>true</code> if the annotation is temporary,
	 * 	<code>false</code> otherwise
	 */
	boolean isTemporary();

	/**
	 * Returns the message of this annotation.
	 * 
	 * @return the message of this annotation or <code>null</code> if none
	 */
	String getMessage();
}
