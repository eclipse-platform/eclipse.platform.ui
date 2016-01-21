/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc.  and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 485201
 *******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;

/**
 * This interface represents a strategy for choosing an IEditorDescriptor for a
 * given file name. It's used by the org.eclipse.ui.ide.unknownEditorStrategy
 * extension point.
 *
 * @since 3.12
 */
public interface IUnknownEditorStrategy {

	/**
	 * @param fileName
	 *            Name of the file to open
	 * @param editorRegistry
	 *            the IDE editor registry
	 * @return an {@link IEditorDescriptor} for editor to use to open this file,
	 *         or null if no editor was resolved for that file name.
	 * @throws CoreException
	 *             in case descriptor lookup fails with an error
	 * @throws OperationCanceledException
	 *             in case descriptor lookup was cancelled by the user
	 */
	IEditorDescriptor getEditorDescriptor(String fileName, IEditorRegistry editorRegistry)
			throws CoreException, OperationCanceledException;

}
