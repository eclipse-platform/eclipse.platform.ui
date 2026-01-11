/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
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
package org.eclipse.ui.editors.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.ContributorFactorySimple;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

import org.eclipse.ui.editors.text.EditorsUI;

public class MarkerAnnotationOrderTest {

	IContributor pointContributor= null;

	Object masterToken= null;

	@BeforeEach
	void setUp() throws Exception {
		//add the marker updater extension point
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		pointContributor= ContributorFactorySimple.createContributor(this);

		try {
			BufferedInputStream bis= new BufferedInputStream(getClass().getResourceAsStream("plugin.xml"));

			Field field=
					org.eclipse.core.internal.registry.ExtensionRegistry.class
							.getDeclaredField("masterToken");
			field.setAccessible(true);
			masterToken= field.get(registry);
			registry.addContribution(bis, pointContributor, true, null, null, masterToken);
		} catch (Exception ex) {
			fail("update marker setup failed to execute");
			log(ex);
		}
	}

	@AfterEach
	void tearDown() throws Exception {
		// remove the marker updater extension point
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IExtension[] extensions = registry.getExtensions(pointContributor);
		for (IExtension extension : extensions) {
			if ("org.eclipse.ui.editors.markerUpdaters".equals(extension.getExtensionPointUniqueIdentifier())) {
				registry.removeExtension(extension, masterToken);
			}
		}
		TestUtil.cleanUp();
	}

	@Test
	void testDirectDependency() {
		final ArrayList<IStatus> list= new ArrayList<>(2);
		Bundle bundle= Platform.getBundle(EditorsUI.PLUGIN_ID);
		ILog log= ILog.of(bundle);
		log.addLogListener((status, plugin) -> list.add(status));

		TestMarkerAnnotationModel t1= new TestMarkerAnnotationModel();
		Position position= new Position(0);
		position.delete();
		IDocument d= null;
		try {
			t1.updateMarker(d, null, position);
		} catch (CoreException e) {
			fail("update marker failed to execute");
			log(e);
		}

		assertEquals(2, list.size(), "Wrong number of messages");
		assertEquals(
				"Marker Updater 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest2' and 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest1' depend on each other, 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest2' will run before 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest1'",
				((Status)list.get(0)).getMessage(), "Wrong Message for first status");
		assertEquals(
				"Marker Updater 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest4' and 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest1' depend on each other, 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest4' will run before 'org.eclipse.ui.texteditor.BasicMarkerUpdaterTest1'",
				((Status)list.get(1)).getMessage(), "Wrong Message for second status");

	}

	public static class TestMarkerAnnotationModel extends AbstractMarkerAnnotationModel {
		@Override
		protected IMarker[] retrieveMarkers() throws CoreException {
			return null;
		}

		@Override
		protected void deleteMarkers(IMarker[] markers) throws CoreException {
		}

		@Override
		protected void listenToMarkerChanges(boolean listen) {
		}

		@Override
		protected boolean isAcceptable(IMarker marker) {
			return false;
		}

	}

	private static void log(Exception ex) {
		String PLUGIN_ID= "org.eclipse.jface.text"; //$NON-NLS-1$
		ILog log= ILog.of(Platform.getBundle(PLUGIN_ID));
		log.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, ex.getMessage(), ex));
	}

}