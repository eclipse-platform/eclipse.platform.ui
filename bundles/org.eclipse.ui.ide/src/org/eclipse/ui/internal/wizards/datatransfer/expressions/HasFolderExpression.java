/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat Inc., and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer.expressions;

import java.io.File;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;

/**
 * Expression to check whether a given container contains a folder under the
 * provided path.
 *
 * @since 3.12
 */
public class HasFolderExpression extends Expression {

	/**
	 * The name of the XML tag to use this rule in a plugin.xml
	 */
	public static final String TAG = "hasFolder"; //$NON-NLS-1$

	String path;

	/**
	 * Build expression with a path.
	 *
	 * @param path
	 *            Path of a folder to look for under given container.
	 */
	public HasFolderExpression(String path) {
		this.path = path;
	}

	/**
	 * Build expression retrieving the suffix as the 'path' attribute on the
	 * provided {@link IConfigurationElement}.
	 *
	 * @param element
	 */
	public HasFolderExpression(IConfigurationElement element) {
		this(element.getAttribute("path")); //$NON-NLS-1$
	}

	@Override
	public EvaluationResult evaluate(IEvaluationContext context) {
		Object root = context.getDefaultVariable();
		if (root instanceof File) {
			return EvaluationResult.valueOf( new File((File)root, this.path).isDirectory() );
		}
		IContainer container = Adapters.adapt(root, IContainer.class);
		return EvaluationResult.valueOf(container.getFolder(new Path(this.path)).exists());
	}

}
