package org.eclipse.core.tests.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.*;
import java.io.*;
import java.util.*;
import junit.framework.*;
import junit.textui.TestRunner;
//
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
//
public class IResourceTest extends EclipseWorkspaceTest {


	static boolean noSideEffects = false;
	protected static IResource[] interestingResources;
	protected static Set nonExistingResources = new HashSet();
	protected static Set unsynchronizedResources = new HashSet();
	protected static IPath[] interestingPaths;

	protected static final Boolean[] TRUE_AND_FALSE = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
	protected static final Boolean[] FALSE_AND_TRUE = new Boolean[] {Boolean.FALSE, Boolean.TRUE};
	protected static final IProgressMonitor[] PROGRESS_MONITORS = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};

	/* the delta verifier */
	ResourceDeltaVerifier verifier;

	/**
	 * Resource only exists in the workspace.  It has been deleted
	 * from the filesystem manually
	 */
	protected static final int S_WORKSPACE_ONLY = 0;

	/**
	 * Resource exists in the filesystem only.  It has been added
	 * to the filesystem manually since the last local refresh.
	 */
	protected static final int S_FILESYSTEM_ONLY = 1;

	/**
	 * Resource exists in the filesytem and workspace, and is in sync
	 */
	protected static final int S_UNCHANGED = 2;

	/**
	 * Resource exists in both filesystem and workspace, but
	 * has been changed in the filesystem since the last sync.
	 * This only applies to files.
	 */
	protected static final int S_CHANGED = 3;

	/**
	 * Resource does not exist in filesystem or workspace.
	 */
	protected static final int S_DOES_NOT_EXIST = 4;

	/**
	 * Resource is a folder in the workspace, but has been converted
	 * to a file in the filesystem.
	 */
	protected static final int S_FOLDER_TO_FILE = 5;

	/**
	 * Resource is a file in the workspace, but has been converted
	 * to a folder in the filesystem.
	 */
	protected static final int S_FILE_TO_FOLDER = 6;
public IResourceTest() {
}
public IResourceTest(String name) {
	super(name);
}
private IResource[] buildSampleResources(IContainer root) throws CoreException {
	// do not change the example resources unless you change references to specific indices in setUp()
	IResource[] result = buildResources(root, new String[] {"1/", "1/1/", "1/1/1/", "1/1/1/1", "1/1/2/", "1/1/2/1/", "1/1/2/2/", "1/1/2/3/", "1/2/", "1/2/1", "1/2/2", "1/2/3/", "1/2/3/1", "1/2/3/2", "1/2/3/3", "1/2/3/4", "2", "2"});
	ensureExistsInWorkspace(result, true);
	result[result.length-1] = root.getFolder(new Path("2/"));
	nonExistingResources.add(result[result.length-1]);

	IResource[] deleted = buildResources(root, new String[] {"1/1/2/1/", "1/2/3/1"});
	ensureDoesNotExistInWorkspace(deleted);
	for(int i = 0; i < deleted.length; ++i){
		nonExistingResources.add(deleted[i]);
	}

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) {}
	
	IResource[] unsynchronized = buildResources(root, new String[] {"1/1/2/2/1", "1/2/3/3"});
	ensureExistsInFileSystem(unsynchronized);
	for(int i = 0; i < unsynchronized.length; ++i)
		unsynchronizedResources.add(unsynchronized[i]);

	return result;
}
/**
 * Checks that the after state is as expected.
 * @param receiver the resource that was the receiver of the refreshLocal call
 * @param target the resource that was out of sync
 */
protected boolean checkAfterState(IResource receiver, IResource target, int state, int depth) {
	assertTrue(verifier.getMessage(), verifier.isDeltaValid());
	switch (state) {
		case S_FILESYSTEM_ONLY:
			assertExistsInFileSystem(target);
			//if receiver was a parent, then refreshLocal
			//will have added the target
			if (hasParent(target, receiver, depth) || target.equals(receiver)) {
				assertExistsInWorkspace(target);
			} else {
				assertDoesNotExistInWorkspace(target);
			}
			break;
		case S_UNCHANGED:
		case S_CHANGED:
			assertExistsInWorkspace(target);
			assertExistsInFileSystem(target);
			break;
		case S_WORKSPACE_ONLY:
			assertDoesNotExistInFileSystem(target);
			//if receiver was a parent, then refreshLocal
			//will have deleted the target
			if (hasParent(target, receiver, depth) || target.equals(receiver)) {
				assertDoesNotExistInWorkspace(target);
			} else {
				assertExistsInWorkspace(target);
			}
			break;
		case S_DOES_NOT_EXIST:
			assertDoesNotExistInWorkspace(target);
			assertDoesNotExistInFileSystem(target);
			break;
		case S_FOLDER_TO_FILE:
			break;
		case S_FILE_TO_FOLDER:
			break;		
	}

	//now clean up the state
	try {
		tearDown();
	} catch (Exception e) {
		fail("Exception tearing down in checkAfterState", e);
	}

	return true;
}
/**
 * @return Set
 * @param File
 */
static protected Set getAllFilesForDirectory(File dir) {
	Set result = new HashSet(50);
	String[] members = dir.list();
	if (members != null) {
		for (int i = 0; i < members.length; i++) {
			File member = new File(dir, members[i]);
			result.add(member);
			if (member.isDirectory()) {
				result.addAll(getAllFilesForDirectory(member));
			}
		}
	}
	return result;
}
/**
 * @return Set
 * @param resource IResource
 */
static protected Set getAllFilesForResource(IResource resource, boolean considerUnsyncLocalFiles) throws CoreException {
	Set result = new HashSet(50);
	if (resource.getLocation() != null && (resource.getType() != IResource.PROJECT || ((IProject) resource).isOpen())) {
		java.io.File file = resource.getLocation().toFile();
		if (considerUnsyncLocalFiles) {
			if (file.exists()) {
				result.add(file);
				if (file.isDirectory()) {
					result.addAll(getAllFilesForDirectory(file));
				}
			}
		} else {
			if (resource.exists()) {
				result.add(file);
				if (resource.getType() != IResource.FILE) {
					IContainer container = (IContainer) resource;
					IResource[] children = container.members();
					for (int i = 0; i < children.length; i++) {
						IResource member = children[i];
						result.addAll(getAllFilesForResource(member, considerUnsyncLocalFiles));
					}
				}
			}
		}
	}
	return result;
}
/**
 * @return Set
 * @param resource IResource
 */
