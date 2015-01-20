/*******************************************************************************
 * Copyright (c) 2015 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ResourceTransfer;
import org.junit.Test;

public class ResourceTransferTest {

	/**
	 * Test for bug 205678: don't allow to transfer unlimited number of
	 * resources, this will cause OOM
	 */
	@Test
	public void testMaxResourcesLimitForTransfer() throws Exception {
		System.gc();

		ResourceTransfer transfer = ResourceTransfer.getInstance();
		IProject dummyProject = ResourcesPlugin.getWorkspace().getRoot().getProject("Dummy");
		Transfer[] types = new Transfer[] { transfer, TextTransfer.getInstance() };
		Clipboard clip = new Clipboard(Display.getDefault());
		try {
			clip.clearContents();
			// Good case: ResourceTransfer.MAX_RESOURCES_TO_TRANSFER
			int count = 1000 * 1000;
			IResource[] data = new IResource[count];
			String names = createtextBuffer(count, dummyProject.getName());
			Arrays.fill(data, dummyProject);
			clip.setContents(new Object[] { data, names }, types);
			assertNotNull(clip.getContents(transfer));

			// Bad case: ResourceTransfer.MAX_RESOURCES_TO_TRANSFER + 1
			count++;
			data = new IResource[count];
			names = createtextBuffer(count, dummyProject.getName());
			Arrays.fill(data, dummyProject);
			clip.setContents(new Object[] { data, names }, types);
			assertNull(clip.getContents(transfer));
			clip.clearContents();
		} finally {
			clip.dispose();
			System.gc();
		}
	}

	// See org.eclipse.ui.internal.navigator.resources.actions.CopyAction
	String createtextBuffer(int count, String name) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sb.append('\n');
			}
			sb.append(name);
		}
		return sb.toString();
	}
}
