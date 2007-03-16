/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

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

public class WorkbenchSaveRestoreStateTest extends TestCase {
	
	private static final String ADVISOR_STATE_KEY = "advisorStateKey";
	private static final String WINDOW_ADVISOR_STATE_KEY = "windowAdvisorStateKey";
	private static final String ACTIONBAR_ADVISOR_STATE_KEY = "actionBarAdvisorStateKey";
	
    public WorkbenchSaveRestoreStateTest(String name) {
        super(name);
    }

    private Display display = null;

    protected void setUp() throws Exception {
        super.setUp();

        assertNull(display);
		display = PlatformUI.createDisplay();
        assertNotNull(display);
    }

    protected void tearDown() throws Exception {
        assertNotNull(display);
        display.dispose();
        assertTrue(display.isDisposed());

        super.tearDown();
    }

	/**
	 * Test save/restore state lifecycle API for WorkbenchAdvisor
	 */
	public void testSaveRestoreAdvisorState() {		
		final String advisorStateData = Long.toString(System.currentTimeMillis());
		
		// launch workbench and save some state data
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
		
			public IStatus saveState (IMemento memento) {
				assertNotNull(memento);
				memento.putString(ADVISOR_STATE_KEY, advisorStateData);
				return super.saveState(memento);
			}
			
			public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                c.setSaveAndRestore(true);
            }

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

			public IStatus restoreState(IMemento memento) {
				assertNotNull(memento);
				String stateData = memento.getString(ADVISOR_STATE_KEY);
				assertNotNull(stateData);
				assertTrue(advisorStateData.equals(stateData));
				return super.restoreState(memento);
			}
			
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
	public void testSaveRestoreWindowState() {
		final String advisorStateData = Long.toString(System.currentTimeMillis());
		
		// launch workbench and save some state data
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
			
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					public IStatus saveState(IMemento memento) {
						assertNotNull(memento);
						memento.putString(WINDOW_ADVISOR_STATE_KEY, advisorStateData);
						return super.saveState(memento);
					}
				};
			}

			public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                c.setSaveAndRestore(true);
            }

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

			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					public IStatus restoreState(IMemento memento) {
						assertNotNull(memento);
						String stateData = memento.getString(WINDOW_ADVISOR_STATE_KEY);
						assertNotNull(stateData);
						assertTrue(advisorStateData.equals(stateData));
						return super.restoreState(memento);
					}
				};
			}
			
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
	public void testSaveRestoreActionBarState() {
		final String advisorStateData = Long.toString(System.currentTimeMillis());
		
		// launch workbench and save some state data
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
			
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer1) {
						return new ActionBarAdvisor(configurer1) {
							public IStatus saveState(IMemento memento) {
								assertNotNull(memento);
								memento.putString(ACTIONBAR_ADVISOR_STATE_KEY, advisorStateData);
								return super.saveState(memento);
							}
						};
					}
				};
			}

			public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                c.setSaveAndRestore(true);
            }

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

			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer1) {
						return new ActionBarAdvisor(configurer1) {
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
	public void testOnDemandSaveRestoreState() {
		
		// save some window state on demand
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
			
			public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                c.setSaveAndRestore(true);
            }

            public void eventLoopIdle(Display d) {
                workbenchConfig.getWorkbench().restart();
            }
			
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {
					public void postWindowOpen() {
						File stateLocation = getStateFileLocation();
						ensureDirectoryExists(stateLocation);
						String stateFileName = stateLocation.getPath() + File.separator + "testOnDemandSaveRestoreState.xml";
							
					    OutputStreamWriter writer = null;
				        try {
				            writer = new OutputStreamWriter(new FileOutputStream(stateFileName),"UTF-8");
				            
				        } catch (UnsupportedEncodingException e1) {
				            // not possible, UTF-8 is required to be implemented by all JVMs
				        } catch (FileNotFoundException e1) {
				            // creating a new file, won't happen  unless the path eclipse 
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
			
			public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                c.setSaveAndRestore(true);
            }
			
			public boolean openWindows() {
				File stateLocation = getStateFileLocation();
		        String stateFileName = "testOnDemandSaveRestoreState.xml";
		        File stateFile = new File(stateLocation.getPath() + File.separator + stateFileName);
		        assertTrue(stateFile.exists());
	            
				IMemento memento = null;
		        try {
					memento = XMLMemento.createReadRoot( new InputStreamReader (
							new FileInputStream(stateFile),"UTF-8"));
		        } catch (WorkbenchException e) {
		            e.printStackTrace();
		        } catch (FileNotFoundException e) {
		            // won't happen because we already checked it exists
		        } catch (UnsupportedEncodingException e) {
		           // not possible - UTF8 is required
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
	    StringBuffer fileName = new StringBuffer();
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
