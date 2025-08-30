/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.zoom;

import org.junit.Assert;
import org.junit.Test;

public class OpenEditorTest extends ZoomTestCase {

	/**
	 * <p>Test: Zoom an editor then open an existing editor in the same stack. Do not force activation.</p>
	 * <p>Expected result: the new editor is zoomed and active</p>
	 */
	@Test
	public void testOpenExistingEditorInZoomedStack() {
		zoom(editor1);
		openEditor(file2, false);
		Assert.assertTrue(isZoomed(editor2));
		Assert.assertTrue(page.getActivePart() == editor2);
	}

	/**
	 * <p>Test: Zoom an editor then open a new editor in the same stack. Do not force activation.</p>
	 * <p>Expected result: the new editor is zoomed and active</p>
	 */
	@Test
	public void testOpenNewEditorInZoomedStack() {
		close(editor2);

		zoom(editor1);
		openEditor(file2, false);
		Assert.assertTrue(isZoomed(editor2));
		Assert.assertTrue(page.getActivePart() == editor2);
	}

	/**
	 * <p>Test: Zoom an editor then open an existing in the same stack. Use the activate-on-open mode.</p>
	 * <p>Expected result: the new editor is zoomed and active</p>
	 */
	@Test
	public void testOpenAndActivateExistingEditorInZoomedStack() {
		zoom(editor1);
		openEditor(file2, true);

		assertZoomed(editor2);
		assertActive(editor2);
	}

	/**
	 * <p>Test: Zoom an editor then open a new editor in the same stack. Use the activate-on-open mode.</p>
	 * <p>Expected result: the new editor is zoomed and active</p>
	 */
	@Test
	public void testOpenAndActivateNewEditorInZoomedStack() {
		close(editor2);

		zoom(editor1);
		openEditor(file2, true);

		assertZoomed(editor2);
		assertActive(editor2);
	}

}
