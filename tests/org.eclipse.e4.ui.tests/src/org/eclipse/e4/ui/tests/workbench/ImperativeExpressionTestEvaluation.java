/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.core.di.annotations.Optional;

public class ImperativeExpressionTestEvaluation {
	@Evaluate
	public boolean isVisible(@Optional @Named("mmc1") boolean mmc1) {
		return mmc1;
	}
}
