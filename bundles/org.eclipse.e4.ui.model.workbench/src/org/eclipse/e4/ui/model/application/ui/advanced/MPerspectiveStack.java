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
package org.eclipse.e4.ui.model.application.ui.advanced;

import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Perspective Stack</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * The PerspectiveStack is a collectin of Perspectives. Only one perspective may be
 * visible at a time and is determined by the container's 'selectedElement'.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 *
 * @model
 * @generated
 */
public interface MPerspectiveStack extends MGenericStack<MPerspective>, MPartSashContainerElement, MWindowElement {
} // MPerspectiveStack