static protected Set getAllResourcesForResource(IResource resource) throws CoreException {
	Set result = new HashSet(50);
	if (resource.exists()) {
		result.add(resource);
		if (resource.getType() != IResource.FILE) {
			IContainer container = (IContainer) resource;
			IResource[] children = container.members();
			for (int i = 0; i < children.length; i++) {
				IResource member = children[i];
				result.addAll(getAllResourcesForResource(member));
			}
		}
	}
	return result;
}
/**
 * Returns an array of all projects in the given resource array.
 */
protected IProject[] getProjects(IResource[] resources) {
	ArrayList list = new ArrayList();
	for (int i = 0; i < resources.length; i++) {
		if (resources[i].getType() == IResource.PROJECT) {
			list.add(resources[i]);
		}
	}
	return (IProject[])list.toArray(new IProject[list.size()]);
}
/**
 * Returns true if resource1 has parent resource2, in range of the
 * given depth.  This is basically asking if refreshLocal on resource2
 * with depth "depth" will hit resource1.
 */
protected boolean hasParent(IResource resource1, IResource resource2, int depth) {
	if (depth == IResource.DEPTH_ZERO) return false;
	if (depth == IResource.DEPTH_ONE) {
		return resource2.equals(resource1.getParent());
	}
	IResource parent = resource1.getParent();
	while (parent != null) {
		if (parent.equals(resource2)) {
			return true;
		}
		parent = parent.getParent();
	}
	return false;
}
/**
 * Returns interesting resource states.
 */
protected Integer[] interestingDepths() {
	return new Integer[] {
		new Integer(IResource.DEPTH_ZERO),
		new Integer(IResource.DEPTH_ONE),
		new Integer(IResource.DEPTH_INFINITE)};
}
/**
 * Returns interesting resources.
 */
protected IResource[] interestingResources() {
	IProject emptyProject = getWorkspace().getRoot().getProject("EmptyProject");
	IProject fullProject = getWorkspace().getRoot().getProject("FullProject");
	IResource[] resources = buildResources(fullProject, defineHierarchy());

	IResource[] result = new IResource[resources.length + 2];
	result[0] = emptyProject;
	result[1] = fullProject;
	System.arraycopy(resources, 0, result, 2, resources.length);
	return result;
}
/**
 * Returns interesting resource states.
 */
protected Integer[] interestingStates() {
	return new Integer[] {
		new Integer(S_WORKSPACE_ONLY),
		new Integer(S_FILESYSTEM_ONLY),
		new Integer(S_UNCHANGED),
		new Integer(S_CHANGED),
		new Integer(S_DOES_NOT_EXIST)};
//		new Integer(S_FOLDER_TO_FILE),
//		new Integer(S_FILE_TO_FOLDER)};
}
protected boolean isFile(IResource r) {
	return r.getType() == IResource.FILE;
}
protected boolean isFolder(IResource r) {
	return r.getType() == IResource.FOLDER;
}
protected boolean isProject(IResource r) {
	return r.getType() == IResource.PROJECT;
}
/**
 * Returns true if this combination of arguments makes sense.
 */
protected boolean makesSense(IResource receiver, IResource target, int state, int depth) {
	/* don't allow projects as targets for now */
	if (isProject(target)) {
		return false;
	}

	/* target cannot be a parent of receiver */
	if (hasParent(receiver, target, IResource.DEPTH_INFINITE)) {
		return false;
	}

	/* target can only take certain forms for some states */
	switch (state) {
		case S_WORKSPACE_ONLY:
			return true;
		case S_FILESYSTEM_ONLY:
			return true;
		case S_UNCHANGED:
			return true;
		case S_CHANGED:
			return isFile(target);
		case S_DOES_NOT_EXIST:
			return true;
		case S_FOLDER_TO_FILE:
			return isFolder(target);
		case S_FILE_TO_FOLDER:
			return isFile(target);
	}

	return true;
}
protected void setUp() throws Exception {
	super.setUp();
	IWorkspaceDescription description = getWorkspace().getDescription();
	description.setAutoBuilding(false);
	getWorkspace().setDescription(description);

	if (noSideEffects) {
		noSideEffects = false;
		return;
	}

	try {
		// open project
		IProject openProject = getWorkspace().getRoot().getProject("openProject");
		openProject.create(null);
		openProject.open(null);
		IResource[] resourcesInOpenProject = buildSampleResources(openProject);

		// closed project
		IProject closedProject = getWorkspace().getRoot().getProject("closedProject");
		closedProject.create(null);
		closedProject.open(null);
		IResource[] resourcesInClosedProject = buildSampleResources(closedProject);
		closedProject.close(null);

		// non-existant project
		IProject nonExistingProject = getWorkspace().getRoot().getProject("nonExistingProject");
		nonExistingProject.create(null);
		nonExistingProject.open(null);
		nonExistingProject.delete(true, null);

		Vector resources = new Vector();
		resources.addElement(openProject);
		for (int i = 0; i < resourcesInOpenProject.length; i++) {
			resources.addElement(resourcesInOpenProject[i]);
		}

		resources.addElement(closedProject);
		for (int i = 0; i < resourcesInClosedProject.length; i++) {
			resources.addElement(resourcesInClosedProject[i]);
			nonExistingResources.add(resourcesInClosedProject[i]);
		}

		resources.addElement(nonExistingProject);
		nonExistingResources.add(nonExistingProject);

		interestingResources = new IResource[resources.size()];
		resources.copyInto(interestingResources);

		String[] interestingPathnames = new String[] {"1/", "1/1/", "1/1/1/", "1/1/1/1", "1/1/2/1/", "1/1/2/2/", "1/1/2/3/", "1/2/", "1/2/1", "1/2/2", "1/2/3/", "1/2/3/1", "1/2/3/2", "1/2/3/3", "1/2/3/4", "2", "2/1", "2/2", "2/3", "2/4", "2/1/", "2/2/", "2/3/", "2/4/", "..", "."};
		interestingPaths = new IPath[interestingPathnames.length];
		for (int i = 0; i < interestingPathnames.length; i++) {
			interestingPaths[i] = new Path(interestingPathnames[i]);
		}
	} catch (Exception ex) {
		ex.printStackTrace();
		throw ex;
	}
}
/**
 * Sets up the workspace and filesystem for this test.
 */
