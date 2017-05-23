/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.emf.ecore.EObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class E4ResourceTest {
	@Test
	public void bug_leak_517124() {
		E4XMIResource r = new E4XMIResource();

		MApplication a = MApplicationFactory.INSTANCE.createApplication();

		MTrimmedWindow w = MBasicFactory.INSTANCE.createTrimmedWindow();
		a.getChildren().add(w);

		r.getContents().add((EObject) a);

		String aId = r.getID((EObject) a);
		String wId = r.getID((EObject) w);

		Assert.assertNotNull(aId);
		Assert.assertNotNull(wId);
		assertEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());

		a.getChildren().remove(w);
		assertEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());

		a.getChildren().add(w);
		assertEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());

		a.getChildren().remove(w);
		assertEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());
	}

	private void assertEquals(Map<String, EObject> idToObject, Map<EObject, String> objectToId) {
		Assert.assertEquals(idToObject.size(), objectToId.size());
		Map<String, EObject> checkMap = objectToId.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));

		for (Entry<String, EObject> e : idToObject.entrySet()) {
			EObject eObject = checkMap.get(e.getKey());
			Assert.assertSame(e.getValue(), eObject);
		}
	}
}
