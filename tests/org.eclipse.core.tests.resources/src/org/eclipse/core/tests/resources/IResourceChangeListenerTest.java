/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class IResourceChangeListenerTest extends EclipseWorkspaceTest {
	ResourceDeltaVerifier verifier;

	protected static final String VERIFIER_NAME = "TestListener";

	/* some random resource handles */
	IProject project1;
	IProject project2;
	IFolder folder1;//below project2
	IFolder folder2;//below folder1
	IFolder folder3;//same as file1
	IFile file1;//below folder1
	IFile file2;//below folder1
	IFile file3;//below folder2
	IFile project1MetaData;
	IFile project2MetaData;
public IResourceChangeListenerTest() {
}
public IResourceChangeListenerTest(String name) {
	super(name);
}
public void _testBenchMark_1GBYQEZ() {
	// start with a clean workspace
	getWorkspace().removeResourceChangeListener(verifier);
	try {
		getWorkspace().getRoot().delete(false, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	// create the listener
	IResourceChangeListener listener = new IResourceChangeListener() {
		public int fCounter;
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				System.out.println("Start");
				for (int i = 0; i < 10; i++) {
					fCounter = 0;
					long start = System.currentTimeMillis();
					IResourceDelta delta = event.getDelta();
					delta.accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta) throws CoreException {
							fCounter++;
							return true;
						}
					});
					long end = System.currentTimeMillis();
					System.out.println("    Number of deltas: " + fCounter + ". Time needed: " + (end - start));
				}
				System.out.println("End");
			} catch (CoreException e) {
				fail("1.0", e);
			}
		}
	};

	// add the listener
	getWorkspace().addResourceChangeListener(listener);

	// setup the test data
	IWorkspaceRunnable body = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			IProject project = getWorkspace().getRoot().getProject("Test");
			IProjectDescription description = getWorkspace().newProjectDescription(project.getName());
			IPath root = getWorkspace().getRoot().getLocation();
			IPath contents = root.append("temp/testing");
			description.setLocation(contents);
			project.create(description, getMonitor());
			project.open(getMonitor());
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		}
	};
	try {
		getWorkspace().run(body, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}

	// touch all resources (so that they appear in the delta)
	body = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					resource.touch(getMonitor());
					return true;
				}
			};
			getWorkspace().getRoot().accept(visitor);
		}
	};
	try {
		getWorkspace().run(body, getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}

	// un-register our listener
	getWorkspace().removeResourceChangeListener(listener);
}
/**
 * Tests that the builder is receiving an appropriate delta
 * @see SortBuilderPlugin
 * @see SortBuilder
 */
public void assertDelta() {
	assertTrue(verifier.getMessage(), verifier.isDeltaValid());
}
/**
 * Runs code to handle a core exception
 */
protected void handleCoreException(CoreException e) {
	assertTrue("CoreException: " + e.getMessage(), false);
}
/**
 * Sets up the fixture, for example, open a network connection.
 * This method is called before a test is executed.
 */