protected void setupBeforeState(IResource receiver, IResource target, int state, int depth) throws CoreException {
	/* make sure we're starting with a clean workspace */
	assertTrue("workspace not clean in setupBeforeState", getWorkspace().getRoot().getProjects().length == 0);
	assertTrue("workspace not clean in setupBeforeState", !target.getProject().exists());

	/* install the verifier */
	verifier = new ResourceDeltaVerifier();
	IResourceChangeListener listener = (IResourceChangeListener) verifier;
	getWorkspace().addResourceChangeListener(listener);

	/* the target's parents must exist */
	ensureExistsInWorkspace(target.getParent(), true);
	switch (state) {
		case S_WORKSPACE_ONLY :
			ensureExistsInWorkspace(target, true);
			ensureDoesNotExistInFileSystem(target);
			verifier.reset();
			// we only get a delta if the receiver of refreshLocal
			// is a parent of the changed resource, or they're the same resource.
			if (hasParent(target, receiver, depth) || target.equals(receiver))
				verifier.addExpectedChange(target, IResourceDelta.REMOVED, 0);
			break;
		case S_FILESYSTEM_ONLY :
			ensureExistsInFileSystem(target);
			assertDoesNotExistInWorkspace(target);
			verifier.reset();
			// we only get a delta if the receiver of refreshLocal
			// is a parent of the changed resource, or they're the same resource.
			if (hasParent(target, receiver, depth) || target.equals(receiver))
				verifier.addExpectedChange(target, IResourceDelta.ADDED, 0);
			break;
		case S_UNCHANGED :
			ensureExistsInWorkspace(target, true);
			verifier.reset();
			break;
		case S_CHANGED :
			ensureExistsInWorkspace(target, true);
			try {
				Thread.sleep(2500); //see PR:1FPNLSM
			} catch (InterruptedException e) {
			}
			modifyInFileSystem((IFile) target);
			verifier.reset();
			// we only get a delta if the receiver of refreshLocal
			// is a parent of the changed resource, or they're the same resource.
			if (hasParent(target, receiver, depth) || target.equals(receiver))
				verifier.addExpectedChange(target, IResourceDelta.CHANGED, IResourceDelta.CONTENT);
			break;
		case S_DOES_NOT_EXIST :
			verifier.reset();
			break;
		case S_FOLDER_TO_FILE :
			ensureExistsInWorkspace(target, true);
			ensureDoesNotExistInFileSystem(target);
			IPath location = target.getLocation();
			createFileInFileSystem(location);
			verifier.reset();
			// we only get a delta if the receiver of refreshLocal
			// is a parent of the changed resource, or they're the same resource.
			if (hasParent(target, receiver, depth) || target.equals(receiver))
				verifier.addExpectedChange(target, IResourceDelta.CHANGED, IResourceDelta.CONTENT | IResourceDelta.TYPE);
			break;
		case S_FILE_TO_FOLDER :
			ensureExistsInWorkspace(target, true);
			ensureDoesNotExistInFileSystem(target);
			target.getLocation().toFile().mkdirs();
			verifier.reset();
			// we only get a delta if the receiver of refreshLocal
			// is a parent of the changed resource, or they're the same resource.
			if (hasParent(target, receiver, depth) || target.equals(receiver))
				verifier.addExpectedChange(target, IResourceDelta.CHANGED, IResourceDelta.CONTENT | IResourceDelta.TYPE);
			break;
	}
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new IResourceTest("testAddLocalProject"));
	suite.addTest(new IResourceTest("testMultiCreation"));
	suite.addTest(new IResourceTest("testRefreshLocal"));
	suite.addTest(new IResourceTest("testRefreshWithMissingParent"));
	suite.addTest(new IResourceTest("testRefreshLocalWithDepth"));
	suite.addTest(new IResourceTest("testAccept2"));
	suite.addTest(new IResourceTest("testEquals"));
	suite.addTest(new IResourceTest("testCopy"));
	suite.addTest(new IResourceTest("testMove"));
	suite.addTest(new IResourceTest("testExists"));
	suite.addTest(new IResourceTest("testGetLocalLocation"));
	suite.addTest(new IResourceTest("testGetModificationStamp"));
	suite.addTest(new IResourceTest("testReadOnly"));
	suite.addTest(new IResourceTest("testConstants"));
	suite.addTest(new IResourceTest("testProjectDescriptionFileModification"));
	suite.addTest(new IResourceTest("testDerived"));
	suite.addTest(new IResourceTest("testTeamPrivateMember"));
	suite.addTest(new IResourceTest("testDelete")); // make sure that last test has side effects
	
	return suite;
}
protected void tearDown() throws Exception {
	if(noSideEffects) return;
	getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	interestingPaths = null;
	interestingResources = null;
	super.tearDown();
}
/**
 * Performs black box testing of the following method:
 *     void accept(IResourceVisitor)
 */
public void testAccept2() {
	noSideEffects = true;

	class LoggingResourceVisitor implements IResourceVisitor {
		Vector visitedResources = new Vector();
		void clear() {
			visitedResources.removeAllElements();
		}
		void recordVisit(IResource r) {
			visitedResources.addElement(r);
		}
		public boolean visit(IResource r) {
			throw new RuntimeException("this class is abstract");
		}
	};

	final LoggingResourceVisitor deepVisitor = new LoggingResourceVisitor() {
		public boolean visit(IResource r) {
			recordVisit(r);
			return true;
		}
	};

	final LoggingResourceVisitor shallowVisitor = new LoggingResourceVisitor() {
		public boolean visit(IResource r) {
			recordVisit(r);
			return false;
		}
	};

	LoggingResourceVisitor[] interestingVisitors = new LoggingResourceVisitor[] {shallowVisitor, deepVisitor};
	Object[][] inputs = new Object[][] {interestingResources, interestingVisitors, TRUE_AND_FALSE,};
	new TestPerformer("IResourceTest.testAccept2") {
		public boolean shouldFail(Object[] args, int count) {
			deepVisitor.clear();
			shallowVisitor.clear();
			IResource resource = (IResource) args[0];
			return nonExistingResources.contains(resource);
		}
		public Object[] interestingOldState(Object[] args) {
			return null;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IResource resource = (IResource) args[0];
			IResourceVisitor visitor = (IResourceVisitor) args[1];
			Boolean includePhantoms = (Boolean) args[2];
			resource.accept(visitor, IResource.DEPTH_INFINITE, includePhantoms.booleanValue());
			return null;
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) {
			IResource resource = (IResource) args[0];
			LoggingResourceVisitor visitor = (LoggingResourceVisitor) args[1];
			//Boolean includePhantoms = (Boolean) args[2];
			Vector visitedResources = visitor.visitedResources;
			if (visitor == shallowVisitor) {
				return visitedResources.size() == 1 && visitedResources.elementAt(0) == resource;
			} else
				if (visitor == deepVisitor) {
					if (resource.getType() == IResource.FILE) {
						return visitedResources.size() == 1 && visitedResources.elementAt(0) == resource;
					} else {
						IContainer container = (IContainer) resource;
						int memberCount = 0;
						try {
							memberCount = memberCount + container.members().length;
						} catch (CoreException ex) {
							return false;
						}
						return visitedResources.size() >= memberCount + 1 && visitedResources.elementAt(0) == resource;
					}
				} else {
					return false;
				}
		}
	}
	.performTest(inputs);
}
/**
 * This method tests the IResource.refreshLocal() operation
 */
