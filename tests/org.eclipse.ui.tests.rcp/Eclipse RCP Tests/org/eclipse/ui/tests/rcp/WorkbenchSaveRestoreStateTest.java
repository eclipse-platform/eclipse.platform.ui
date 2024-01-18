/*******************************************************************************
 * Copyright (c) 2004, 2006, 2016 IBM Corporation and others.
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
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class WorkbenchSaveRestoreStateTest {

	private static final String ADVISOR_STATE_KEY = "advisorStateKey";
	private static final String WINDOW_ADVISOR_STATE_KEY = "windowAdvisorStateKey";
	private static final String ACTIONBAR_ADVISOR_STATE_KEY = "actionBarAdvisorStateKey";


	private Display display = null;

	@BeforeEach
	public void setUp() throws Exception {
		assertNull(display);
		display = PlatformUI.createDisplay();
		assertNotNull(display);
	}

	@AfterEach
	public void tearDown() throws Exception {
		assertNotNull(display);
		display.dispose();
		assertTrue(display.isDisposed());

	}

	/**
	 * Test save/restore state lifecycle API for WorkbenchAdvisor
	 */
	@Test
	public void testSaveRestoreAdvisorState() {
		final String advisorStateData = Long.toString(System.currentTimeMillis());

		// launch workbench and save some state data
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public IStatus saveState (IMemento memento) {
				assertNotNull(memento);
				memento.putString(ADVISOR_STATE_KEY, advisorStateData);
				return super.saveState(memento);
			}

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}

			@Override
			public void eventLoopIdle(Display d) {
				workbenchConfig.getWorkbench().restart();
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_RESTART, code);
		assertFalse(display.isDisposed());
		display.dispose();
		assertTrue(display.isDisposed());

		// restore the workbench and check for state data
		display = PlatformUI.createDisplay();
		WorkbenchAdvisorObserver wa2 = new WorkbenchAdvisorObserver(1) {

			@Override
			public IStatus restoreState(IMemento memento) {
				assertNotNull(memento);
				String stateData = memento.getString(ADVISOR_STATE_KEY);
				assertNotNull(stateData);
				assertTrue(advisorStateData.equals(stateData));
				return super.restoreState(memento);
			}

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}
		};

		int code2 = PlatformUI.createAndRunWorkbench(display, wa2);
		assertEquals(PlatformUI.RETURN_OK, code2);
	}

	/**
	 * Test save/restore state lifecycle API for WorkbenchWindowAdvisor
	 */
	@Test
	public void testSaveRestoreWindowState() {
		final String advisorStateData = Long.toString(System.currentTimeMillis());

		// launch workbench and save some state data
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					@Override
					public IStatus saveState(IMemento memento) {
						assertNotNull(memento);
						memento.putString(WINDOW_ADVISOR_STATE_KEY, advisorStateData);
						return super.saveState(memento);
					}
				};
			}

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}

			@Override
			public void eventLoopIdle(Display d) {
				workbenchConfig.getWorkbench().restart();
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_RESTART, code);
		assertFalse(display.isDisposed());
		display.dispose();
		assertTrue(display.isDisposed());

		// restore the workbench and check for state data
		display = PlatformUI.createDisplay();
		WorkbenchAdvisorObserver wa2 = new WorkbenchAdvisorObserver(1) {

			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					@Override
					public IStatus restoreState(IMemento memento) {
						assertNotNull(memento);
						String stateData = memento.getString(WINDOW_ADVISOR_STATE_KEY);
						assertNotNull(stateData);
						assertTrue(advisorStateData.equals(stateData));
						return super.restoreState(memento);
					}
				};
			}

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}
		};

		int code2 = PlatformUI.createAndRunWorkbench(display, wa2);
		assertEquals(PlatformUI.RETURN_OK, code2);
	}

	/**
	 * Test save/restore state lifecycle API for WorkbenchWindowAdvisor
	 */
	@Test
	public void testSaveRestoreActionBarState() {
		final String advisorStateData = Long.toString(System.currentTimeMillis());

		// launch workbench and save some state data
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					@Override
					public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer1) {
						return new ActionBarAdvisor(configurer1) {
							@Override
							public IStatus saveState(IMemento memento) {
								assertNotNull(memento);
								memento.putString(ACTIONBAR_ADVISOR_STATE_KEY, advisorStateData);
								return super.saveState(memento);
							}
						};
					}
				};
			}

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}

			@Override
			public void eventLoopIdle(Display d) {
				workbenchConfig.getWorkbench().restart();
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_RESTART, code);
		assertFalse(display.isDisposed());
		display.dispose();
		assertTrue(display.isDisposed());

		// restore the workbench and check for state data
		display = PlatformUI.createDisplay();
		WorkbenchAdvisorObserver wa2 = new WorkbenchAdvisorObserver(1) {

			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					@Override
					public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer1) {
						return new ActionBarAdvisor(configurer1) {
							@Override
							public IStatus restoreState(IMemento memento) {
								assertNotNull(memento);
								String stateData = memento.getString(ACTIONBAR_ADVISOR_STATE_KEY);
								assertNotNull(stateData);
								assertTrue(advisorStateData.equals(stateData));
								return super.restoreState(memento);
							}
						};
					}
				};
			}

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}
		};

		int code2 = PlatformUI.createAndRunWorkbench(display, wa2);
		assertEquals(PlatformUI.RETURN_OK, code2);
	}

	/**
	 * Test on-demand save/restore state API
	 */
	@Disabled
	@Test
	public void testOnDemandSaveRestoreState() {

		// save some window state on demand
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}

			@Override
			public void eventLoopIdle(Display d) {
				workbenchConfig.getWorkbench().restart();
			}

			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					@Override
					public void postWindowOpen() {
						File stateLocation = getStateFileLocation();
						ensureDirectoryExists(stateLocation);
						String stateFileName = stateLocation.getPath() + File.separator
								+ "testOnDemandSaveRestoreState.xml";

						OutputStreamWriter writer = null;
						try {
							writer = new OutputStreamWriter(new FileOutputStream(stateFileName),
									StandardCharsets.UTF_8);

						} catch (FileNotFoundException e1) {
							// creating a new file, won't happen unless the path
							// eclipse
							// specifies is totally wrong, or its read-only
						}

						XMLMemento xmlm = XMLMemento.createWriteRoot("TestState");
						saveState(xmlm);

						try {
							xmlm.save(writer);
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_RESTART, code);
		assertFalse(display.isDisposed());
		display.dispose();
		assertTrue(display.isDisposed());

		// restore the workbench and restore a window
		// with state data on demand
		display = PlatformUI.createDisplay();
		WorkbenchAdvisorObserver wa2 = new WorkbenchAdvisorObserver(1) {

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}

			@Override
			public boolean openWindows() {
				File stateLocation = getStateFileLocation();
				String stateFileName = "testOnDemandSaveRestoreState.xml";
				File stateFile = new File(stateLocation.getPath() + File.separator + stateFileName);
				assertTrue(stateFile.exists());

				IMemento memento = null;
				try {
					memento = XMLMemento.createReadRoot(
							new InputStreamReader(new FileInputStream(stateFile), StandardCharsets.UTF_8));
				} catch (WorkbenchException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// won't happen because we already checked it exists
				}

				assertNotNull(memento);
				IWorkbenchWindowConfigurer window = null;
				try {
					window = getWorkbenchConfigurer().restoreWorkbenchWindow(memento);
				} catch (WorkbenchException e) {
					e.printStackTrace();
				}
				assertNotNull(window);
				return true;
			}

			@Override
			public void postWindowRestore(IWorkbenchWindowConfigurer configurer) throws WorkbenchException {
				// TODO Auto-generated method stub
				super.postWindowRestore(configurer);
			}
		};

		int code2 = PlatformUI.createAndRunWorkbench(display, wa2);
		assertEquals(PlatformUI.RETURN_OK, code2);
	}

	private File getStateFileLocation() {
		IPath path = UIPlugin.getDefault().getStateLocation();
		StringBuilder fileName = new StringBuilder();
		fileName.append(File.separator);
		fileName.append("TestWorkbenchState");
		fileName.append(File.separator);

		File stateLocation = path.append(fileName.toString()).toFile();
		ensureDirectoryExists(stateLocation);

		return stateLocation;
	}

	private void ensureDirectoryExists(File directory) {
		directory.mkdirs();
	}

}
