/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.javaeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;

import org.eclipse.jface.text.rules.RuleBasedScanner;

import org.eclipse.ui.examples.javaeditor.java.JavaCodeScanner;
import org.eclipse.ui.examples.javaeditor.javadoc.JavaDocScanner;
import org.eclipse.ui.examples.javaeditor.util.JavaColorProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The example java editor plugin class.
 * 
 * @since 3.0
 */
public class JavaEditorExamplePlugin extends AbstractUIPlugin {
	
	public final static String JAVA_PARTITIONING= "__java_example_partitioning";   //$NON-NLS-1$
	
	private static JavaEditorExamplePlugin fgInstance;
	private JavaPartitionScanner fPartitionScanner;
	private JavaColorProvider fColorProvider;
	private JavaCodeScanner fCodeScanner;
	private JavaDocScanner fDocScanner;

	/**
	 * Creates a new plugin instance.
	 * 
	 * @param descriptor
	 */
	public JavaEditorExamplePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgInstance= this;
	}
	
	/**
	 * Returns the default plugin instance.
	 * 
	 * @return the default plugin instance
	 */
	public static JavaEditorExamplePlugin getDefault() {
		return fgInstance;
	}
	
	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
	}
	
	/**
	 * Return a scanner for creating java partitions.
	 */
	 public JavaPartitionScanner getJavaPartitionScanner() {
		if (fPartitionScanner == null)
			fPartitionScanner= new JavaPartitionScanner();
		return fPartitionScanner;
	}
	
	/**
	 * Returns the singleton scanner.
	 */
	 public RuleBasedScanner getJavaCodeScanner() {
	 	if (fCodeScanner == null)
			fCodeScanner= new JavaCodeScanner(getJavaColorProvider());
		return fCodeScanner;
	}
	
	/**
	 * Returns the singleton color provider.
	 */
	 public JavaColorProvider getJavaColorProvider() {
	 	if (fColorProvider == null)
			fColorProvider= new JavaColorProvider();
		return fColorProvider;
	}
	
	/**
	 * Returns the singleton document scanner.
	 */
	 public RuleBasedScanner getJavaDocScanner() {
	 	if (fDocScanner == null)
			fDocScanner= new JavaDocScanner(fColorProvider);
		return fDocScanner;
	}
}
