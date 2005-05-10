/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.internal;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An intentionally thrown exception for use in testing error handling code.
 *
 * @since 3.1
 */
public class ForcedException extends RuntimeException {

	 private static final long serialVersionUID= 1L;

	 /**
	  * Creates a <code>ForcedException</code> with the given message.
	  *  
	  * @param message the message
	  */
	 public ForcedException(String message) {
		 super(message);
	 }
	 
	 public void printStackTrace(PrintStream s) {
	 		 s.println("!FORCED BY TEST: this entry is intentional: " + getMessage());
	 }
	 		 		 
	 public void printStackTrace(PrintWriter s) {
	 		 s.println("!FORCED BY TEST: this entry is intentional:" + getMessage());
	 }
}

