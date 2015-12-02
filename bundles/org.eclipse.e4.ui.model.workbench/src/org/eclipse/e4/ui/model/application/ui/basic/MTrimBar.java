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
package org.eclipse.e4.ui.model.application.ui.basic;

import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MGenericTrimContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Trim Bar</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is a concrete class representing the trim along a Window's edge.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar#getPendingCleanup <em>Pending Cleanup</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MTrimBar extends MGenericTrimContainer<MTrimElement>, MUIElement {

	/**
	 * Returns the value of the '<em><b>Pending Cleanup</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MTrimElement}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is for internal use...
	 * </p>
	 * @noreference
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Pending Cleanup</em>' reference list.
	 * @model transient="true"
	 * @generated
	 */
	List<MTrimElement> getPendingCleanup();
} // MTrimBar
