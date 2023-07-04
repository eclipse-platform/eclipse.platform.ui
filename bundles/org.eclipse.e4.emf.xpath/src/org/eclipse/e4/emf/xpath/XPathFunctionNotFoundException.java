/*******************************************************************************
 * Copyright (c) 2025, 2025 Hannes Wellmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hannes Wellmann - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.emf.xpath;

/**
 * Thrown when an unknown extension function is encountered.
 *
 * @since 0.6
 */
public class XPathFunctionNotFoundException extends IllegalArgumentException {
	// TODO: revisit

	private static final long serialVersionUID = -6825059451789853064L;

	public XPathFunctionNotFoundException(String message) {
		super(message);
	}

}
