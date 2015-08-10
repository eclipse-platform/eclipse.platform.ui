/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;

public class HeadlessContactsDemoTest extends HeadlessApplicationTest {

	@Override
	protected String getURI() {
		return "org.eclipse.e4.ui.tests/xmi/contacts.e4xmi";
	}

	@Override
	protected MPart getFirstPart() {
		return (MPart) findElement("DetailsView");
	}

	@Override
	protected MPart getSecondPart() {
		return (MPart) findElement("ContactsView");
	}

	@Override
	protected IPresentationEngine createPresentationEngine(
			String renderingEngineURI) throws Exception {
		HeadlessContextPresentationEngine engine = (HeadlessContextPresentationEngine) super
				.createPresentationEngine(renderingEngineURI);
		engine.setCreateContributions(false);
		return engine;
	}

}
