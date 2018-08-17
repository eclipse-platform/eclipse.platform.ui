/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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

package org.eclipse.e4.ui.tests.application;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 *
 */
public class PostModelProcessor extends AbstractModelProcessorImpl {
	@Inject
	@Named("fragment.contributedWindow")
	private MWindow window;

	@Override
	protected void doRun() {
		if (window != null) {
			window.getVariables().add("postAddition");
		}
	}

	@Override
	protected String getSuffix() {
		return "post";
	}

}