public void testAddLocalProject() throws CoreException {
	/**
	 * Add a project in the filesystem, but not in the workspace
	 */

	IProject project1 = getWorkspace().getRoot().getProject("Project");
	project1.create(getMonitor());
	project1.open(getMonitor());

	IProject project2 = getWorkspace().getRoot().getProject("NewProject");
	
	IPath projectPath = project1.getLocation().removeLastSegments(1).append("NewProject");
	projectPath.toFile().mkdirs();

	project1.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
	project2.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
}

/**
 * Tests various resource constants.
 */
public void testConstants() {
	
	// IResource constants (all have fixed values)
	assertEquals("1.0", 0, IResource.NONE);
	
	assertEquals("2.1", 0x1, IResource.FILE);
	assertEquals("2.2", 0x2, IResource.FOLDER);
	assertEquals("2.3", 0x4, IResource.PROJECT);
	assertEquals("2.4", 0x8, IResource.ROOT);
	
	assertEquals("3.1", 0, IResource.DEPTH_ZERO);
	assertEquals("3.2", 1, IResource.DEPTH_ONE);
	assertEquals("3.1", 2, IResource.DEPTH_INFINITE);
	
	assertEquals("4.1", -1, IResource.NULL_STAMP);
	
	assertEquals("5.1", 0x1, IResource.FORCE);
	assertEquals("5.2", 0x2, IResource.KEEP_HISTORY);
	assertEquals("5.3", 0x4, IResource.ALWAYS_DELETE_PROJECT_CONTENT);
	assertEquals("5.4", 0x8, IResource.NEVER_DELETE_PROJECT_CONTENT);
	
	// IContainer constants (all have fixed values)
	assertEquals("6.1", 0x1, IContainer.INCLUDE_PHANTOMS);
	assertEquals("6.2", 0x2, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	
}

/**
 * Performs black box testing of the following method:
 *     void copy(IPath, boolean, IProgressMonitor)
 */
public void testCopy() {
	Object[][] inputs = new Object[][] { interestingResources, interestingPaths, TRUE_AND_FALSE, PROGRESS_MONITORS };
	new TestPerformer("IResourceTest.testCopy") {
		public boolean shouldFail(Object[] args, int count) {
			IResource resource = (IResource) args[0];
			IPath destination = (IPath) args[1];
			//Boolean force = (Boolean) args[2];
			if (!resource.isAccessible())
				return true;
			if (isProject(resource) && destination.segmentCount() > 1 && !getWorkspace().validatePath(destination.toString(), IResource.FOLDER).isOK())
				return true;
			java.io.File destinationParent = destination.isAbsolute() ? destination.removeLastSegments(1).toFile() : resource.getLocation().removeLastSegments(1).append(destination.removeLastSegments(1)).toFile();
			java.io.File destinationFile = destination.isAbsolute() ? destination.toFile() : resource.getLocation().removeLastSegments(1).append(destination).removeTrailingSeparator().toFile();
			return !destinationParent.exists() || !destinationParent.isDirectory() || destinationFile.exists() || destinationFile.toString().startsWith(resource.getLocation().toFile().toString());
		}
		public Object[] interestingOldState(Object[] args) {
			return null;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IResource resource = (IResource) args[0];
			IPath destination = (IPath) args[1];
			Boolean force = (Boolean) args[2];
			IProgressMonitor monitor = (IProgressMonitor) args[3];
			if (monitor instanceof FussyProgressMonitor)
				((FussyProgressMonitor) monitor).prepare();
			resource.copy(destination, force.booleanValue(), monitor);
			if (monitor instanceof FussyProgressMonitor)
				((FussyProgressMonitor) monitor).sanityCheck();
			return null;
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) {
			return true;
		}
	}
	.performTest(inputs);
}
/**
 * Performs black box testing of the following method:
 *     void delete(boolean, IProgressMonitor)
 */
public void testDelete() {
	Object[][] inputs = new Object[][] {FALSE_AND_TRUE, PROGRESS_MONITORS, interestingResources};
	new TestPerformer("IResourceTest.testDelete") {
		public boolean shouldFail(Object[] args, int count) {
			Boolean force = (Boolean) args[0];
			IResource resource = (IResource) args[2];
			if (force.booleanValue() || !resource.exists())
				return false;
			if (resource.getType() == IResource.PROJECT) {
				IProject project = (IProject) resource;
				try {
					if (!project.isOpen())
						return false;
					IResource[] children = project.members();
					for (int i = 0; i < children.length; i++) {
						IResource member = children[i];
						if (shouldFail(new Object[] {args[0], args[1], member}, count))
							return true;
					}
				} catch (CoreException ex) {
					ex.printStackTrace();
					throw new RuntimeException("there is a problem in the testing method 'shouldFail'");
				}
				return false;
			}
			final boolean[] hasUnsynchronizedResources = new boolean[] {false};
			try {
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						File target = resource.getLocation().toFile();
						if (target.exists() != resource.exists()) {
							hasUnsynchronizedResources[0] = true;
							return false;
						}
						if (target.isFile() != (resource.getType() == IResource.FILE)) {
							hasUnsynchronizedResources[0] = true;
							return false;
						}
						if (unsynchronizedResources.contains(resource)) {
							hasUnsynchronizedResources[0] = true;
							return false;
						}
						if (target.isFile())
							return false;
						String[] list = target.list();
						if (list == null)
							return true;
						IContainer container = (IContainer) resource;
						for (int i = 0; i < list.length; i++) {
							File file = new File(target, list[i]);
							IResource child = file.isFile() ? (IResource) container.getFile(new Path(list[i])) : container.getFolder(new Path(list[i]));
							if (!child.exists())
								visit(child);
						}
						return true;
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException("there is a problem in the testing method 'shouldFail'");
			}
			return hasUnsynchronizedResources[0];
		}
		public Object[] interestingOldState(Object[] args) throws Exception {
			Boolean force = (Boolean) args[0];
			IResource resource = (IResource) args[2];
			return new Object[] { new Boolean(resource.isAccessible()), getAllFilesForResource(resource, force.booleanValue()), getAllResourcesForResource(resource)
		 };
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			Boolean force = (Boolean) args[0];
			IProgressMonitor monitor = (IProgressMonitor) args[1];
			IResource resource = (IResource) args[2];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			resource.delete(force.booleanValue(), monitor);
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).sanityCheck();
			return null;
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
			Boolean force = (Boolean) args[0];
			IResource resource = (IResource) args[2];
			//oldState[0] : was resource accessible before the invocation?
			//oldState[1] : all files that should have been deleted from the file system
			//oldState[2] : all resources that should have been deleted from the workspace
			if (resource.getType() != IResource.PROJECT && ((Boolean) oldState[0]).booleanValue()) {
				// check the parent's members, deleted resource should not be a member
				IResource[] children = ((IContainer) getWorkspace().getRoot().findMember(resource.getFullPath().removeLastSegments(1))).members();
				for (int i = 0; i < children.length; i++) {
					if (resource == children[i])
						return false;
				}
			}
			if (!getAllFilesForResource(resource, force.booleanValue()).isEmpty())
				return false;
			Set oldFiles = (Set) oldState[1];
			for (Iterator i = oldFiles.iterator(); i.hasNext();) {
				File oldFile = (File) i.next();
				if (oldFile.exists())
					return false;
			}
			Set oldResources = (Set) oldState[2];
			for (Iterator i = oldResources.iterator(); i.hasNext();) {
				IResource oldResource = (IResource) i.next();
				if (oldResource.exists() || getWorkspace().getRoot().findMember(oldResource.getFullPath()) != null)
					return false;
			}
			return true;
		}
	}
	.performTest(inputs);
}
/** 
 * Performs black box testing of the following methods:
 *     isDerived() and setDerived(boolean)
 */
