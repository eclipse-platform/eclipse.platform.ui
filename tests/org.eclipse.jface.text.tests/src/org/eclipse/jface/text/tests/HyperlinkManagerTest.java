/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.HyperlinkManager;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;

public class HyperlinkManagerTest {

	private static final IRegion LINK_REGION= new Region(0, 5);

	private Shell fShell;
	private TextViewer fViewer;
	private TestHyperlinkManager fManager;
	private int fDetections;
	private IHyperlink[] fShownLinks;

	@BeforeEach
	public void before() {
		fShell= new Shell();
		fViewer= new TextViewer(fShell, SWT.NONE);
		fViewer.setDocument(new Document("hello world"));
		fManager= new TestHyperlinkManager();
		fManager.install(fViewer, new TestPresenter(), new IHyperlinkDetector[] { new CountingDetector() }, SWT.MOD1);
	}

	@AfterEach
	public void after() {
		fManager.uninstall();
		fShell.dispose();
	}

	@Test
	public void testDetectionWaitsForMouseToRest() {
		fManager.mouseMove(mouseEvent(SWT.MOD1));
		assertEquals(0, fDetections);

		waitUntil(() -> fDetections > 0);
		assertEquals(1, fDetections);
		assertTrue(fShownLinks != null && fShownLinks.length == 1);
	}

	@Test
	public void testContinuousMovementDetectsOnce() {
		for (int i= 0; i < 5; i++) {
			fManager.offset= 6 + i % 2;
			fManager.mouseMove(mouseEvent(SWT.MOD1));
		}
		assertEquals(0, fDetections);

		waitUntil(() -> fDetections > 0);
		runEventLoop(500);
		assertEquals(1, fDetections);
	}

	@Test
	public void testModifierReleaseCancelsDetection() {
		fManager.mouseMove(mouseEvent(SWT.MOD1));
		fManager.handleEvent(new Event());

		runEventLoop(500);
		assertEquals(0, fDetections);
	}

	@Test
	public void testMouseExitCancelsDetection() {
		fManager.mouseMove(mouseEvent(SWT.MOD1));
		fManager.mouseExit(mouseEvent(SWT.MOD1));

		runEventLoop(500);
		assertEquals(0, fDetections);
	}

	@Test
	public void testMouseDownDetectsImmediately() {
		MouseEvent event= mouseEvent(SWT.MOD1);
		event.button= 1;
		fManager.mouseDown(event);
		assertEquals(1, fDetections);
	}

	@Test
	public void testMovingWithinShownLinkDoesNotDetectAgain() {
		fManager.mouseMove(mouseEvent(SWT.MOD1));
		waitUntil(() -> fDetections > 0);

		fManager.offset= 3;
		fManager.mouseMove(mouseEvent(SWT.MOD1));
		runEventLoop(500);
		assertEquals(1, fDetections);
	}

	@Test
	public void testLinkShownAgainAfterModifierReleased() {
		fManager.mouseMove(mouseEvent(SWT.MOD1));
		waitUntil(() -> fShownLinks != null);

		fManager.handleEvent(new Event());
		assertNull(fShownLinks);

		fManager.mouseMove(mouseEvent(SWT.MOD1));
		waitUntil(() -> fShownLinks != null);
		assertEquals(2, fDetections);
	}

	@Test
	public void testLinkShownOnHoverAfterOpenHyperlink() {
		fViewer.setSelectedRange(1, 0);
		assertTrue(fManager.openHyperlink());

		fManager.mouseMove(mouseEvent(SWT.MOD1));
		waitUntil(() -> fShownLinks != null);
		assertEquals(2, fDetections);
	}

	private MouseEvent mouseEvent(int stateMask) {
		Event event= new Event();
		event.widget= fViewer.getTextWidget();
		event.display= fShell.getDisplay();
		event.stateMask= stateMask;
		return new MouseEvent(event);
	}

	private void waitUntil(BooleanSupplier condition) {
		Display display= fShell.getDisplay();
		long end= System.currentTimeMillis() + 5000;
		while (!condition.getAsBoolean() && System.currentTimeMillis() < end) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		assertTrue(condition.getAsBoolean(), "condition not met in time");
	}

	private void runEventLoop(long millis) {
		Display display= fShell.getDisplay();
		boolean[] done= { false };
		display.timerExec((int) millis, () -> done[0]= true);
		waitUntil(() -> done[0]);
	}

	private static class TestHyperlinkManager extends HyperlinkManager {
		int offset= 1;

		TestHyperlinkManager() {
			super(FIRST);
		}

		@Override
		protected int getCurrentTextOffset() {
			return offset;
		}
	}

	private class CountingDetector implements IHyperlinkDetector {
		@Override
		public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
			fDetections++;
			int offset= region.getOffset();
			if (offset < LINK_REGION.getOffset() || offset >= LINK_REGION.getOffset() + LINK_REGION.getLength()) {
				return null;
			}
			return new IHyperlink[] { new TestHyperlink() };
		}
	}

	private static class TestHyperlink implements IHyperlink {
		@Override
		public IRegion getHyperlinkRegion() {
			return LINK_REGION;
		}

		@Override
		public String getTypeLabel() {
			return null;
		}

		@Override
		public String getHyperlinkText() {
			return null;
		}

		@Override
		public void open() {
		}
	}

	private class TestPresenter implements IHyperlinkPresenter {
		@Override
		public boolean canShowMultipleHyperlinks() {
			return false;
		}

		@Override
		public void showHyperlinks(IHyperlink[] hyperlinks) {
			fShownLinks= hyperlinks;
		}

		@Override
		public void hideHyperlinks() {
			fShownLinks= null;
		}

		@Override
		public void install(ITextViewer textViewer) {
		}

		@Override
		public void uninstall() {
		}
	}
}
