/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.common.notify.Notifier;

public abstract class HeadlessApplicationElementTest extends
		HeadlessStartupTest {

	protected MApplicationElement applicationElement;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		applicationElement = createApplicationElement(applicationContext);

		// Hook the global notifications
		final UIEventPublisher ep = new UIEventPublisher(applicationContext);
		((Notifier) applicationElement).eAdapters().add(ep);
		applicationContext.set(UIEventPublisher.class, ep);
	}

	protected abstract MApplicationElement createApplicationElement(
			IEclipseContext appContext) throws Exception;
}
