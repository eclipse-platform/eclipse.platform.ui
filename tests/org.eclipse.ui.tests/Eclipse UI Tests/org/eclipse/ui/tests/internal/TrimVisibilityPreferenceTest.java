/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests that the toolbar and perspective bar follow their workspace preferences
 * unless a window carries an override.
 */
@ExtendWith(CloseTestWindowsExtension.class)
public class TrimVisibilityPreferenceTest {

	private static final String KEY = IPreferenceConstants.COOLBAR_VISIBLE;

	private static final String OVERRIDE_KEY = KEY + ".override";

	private static final String PERSPECTIVE_BAR_KEY = IPreferenceConstants.PERSPECTIVEBAR_VISIBLE;

	private static final String PERSPECTIVE_BAR_OVERRIDE_KEY = PERSPECTIVE_BAR_KEY + ".override";

	private WorkbenchWindow window;

	private IPreferenceStore preferences;

	@BeforeEach
	public void setUp() throws Exception {
		preferences = PrefUtil.getInternalPreferenceStore();
		preferences.setToDefault(KEY);
		preferences.setToDefault(PERSPECTIVE_BAR_KEY);
		window = (WorkbenchWindow) openTestWindow();
		processEvents();
	}

	@AfterEach
	public void tearDown() {
		preferences.setToDefault(KEY);
		preferences.setToDefault(PERSPECTIVE_BAR_KEY);
		processEvents();
	}

	@Test
	public void testWindowWithoutOverrideFollowsPreference() {
		assertFalse(persistedState().containsKey(OVERRIDE_KEY), "a fresh window must not pin the value");
		assertTrue(window.getCoolBarVisible());
		List<String> events = new ArrayList<>();
		window.addPropertyChangeListener(event -> events.add(event.getProperty()));

		preferences.setValue(KEY, false);
		processEvents();

		assertFalse(window.getCoolBarVisible());
		assertEquals(List.of(WorkbenchWindow.PROP_COOLBAR_VISIBLE), events);
		assertFalse(persistedState().containsKey(OVERRIDE_KEY), "following the preference must not create an override");
	}

	@Test
	public void testTogglingAwayFromPreferenceStoresOverride() {
		window.setCoolBarVisible(false);

		assertEquals(Boolean.FALSE.toString(), persistedState().get(OVERRIDE_KEY));
		assertFalse(window.getCoolBarVisible());
	}

	@Test
	public void testTogglingBackToPreferenceDropsOverride() {
		window.setCoolBarVisible(false);
		assertEquals(Boolean.FALSE.toString(), persistedState().get(OVERRIDE_KEY));

		window.setCoolBarVisible(true);

		assertFalse(persistedState().containsKey(OVERRIDE_KEY), "an override equal to the preference must be dropped");
	}

	@Test
	public void testOverrideSurvivesPreferenceRoundTrip() {
		window.setCoolBarVisible(false);

		preferences.setValue(KEY, false);
		processEvents();
		preferences.setValue(KEY, true);
		processEvents();

		assertFalse(window.getCoolBarVisible(), "a per-window choice must outlive preference changes");
		assertEquals(Boolean.FALSE.toString(), persistedState().get(OVERRIDE_KEY));
	}

	@Test
	public void testOverrideAppliesToItsWindowOnly() {
		WorkbenchWindow other = (WorkbenchWindow) openTestWindow();
		processEvents();
		other.setCoolBarVisible(false);

		preferences.setValue(KEY, false);
		processEvents();
		assertFalse(window.getCoolBarVisible());
		preferences.setValue(KEY, true);
		processEvents();

		assertTrue(window.getCoolBarVisible());
		assertFalse(other.getCoolBarVisible(), "the override of one window must not affect another");
		assertFalse(persistedState().containsKey(OVERRIDE_KEY));
	}

	@Test
	public void testPerspectiveBarFollowsPreference() {
		assertTrue(window.getPerspectiveBarVisible());

		preferences.setValue(PERSPECTIVE_BAR_KEY, false);
		processEvents();

		assertFalse(window.getPerspectiveBarVisible());
		assertFalse(persistedState().containsKey(PERSPECTIVE_BAR_OVERRIDE_KEY));

		window.setPerspectiveBarVisible(true);

		assertEquals(Boolean.TRUE.toString(), persistedState().get(PERSPECTIVE_BAR_OVERRIDE_KEY));
	}

	@Test
	public void testLegacyVisibleSnapshotDoesNotPinWindow() {
		persistedState().put(KEY, Boolean.TRUE.toString());

		preferences.setValue(KEY, false);
		processEvents();

		assertFalse(window.getCoolBarVisible(), "a copied preference must not act as an override");
		assertFalse(persistedState().containsKey(KEY));
		assertFalse(persistedState().containsKey(OVERRIDE_KEY));
	}

	@Test
	public void testLegacyHiddenSnapshotBecomesOverride() {
		persistedState().put(KEY, Boolean.FALSE.toString());

		// an unchanged value, only to make the window read its persisted state again
		preferences.firePropertyChangeEvent(KEY, Boolean.TRUE, Boolean.TRUE);
		processEvents();

		assertFalse(window.getCoolBarVisible(), "a hidden bar was a user choice and must stay hidden");
		assertFalse(persistedState().containsKey(KEY));
		assertEquals(Boolean.FALSE.toString(), persistedState().get(OVERRIDE_KEY));
	}

	private Map<String, String> persistedState() {
		return window.getModel().getPersistedState();
	}
}
