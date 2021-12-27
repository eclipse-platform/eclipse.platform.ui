/*******************************************************************************
 * Copyright (c) 2022 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.databinding.bind.steps;

/**
 * Classes marked with this interface are steps in a fluent API chain. The
 * interfaces that extend this define the API for the fluent databinding
 * builders.
 * <p>
 * Client rules for the using classes that implement this interface:
 * <ul>
 * <li>Only one single method call should ever be made on a step object. It is
 * not allowed to keep a reference to a step and make multiple method calls to
 * it.
 * <li>Step objects must only be accessed from the thread on which they where
 * created.
 * </ul>
 *
 * @noimplement
 * @since 1.11
 */
public interface Step {
}
