package org.eclipse.team.tests.ccvs.ui.benchmark;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.tests.ccvs.core.CommandLineCVSClient;
import org.eclipse.team.tests.ccvs.core.EclipseCVSClient;
import org.eclipse.team.tests.ccvs.core.ICVSClient;
import org.eclipse.team.tests.ccvs.ui.CVSUITestCase;
import org.eclipse.team.tests.ccvs.ui.Util;

public class CommandTests extends CVSUITestCase {
	private String baseName;
	private IProject referenceProject;
	private IProject eclipseProject;
	private IProject uiProject;
	
	public CommandTests(String name) {
		super(name);
	}
	public CommandTests() {
		super("");
	}
	public static Test suite() {
    	return new BenchmarkTestSetup(new TestSuite(CommandTests.class));
	}

	public void setUp() throws Exception {
		super.setUp();
		baseName = Util.makeUniqueName(null, getName(), null);
		referenceProject = Util.createProject(baseName + "-reference");
		eclipseProject = Util.createProject(baseName + "-eclipse");
		uiProject = Util.createProject(baseName);
	}
	
	public void testImportAddCommitCheckout() throws Throwable {
		// import a project using each client
		new ComparativeTest("import/add/commit big project") {
			protected void runCommandTest(final IContainer localRoot, ICVSClient client) throws Exception {
				// use the import command to create a new module
				IFolder folder = localRoot.getFolder(new Path("empty_folder"));
				folder.create(false /*force*/, true /*local*/, null);
				execute(client, "import empty module", folder, "import",
					new String[] { },
					new String[] { "-m", "initial import" },
					new String[] { localRoot.getName(), "vendor", "start" });
				folder.delete(false /*force*/, null);
				
				// checkout the project
				localRoot.delete(false /*force*/, null);
				execute(client, "checkout module", localRoot.getParent(), "co",
					new String[] { },
					new String[] { },
					new String[] { localRoot.getName() });
				
				// prepare contents
				prepareContents(localRoot);

				// determine the set of files and folders to be added
				final Map /* from KSubstOption to String */ files = new HashMap();
				final Set /* of String */ folders = new TreeSet();
				final int trim = localRoot.getProjectRelativePath().segmentCount();
				localRoot.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if (! resource.equals(localRoot)) {
							String name = resource.getProjectRelativePath().removeFirstSegments(trim).toString();
							if (resource.getType() == IResource.FILE) {
								KSubstOption ksubst = KSubstOption.fromFile((IFile) resource);
									Set set = (Set) files.get(ksubst);
									if (set == null) {
										set = new HashSet();
										files.put(ksubst, set);
									}
								set.add(name);
							} else {
								folders.add(name);
							}
						}
						return true;
					}
				}, IResource.DEPTH_INFINITE, false);
				
				// add all folders
				if (!folders.isEmpty()) {
					executeInParts(client, "add folders", localRoot, "add",
						new String[] { },
						new String[] { },
						(String[])folders.toArray(new String[folders.size()]));
				}
				// add all files
				for (Iterator it = files.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					KSubstOption ksubst = (KSubstOption) entry.getKey();
					Set set = (Set) entry.getValue();
					executeInParts(client, "add files (" + ksubst.getShortDisplayText() + ")", localRoot, "add",
						new String[] { },
						new String[] { ksubst.toString() },
						(String[])set.toArray(new String[set.size()]));
				}
				
				// commit everything
				execute(client, "commit module", localRoot, "ci",
					new String[] { },
					new String[] { "-m", "dummy message" },
					new String[] { });
			}
			protected void runUITest(IContainer localRoot) throws Exception {
				prepareContents(localRoot);
				actionShareProject(uiProject);
				syncCommitResources(new IResource[] { uiProject }, null, "initial");
			}
			private void prepareContents(IContainer localRoot) throws Exception {
				Util.importZip(localRoot, BenchmarkTestSetup.BIG_ZIP_FILE);
			}
		}.run();
		
		// check it out using each client
		new ComparativeTest("checkout big project") {
			protected void runCommandTest(IContainer localRoot, ICVSClient client) throws Exception {
				execute(client, "checkout module", localRoot.getParent(), "co",
					new String[] { },
					new String[] { "-P" },
					new String[] { localRoot.getName() });
			}
			protected void runUITest(IContainer localRoot) throws Exception {
				actionCheckoutProjects(new String[] { localRoot.getName() }, new CVSTag[] { CVSTag.DEFAULT });
			}
			protected void setUp(IContainer localRoot) throws Exception {
				// delete then recreate the container
				IProject project = (IProject) localRoot;			
				Util.deleteProject(project);
				project.create(null);
				project.open(null);
				// delete the .project file (avoid .project is in the way error)
				IFile file = project.getFile(".project");
				file.delete(false /*force*/, null);
			}
		}.run();
	}
	
	protected abstract class ComparativeTest {
		private String name;
		public ComparativeTest(String name) {
			this.name = name;
		}
		public void run() throws Exception {
			startGroup(name);
			
			startGroup("command line client");
			setUp(referenceProject);
			runCommandTest(referenceProject, CommandLineCVSClient.INSTANCE);
			tearDown(referenceProject);
			endGroup();
			
			startGroup("eclipse client");
			setUp(eclipseProject);
			runCommandTest(eclipseProject, EclipseCVSClient.INSTANCE);
			tearDown(eclipseProject);
			endGroup();
			
			startGroup("user interface");
			setUp(uiProject);
			runUITest(uiProject);
			tearDown(uiProject);
			endGroup();
			
			endGroup();
		}
		protected abstract void runCommandTest(IContainer localRoot, ICVSClient client) throws Exception ;
		protected abstract void runUITest(IContainer localRoot) throws Exception;
		protected void setUp(IContainer localRoot) throws Exception {
		}
		protected void tearDown(IContainer localRoot) throws Exception {
		}
		protected void execute(ICVSClient client, String taskname,
			IContainer localRoot, String command,
			String[] globalOptions, String[] localOptions, String[] arguments) throws CVSException {
			// The execution time for the client will include overhead associated with
			// computing the command to be run and cleaning up Eclipse state once it has
			// completed, including notifying resource delta listener.  Since all clients
			// in the Eclipse environment are subject to this overhead, the theory is that
			// it will be a constant factor that we can neglect.
			startTask(taskname);
			client.executeCommand(testRepository, localRoot, command, globalOptions, localOptions, arguments);
			endTask();
		}
		protected void executeInParts(ICVSClient client, String taskname,
			IContainer localRoot, String command,
			String[] globalOptions, String[] localOptions, String[] arguments) throws CVSException {
			// There are problems executing commands with too many arguments
			// so we have to break them up into chunks.
			startTask(taskname);
			int i = 0;
			do {
				int len = Math.min(200, arguments.length - i);
				String[] args;
				if (i == 0 && len == arguments.length) {
					args = arguments;
				} else {
					args = new String[len];
					System.arraycopy(arguments, i, args, 0, len);
				}
				client.executeCommand(testRepository, localRoot, command, globalOptions, localOptions, args);
				i += len;
			} while (arguments.length - i > 0);
			endTask();
		}
	}
}
