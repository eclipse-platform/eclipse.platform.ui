/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 * Annotation managed by an
 * {@link org.eclipse.jface.text.source.IAnnotationModel}.
 * <p>
 * Annotations are typed, can have an associated text and can be marked as persistent and
 * deleted. Annotations which are not explicitly initialized with an annotation
 * type are of type <code>"org.eclipse.text.annotation.unknown"</code>.
 */
public class Annotation {

	/**
	 * Constant for unknown annotation types.<p>
	 * Value: <code>"org.eclipse.text.annotation.unknown"</code>
	 * @since 3.0
	 */
	public final static String TYPE_UNKNOWN= "org.eclipse.text.annotation.unknown";  //$NON-NLS-1$


	/**
	 * The type of this annotation.
	 * @since 3.0
	 */
	private String fType;
	/**
	 * Indicates whether this annotation is persistent or not.
	 * @since 3.0
	 */
	private boolean fIsPersistent= false;
	/**
	 * Indicates whether this annotation is marked as deleted or not.
	 * @since 3.0
	 */
	private boolean fMarkedAsDeleted= false;
	/**
	 * The text associated with this annotation.
	 * @since 3.0
	 */
	private String fText;


	/**
	 * Creates a new annotation that is not persistent and type less.
	 */
	protected Annotation() {
		this(null, false, null);
	}

	/**
	 * Creates a new annotation with the given properties.
	 *
	 * @param type the unique name of this annotation type
	 * @param isPersistent <code>true</code> if this annotation is
	 *            persistent, <code>false</code> otherwise
	 * @param text the text associated with this annotation
	 * @since 3.0
	 */
	public Annotation(String type, boolean isPersistent, String text) {
		fType= type;
		fIsPersistent= isPersistent;
		fText= text;
	}

	/**
	 * Creates a new annotation with the given persistence state.
	 *
	 * @param isPersistent <code>true</code> if persistent, <code>false</code> otherwise
	 * @since 3.0
	 */
	public Annotation(boolean isPersistent) {
		this(null, isPersistent, null);
	}

	/**
	 * Returns whether this annotation is persistent.
	 *
	 * @return <code>true</code> if this annotation is persistent, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	public boolean isPersistent() {
		return fIsPersistent;
	}

	/**
	 * Sets the type of this annotation.
	 *
	 * @param type the annotation type
	 * @since 3.0
	 */
	public void setType(String type) {
		fType= type;
	}

	/**
	 * Returns the type of the annotation.
	 *
	 * @return the type of the annotation
	 * @since 3.0
	 */
	public String getType() {
		return fType == null ? TYPE_UNKNOWN : fType;
	}

	/**
	 * Marks this annotation deleted according to the value of the
	 * <code>deleted</code> parameter.
	 *
	 * @param deleted <code>true</code> if annotation should be marked as deleted
	 * @since 3.0
	 */
	public void markDeleted(boolean deleted) {
		fMarkedAsDeleted= deleted;
	}

	/**
	 * Returns whether this annotation is marked as deleted.
	 *
	 * @return <code>true</code> if annotation is marked as deleted, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	public boolean isMarkedDeleted() {
		return fMarkedAsDeleted;
	}

	/**
	 * Sets the text associated with this annotation.
	 *
	 * @param text the text associated with this annotation
	 * @since 3.0
	 */
	public void setText(String text) {
		fText= text;
	}

	/**
	 * Returns the text associated with this annotation.
	 *
	 * @return the text associated with this annotation or <code>null</code>
	 * @since 3.0
	 */
	public String getText() {
		return fText;
	}
}
