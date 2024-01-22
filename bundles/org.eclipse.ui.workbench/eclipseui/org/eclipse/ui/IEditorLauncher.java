/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IPath;

/**
 * An editor launcher is used to launch external editors on a file in the local
 * file system.
 * <p>
 * Clients should implement this interface to define a new type of editor
 * launcher. Each new launcher must be registered as an editor in the
 * workbench's editor extension point (named
 * <code>"org.eclipse.ui.editors"</code>).
 *
 * For example, the plug-in's XML markup might contain:
 * </p>
 *
 * <pre>
 * &lt;extension point = "org.eclipse.ui.editors"&gt;
 *   &lt;editor
 *       id="org.eclipse.ui.SwingEditorLauncher"
 *       name="Swing Editor"
 *       extensions="xml"
 *       launcher="org.eclipse.ui.examples.swingeditor.SwingEditorLauncher"
 *       icon="icons/xml.gif"&gt;
 *   &lt;/editor&gt;
 * &lt;/extension&gt;
 * </pre>
 * <p>
 * In this example a launcher has been registered for use with <code>xml</code>
 * files. Once registered, the launcher will appear in the
 * <code>Open With</code> menu for an <code>xml</code> file. If the item is
 * invoked the workbench will create an instance of the launcher class and call
 * <code>open</code> on it, passing the input file.
 * </p>
 */
public interface IEditorLauncher {

	/**
	 * Launches this external editor to edit the file at the given location in the
	 * local file system.
	 *
	 * @param file the local file system path of the file to edit
	 */
	void open(IPath file);
}
