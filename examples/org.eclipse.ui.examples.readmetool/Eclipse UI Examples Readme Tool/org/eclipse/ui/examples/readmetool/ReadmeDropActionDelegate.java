/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
package org.eclipse.ui.examples.readmetool;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.IDropActionDelegate;

/**
 * Adapter for handling the dropping of readme segments into another plugin. In
 * this case, we expect the segments to be dropped onto <code>IFile</code>
 * object, or an adapter that supports <code>IFile</code>.
 */
public class ReadmeDropActionDelegate implements IDropActionDelegate {
	public static final String ID = "org_eclipse_ui_examples_readmetool_drop_actions"; //$NON-NLS-1$

	@Override
	public boolean run(Object source, Object target) {
		if (source instanceof byte[] && target instanceof IFile file) {
			try {
				file.appendContents(new ByteArrayInputStream((byte[]) source), false, true, null);
			} catch (CoreException e) {
				System.out.println(
						MessageUtil.getString("Exception_in_readme_drop_adapter") + e.getStatus().getMessage()); //$NON-NLS-1$
				return false;
			}
			return true;
		}
		return false;
	}
}
