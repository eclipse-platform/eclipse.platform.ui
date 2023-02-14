/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty;

/**
 * Options for working with empty values in filters. <br />
 * An empty value is a null object, empty string, or empty collection.
 *
 * @author Steven Spungin
 *
 */
public enum EmptyFilterOption {
	/**
	 * Do not include empty values
	 */
	EXCLUDE,
	/**
	 * Include empty values
	 */
	INCLUDE,
	/**
	 * Include only empty values
	 */
	ONLY
}