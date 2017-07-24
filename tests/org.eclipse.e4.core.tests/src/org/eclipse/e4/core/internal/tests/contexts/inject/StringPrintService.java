/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.internal.tests.contexts.inject;

/**
 *
 */
public class StringPrintService implements PrintService {
	private StringBuilder buf = new StringBuilder();

	@Override
	public void print(String message) {
		buf.append(message);
	}

	@Override
	public String toString() {
		return buf.toString();
	}

}
