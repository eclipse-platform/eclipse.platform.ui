/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

/**
 * An annotation access provides access to information that is not available via the
 * API of <code>Annotation</code>. Clients usually implement this interface. 
 * 
 * @see org.eclipse.jface.text.source.Annotation
 * @since 2.1
 */
public interface IAnnotationAccess {

	/**
	 * Returns the type of the given annotation.
	 * 
	 * @param annotation the annotation
	 * @return the type of the given annotation or <code>null</code> if it has none.
	 * @deprecated use <code>Annotation.getType()</code>
	 */
	Object getType(Annotation annotation);

	/**
	 * Returns whether the given annotation spans multiple lines.
	 * 
	 * @param annotation the annotation
	 * @return <code>true</code> if the annotation spans multiple lines,
	 * 	<code>false</code> otherwise
	 * 
	 * @deprecated assumed to always return <code>true</code>
	 */
	boolean isMultiLine(Annotation annotation);
	
	/**
	 * Returns whether the given annotation is temporary rather than persistent.
	 * 
	 * @param annotation the annotation
	 * @return <code>true</code> if the annotation is temporary,
	 * 	<code>false</code> otherwise
	 * @deprecated use <code>Annotation.isPersistent()</code>
	 */
	boolean isTemporary(Annotation annotation);
}
