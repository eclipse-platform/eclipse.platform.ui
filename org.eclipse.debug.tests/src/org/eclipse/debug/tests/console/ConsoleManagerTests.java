/*******************************************************************************
 * Copyright (c) 2016 Andrey Loskutov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.console;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;

import junit.framework.TestCase;

/**
 * Tests console manager
 */
public class ConsoleManagerTests extends TestCase {

	private static final String INTROVIEW_ID = "org.eclipse.ui.internal.introview"; //$NON-NLS-1$
	private ExecutorService executorService;
	private IConsoleManager manager;
	private int count;
	private CountDownLatch latch;
	private ConsoleMock[] consoles;
	ConsoleMock firstConsole;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		count = 20;
		latch = new CountDownLatch(count);
		executorService = Executors.newFixedThreadPool(count);
		manager = ConsolePlugin.getDefault().getConsoleManager();
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		hideWelcomePage(activePage);
		processUIEvents(100);
		consoles = new ConsoleMock[count];
		for (int i = 0; i < count; i++) {
			final ConsoleMock console = new ConsoleMock(i + 1);
			consoles[i] = console;
		}
		// register consoles (this does *not* show anything)
		manager.addConsoles(consoles);

		IViewPart consoleView = activePage.showView("org.eclipse.ui.console.ConsoleView"); //$NON-NLS-1$
		activePage.activate(consoleView);
		processUIEvents(100);

		// The test is unstable ("show" event on the the first console seem to
		// be not always sent), so make sure console view has shown at least
		// one console for real before starting the main test
		firstConsole = new ConsoleMock(0);
		manager.addConsoles(new ConsoleMock[] { firstConsole });
		manager.showConsoleView(firstConsole);
		waitForJobs();
		processUIEvents(100);
	}

	@Override
	protected void tearDown() throws Exception {
		executorService.shutdownNow();
		manager.removeConsoles(consoles);
		manager.removeConsoles(new ConsoleMock[] { firstConsole });
		processUIEvents(100);
		super.tearDown();
	}

	private void hideWelcomePage(IWorkbenchPage activePage) {
		IViewReference[] refs = activePage.getViewReferences();
		IViewPart intro = null;
		for (IViewReference ref : refs) {
			if (INTROVIEW_ID.equals(ref.getId())) {
				intro = ref.getView(false);
			}
		}
		if (intro != null) {
			activePage.hideView(intro);
			processUIEvents(100);
		}
	}

	/**
	 * The test triggers {@link #count} simultaneous calls to the
	 * {@link IConsoleManager#showConsoleView(IConsole)} and checks if all of
	 * this calls were properly proceeded by the manager.
	 * <p>
	 * See bug 489546.
	 *
	 * @throws Exception
	 */
	public void testShowAllConsoles() throws Exception {
		// Create a number of threads which will start and wait for the last one
		// created to call ConsoleManager.show.
		for (ConsoleMock console : consoles) {
			showConsole(console);
		}
		// Console manager starts a job with delay, let wait for him a bit
		waitForJobs();

		// Give UI a chance to proceed pending console manager jobs
		processUIEvents(3000);

		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);
		List<ConsoleMock> shown = new ArrayList<>();
		for (ConsoleMock console : consoles) {
			if (console.showCalled > 0) {
				shown.add(console);
			}
		}
		assertEquals("Only " + shown.size() + " consoles were shown from " + count, count, shown.size()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void processUIEvents(final long millis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < millis) {
			PlatformUI.getWorkbench().getDisplay().readAndDispatch();
		}
	}

	private void waitForJobs() throws Exception {
		if (Display.getCurrent() == null) {
			Thread.sleep(200);
		} else {
			processUIEvents(200);
		}
		while (!Job.getJobManager().isIdle()) {
			if (Display.getCurrent() == null) {
				Thread.sleep(200);
			} else {
				processUIEvents(200);
			}
		}
	}

	private void showConsole(final ConsoleMock console) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				// last one arriving here triggers execution for all at same
				// time
				latch.countDown();
				try {
					latch.await(1, TimeUnit.MINUTES);
					System.out.println("Requesting to show: " + console); //$NON-NLS-1$
					manager.showConsoleView(console);
					waitForJobs();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Dummy console page showing mock number and counting the numbers its
	 * control was shown in the console view.
	 */
	static final class ConsoleMock implements IConsole {
		MessagePage page;
		volatile int showCalled;
		final int number;

		public ConsoleMock(int number) {
			this.number = number;
		}

		@Override
		public void removePropertyChangeListener(IPropertyChangeListener listener) {
		}

		@Override
		public String getType() {
			return null;
		}

		@Override
		public String getName() {
			return toString();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public void addPropertyChangeListener(IPropertyChangeListener listener) {
		}

		/**
		 * Just a page showing the mock console name
		 */
		@Override
		public IPageBookViewPage createPage(IConsoleView view) {
			page = new MessagePage() {
				@Override
				public void createControl(Composite parent) {
					super.createControl(parent);
					// This listener is get called if the page is really shown
					// in the console view
					getControl().addListener(SWT.Show, new Listener() {
						@Override
						public void handleEvent(Event event) {
							showCalled++;
							System.out.println("Shown: " + ConsoleMock.this); //$NON-NLS-1$
						}
					});
				}

			};
			page.setMessage(toString());
			return page;
		}

		@Override
		public String toString() {
			return "mock #" + number; //$NON-NLS-1$
		}
	}
}
