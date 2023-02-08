/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.advanced.impl;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.impl.GenericStackImpl;
import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Perspective Stack</b></em>'.
 * <!-- end-user-doc -->
 *
 * @since 1.0
 * @generated
 */
public class PerspectiveStackImpl extends GenericStackImpl<MPerspective> implements MPerspectiveStack {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PerspectiveStackImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AdvancedPackageImpl.Literals.PERSPECTIVE_STACK;
	}

} //PerspectiveStackImpl