public void testDerived() {
	IWorkspaceRoot root = getWorkspace().getRoot();
	IProject project = root.getProject("Project");
	IFolder folder = project.getFolder("folder"); 
	IFile file = folder.getFile("target");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		
	} catch (CoreException e) {
		fail("1.0", e);
	}

	// all resources have independent derived flag; all non-derived by default; check each type
	try {
		
		// root - cannot be marked as derived
		assertTrue("2.1.1", !root.isDerived());
		assertTrue("2.1.2", !project.isDerived());
		assertTrue("2.1.3", !folder.isDerived());
		assertTrue("2.1.4", !file.isDerived());
		root.setDerived(true);
		assertTrue("2.2.1", !root.isDerived());
		assertTrue("2.2.2", !project.isDerived());
		assertTrue("2.2.3", !folder.isDerived());
		assertTrue("2.2.4", !file.isDerived());
		root.setDerived(false);
		assertTrue("2.3.1", !root.isDerived());
		assertTrue("2.3.2", !project.isDerived());
		assertTrue("2.3.3", !folder.isDerived());
		assertTrue("2.3.4", !file.isDerived());
	
		// project - cannot be marked as derived
		project.setDerived(true);
		assertTrue("3.1.1", !root.isDerived());
		assertTrue("3.1.2", !project.isDerived());
		assertTrue("3.1.3", !folder.isDerived());
		assertTrue("3.1.4", !file.isDerived());
		project.setDerived(false);
		assertTrue("3.2.1", !root.isDerived());
		assertTrue("3.2.2", !project.isDerived());
		assertTrue("3.2.3", !folder.isDerived());
		assertTrue("3.2.4", !file.isDerived());
	
		// folder
		folder.setDerived(true);
		assertTrue("4.1.1", !root.isDerived());
		assertTrue("4.1.2", !project.isDerived());
		assertTrue("4.1.3", folder.isDerived());
		assertTrue("4.1.4", !file.isDerived());
		folder.setDerived(false);
		assertTrue("4.2.1", !root.isDerived());
		assertTrue("4.2.2", !project.isDerived());
		assertTrue("4.2.3", !folder.isDerived());
		assertTrue("4.2.4", !file.isDerived());
	
		// file
		file.setDerived(true);
		assertTrue("5.1.1", !root.isDerived());
		assertTrue("5.1.2", !project.isDerived());
		assertTrue("5.1.3", !folder.isDerived());
		assertTrue("5.1.4", file.isDerived());
		file.setDerived(false);
		assertTrue("5.2.1", !root.isDerived());
		assertTrue("5.2.2", !project.isDerived());
		assertTrue("5.2.3", !folder.isDerived());
		assertTrue("5.2.4", !file.isDerived());
		
	} catch (CoreException e) {
		fail("6.0", e);
	}

	/* remove trash */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("7.0", e);
	}
	
	// isDerived should return false when resource does not exist
	assertTrue("8.1", !project.isDerived());
	assertTrue("8.2", !folder.isDerived());
	assertTrue("8.3", !file.isDerived());
	
	// setDerived should fail when resource does not exist
	try {
		project.setDerived(false);
		assertTrue("9.1", false);
	} catch (CoreException e) {
		// pass
	}
	try {
		folder.setDerived(false);
		assertTrue("9.2", false);
	} catch (CoreException e) {
		// pass
	}
	try {
		file.setDerived(false);
		assertTrue("9.3", false);
	} catch (CoreException e) {
		// pass
	}
}/**
 * Performs black box testing of the following method:
 *     boolean equals(Object)
 */
public void testEquals() {
	noSideEffects = true;
	Object[][] inputs = new Object[][] {interestingResources, interestingResources};
	new TestPerformer("IResourceTest.testEquals") {
		public boolean shouldFail(Object[] args, int count) {
			return false;
		}
		public Object[] interestingOldState(Object[] args) throws Exception {
			return null;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IResource resource0 = (IResource) args[0];
			IResource resource1 = (IResource) args[1];
			return new Boolean(resource0.equals(resource1));
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
			IResource resource0 = (IResource) args[0];
			IResource resource1 = (IResource) args[1];
			boolean booleanResult = ((Boolean)result).booleanValue();
			boolean expectedResult = resource0.getFullPath().equals(resource1.getFullPath()) && resource0.getType()==resource1.getType() && resource0.getWorkspace().equals(resource1.getWorkspace());
			if(booleanResult)
			{
				assertTrue("hashCode should be equal if equals returns true", resource0.hashCode()==resource1.hashCode());
			}
			return booleanResult==expectedResult;
		}
	}
	.performTest(inputs);
}
/**
 * Performs black box testing of the following method:
 *     boolean exists()
 */
