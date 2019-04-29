/*******************************************************************************
 * Copyright (c) 2019 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.model.application.ui.MImperativeExpression;

public class ImperativeExpressionTestEvaluationPersistedState {
	public static final String PERSISTED_STATE_TEST = "persisted-state-test";

	@Evaluate
	public boolean isVisible(MImperativeExpression exp) {
		return exp.getPersistedState().containsKey(PERSISTED_STATE_TEST);
	}
}
