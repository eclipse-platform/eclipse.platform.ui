/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.ui.tests.progress;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.PlatformUI;

/**
 * Tests that the E3
 * {@link org.eclipse.ui.progress.IProgressService#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)}
 * can be called from a thread that is not the UI thread.
 */
public class ProgressServiceOffUIThreadTest extends AbstractProgressServiceOffUIThreadTest {

	@Override
	protected IRunnableContext getRunContext() {
		return PlatformUI.getWorkbench().getProgressService();
	}
}
