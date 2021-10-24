/*******************************************************************************
 * Copyright (c) 2021 Joerg Kubitz.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.reconciler;

import org.eclipse.jface.text.reconciler.AbstractReconciler;

public class FastAbstractReconcilerTest extends AbstractReconcilerTest {

	@Override
	int getDelay() {
		return 10000; // make tests run slower (too slow without signalWaitForFinish)
	}

	@Override
	void aboutToWork(AbstractReconciler reconciler) {
		reconciler.signalWaitForFinish(); // make tests run faster (instant)
	}
}
