package org.eclipse.ui.examples.javaeditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
