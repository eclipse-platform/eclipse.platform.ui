/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.tests.navigator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.navigator.resources.ResourceBookmarkDecorator;
import org.eclipse.ui.internal.navigator.resources.ResourceBookmarks;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that adding and removing the bookmark marker updates an existing
 * navigator item through the resource change listener of
 * {@link ResourceBookmarkDecorator}, without any manual viewer refresh.
 */
public class ResourceBookmarkDecoratorTest extends NavigatorTestBase {

	private static final long TIMEOUT = 10000;

	/** Distinct from the default foreground, which would make the test vacuous. */
	private static final RGB BOOKMARK_RGB = new RGB(12, 200, 34);

	private boolean decoratorWasEnabled;
	private RGB originalBookmarkRgb;
	private IProject p1Project;

	public ResourceBookmarkDecoratorTest() {
		_navigatorInstanceId = "org.eclipse.ui.tests.navigator.OverrideTestView";
	}

	@Override
	@BeforeEach
	public void setUp() throws CoreException {
		super.setUp();

		_contentService.bindExtensions(new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);
		_contentService.getActivationService().activateExtensions(new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);

		IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
		decoratorWasEnabled = manager.getEnabled(ResourceBookmarkDecorator.ID);
		manager.setEnabled(ResourceBookmarkDecorator.ID, true);

		ColorRegistry colors = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
		originalBookmarkRgb = colors.getRGB(ResourceBookmarkDecorator.HIGHLIGHT_COLOR_ID);
		colors.put(ResourceBookmarkDecorator.HIGHLIGHT_COLOR_ID, BOOKMARK_RGB);

		p1Project = ResourcesPlugin.getWorkspace().getRoot().getProject(TestWorkspace.P1_PROJECT_NAME);
		assertFalse(ResourceBookmarks.isBookmarked(p1Project), "project must not be bookmarked initially");

		waitForDecorationUpdate();
	}

	@AfterEach
	public void restoreDecoratorState() throws CoreException {
		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
				.put(ResourceBookmarkDecorator.HIGHLIGHT_COLOR_ID, originalBookmarkRgb);
		PlatformUI.getWorkbench().getDecoratorManager().setEnabled(ResourceBookmarkDecorator.ID, decoratorWasEnabled);
	}

	@Test
	public void testBookmarkMarkerAddsAndRemovesDecoration() throws Exception {
		TreeItem item = findItem(p1Project);
		assertNotNull(item, "no tree item for " + p1Project);
		Image plainImage = item.getImage(0);
		assertNotEquals(boldFont(), item.getFont(0), "item must not be bold initially");
		assertNotEquals(BOOKMARK_RGB, foreground(item), "item must not have the bookmark color initially");

		IMarker marker = p1Project.createMarker(ResourceBookmarks.MARKER_TYPE);
		assertTrue(ResourceBookmarks.isBookmarked(p1Project));
		waitForBookmarkDecoration(true);

		item = findItem(p1Project);
		assertNotNull(item, "no tree item for " + p1Project);
		assertNotEquals(plainImage, item.getImage(0), "bookmark overlay missing");
		assertEquals(boldFont(), item.getFont(0), "bold font missing");
		assertEquals(BOOKMARK_RGB, foreground(item), "bookmark foreground color missing");

		marker.delete();
		assertFalse(ResourceBookmarks.isBookmarked(p1Project));
		waitForBookmarkDecoration(false);

		item = findItem(p1Project);
		assertNotNull(item, "no tree item for " + p1Project);
		assertEquals(plainImage, item.getImage(0), "bookmark overlay not removed");
		assertNotEquals(boldFont(), item.getFont(0), "bold font not removed");
		assertNotEquals(BOOKMARK_RGB, foreground(item), "bookmark foreground color not removed");
	}

	/**
	 * Waits until the bold font of the item matches the expected state and the
	 * decoration jobs, including the update job that applies the results to the
	 * viewer, have finished. The update job runs in the UI thread, so the event
	 * loop has to keep spinning instead of joining the job family.
	 */
	private void waitForBookmarkDecoration(boolean decorated) {
		DisplayHelper.waitForCondition(Display.getCurrent(), TIMEOUT, () -> {
			TreeItem item = findItem(p1Project);
			return item != null && boldFont().equals(item.getFont(0)) == decorated;
		});
		waitForDecorationUpdate();
	}

	private static void waitForDecorationUpdate() {
		boolean idle = DisplayHelper.waitForCondition(Display.getCurrent(), TIMEOUT,
				() -> Job.getJobManager().find(DecoratorManager.FAMILY_DECORATE).length == 0);
		assertTrue(idle, "decoration did not finish within " + TIMEOUT + "ms");
	}

	private TreeItem findItem(IResource resource) {
		for (TreeItem item : _viewer.getTree().getItems()) {
			if (resource.equals(Adapters.adapt(item.getData(), IResource.class))) {
				return item;
			}
		}
		return null;
	}

	private static Font boldFont() {
		return JFaceResources.getFontRegistry().getBold(ResourceBookmarkDecorator.TREE_TABLE_FONT_ID);
	}

	private static RGB foreground(TreeItem item) {
		Color color = item.getForeground(0);
		return color == null ? null : color.getRGB();
	}
}
