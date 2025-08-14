/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.decorators;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.junit.After;
import org.junit.Before;

/**
 * @version 	1.0
 */
public class ExceptionDecoratorTestCase extends DecoratorEnablementTestCase {
	private final Collection<DecoratorDefinition> problemDecorators = new ArrayList<>();

	private DecoratorDefinition light;

	@Override
	protected String getTestDecoratorId() {
		return "org.eclipse.ui.tests.decorators.lightweightdecorator";
	}

	@Before
	public void setUpAdditionalDecorators() throws Exception {
		//reset the static fields so that the decorators will fail
		HeavyNullImageDecorator.fail = true;
		HeavyNullTextDecorator.fail = true;
		NullImageDecorator.fail = true;
		DecoratorDefinition[] definitions = WorkbenchPlugin.getDefault()
				.getDecoratorManager().getAllDecoratorDefinitions();
		for (DecoratorDefinition definition2 : definitions) {
			String id = definition2.getId();
			if (id.equals("org.eclipse.ui.tests.heavyNullImageDecorator")
					|| id.equals("org.eclipse.ui.tests.heavyNullTextDecorator")) {
				definition2.setEnabled(true);
				problemDecorators.add(definition2);
			}

			//Do not cache the light one - the disabling issues
			//still need to be worked out.
			if (id.equals("org.eclipse.ui.tests.lightNullImageDecorator")) {
				definition2.setEnabled(true);
				light = definition2;
			}
		}
	}

	@After
	public void tearDown() throws Exception {

		//Need to wait for decoration to end to allow for all
		//errors to occur
		try {
			Job.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);
		} catch (OperationCanceledException | InterruptedException e) {
		}

		for (DecoratorDefinition next : problemDecorators) {
			assertFalse("Enabled " + next.getName(), next.isEnabled());
		}

		//Turnoff the lightweight one so as not to clutter the methods.
		light.setEnabled(false);
	}
}
