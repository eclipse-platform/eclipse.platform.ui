/*******************************************************************************
 * Copyright (c) 2019 Andrey Loskutov.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.concurrent.atomic.AtomicInteger;

public class NotInitializedWorkbenchWindowActionDelegate extends MockWorkbenchWindowActionDelegate {

	public static AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);
	public static AtomicInteger INIT_COUNT = new AtomicInteger(0);

	public NotInitializedWorkbenchWindowActionDelegate() {
		INIT_COUNT.incrementAndGet();
		INSTANCE_COUNT.incrementAndGet();
	}

	@Override
	public void dispose() {
		INSTANCE_COUNT.decrementAndGet();
		super.dispose();
	}
}
