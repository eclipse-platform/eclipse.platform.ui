/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui;

/**
 * An editor can implement this interface and participate in the workbench
 * session save/restore cycle using <code>IMemento</code>, similar to how
 * <code>IViewPart</code> currently works.
 * <p>
 * Refer to IWorkbenchPart for the part lifecycle.
 * </p>
 * <p>
 * If a memento is available, restoreState(*) will be inserted into the editor
 * startup.
 * </p>
 * <ol>
 * <li><code>editor.init(site, input)</code></li>
 * <li><code>editor.restoreState(memento)</code></li>
 * <li><code>editor.createPartControl(parent)</code></li>
 * <li>...</li>
 * </ol>
 * <p>
 * On workbench shutdown, the editor state will be persisted when the editor
 * references are saved.
 * </p>
 *
 * @since 3.3
 */
public interface IPersistableEditor extends IPersistable {
	/**
	 * Called with a memento for this editor. The editor can parse the data or save
	 * the memento. This method may not be called.
	 *
	 * @param memento the saved state for this editor. May be <code>null</code>.
	 */
	void restoreState(IMemento memento);
}
