package org.eclipse.core.tests.internal.localstore;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import java.io.File;
import java.util.*;
import junit.framework.*;
/**
 *
 */
public class DeleteTest extends LocalStoreTest {
public DeleteTest() {
	super();
}
public DeleteTest(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new DeleteTest("testDeleteResource"));
	suite.addTest(new DeleteTest("testDeleteProject"));
	return suite;
}
public void testDeleteProject() throws Throwable {
	/* =========================================================== */
	/* (1) project is initially OPEN and deleteContents = FALSE    */
	/* =========================================================== */

	/* create some resources */
	IFolder folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	IFile file = folder.getFile("file");
	ensureExistsInWorkspace(file, true);
	IPath folderPath = folder.getLocation();
	IPath filePath = file.getLocation();

	/* delete */
	IPath projectLocation = projects[0].getLocation();
	projects[0].delete(false, true, null);

	/* assert project does not exist anymore */
	assertTrue("1.1", !projects[0].exists());
	assertTrue("1.2", !((Workspace) getWorkspace()).getMetaArea().locationFor((Project) projects[0]).toFile().exists());
	assertNull("1.3", projects[0].getLocation());

	/* assert resources still exist */
	assertTrue("1.4", folderPath.toFile().isDirectory());
	assertTrue("1.5", filePath.toFile().isFile());

	/* remove trash */
	Workspace.clear(projectLocation.toFile());

	/* =========================================================== */
	/* (2) project is initially CLOSED and deleteContents = FALSE  */
	/* =========================================================== */

	/* initialize common objects */
	ensureExistsInWorkspace(projects[0], true);
	projects[0].open(null);

	/* create some resources */
	folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	file = folder.getFile("file");
	ensureExistsInWorkspace(file, true);
	folderPath = folder.getLocation();
	filePath = file.getLocation();

	/* close and delete */
	projectLocation = projects[0].getLocation();
	projects[0].close(null);
	projects[0].delete(false, true, null);

	/* assert project does not exist anymore */
	assertTrue("2.1", !projects[0].exists());
	assertTrue("2.2", !((Workspace) getWorkspace()).getMetaArea().locationFor((Project) projects[0]).toFile().exists());
	assertNull("2.3", projects[0].getLocation());

	/* assert resources still exist */
	assertTrue("2.4", folderPath.toFile().isDirectory());
	assertTrue("2.5", filePath.toFile().isFile());

	/* remove trash */
	Workspace.clear(projectLocation.toFile());

	/* =========================================================== */
	/* (3) project is initially OPEN and deleteContents = TRUE     */
	/*     - uses default default mapping                          */
	/* =========================================================== */

	/* initialize common objects */
	ensureExistsInWorkspace(projects[0], true);
	projects[0].open(null);

	/* create some resources */
	folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	file = folder.getFile("file");
	ensureExistsInWorkspace(file, true);
	folderPath = folder.getLocation();
	filePath = file.getLocation();

	/* delete */
	projects[0].delete(true, true, null);

	/* assert project does not exist anymore */
	assertTrue("3.1", !projects[0].exists());
	assertTrue("3.2", !((Workspace) getWorkspace()).getMetaArea().locationFor((Project) projects[0]).toFile().exists());
	assertNull("3.3", projects[0].getLocation());

	/* assert resources do not exist anymore */
	assertTrue("3.4", !folderPath.toFile().isDirectory());
	assertTrue("3.5", !filePath.toFile().isFile());

	/* =========================================================== */
	/* (4) project is initially CLOSED and deleteContents = TRUE   */
	/*     - uses default default mapping                          */
	/* =========================================================== */

	/* initialize common objects */
	ensureExistsInWorkspace(projects[0], true);
	projects[0].open(null);

	/* create some resources */
	folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	file = folder.getFile("file");
	ensureExistsInWorkspace(file, true);
	folderPath = folder.getLocation();
	filePath = file.getLocation();

	/* close and delete */
	projects[0].close(null);
	projects[0].delete(true, true, null);

	/* assert project does not exist anymore */
	ResourceInfo info = ((Project) projects[0]).getResourceInfo(true, false);
	int flags = ((Project) projects[0]).getFlags(info);
	assertTrue("4.1", !((Project) projects[0]).exists(flags, true));
	assertTrue("4.2", !((Workspace) getWorkspace()).getMetaArea().locationFor((Project) projects[0]).toFile().exists());
	assertNull("4.3", projects[0].getLocation());

	/* assert resources do not exist anymore */
	assertTrue("4.4", !folderPath.toFile().isDirectory());
	assertTrue("4.5", !filePath.toFile().isFile());

	/* =========================================================== */
	/* (5) project is initially OPEN and deleteContents = TRUE     */
	/*     - defines default mapping                               */
	/*     - does not create resources at default default area     */
	/* =========================================================== */

	/* initialize common objects */
	ensureExistsInWorkspace(projects[0], true);
	projects[0].open(null);

	/* create some resources */
	folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	file = folder.getFile("file");
	ensureExistsInWorkspace(file, true);
	folderPath = folder.getLocation();
	filePath = file.getLocation();

	/* delete */
	projects[0].delete(true, true, null);

	/* assert project does not exist anymore */
	assertTrue("5.1", !projects[0].exists());
	assertTrue("5.2", !((Workspace) getWorkspace()).getMetaArea().locationFor((Project) projects[0]).toFile().exists());
	assertNull("5.3", projects[0].getLocation());

	/* assert resources do not exist anymore */
	assertTrue("5.4", !folderPath.toFile().isDirectory());
	assertTrue("5.5", !filePath.toFile().isFile());

	///* =========================================================== */
	///* (6) project is initially OPEN and deleteContents = TRUE     */
	///*     - create resources at default default area              */
	///*     - defines default mapping                               */
	///* =========================================================== */

	///* initialize common objects */
	//ensureExistsInWorkspace(projects[0]);
	//projects[0].open(null);

	///* create some resources */
	//folder = projects[0].getFolder("folder");
	//ensureExistsInWorkspace(folder);
	//ensureExistsInFileSystem(folder);
	//file = folder.getFile("file");
	//ensureExistsInWorkspace(file);
	//ensureExistsInFileSystem(file);
	//folderPath = folder.getLocation();
	//filePath = file.getLocation();

	///* delete */
	//projects[0].delete(true, true, null);

	///* assert project does not exist anymore */
	//assert("6.1", !projects[0].exists());
	//assert("6.2", !((Workspace) getWorkspace()).getMetaArea().getLocationFor((Project) projects[0]).toFile().exists());
	//assertNull("6.3", projects[0].getLocation());

	///* assert resources still exist at default default area */
	//assert("6.4", folderPath.toFile().isDirectory());
	//assert("6.5", filePath.toFile().isFile());

	///* remove trash */
	//Workspace.clear(folderPath.toFile());

	/* =========================================================== */
	/* (7) project is initially CLOSED and deleteContents = TRUE   */
	/*     force = FALSE				                           */
	/*     - uses default default mapping                          */
	/*     - there is a file out of sync, so delete should fail */
	/* =========================================================== */

	/* initialize common objects */
	ensureExistsInWorkspace(projects[0], true);
	projects[0].open(null);

	/* create some resources and a persistent property*/
	folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	file = folder.getFile("file");
	ensureExistsInFileSystem(file);
	folderPath = folder.getLocation();
	filePath = file.getLocation();
	QualifiedName name = new QualifiedName("foo", "bar");
	projects[0].setPersistentProperty(name, "none");

	/* close and delete */
	projects[0].close(null);
	try {
		projects[0].delete(true, false, null);
		fail("7.0");
	} catch (CoreException e) {
	}

	/* assert project still exists */
	assertTrue("7.1", projects[0].exists());
	IPath metaAreaLocation = ((Workspace) getWorkspace()).getMetaArea().locationFor((Project) projects[0]);
	assertTrue("7.2", metaAreaLocation.toFile().exists());
	assertTrue("7.3", metaAreaLocation.append(".properties").toFile().exists());
	assertTrue("7.4", projects[0].getLocation().append(".project").toFile().exists());
	assertTrue("7.5", projects[0].getLocation().toFile().exists());

	/* assert resources do not exist anymore */
	assertTrue("7.6", folderPath.toFile().isDirectory());
	assertTrue("7.7", filePath.toFile().isFile());
}
public void testDeleteResource() throws Throwable {
	/* test's hierarchy
	
	P0
	|
	|-- folder
	|
	|-- fileSync
	|
	|-- fileUnsync
	|
	|-- fileCreated
	|
	|-- subfolderSync
	|	|
	|	|-- deletedfolderSync
	|
	|-- subfolderUnsync
	|	|
	|	|-- subsubfolderUnsync
	|		|
	|		|-- susubfileSync
	|		|
	|		|-- susubfileUnsync
	
	*/

	/* =================== */
	/* (1) force = TRUE    */
	/* =================== */

	/* create some resources */
	IFolder folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	IFile fileSync = folder.getFile("fileSync");
	ensureExistsInWorkspace(fileSync, true);
	IFile fileUnsync = folder.getFile("fileUnsync");
	ensureExistsInWorkspace(fileUnsync, true);
	IFile fileCreated = folder.getFile("fileCreated");
	ensureExistsInFileSystem(fileCreated); // create only in file system
	IFolder subfolderSync = folder.getFolder("subfolderSync");
	ensureExistsInWorkspace(subfolderSync, true);
	IFolder deletedfolderSync = subfolderSync.getFolder("deletedfolderSync");
	ensureExistsInWorkspace(deletedfolderSync, true);
	IFolder subfolderUnsync = folder.getFolder("subfolderUnsync");
	ensureExistsInWorkspace(subfolderUnsync, true);
	IFolder subsubfolderUnsync = subfolderUnsync.getFolder("subsubfolderUnsync");
	ensureExistsInWorkspace(subsubfolderUnsync, true);
	IFile subsubfileSync = subsubfolderUnsync.getFile("subsubfileSync");
	ensureExistsInWorkspace(subsubfileSync, true);
	IFile subsubfileUnsync = subsubfolderUnsync.getFile("subsubfileUnsync");
	ensureExistsInWorkspace(subsubfileUnsync, true);

	/* make some resources "unsync" with the workspace */
	Thread.sleep(sleepTime);
	ensureExistsInFileSystem(fileUnsync);
	ensureDoesNotExistInFileSystem(deletedfolderSync);
	ensureExistsInFileSystem(subsubfileUnsync);

	/* delete */
	folder.delete(true, null);

	/* assert resources do not exist anymore */
	assertTrue("1.1", !folder.getLocation().toFile().exists());

	/* =================== */
	/* (2) force = FALSE   */
	/* =================== */

	/* create some resources */
	folder = projects[0].getFolder("folder");
	ensureExistsInWorkspace(folder, true);
	//
	fileSync = folder.getFile("fileSync");
	ensureExistsInWorkspace(fileSync, true);
	//
	fileUnsync = folder.getFile("fileUnsync");
	ensureExistsInWorkspace(fileUnsync, true);
	//
	fileCreated = folder.getFile("fileCreated");
	ensureExistsInFileSystem(fileCreated); // create only in file system
	//
	subfolderSync = folder.getFolder("subfolderSync");
	ensureExistsInWorkspace(subfolderSync, true);
	//
	deletedfolderSync = subfolderSync.getFolder("deletedfolderSync");
	ensureExistsInWorkspace(deletedfolderSync, true);
	//
	subfolderUnsync = folder.getFolder("subfolderUnsync");
	ensureExistsInWorkspace(subfolderUnsync, true);
	//
	subsubfolderUnsync = subfolderUnsync.getFolder("subsubfolderUnsync");
	ensureExistsInWorkspace(subsubfolderUnsync, true);
	//
	subsubfileSync = subsubfolderUnsync.getFile("subsubfileSync");
	ensureExistsInWorkspace(subsubfileSync, true);
	//
	subsubfileUnsync = subsubfolderUnsync.getFile("subsubfileUnsync");
	ensureExistsInWorkspace(subsubfileUnsync, true);

	/* make some resources "unsync" with the workspace */
	Thread.sleep(sleepTime);
	ensureExistsInFileSystem(fileUnsync);
	ensureDoesNotExistInFileSystem(deletedfolderSync);
	ensureExistsInFileSystem(subsubfileUnsync);

	/* delete */
	try {
		folder.delete(false, null);
		fail("2.0");
	} catch (CoreException e) {
	}

	/* assert resources do not exist anymore in the file system */
	assertTrue("2.1", folder.getLocation().toFile().exists());
	assertTrue("2.2", !fileSync.getLocation().toFile().exists());
	assertTrue("2.3", fileUnsync.getLocation().toFile().exists());
	assertTrue("2.4", subfolderSync.getLocation().toFile().exists());
	assertTrue("2.5", subfolderUnsync.getLocation().toFile().exists());
	assertTrue("2.6", !deletedfolderSync.getLocation().toFile().exists());
	assertTrue("2.7", subsubfolderUnsync.getLocation().toFile().exists());
	assertTrue("2.8", subsubfileUnsync.getLocation().toFile().exists());
	assertTrue("2.9", !subsubfileSync.getLocation().toFile().exists());
	assertTrue("2.10", fileCreated.getLocation().toFile().exists());
}
}
