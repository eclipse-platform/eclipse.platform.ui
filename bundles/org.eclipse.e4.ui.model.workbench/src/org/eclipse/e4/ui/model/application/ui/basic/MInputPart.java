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
package org.eclipse.e4.ui.model.application.ui.basic;

import org.eclipse.e4.ui.model.application.ui.MInput;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is a subclass of Part that allows tracking of an 'input'. While originally defined as
 * a type of 'editor' it turns out that it may well be better to just use a regular Part and
 * to store what would be the input as an entry on the Part's 'persistentData' map.
 * </p>
 * @since 1.0
 * @deprecated Use Part instead.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noreference This interface is not intended to be referenced by clients.
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug 509868</a>
 * <!-- end-model-doc -->
 *
 *
 * @model
 * @generated
 */
@Deprecated
public interface MInputPart extends MPart, MInput {
} // MInputPart
