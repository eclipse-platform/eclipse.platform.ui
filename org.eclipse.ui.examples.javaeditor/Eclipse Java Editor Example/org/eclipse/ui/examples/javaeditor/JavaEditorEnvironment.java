/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.javaeditor;


import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.ui.examples.javaeditor.java.JavaCodeScanner;
import org.eclipse.ui.examples.javaeditor.javadoc.JavaDocScanner;
import org.eclipse.ui.examples.javaeditor.util.JavaColorProvider;

/** The JavaEditorEnvironment maintains singletons used by the java editor
 * examples.
 */
public class JavaEditorEnvironment {

	private static JavaColorProvider fgColorProvider;
	private static JavaCodeScanner fgCodeScanner;
	private static JavaDocScanner fgDocScanner;

	private static int fgRefCount= 0;

	/**
	 * A connection has occured - initialize the receiver if it is the first activation.
	 */
	public static void connect(Object client) {
		if (++fgRefCount == 1) {
			fgColorProvider= new JavaColorProvider();
			fgCodeScanner= new JavaCodeScanner(fgColorProvider);
			fgDocScanner= new JavaDocScanner(fgColorProvider);
		}
	}
	
	/**
	 * A disconnection has occured - clear the receiver if it is the last deactivation.
	 */
	 public static void disconnect(Object client) {
		if (--fgRefCount == 0) {
			fgCodeScanner= null;
			fgDocScanner= null;
			fgColorProvider.dispose();
			fgColorProvider= null;
		}
	}
	
	/**
	 * Returns the singleton scanner.
	 */
	 public static RuleBasedScanner getJavaCodeScanner() {
		return fgCodeScanner;
	}
	
	/**
	 * Returns the singleton color provider.
	 */
	 public static JavaColorProvider getJavaColorProvider() {
		return fgColorProvider;
	}
	
	/**
	 * Returns the singleton document scanner.
	 */
	 public static RuleBasedScanner getJavaDocScanner() {
		return fgDocScanner;
	}
}