public void testExists() {
	noSideEffects = true;

	Object[][] inputs = new Object[][] {interestingResources};
	new TestPerformer("IResourceTest.testExists") {
		public boolean shouldFail(Object[] args, int count) {
			return false;
		}
		public Object[] interestingOldState(Object[] args) throws Exception {
			return null;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IResource resource = (IResource) args[0];
			return new Boolean(resource.exists());
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
			boolean booleanResult = ((Boolean) result).booleanValue();
			IResource resource = (IResource) args[0];
			return booleanResult != nonExistingResources.contains(resource);
		}
	}
	.performTest(inputs);
}
/**
 * Performs black box testing of the following method:
 *     IPath getLocalLocation()
 */
public void testGetLocalLocation() {
	if (true)
		return;
	noSideEffects = true;
	Object[][] inputs = new Object[][] {interestingResources};
	new TestPerformer("IResourceTest.testGetLocalLocation") {
		public boolean shouldFail(Object[] args, int count) {
			return false;
		}
		public Object[] interestingOldState(Object[] args) {
			return null;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IResource resource = (IResource) args[0];
			return resource.getLocation();
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) {
			IResource resource = (IResource) args[0];
			IPath resultPath = (IPath) result;
			if (resource.getType() == IResource.PROJECT) {
				if (!resource.exists())
					return resultPath == null;
				return resultPath != null;
			}
			if (!resource.isAccessible())
				return resultPath == null;
			return resultPath != null;
		}
	}
	.performTest(inputs);
}
public void testGetModificationStamp() {
	// cleanup auto-created resources
	try {
		getWorkspace().getRoot().delete(true, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	// setup
	IResource[] resources = buildResources(getWorkspace().getRoot(), new String[] { "/1/", "/1/1", "/1/2", "/1/3", "/2/", "/2/1" });
	final Map table = new HashMap(resources.length);

	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.ROOT)
			assertEquals("1.0." + resource.getFullPath(), IResource.NULL_STAMP, resource.getModificationStamp());
	}

	// create the project(s). the resources should still have null modification stamp
	IProject[] projects = getProjects(resources);
	IProject project;
	for (int i = 0; i < projects.length; i++) {
		project = projects[i];
		try {
			project.create(getMonitor());
		} catch (CoreException e) {
			fail("2.0." + project.getFullPath(), e);
		}
		assertEquals("2.1." + project.getFullPath(), IResource.NULL_STAMP, project.getModificationStamp());
	}

	// open the project(s) and create the resources. none should have a null stamp anymore.
	for (int i = 0; i < projects.length; i++) {
		project = projects[i];
		assertEquals("3.1." + project.getFullPath(), IResource.NULL_STAMP, project.getModificationStamp());
		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("3.2", e);
		}
		assertTrue("3.3." + project.getFullPath(), project.getModificationStamp() != IResource.NULL_STAMP);
		// cache the value for later use
		table.put(project.getFullPath(), new Long(project.getModificationStamp()));
	}
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.PROJECT) {
			assertEquals("3.4." + resource.getFullPath(), IResource.NULL_STAMP, resource.getModificationStamp());
			ensureExistsInWorkspace(resource, true);
			assertTrue("3.5." + resource.getFullPath(), resource.getModificationStamp() != IResource.NULL_STAMP);
			// cache the value for later use
			table.put(resource.getFullPath(), new Long(resource.getModificationStamp()));
		}
	}

	// close the projects. now all resources should have a null stamp again
	for (int i = 0; i < projects.length; i++) {
		project = projects[i];
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("4.0." + project.getFullPath(), e);
		}
	}
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.ROOT)
			assertEquals("4.1." + resource.getFullPath(), IResource.NULL_STAMP, resource.getModificationStamp());
	}

	// re-open the projects. all resources should have the same stamps
	for (int i = 0; i < projects.length; i++) {
		project = projects[i];
		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("5.0." + project.getFullPath(), e);
		}
	}
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.PROJECT) {
			Object v = table.get(resource.getFullPath());
			assertNotNull("5.1." + resource.getFullPath(), v);
			long old = ((Long) v).longValue();
			assertEquals("5.2." + resource.getFullPath(), old, resource.getModificationStamp());
		}
	}

	// touch all the resources. this will update the modification stamp
	final Map tempTable = new HashMap(resources.length);
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.ROOT) {
			try {
				resource.touch(getMonitor());
			} catch (CoreException e) {
				fail("6.2", e);
			}
			long stamp = resource.getModificationStamp();
			Object v = table.get(resource.getFullPath());
			assertNotNull("6.0." + resource.getFullPath(), v);
			long old = ((Long) v).longValue();
			assertTrue("6.1." + resource.getFullPath(), old != stamp);
			// cache for next time
			tempTable.put(resource.getFullPath(), new Long(stamp));
		}
	}
	table.clear();
	table.putAll(tempTable);

	// mark all resources as non-local. all non-local resources have a null stamp
	try {
		getWorkspace().getRoot().setLocal(false, IResource.DEPTH_INFINITE, getMonitor());
	} catch (CoreException e) {
		fail("7.1", e);
	}
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			//projects and root are always local
			if (resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT) {
				assertTrue("7.2" + resource.getFullPath(), IResource.NULL_STAMP != resource.getModificationStamp());
			} else {
				assertEquals("7.3." + resource.getFullPath(), IResource.NULL_STAMP, resource.getModificationStamp());
			}
			return true;
		}
	};
	try {
		getWorkspace().getRoot().accept(visitor, IResource.DEPTH_INFINITE, false);
	} catch (CoreException e) {
		fail("7.4", e);
	}

	// mark all resources as local. none should have a null stamp and it should be different than
	// the last one
	try {
		getWorkspace().getRoot().setLocal(true, IResource.DEPTH_INFINITE, getMonitor());
	} catch (CoreException e) {
		fail("8.1", e);
	}
	tempTable.clear();
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.ROOT) {
			long stamp = resource.getModificationStamp();
			assertTrue("8.2." + resource.getFullPath(), stamp != IResource.NULL_STAMP);
			Object v = table.get(resource.getFullPath());
			assertNotNull("8.3." + resource.getFullPath(), v);
			long old = ((Long) v).longValue();
			assertTrue("8.4." + resource.getFullPath(), old != IResource.NULL_STAMP);
			tempTable.put(resource.getFullPath(), new Long(stamp));
		}
	}
	table.clear();
	table.putAll(tempTable);
	//set local on resources that are already local, this should not affect the modification stamp
	try {
		getWorkspace().getRoot().setLocal(true, IResource.DEPTH_INFINITE, getMonitor());
	} catch (CoreException e) {
		fail("9.1", e);
	}
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.ROOT) {
			long newStamp = resource.getModificationStamp();
			assertTrue("9.2." + resource.getFullPath(), newStamp != IResource.NULL_STAMP);
			Object v = table.get(resource.getFullPath());
			assertNotNull("9.3." + resource.getFullPath(), v);
			long oldStamp = ((Long) v).longValue();
			assertEquals("9.4." + resource.getFullPath(), oldStamp, newStamp);
		}
	}
	
	// delete all the resources so we can start over.
	try {
		getWorkspace().getRoot().delete(true, getMonitor());
	} catch (CoreException e) {
		fail("10.0", e);
	}

	// none of the resources exist yet so all the modification stamps should be null
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		if (resource.getType() != IResource.ROOT)
			assertEquals("10.1" + resource.getFullPath(), IResource.NULL_STAMP, resource.getModificationStamp());
	}

	// create all the resources (non-local) and ensure all stamps are null
	ensureExistsInWorkspace(resources, false);
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		switch (resource.getType()) {
			case IResource.ROOT :
				break;
			case IResource.PROJECT :
				assertTrue("11.1." + resource.getFullPath(), resource.getModificationStamp() != IResource.NULL_STAMP);
				break;
			default :
				assertEquals("11.2." + resource.getFullPath(), IResource.NULL_STAMP, resource.getModificationStamp());
				break;
		}
	}
	// now make all resources local and re-check stamps
	try {
		getWorkspace().getRoot().setLocal(true, IResource.DEPTH_INFINITE, getMonitor());
	} catch (CoreException e) {
		fail("12.0", e);
	}
	visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() != IResource.ROOT)
				assertTrue("12.1." + resource.getFullPath(), IResource.NULL_STAMP != resource.getModificationStamp());
			return true;
		}
	};
	try {
		getWorkspace().getRoot().accept(visitor, IResource.DEPTH_INFINITE, false);
	} catch (CoreException e) {
		fail("12.2", e);
	}
}
/**
 * Performs black box testing of the following method:
 *     void move(IPath, boolean, IProgressMonitor)
 */
