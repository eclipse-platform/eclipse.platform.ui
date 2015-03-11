/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Trimmed Window</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * A subclass of Window that also supports TrimBars on its edges.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow#getTrimBars <em>Trim Bars</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MTrimmedWindow extends MWindow {
	/**
	 * Returns the value of the '<em><b>Trim Bars</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The collection of TrimBars associated with this window.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Trim Bars</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MTrimBar> getTrimBars();

} // MTrimmedWindow
