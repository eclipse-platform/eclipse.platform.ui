/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.markers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.tests.internal.TestMemento;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

/**
 * Testing for https://bugs.eclipse.org/bugs/show_bug.cgi?id=75909 .
 * Only the marker IDs of the selected filters are saved for the Problems view.
 * If a new plugin is deployed with a new MarkerType, when the session 
 * restarts it looks like it was "unselected" from the filter list, and
 * so it doesn't show up.
 * 
 * These tests cover restoring state from old and new settings files.
 * 
 * @since 3.1
 *
 */
public class Bug75909Test extends TestCase {

	private static final int OLD_SETTINGS_SELECTED = 4;

	private static final String REMOVED_MARKER_ID = "org.eclipse.pde.core.problem";

	private static final String INCLUDED_MARKER_ID = "org.eclipse.core.resources.problemmarker";

	private static final String MISSING_MARKER_ID = "org.eclipse.jdt.core.problem";

	private static final String OLD_DIALOG_SETTINGS_XML = "old_dialog_settings.xml";

	public static TestSuite suite() {
		return new TestSuite(Bug75909Test.class);
	}

	/**
	 * MarkerFilter.resetState() should make all of the available
	 * MarkerTypes selected.  The just tests that the environment
	 * has some datafill in it.
	 * 
	 * @throws Throwable
	 */
	public void testBasicFilter() throws Throwable {
		ProblemFilter filter = new ProblemFilter("Bug75909Test");
		filter.resetState();

		List allTypes = new ArrayList();
		filter.addAllSubTypes(allTypes);
		int num_types = allTypes.size();

		// there are more than 4 marker types in the default env.
		assertTrue("There should be more than 4 types in the system",
				num_types > 4);

		// after a reset, the number of selected types should
		// equal the total number of types.
		assertEquals(num_types, filter.getSelectedTypes().size());
	}


	/**
	 * Settings can be restored (at least once :-) from the old settings
	 * attribute format.  Any marker type not listed in the attribute
	 * will appear as not selected.
	 * 
	 * @throws Throwable
	 */
	public void testRestoreOldState() throws Throwable {
		IDialogSettings settings = new DialogSettings("Workbench");
		loadSettings(settings, Bug75909Test.OLD_DIALOG_SETTINGS_XML);

		ProblemFilter filter = new ProblemFilter("Bug75909Test");
		filter.restoreFilterSettings(getFilterSettings(settings));

		List selected = filter.getSelectedTypes();
		assertEquals(Bug75909Test.OLD_SETTINGS_SELECTED, selected.size());

		MarkerType marker = getType(filter, Bug75909Test.INCLUDED_MARKER_ID);
		// this was marked as selected in the old attribute
		assertTrue(selected.contains(marker));

		MarkerType removed = getType(filter, Bug75909Test.REMOVED_MARKER_ID);
		// this was missing from the old attribute, so it should not be
		// selected
		assertFalse(selected.contains(removed));
	}

	/**
	 * When restoring settings from the new attribute, a marker type
	 * id that is not true or false has just been introduced.  Test that
	 * the new marker type shows up as selected.
	 * 
	 * @throws Throwable
	 */
	public void testRestoreNewStateMissingId() throws Throwable {
		IMemento settings = createMissingMemento();

		ProblemFilter filter = new ProblemFilter("Bug75909Test");
		filter.restoreState(settings);

		List included = new ArrayList();
		filter.addAllSubTypes(included);
		
		List selected = filter.getSelectedTypes();
		assertEquals(included.size() - 1, selected.size());

		MarkerType marker = getType(filter, Bug75909Test.INCLUDED_MARKER_ID);
		// was in the file as true, so it should be selected.
		assertTrue(selected.contains(marker));

		MarkerType removed = getType(filter, Bug75909Test.REMOVED_MARKER_ID);
		// was in the file as false, so it won't be selected.
		assertFalse(selected.contains(removed));

		MarkerType missing = getType(filter, Bug75909Test.MISSING_MARKER_ID);
		// was missing from the file, so it should be selected.
		assertTrue(selected.contains(missing));
	}
	
	/**
	 * Create a missing memento that is missing it's name.
	 * @return IMemento
	 */
	private IMemento createMissingMemento() {
		TestMemento memento = new TestMemento("filter","Filter Test");
		memento.putString("selectBySeverity","false");
		memento.putString("contains","true");
		memento.putString("enabled","true");
		memento.putInteger("severity",0);
		memento.putString("description","");
		memento.putString("filterOnMarkerLimit","true");
		memento.putString("selectionStatus" ,"org.eclipse.core.resources.problemmarker:true:org.eclipse.pde.core.problem:false:org.eclipse.jdt.core.buildpath_problem:true:org.eclipse.ant.ui.buildFileProblem:true:");
		memento.putInteger("onResource",0);
		return memento;
	}

	/**
	 * Get the settings for the filter tag.
	 * @param settings
	 * @return IDialogSettings
	 */
	private IDialogSettings getFilterSettings(IDialogSettings settings) {
		return settings.getSection("filter");
	}

	/**
	 * MarkerFilter.saveState(IDialogSettings) should now save selected
	 * types with <b>true</b> and unselected types are now saved to the
	 * list with <b>false</b>.  This is an "identity transform" test.
	 * 
	 * @throws Throwable
	 */
	public void testSaveState() throws Throwable {
		ProblemFilter filter = new ProblemFilter("Bug75909Test");
		filter.resetState();

		List allTypes = new ArrayList();
		filter.addAllSubTypes(allTypes);

		MarkerType removed = getType(filter, Bug75909Test.REMOVED_MARKER_ID);
		
		filter.getSelectedTypes().remove(removed);
		// there should be one less select type than all of the types.
		assertEquals(allTypes.size() - 1, filter.getSelectedTypes().size());

		IMemento settings = new TestMemento("Test","Bug75909Test");
		filter.saveFilterSettings(settings);

		ProblemFilter f2 = new ProblemFilter("Bug75909Test");
		f2.restoreState(settings);
		
		assertEquals(filter.getSelectedTypes().size(),
				f2.getSelectedTypes().size());
		assertFalse(f2.getSelectedTypes().contains(removed));
	}


	private void loadSettings(IDialogSettings settings, String resource)
			throws UnsupportedEncodingException, IOException {
		InputStream io = null;
		try {
			io = getClass().getResourceAsStream(resource);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					io, "utf-8"));
			settings.load(reader);
		} finally {
			if (io != null) {
				io.close();
			}
		}
	}

	private MarkerType getType(ProblemFilter filter, String id) {
		return filter.getMarkerType(id);
	}

}