public void testMove() {
	Object[][] inputs = new Object[][] { interestingResources, interestingPaths, TRUE_AND_FALSE, PROGRESS_MONITORS };
	new TestPerformer("IResourceTest.testMove") {
		public boolean shouldFail(Object[] args, int count) {
			IResource resource = (IResource) args[0];
			IPath destination = (IPath) args[1];
//			Boolean force = (Boolean) args[2];
			if (!resource.isAccessible())
				return true;
			if (isProject(resource)) {
				if (destination.isAbsolute() ? destination.segmentCount() != 2 : destination.segmentCount() != 1)
					return true;
				return !getWorkspace().validateName(destination.segment(0), IResource.PROJECT).isOK();
			}
			File destinationParent = destination.isAbsolute() ? destination.removeLastSegments(1).toFile() : resource.getLocation().removeLastSegments(1).append(destination.removeLastSegments(1)).toFile();
			File destinationFile = destination.isAbsolute() ? destination.toFile() : resource.getLocation().removeLastSegments(1).append(destination).removeTrailingSeparator().toFile();
			return !destinationParent.exists() || !destinationParent.isDirectory() || destinationFile.exists() || destinationFile.toString().startsWith(resource.getLocation().toFile().toString());
		}
		public Object[] interestingOldState(Object[] args) {
			return null;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IResource resource = (IResource) args[0];
			IPath destination = (IPath) args[1];
			Boolean force = (Boolean) args[2];
			IProgressMonitor monitor = (IProgressMonitor) args[3];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			resource.move(destination, force.booleanValue(), monitor);
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).sanityCheck();
			return null;
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) {
			return true;
		}
	}
	.performTest(inputs);
}
public void testMultiCreation() {

	final IProject project = getWorkspace().getRoot().getProject("bar");
	final IResource[] resources = buildResources(project, new String[] {"a/", "a/b"});
	// create the project.  Have to do this outside the resource operation
	// to ensure that things are setup properly (e.g., add the delta listener)
	try {
		project.create(null);
		project.open(null);
	} catch (CoreException e) {
		fail("1.2", e);
	}
	assertExistsInWorkspace("1.3", project);
	// define an operation which will create a bunch of resources including a project.
	for (int i = 0; i < resources.length; i++) {
		IResource resource = resources[i];
		try {
			switch (resource.getType()) {
				case IResource.FILE :
				 	((IFile) resource).create(null, false, getMonitor());
					break;
				case IResource.FOLDER :
				 	((IFolder) resource).create(false, true, getMonitor());
					break;
				case IResource.PROJECT :
				 	((IProject) resource).create(getMonitor());
					break;
			}
		} catch (CoreException e) {
			fail("1.4: " + resource.getFullPath(), e);
		}
	}
	assertExistsInWorkspace("1.5", resources);
	try {
		project.delete(true, false, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}
}
/**
 * Test that opening an closing a project does not affect the description file.
 */
public void testProjectDescriptionFileModification() throws CoreException {
	IProject project = getWorkspace().getRoot().getProject("P1");
	IFile file = project.getFile(".project");
	project.create(null);
	project.open(null);
	long stamp = file.getModificationStamp();
	project.close(null);
	project.open(null);
	assertEquals(stamp, file.getModificationStamp());
}
public void testReadOnly() {
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFile file = project.getFile("target");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	// file
	assertTrue("1.0", !file.isReadOnly());
	file.setReadOnly(true);
	assertTrue("1.2", file.isReadOnly());
	file.setReadOnly(false);
	assertTrue("1.4", !file.isReadOnly());

	// folder
	assertTrue("2.0", !project.isReadOnly());
	project.setReadOnly(true);
	assertTrue("2.2", project.isReadOnly());
	project.setReadOnly(false);
	assertTrue("2.4", !project.isReadOnly());

	/* remove trash */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
/**
 * This method tests the IResource.refreshLocal() operation
 */
public void testRefreshLocal() {
	Object[][] args = new Object[][] {interestingResources(), interestingResources(), interestingStates(), interestingDepths()};
	new TestPerformer("IResourceTest.testRefreshLocal") {
		public Object invokeMethod(Object[] args, int count) throws CoreException {
			//if (count % 10 == 0) {
				//System.out.println("refreshLocal iteration: " + count);
			//}
			IResource receiver = (IResource) args[0];
			IResource target = (IResource)args[1];
			int state = ((Integer) args[2]).intValue();
			int depth = ((Integer) args[3]).intValue();
			if (!makesSense(receiver, target, state, depth))
				return null;
			setupBeforeState(receiver, target, state, depth);
			receiver.refreshLocal(depth, getMonitor());
			return null;
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) {
			IResource receiver = (IResource) args[0];
			IResource target = (IResource)args[1];
			int state = ((Integer) args[2]).intValue();
			int depth = ((Integer) args[3]).intValue();
			if (!makesSense(receiver, target, state, depth))
				return true;
			return checkAfterState(receiver, target, state, depth);
		}
		public boolean shouldFail(Object[] args, int count) {
			return false;
		}
		public Object[] interestingOldState(Object[] args) {
			return null;
		}		
	}
	.performTest(args);
}
public void testRefreshLocalWithDepth() {
	IProject project = getWorkspace().getRoot().getProject("Project");
	IFolder folder = project.getFolder("Folder");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		folder.create(true, true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	String[] hierarchy = { "Folder/", "Folder/Folder/", "Folder/Folder/Folder/", "Folder/Folder/Folder/Folder/" };
	IResource[] resources = buildResources(folder, hierarchy);
	ensureExistsInFileSystem(resources);
	assertDoesNotExistInWorkspace("3.0", resources);

	try {
		folder.refreshLocal(IResource.DEPTH_ONE, getMonitor());
	} catch (CoreException e) {
		fail("4.0", e);
	}

	assertExistsInWorkspace("5.0", folder.getFolder("Folder"));
	assertDoesNotExistInWorkspace("5.1", folder.getFolder("Folder/Folder"));
}
/**
 * This method tests the IResource.refreshLocal() operation
 */
public void testRefreshWithMissingParent() throws CoreException {
	/**
	 * Add a folder and file to the filesystem.  Call refreshLocal
	 * on the file, when neither of them exist in the workspace.
	 */
	IProject project1 = getWorkspace().getRoot().getProject("Project");
	project1.create(getMonitor());
	project1.open(getMonitor());

	IFolder folder = project1.getFolder("Folder");
	IFile file = folder.getFile("File");
	
	ensureExistsInFileSystem(file);

	file.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
}

/**
 * Performs black box testing of the following methods:
 *     isTeamPrivateMember() and setTeamPrivateMember(boolean)
 */
public void testTeamPrivateMember() {
	// FIXME: enable this test when team private members are enabled.
	if (true)
		return;
	IWorkspaceRoot root = getWorkspace().getRoot();
	IProject project = root.getProject("Project");
	IFolder folder = project.getFolder("folder"); 
	IFile file = folder.getFile("target");
	try {
		project.create(getMonitor());
		project.open(getMonitor());
		folder.create(true, true, getMonitor());
		file.create(getRandomContents(), true, getMonitor());
		
	} catch (CoreException e) {
		fail("1.0", e);
	}

	// all resources have independent team private member flag
	// all non-TPM by default; check each type
	try {
		
		// root - cannot be made team private member
		assertTrue("2.1.1", !root.isTeamPrivateMember());
		assertTrue("2.1.2", !project.isTeamPrivateMember());
		assertTrue("2.1.3", !folder.isTeamPrivateMember());
		assertTrue("2.1.4", !file.isTeamPrivateMember());
		root.setTeamPrivateMember(true);
		assertTrue("2.2.1", !root.isTeamPrivateMember());
		assertTrue("2.2.2", !project.isTeamPrivateMember());
		assertTrue("2.2.3", !folder.isTeamPrivateMember());
		assertTrue("2.2.4", !file.isTeamPrivateMember());
		root.setTeamPrivateMember(false);
		assertTrue("2.3.1", !root.isTeamPrivateMember());
		assertTrue("2.3.2", !project.isTeamPrivateMember());
		assertTrue("2.3.3", !folder.isTeamPrivateMember());
		assertTrue("2.3.4", !file.isTeamPrivateMember());
	
		// project - cannot be made team private member
		project.setTeamPrivateMember(true);
		assertTrue("3.1.1", !root.isTeamPrivateMember());
		assertTrue("3.1.2", !project.isTeamPrivateMember());
		assertTrue("3.1.3", !folder.isTeamPrivateMember());
		assertTrue("3.1.4", !file.isTeamPrivateMember());
		project.setTeamPrivateMember(false);
		assertTrue("3.2.1", !root.isTeamPrivateMember());
		assertTrue("3.2.2", !project.isTeamPrivateMember());
		assertTrue("3.2.3", !folder.isTeamPrivateMember());
		assertTrue("3.2.4", !file.isTeamPrivateMember());
	
		// folder
		folder.setTeamPrivateMember(true);
		assertTrue("4.1.1", !root.isTeamPrivateMember());
		assertTrue("4.1.2", !project.isTeamPrivateMember());
		assertTrue("4.1.3", folder.isTeamPrivateMember());
		assertTrue("4.1.4", !file.isTeamPrivateMember());
		folder.setTeamPrivateMember(false);
		assertTrue("4.2.1", !root.isTeamPrivateMember());
		assertTrue("4.2.2", !project.isTeamPrivateMember());
		assertTrue("4.2.3", !folder.isTeamPrivateMember());
		assertTrue("4.2.4", !file.isTeamPrivateMember());
	
		// file
		file.setTeamPrivateMember(true);
		assertTrue("5.1.1", !root.isTeamPrivateMember());
		assertTrue("5.1.2", !project.isTeamPrivateMember());
		assertTrue("5.1.3", !folder.isTeamPrivateMember());
		assertTrue("5.1.4", file.isTeamPrivateMember());
		file.setTeamPrivateMember(false);
		assertTrue("5.2.1", !root.isTeamPrivateMember());
		assertTrue("5.2.2", !project.isTeamPrivateMember());
		assertTrue("5.2.3", !folder.isTeamPrivateMember());
		assertTrue("5.2.4", !file.isTeamPrivateMember());
		
	} catch (CoreException e) {
		fail("6.0", e);
	}

	/* remove trash */
	try {
		project.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("7.0", e);
	}
	
	// isTeamPrivateMember should return false when resource does not exist
	assertTrue("8.1", !project.isTeamPrivateMember());
	assertTrue("8.2", !folder.isTeamPrivateMember());
	assertTrue("8.3", !file.isTeamPrivateMember());
	
	// setTeamPrivateMember should fail when resource does not exist
	try {
		project.setTeamPrivateMember(false);
		assertTrue("9.1", false);
	} catch (CoreException e) {
		// pass
	}
	try {
		folder.setTeamPrivateMember(false);
		assertTrue("9.2", false);
	} catch (CoreException e) {
		// pass
	}
	try {
		file.setTeamPrivateMember(false);
		assertTrue("9.3", false);
	} catch (CoreException e) {
		// pass
	}
}}
