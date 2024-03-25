/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.resources;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class JFaceResourcesTest {

	@Test
	public void ensureLocalResourceManagerCanBeCreated() {
		Display d = Display.getDefault();
		Shell shell = new Shell(d);
		Composite composite = new Composite(shell, SWT.NONE);
		LocalResourceManager localResourceManager = JFaceResources.managerFor(composite);

		assertNotNull("LocalResourceManager cannot be created via static accessor", localResourceManager);
	}

}
