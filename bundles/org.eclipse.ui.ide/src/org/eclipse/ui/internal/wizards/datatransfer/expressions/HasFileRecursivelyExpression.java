/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer.expressions;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.wizards.datatransfer.RecursiveFileFinder;

/**
 * Expression to check whether a given container contains a file with provided
 * name (with whatever depth level).
 *
 * @since 3.12
 */
public class HasFileRecursivelyExpression extends Expression {

	/**
	 * The name of the XML tag to use this rule in a plugin.xml
	 */
	public static final String TAG = "hasFileRecursively"; //$NON-NLS-1$

	private String filename;

	/**
	 * Build expression with a filename.
	 *
	 * @param filename
	 *            Name of the file to look for (recursively) under given
	 *            container.
	 */
	public HasFileRecursivelyExpression(String filename) {
		this.filename = filename;
	}

	/**
	 * Build expression retrieving the suffix as the 'filename' attribute on the
	 * provided {@link IConfigurationElement}.
	 *
	 * @param element
	 */
	public HasFileRecursivelyExpression(IConfigurationElement element) {
		this(element.getAttribute("filename")); //$NON-NLS-1$
	}

	@Override
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object root = context.getDefaultVariable();
		IContainer container = null;
		if (root instanceof IContainer) {
			container = (IContainer)root;
		} else if (root instanceof IAdaptable) {
			container = ((IAdaptable)root).getAdapter(IContainer.class);
		}
		if (container != null) {
			RecursiveFileFinder finder = new RecursiveFileFinder(this.filename, null);
			container.accept(finder);
			return EvaluationResult.valueOf(!finder.getFiles().isEmpty());
		} else if (root instanceof File && ((File)root).isDirectory()) {
			LinkedList<File> directoriesToVisit = new LinkedList<>();
			directoriesToVisit.add((File)root);
			while (!directoriesToVisit.isEmpty()) {
				File currentDirectory = directoriesToVisit.pop();
				if (new File(currentDirectory, this.filename).exists()) {
					return EvaluationResult.TRUE;
				}
				File[] files = currentDirectory.listFiles(new FileFilter() {
					@Override
					public boolean accept(File child) {
						return child.isDirectory();
					}
				});
				if (files != null) {
					directoriesToVisit.addAll(Arrays.asList(files));
				}
			}
		}
		return EvaluationResult.FALSE;
	}

}
