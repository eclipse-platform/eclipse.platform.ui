/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

/**
 * An annotation access provides access to information that is not available via
 * the API of {@link org.eclipse.jface.text.source.Annotation}. With version
 * 3.0 all this information is now available from the annotation itself.
 * <p>
 *
 * In order to provide backward compatibility for clients of
 * <code>IAnnotationAccess</code>, extension interfaces are used as a means
 * of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.source.IAnnotationAccessExtension} since
 *     version 3.0 replacing all methods in that interface</li>
 * <li>{@link IAnnotationAccessExtension2} since
 *     version 3.2 allowing to set a quick assist assistant to an annotation access.</li>
 * </ul></p>
 * <p>
 * Clients usually implement this interface and its extension interfaces.</p>
 *
 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension
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
