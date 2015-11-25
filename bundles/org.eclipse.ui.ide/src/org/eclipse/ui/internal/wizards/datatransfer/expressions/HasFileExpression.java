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

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;

/**
 * Expression to check whether a given container (IContainer or folder typed as
 * {@link File}) contains a file with provided name as a direct child.
 *
 * @since 3.12
 */
public class HasFileExpression extends Expression {

	/**
	 * The name of the XML tag to use this rule in a plugin.xml
	 */
	public static final String TAG = "hasFile"; //$NON-NLS-1$

	String path;

	/**
	 * Build expression with a path.
	 *
	 * @param path
	 *            path where to look for a file under given container.
	 */
	public HasFileExpression(String path) {
		this.path = path;
	}

	/**
	 * Build expression retrieving the suffix as the 'path' attribute on the
	 * provided {@link IConfigurationElement}.
	 *
	 * @param element
	 */
	public HasFileExpression(IConfigurationElement element) {
		this(element.getAttribute("path")); //$NON-NLS-1$
	}

	@Override
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object root = context.getDefaultVariable();
		if (root instanceof File) {
			return EvaluationResult.valueOf( new File((File)root, this.path).exists() );
		} else if (root instanceof IContainer) {
			return EvaluationResult.valueOf( ((IContainer)root).getFile(new Path(this.path)).exists() );
		} else if (root instanceof IAdaptable) {
			IContainer container = ((IAdaptable)root).getAdapter(IContainer.class);
			return EvaluationResult.valueOf( container.getFile(new Path(this.path)).exists() );
		}
		return EvaluationResult.FALSE;
	}

}