protected void setUp() throws Exception {
	super.setUp();

	// Create some resource handles
	project1 = getWorkspace().getRoot().getProject("Project" + 1);
	project2 = getWorkspace().getRoot().getProject("Project" + 2);
	folder1 = project1.getFolder("Folder" + 1);
	folder2 = folder1.getFolder("Folder" + 2);
	folder3 = folder1.getFolder("File" + 1);
	file1 = folder1.getFile("File" + 1);
	file2 = folder1.getFile("File" + 2);
	file3 = folder2.getFile("File" + 1);
	project1MetaData = project1.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
	project2MetaData = project2.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);

	// Create and open a project, folder and file
	IWorkspaceRunnable body = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			project1.create(getMonitor());
			project1.open(getMonitor());
			folder1.create(true, true, getMonitor());
			file1.create(getRandomContents(), true, getMonitor());
		}
	};
	try {
		getWorkspace().run(body, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	verifier = new ResourceDeltaVerifier();
	getWorkspace().addResourceChangeListener(verifier);
}
public static Test suite() {
	return new TestSuite(IResourceChangeListenerTest.class);
}
/**
 * Tears down the fixture, for example, close a network connection.
 * This method is called after a test is executed.
 */
protected void tearDown() throws Exception {
	super.tearDown();
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	getWorkspace().removeResourceChangeListener(verifier);
}
public void testAddAndRemoveFile() {
	try {
		verifier.reset();
		getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run (IProgressMonitor m) throws CoreException {
					m.beginTask("Creating and deleting", 100);
					try {
						file2.create(getRandomContents(), true, new SubProgressMonitor(m, 50));
						file2.delete(true, new SubProgressMonitor(m, 50));
					}
					finally {
						m.done();
					}
				}
			}, getMonitor());
		//should not have been verified since there was no change
		assertTrue("Unexpected notification on no change", !verifier.hasBeenNotified());
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testMulti() {

	class Listener1 implements IResourceChangeListener {
		public boolean done = false;
		public void resourceChanged(IResourceChangeEvent event) {
			assertEquals("1.0", IResourceChangeEvent.POST_CHANGE, event.getType());
			done = true;
		}
	}

	class Listener2 extends Listener1 implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			assertEquals("2.0", IResourceChangeEvent.POST_AUTO_BUILD, event.getType());
			done = true;
		}
	}

	Listener1 listener1 = new Listener1();
	Listener2 listener2 = new Listener2();

	getWorkspace().addResourceChangeListener(listener1, IResourceChangeEvent.POST_CHANGE);
	getWorkspace().addResourceChangeListener(listener2, IResourceChangeEvent.POST_AUTO_BUILD);

	try {
		project1.touch(getMonitor());
	} catch (CoreException e) {
		handleCoreException(e);
	}

	assertTrue("3.0", listener1.done);
	assertTrue("3.1", listener2.done);
	getWorkspace().removeResourceChangeListener(listener1);
	getWorkspace().removeResourceChangeListener(listener2);
}
public void testAddAndRemoveFolder() {
	try {
		verifier.reset();
		getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run (IProgressMonitor m) throws CoreException {
					m.beginTask("Creating and deleting", 100);
					try {
						folder2.create(true, true, new SubProgressMonitor(m, 50));
						folder2.delete(true, new SubProgressMonitor(m, 50));
					}
					finally {
						m.done();
					}
				}
			}, getMonitor());
		//should not have been verified since there was no change
		assertTrue("Unexpected notification on no change", !verifier.hasBeenNotified());

	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testAddFile() {
	try {
		verifier.addExpectedChange(file2, IResourceDelta.ADDED, 0);
		file2.create(getRandomContents(), true, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testAddFileAndFolder() {
	try {
		verifier.addExpectedChange(folder2, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(file3, IResourceDelta.ADDED, 0);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Creating folder and file", 100);
				try {
					folder2.create(true, true, new SubProgressMonitor(m, 50));
					file3.create(getRandomContents(), true, new SubProgressMonitor(m, 50));
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testAddFolder() {
	try {
		verifier.addExpectedChange(folder2, IResourceDelta.ADDED, 0);
		folder2.create(true, true, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testAddProject() {
	try {
		verifier.addExpectedChange(project2, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(project2MetaData, IResourceDelta.ADDED, 0);
		project2.create(getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testChangeFile() {
	try {
		/* change file1's contents */
		verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.CONTENT);
		file1.setContents(getRandomContents(), true, false, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testChangeFileToFolder() {
	try {
		/* change file1 into a folder */
		verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.CONTENT | IResourceDelta.TYPE | IResourceDelta.REPLACED);
		getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run (IProgressMonitor m) throws CoreException {
					m.beginTask("Deleting and Creating", 100);
					try {
						file1.delete(true, new SubProgressMonitor(m, 50));
						folder3.create(true, true, new SubProgressMonitor(m, 50));
					}
					finally {
						m.done();
					}
				}
			}, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testChangeFolderToFile() {
	try {
		/* change to a folder */
		file1.delete(true, getMonitor());
		folder3.create(true, true, getMonitor());

		/* now change back to a file and verify */
		verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.CONTENT | IResourceDelta.TYPE | IResourceDelta.REPLACED);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Deleting and Creating", 100);
				try {
					folder3.delete(true, new SubProgressMonitor(m, 50));
					file1.create(getRandomContents(), true, new SubProgressMonitor(m, 50));
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testChangeProject() {
	try {
		project2.create(getMonitor());
		project2.open(getMonitor());
		IProjectDescription desc = project2.getDescription();
		desc.setReferencedProjects(new IProject[] {project1});
		verifier.addExpectedChange(project2, IResourceDelta.CHANGED, IResourceDelta.DESCRIPTION);
		verifier.addExpectedChange(project2MetaData, IResourceDelta.CHANGED, IResourceDelta.CONTENT);
		project2.setDescription(desc, IResource.FORCE, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testCloseOpenReplaceFile() {
	try {
		// FIXME: how to do this?
		//workspace.save(getMonitor());
		//workspace.close(getMonitor());
		//workspace.open(getMonitor());
		verifier.reset();
		getWorkspace().addResourceChangeListener(verifier);

		/* change file1's contents */
		verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.REPLACED | IResourceDelta.CONTENT);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Deleting and Creating", 100);
				try {
					file1.delete(true, new SubProgressMonitor(m, 50));
					file1.create(getRandomContents(), true, new SubProgressMonitor(m, 50));
				} finally {
					m.done();
				}
			}
		}, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testDeleteInPostBuildListener() {
	// create the resource change listener
	IResourceChangeListener listener = new IResourceChangeListener() {
		public void resourceChanged(final IResourceChangeEvent event) {
			try {
				event.getDelta().accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						IResource resource = delta.getResource();
						if (resource.getType() == IResource.FILE) {
							try {
								((IFile)resource).delete(true, true, null);
							} catch (RuntimeException e) {
								throw e;
							}
						}
						return true;
					}
				});
			} catch (CoreException e) {
				fail("1.0", e);
			}
		}
	};
	// register the listener with the workspace.
	getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_AUTO_BUILD);
	try {
		getWorkspace().run(new IWorkspaceRunnable() {
			// cause a delta by touching all resources
			public void run(IProgressMonitor monitor) throws CoreException {
				getWorkspace().getRoot().accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					resource.touch(getMonitor());
					return true;
				}
				});
			}
		}, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	} finally {
		// cleanup: ensure that the listener is removed
		getWorkspace().removeResourceChangeListener(listener);
	}
}
public void testDeleteProject() throws CoreException {
	//test that marker deltas are fired when projects are deleted
	final IMarker marker = project1.createMarker(IMarker.TASK);
	IResourceChangeListener listener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			IMarkerDelta[] deltas = event.findMarkerDeltas(IMarker.TASK, false);
			assertEquals("1.0", 1, deltas.length);
			assertEquals("1.1", marker.getId(), deltas[0].getId());
			assertEquals("1.2", IResourceDelta.REMOVED, deltas[0].getKind());
		}
	};
	try {
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		project1.delete(true, false, getMonitor());
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
	}
}
public void testMoveFile() {
	try {
		verifier.addExpectedChange(folder2, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(file1, IResourceDelta.REMOVED, IResourceDelta.MOVED_TO, file3.getFullPath());
		verifier.addExpectedChange(file3, IResourceDelta.ADDED, IResourceDelta.MOVED_FROM, file1.getFullPath());
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Creating and moving", 100);
				try {
					folder2.create(true, true, new SubProgressMonitor(m, 50));
					file1.move(file3.getFullPath(), true, new SubProgressMonitor(m, 50));
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testModifyMoveFile() {
	try {
		verifier.addExpectedChange(folder2, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(file1, IResourceDelta.REMOVED, IResourceDelta.MOVED_TO, file3.getFullPath());
		verifier.addExpectedChange(file3, IResourceDelta.ADDED, IResourceDelta.MOVED_FROM | IResourceDelta.CONTENT, file1.getFullPath());
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Creating and moving", 100);
				try {
					folder2.create(true, true, new SubProgressMonitor(m, 50));
					file1.setContents(getRandomContents(), IResource.NONE, getMonitor());
					file1.move(file3.getFullPath(), true, new SubProgressMonitor(m, 50));
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testMoveFileAddMarker() {
	try {
		verifier.addExpectedChange(folder2, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(file1, IResourceDelta.REMOVED, IResourceDelta.MOVED_TO, file3.getFullPath());
		verifier.addExpectedChange(file3, IResourceDelta.ADDED, IResourceDelta.MOVED_FROM | IResourceDelta.MARKERS, file1.getFullPath());
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Creating and moving", 100);
				try {
					folder2.create(true, true, new SubProgressMonitor(m, 50));
					file1.move(file3.getFullPath(), true, new SubProgressMonitor(m, 50));
					file3.createMarker(IMarker.TASK);
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testMoveModifyFile() {
	try {
		verifier.addExpectedChange(folder2, IResourceDelta.ADDED, 0);
		verifier.addExpectedChange(file1, IResourceDelta.REMOVED, IResourceDelta.MOVED_TO, file3.getFullPath());
		verifier.addExpectedChange(file3, IResourceDelta.ADDED, IResourceDelta.MOVED_FROM | IResourceDelta.CONTENT, file1.getFullPath());
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Creating and moving", 100);
				try {
					folder2.create(true, true, new SubProgressMonitor(m, 50));
					file1.move(file3.getFullPath(), true, new SubProgressMonitor(m, 50));
					file3.setContents(getRandomContents(), IResource.NONE, getMonitor());
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testRemoveFile() {
	try {
		verifier.addExpectedChange(file1, IResourceDelta.REMOVED, 0);
		file1.delete(true, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testRemoveFileAndFolder() {
	try {
		verifier.addExpectedChange(folder1, IResourceDelta.REMOVED, 0);
		verifier.addExpectedChange(file1, IResourceDelta.REMOVED, 0);
		folder1.delete(true, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testReplaceFile() {
	try {
		/* change file1's contents */
		verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.REPLACED | IResourceDelta.CONTENT);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("Deleting and Creating", 100);
				try {
					file1.delete(true, new SubProgressMonitor(m, 50));
					file1.create(getRandomContents(), true, new SubProgressMonitor(m, 50));
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testSetLocal() {
	try {
		verifier.reset();
		//set local on a file that is already local -- should be no change
		file1.setLocal(true, IResource.DEPTH_INFINITE, getMonitor());
		assertTrue("Unexpected notification on no change", !verifier.hasBeenNotified());
		
		//set non-local, still shouldn't appear in delta
		verifier.reset();
		file1.setLocal(false, IResource.DEPTH_INFINITE, getMonitor());
		assertTrue("Unexpected notification on no change", !verifier.hasBeenNotified());
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
public void testTwoFileChanges() {
	try {
		verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.CONTENT);
		verifier.addExpectedChange(file2, IResourceDelta.ADDED, 0);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("setting contents and creating", 100);
				try {
					file1.setContents(getRandomContents(), true, false,  new SubProgressMonitor(m, 50));
					file2.create(getRandomContents(), true, new SubProgressMonitor(m, 50));
				} finally {
					m.done();
				}
			}
		}
		, getMonitor());
		assertDelta();
	} catch (CoreException e) {
		handleCoreException(e);
	}
}
/**
 * Tests that team private members don't show up in resource deltas when
 * standard traversal and visitor are used.
 */
public void testTeamPrivateChanges() {
	IWorkspace workspace = getWorkspace();
	final IFolder teamPrivateFolder = project1.getFolder("TeamPrivateFolder");
	final IFile teamPrivateFile = folder1.getFile("TeamPrivateFile");
	final IResource[] privateResources = new IResource[] {teamPrivateFolder, teamPrivateFile};
	IResourceChangeListener listener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			//make sure the delta doesn't include the team private members
			assertNotDeltaIncludes("1.0", event.getDelta(), privateResources);
			//make sure a visitor does not find team private members
			assertNotDeltaVisits("1.1", event.getDelta(), privateResources);
		}
	};
	workspace.addResourceChangeListener(listener);
	try {
		//create a team private folder
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				teamPrivateFolder.create(true, true, getMonitor());
				teamPrivateFolder.setTeamPrivateMember(true);
			}
		}, getMonitor());
		//create children in team private folder
		IFile fileInFolder = teamPrivateFolder.getFile("FileInPrivateFolder");
		fileInFolder.create(getRandomContents(), true, getMonitor());
		//modify children in team private folder
		fileInFolder.setContents(getRandomContents(), IResource.NONE, getMonitor());
		//delete children in team private folder
		fileInFolder.delete(IResource.NONE, getMonitor());
		//delete team private folder
		teamPrivateFolder.delete(IResource.NONE, getMonitor());
		//create team private file
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				teamPrivateFile.create(getRandomContents(), true, getMonitor());
				teamPrivateFile.setTeamPrivateMember(true);
			}
		}, getMonitor());
		//modify team private file
		teamPrivateFile.setContents(getRandomContents(), IResource.NONE, getMonitor());
		//delete team private file
		teamPrivateFile.delete(IResource.NONE, getMonitor());
	} catch (CoreException e) {
		handleCoreException(e);
	} finally {
		workspace.removeResourceChangeListener(listener);
	}
}
/**
 * Asserts that a manual traversal of the delta does not find the given resources.
 */
private void assertNotDeltaIncludes(String message, IResourceDelta delta, IResource[] resources) {
	IResource deltaResource = delta.getResource();
	for (int i = 0; i < resources.length; i++) {
		assertTrue(message, !deltaResource.equals(resources[i]));
	}
	IResourceDelta[] children = delta.getAffectedChildren();
	for (int i = 0; i < children.length; i++) {
		assertNotDeltaIncludes(message, children[i], resources);
	}
}
/**
 * Asserts that a visitor traversal of the delta does not find the given resources.
 */
private void assertNotDeltaVisits(final String message, IResourceDelta delta, final IResource[] resources) {
	try {
		delta.accept(new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource deltaResource = delta.getResource();
				for (int i = 0; i < resources.length; i++) {
					assertTrue(message, !deltaResource.equals(resources[i]));
				}
				return true;
			}
		});
	} catch (CoreException e) {
		fail(message, e);
	}
}
/*
 * Create a resource change listener and register it for POST_AUTO_BUILD events.
 * Ensure that you are able to modify the workspace tree.
 */
public void test_1GDK9OG() {
	// create the resource change listener
	IResourceChangeListener listener = new IResourceChangeListener() {
		public void resourceChanged(final IResourceChangeEvent event) {
			try {
				IWorkspaceRunnable body = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						// modify the tree.
						IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
							public boolean visit(IResourceDelta delta) throws CoreException {
								IResource resource = delta.getResource();
								try {
									resource.touch(getMonitor());
								} catch (RuntimeException e) {
									throw e;
								}
								resource.createMarker(IMarker.PROBLEM);
								return true;
							}
						};
						event.getDelta().accept(visitor);
					}
				};
				getWorkspace().run(body, getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}
		}
	};
	// register the listener with the workspace.
	getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_AUTO_BUILD);
	try {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			// cause a delta by touching all resources
			final IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					resource.touch(getMonitor());
					return true;
				}
			};
			public void run(IProgressMonitor monitor) throws CoreException {
				getWorkspace().getRoot().accept(visitor);
			}
		};
		getWorkspace().run(body, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	} finally {
		// cleanup: ensure that the listener is removed
		getWorkspace().removeResourceChangeListener(listener);
	}
}
}
