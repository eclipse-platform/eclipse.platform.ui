/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.emf.ecore.EObject;
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

		assertNotNull(aId);
		assertNotNull(wId);
		assertThatMapsAreEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());

		a.getChildren().remove(w);
		assertThatMapsAreEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());

		a.getChildren().add(w);
		assertThatMapsAreEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());

		a.getChildren().remove(w);
		assertThatMapsAreEquals(r.getIDToEObjectMap(), r.getEObjectToIDMap());
	}

	private void assertThatMapsAreEquals(Map<String, EObject> idToObject, Map<EObject, String> objectToId) {
		assertEquals(idToObject.size(), objectToId.size());
		Map<String, EObject> checkMap = objectToId.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));

		for (Entry<String, EObject> e : idToObject.entrySet()) {
			EObject eObject = checkMap.get(e.getKey());
			assertSame(e.getValue(), eObject);
		}
	}
}
