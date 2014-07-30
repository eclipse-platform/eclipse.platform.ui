/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.launching;

/**
 * Used by {@link ArgumentParsingTests}.
 */
public class ArgumentsPrinter {
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			System.out.println(arg);
		}
	}
}
