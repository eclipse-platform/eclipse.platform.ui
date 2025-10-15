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
package org.eclipse.e4.ui.model.application;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Addon</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * An MAddon represents a self-contained application logic. Addons may be used
 * to augment the UI in a variety of ways without requiring that the base application
 * be aware of the extensions.
 * </p><p>
 * Addons are expected to be capable of being removed without damage to the
 * original UI. While not yet implemented there will be an uninstall protocol defined
 * in the future allowing an addon to remove any model elements specific to the
 * addon (i.e. The MinMaxAddon's TrimElements.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 *
 * @model
 * @generated
 */
public interface MAddon extends MContribution {
} // MAddon
