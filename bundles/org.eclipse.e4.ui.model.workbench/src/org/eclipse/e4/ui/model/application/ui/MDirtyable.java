/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dirtyable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This class should be mixed into any UI element that should participate in the
 * dirty / save handling. Parts are the most likely scenario for this but it exists as
 * a mix-in to allow for future model extensions.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MDirtyable#isDirty <em>Dirty</em>}</li>
 * </ul>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MDirtyable {
	/**
	 * Returns the value of the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The current dirty state of the UI element.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dirty</em>' attribute.
	 * @see #setDirty(boolean)
	 * @model transient="true" derived="true"
	 * @generated
	 */
	boolean isDirty();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MDirtyable#isDirty <em>Dirty</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dirty</em>' attribute.
	 * @see #isDirty()
	 * @generated
	 */
	void setDirty(boolean value);

} // MDirtyable
