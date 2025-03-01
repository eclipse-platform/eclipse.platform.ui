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
 * An exception indicating that the value for a requested XPath could not be
 * found.
 *
 * @since 0.6
 */
@Deprecated(forRemoval = true, since = "2025-03 (removal in 2027-03 or later)")
public class XPathNotFoundException extends IllegalArgumentException {
	private static final long serialVersionUID = -4174244860692153739L;

	public XPathNotFoundException(String message) {
		super(message);
	}

}
