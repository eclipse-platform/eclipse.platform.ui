/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import java.util.Arrays;
import java.util.List;
import junit.framework.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class WorkspacePreferencesTest extends EclipseWorkspaceTest {
	private IWorkspace workspace;
	private Preferences preferences;
	private IWorkspaceDescription initialDescription;

	/**
	 * Constructor for WorkspacePreferencesTest.
	 * @param name
	 */
	public WorkspacePreferencesTest(String name) {
		super(name);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		workspace = ResourcesPlugin.getWorkspace();
		preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		workspace.setDescription(Workspace.defaultWorkspaceDescription());
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		workspace.setDescription(Workspace.defaultWorkspaceDescription());
	}
	/**
	 * Tests properties state in a brand new workspace (must match defaults).
	 */
	public void testDefaults() {
		IWorkspaceDescription description = Workspace.defaultWorkspaceDescription();
		assertEquals("1.0", description.isAutoBuilding(), preferences.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING));
		assertEquals("1.1", description.getBuildOrder() == null, preferences.getBoolean(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER));
		assertEquals("1.2", WorkspacePreferences.convertStringArraytoString(description.getBuildOrder()), preferences.getString(ResourcesPlugin.PREF_BUILD_ORDER));
		assertEquals("1.3", description.getFileStateLongevity(), preferences.getLong(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY));
		assertEquals("1.4", description.getMaxFileStates(), preferences.getInt(ResourcesPlugin.PREF_MAX_FILE_STATES));
		assertEquals("1.5", description.getMaxFileStateSize(), preferences.getLong(ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE));
		assertEquals("1.6", description.getSnapshotInterval(), preferences.getLong(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL));

		String[] descriptionProperties =
			{
				ResourcesPlugin.PREF_AUTO_BUILDING,
				ResourcesPlugin.PREF_BUILD_ORDER,
				ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER,
				ResourcesPlugin.PREF_FILE_STATE_LONGEVITY,
				ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE,
				ResourcesPlugin.PREF_MAX_FILE_STATES,
				ResourcesPlugin.PREF_SNAPSHOT_INTERVAL };
		List defaultPropertiesList = Arrays.asList(preferences.defaultPropertyNames());
		for (int i = 0; i < descriptionProperties.length; i++) {
			String property = descriptionProperties[i];
			assertTrue("2.0 - Description property is not default: " + property, defaultPropertiesList.contains(property));
		}
	}
	/**
	 * Makes changes in the preferences and ensure they are reflected in the
	 * workspace description.
	 */
	public void testSetPreferences() {
		preferences.setValue(ResourcesPlugin.PREF_AUTO_BUILDING, true);
		assertTrue("1.0", workspace.getDescription().isAutoBuilding());

		preferences.setValue(ResourcesPlugin.PREF_AUTO_BUILDING, false);
		assertTrue("1.1", !workspace.getDescription().isAutoBuilding());

		preferences.setValue(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER, true);
		assertTrue("2.0", workspace.getDescription().getBuildOrder() == null);

		preferences.setValue(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER, false);
		assertTrue("2.1", workspace.getDescription().getBuildOrder() != null);

		preferences.setValue(ResourcesPlugin.PREF_BUILD_ORDER, "x:y,z:z");
		List expectedList = Arrays.asList(new String[] { "x", "y,z", "z" });
		List actualList = Arrays.asList(workspace.getDescription().getBuildOrder());
		assertEquals("2.2", expectedList, actualList);

		preferences.setValue(ResourcesPlugin.PREF_BUILD_ORDER, "");
		assertTrue("2.3", workspace.getDescription().getBuildOrder().length == 0);
		
		long snapshotInterval = 800000000L;
		preferences.setValue(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL, snapshotInterval);
		assertEquals("3.0", snapshotInterval, workspace.getDescription().getSnapshotInterval());
		
		long defaultSnapshotInterval = preferences.getDefaultLong(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL);
		preferences.setValue(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL, defaultSnapshotInterval);
		assertEquals("3.1", defaultSnapshotInterval, workspace.getDescription().getSnapshotInterval());
		
		preferences.setToDefault(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL);
		assertEquals("3.2", defaultSnapshotInterval, workspace.getDescription().getSnapshotInterval());
		assertEquals("Description not synchronized", workspace.getDescription(), preferences);

	}

	public void testImportExport() {

	}

	/**
	 * Makes changes through IWorkspace#setDescription and checks if the changes
	 * are reflected in the preferences.
	 */
	public void testSetDescription() {
		IWorkspaceDescription description = workspace.getDescription();
		description.setAutoBuilding(false);
		description.setBuildOrder(new String[] { "a", "b,c", "c" });
		description.setFileStateLongevity(60000 * 5);
		description.setMaxFileStates(16);
		description.setMaxFileStateSize(100050);
		description.setSnapshotInterval(1234567);
		try {
			workspace.setDescription(description);
		} catch (CoreException ce) {
			fail("2.0", ce);
		}
		assertEquals("2.1 - Preferences not synchronized", description, preferences);

		// try to make changes without committing them

		// sets current state to a known value
		description.setFileStateLongevity(90000);
		try {
			workspace.setDescription(description);
		} catch (CoreException ce) {
			fail("3.0", ce);
		}
		// try to make a change 
		description.setFileStateLongevity(100000);
		// the original value should remain set		
		assertEquals("3.1", 90000, workspace.getDescription().getFileStateLongevity());
		assertEquals("3.2", 90000, preferences.getLong(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY));
	}
	/**
	 * Checks if a legacy workspace description is correctly loaded and
	 * its file discarded.
	 */
	public void testMigration() {
		WorkspaceDescription description = new WorkspaceDescription("Legacy workspace");
		description.setAutoBuilding(false);
		final String[] buildOrder = new String[] { "g", "r", "e", "p" };
		description.setBuildOrder(buildOrder);
		description.setFileStateLongevity(Math.abs((long) (Math.random() * 100000L)));
		description.setMaxFileStates(Math.abs((int) (Math.random() * 100000L)));
		description.setMaxFileStateSize(Math.abs((long) (Math.random() * 100000L)));
		description.setSnapshotInterval(Math.abs((long) (Math.random() * 100000L)));
		LocalMetaArea localMetaArea = ((Workspace) workspace).getMetaArea();
		try {
			localMetaArea.write(description);
		} catch (CoreException ce) {
			fail("1.0", ce);
		}
		assertTrue("2.0 - file .description does not exist", localMetaArea.getOldWorkspaceDescriptionLocation().toFile().isFile());
		WorkspaceDescription descriptionFromDisk = localMetaArea.readOldWorkspace();
		assertTrue("2.1 - file .description still exists", !localMetaArea.getOldWorkspaceDescriptionLocation().toFile().isFile());
		assertEquals("3.1", description.getBuildOrder(), descriptionFromDisk.getBuildOrder());
		assertEquals("3.2", description.getFileStateLongevity(), descriptionFromDisk.getFileStateLongevity());
		assertEquals("3.3", description.getMaxFileStates(), descriptionFromDisk.getMaxFileStates());
		assertEquals("3.4", description.getMaxFileStateSize(), descriptionFromDisk.getMaxFileStateSize());
		assertEquals("3.5", description.getSnapshotInterval(), descriptionFromDisk.getSnapshotInterval());
		assertEquals("3.6", description.isAutoBuilding(), descriptionFromDisk.isAutoBuilding());
	}
	public void assertEquals(String message, IWorkspaceDescription description, Preferences preferences) throws ComparisonFailure {
		assertEquals(message + " - 1", description.isAutoBuilding(), preferences.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING));
		assertEquals(message + " - 2", description.getBuildOrder() == null, preferences.getBoolean(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER));
		assertEquals(message + " - 3", WorkspacePreferences.convertStringArraytoString(description.getBuildOrder()), preferences.getString(ResourcesPlugin.PREF_BUILD_ORDER));
		assertEquals(message + " - 4", description.getFileStateLongevity(), preferences.getLong(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY));
		assertEquals(message + " - 5", description.getMaxFileStates(), preferences.getInt(ResourcesPlugin.PREF_MAX_FILE_STATES));
		assertEquals(message + " - 6", description.getMaxFileStateSize(), preferences.getLong(ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE));
		assertEquals(message + " - 7", description.getSnapshotInterval(), preferences.getLong(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL));
	}
	public static Test suite() {
		return new TestSuite(WorkspacePreferencesTest.class);	
	}
}
