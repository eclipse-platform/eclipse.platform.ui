/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;


/**
 * Filter that accepts everything. Available as a singleton since having
 * more than one instance would be wasteful.
 *
 * @since 3.1
 */
public final class AcceptAllFilter implements IFilter {

	/**
	 * Returns the singleton instance of AcceptAllFilter
	 *
	 * @return the singleton instance of AcceptAllFilter
	 */
	public static IFilter getInstance() {
		return singleton;
	}

	/**
	 * The singleton instance
	 */
	private static IFilter singleton = new AcceptAllFilter();

	@Override
	public boolean select(Object toTest) {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		return other == this || other instanceof AcceptAllFilter;
	}

}
