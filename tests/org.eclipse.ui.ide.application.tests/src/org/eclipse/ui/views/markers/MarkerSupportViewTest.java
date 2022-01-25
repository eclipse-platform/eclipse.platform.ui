package org.eclipse.ui.views.markers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MarkerSupportViewTest {

	@Test
	public void canOverrideOpenSelectedMarkers() {
		Boolean[] canOverride = new Boolean[] { false };
		new MarkerSupportView("") {
			@Override
			protected void openSelectedMarkers() {
				canOverride[0] = true;
			}
		}.openSelectedMarkers();

		assertTrue(canOverride[0]);
	}
}
