/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import junit.framework.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.resources.ResourceTest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.*;
import org.eclipse.team.internal.ccvs.core.resources.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.SyncFileChangeListener;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.ui.IWorkbenchPart;

public class EclipseTest extends ResourceTest {

	protected static IProgressMonitor DEFAULT_MONITOR = new NullProgressMonitor();
	protected static final int RANDOM_CONTENT_SIZE = 3876;
	protected static String eol = System.getProperty("line.separator");
	
	public static Test suite(Class c) {
		String testName = System.getProperty("eclipse.cvs.testName");
		if (testName == null) {
			TestSuite suite = new TestSuite(c);
			return new CVSTestSetup(suite);
		} else {
			try {
				return new CVSTestSetup((Test)c.getConstructor(new Class[] { String.class }).newInstance(new Object[] {testName}));
			} catch (Exception e) {
				fail(e.getMessage());
				// Above will throw so below is never actually reached
				return null;
			}
		}
	}
	
	public EclipseTest() {
		super();
		if (eol == null) eol = "\n";
	}

	public EclipseTest(String name) {
		super(name);
		if (eol == null) eol = "\n";
	}

	/*
	 * Get the resources for the given resource names
	 */
	public IResource[] getResources(IContainer container, String[] hierarchy) throws CoreException {
		IResource[] resources = new IResource[hierarchy.length];
		for (int i=0;i<resources.length;i++) {
			resources[i] = container.findMember(hierarchy[i]);
			if (resources[i] == null) {
				resources[i] = buildResources(container, new String[] {hierarchy[i]})[0];
			}
		}
		return resources;
	}
	
	/**
	 * Add the resources to an existing container and upload them to CVS
	 */
	public IResource[] addResources(IContainer container, String[] hierarchy, boolean checkin) throws CoreException, TeamException {
		IResource[] newResources = buildResources(container, hierarchy, false);
		addResources(newResources);
		if (checkin) commitResources(newResources, IResource.DEPTH_ZERO);
		return newResources;
	}
	
	protected void addResources(IResource[] newResources) throws CoreException {
		if (newResources.length == 0) return;
		executeHeadless(new AddOperation(null, newResources));
	}
	
	/**
	 * Perform a CVS edit of the given resources
	 */
	public IResource[] editResources(IContainer container, String[] hierarchy) throws CoreException, TeamException {
		IResource[] resources = getResources(container, hierarchy);
		getProvider(container).edit(resources, true /* recurse */, true /* notifyServer */, ICVSFile.NO_NOTIFICATION, DEFAULT_MONITOR);
		assertReadOnly(resources, false /* isReadOnly */, true /* recurse */);
		return resources;
	}
	
	/**
	 * Perform a CVS unedit of the given resources
	 */
	public IResource[] uneditResources(IContainer container, String[] hierarchy) throws CoreException, TeamException {
		IResource[] resources = getResources(container, hierarchy);
		getProvider(container).unedit(resources, true /* recurse */, true/* notifyServer */, DEFAULT_MONITOR);
		assertReadOnly(resources, true /* isReadOnly */, true /* recurse */);
		return resources;
	}
	
	public void appendText(IResource resource, String text, boolean prepend) throws CoreException, IOException, CVSException {
		IFile file = (IFile)resource;
		String contents = getFileContents(file);
		StringBuffer buffer = new StringBuffer();
		if (prepend) {
			buffer.append(text);
		}
		buffer.append(contents);
		if (!prepend) {
			buffer.append(eol + text);
		}
		setContentsAndEnsureModified(file, buffer.toString());
	}
	
	public void assertEndsWith(IFile file, String text) throws IOException, CoreException {
		assertTrue(getFileContents(file).endsWith(text));		
	}
	
	public void assertStartsWith(IFile file, String text) throws IOException, CoreException {
		assertTrue(getFileContents(file).startsWith(text));		
	}
	
	public static String getFileContents(IFile file) throws IOException, CoreException {
		StringBuffer buf = new StringBuffer();
		Reader reader = new InputStreamReader(new BufferedInputStream(file.getContents()));
		try {
			int c;
			while ((c = reader.read()) != -1) buf.append((char)c);
		} finally {
			reader.close();
		}
		return buf.toString();		
	}
	
	/**
	 * Delete the resources from an existing container and the changes to CVS
	 */
	public IResource[] changeResources(IContainer container, String[] hierarchy, boolean checkin) throws CoreException, TeamException {
		List changedResources = new ArrayList(hierarchy.length);
		for (int i=0;i<hierarchy.length;i++) {
			IResource resource = container.findMember(hierarchy[i]);
			if (resource.getType() == IResource.FILE) {
				changedResources.add(resource);
				setContentsAndEnsureModified((IFile)resource);
			}
		}
		IResource[] resources = (IResource[])changedResources.toArray(new IResource[changedResources.size()]);
		if (checkin) commitResources(resources, IResource.DEPTH_ZERO);
		return resources;
	}
	
	/**
	 * Delete the resources from an existing container and the changes to CVS
	 */
	public IResource[] deleteResources(IContainer container, String[] hierarchy, boolean checkin) throws CoreException, TeamException {
		IResource[] resources = getResources(container, hierarchy);
		deleteResources(resources);
		if (checkin)
			commitResources(resources, IResource.DEPTH_ZERO);
		return resources;
	}
	
	/**
	 * Delete the resources and mark them as outgoing deletions.
	 * Deleting the resources is enough since the move/delete hook will
	 * tak care of making them outgoing deletions.
	 */
	protected void deleteResources(IResource[] resources) throws TeamException, CoreException {
		if (resources.length == 0) return;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			resource.delete(false, DEFAULT_MONITOR);
		}
	}
	/**
	 * Unmanage the resources
	 */
	public void unmanageResources(IContainer container, String[] hierarchy) throws CoreException, TeamException {
		IResource[] resources = getResources(container, hierarchy);
		unmanageResources(resources);
	}
	
	protected void unmanageResources(IResource[] resources) throws TeamException, CoreException {
		for (int i=0;i<resources.length;i++) {
			CVSWorkspaceRoot.getCVSResourceFor(resources[i]).unmanage(null);
		}
	}
	
	/**
	 * Update the resources from an existing container with the changes from the CVS repository
	 */
	public IResource[] updateResources(IContainer container, String[] hierarchy, boolean ignoreLocalChanges) throws CoreException, TeamException {
		IResource[] resources = getResources(container, hierarchy);
		return updateResources(resources, ignoreLocalChanges);
	}
	
	/**
	 * Update the resources from an existing container with the changes from the CVS repository
	 */
    protected IResource[] updateResources(IResource[] resources, boolean ignoreLocalChanges) throws CVSException {
        LocalOption[] options = Command.NO_LOCAL_OPTIONS;
		if(ignoreLocalChanges) {
			options = new LocalOption[] {Update.IGNORE_LOCAL_CHANGES};
		}
		executeHeadless(new UpdateOperation(null, resources, options, null));
		return resources;
    }

    protected void replace(IContainer container, String[] hierarchy, CVSTag tag, boolean recurse) throws CoreException {
		IResource[] resources = getResources(container, hierarchy);
		replace(resources, tag, recurse);
	}
	
	protected void replace(IResource[] resources, CVSTag tag, boolean recurse) throws CoreException {
		ReplaceOperation op = new ReplaceOperation(null, resources, tag, recurse);
		executeHeadless(op);
	}
	
	public void updateProject(IProject project, CVSTag tag, boolean ignoreLocalChanges) throws TeamException {
		LocalOption[] options = Command.NO_LOCAL_OPTIONS;
		if(ignoreLocalChanges) {
			options = new LocalOption[] {Update.IGNORE_LOCAL_CHANGES};
		}
		executeHeadless(new UpdateOperation(null, new IResource[] {project}, options, tag));
	}
	
	public void commitProject(IProject project) throws TeamException, CoreException {
		commitResources(project, true);
	}
	
	public void commitResources(IContainer container, boolean deep) throws TeamException, CoreException {
		commitResources(new IResource[] {container }, deep?IResource.DEPTH_INFINITE:IResource.DEPTH_ZERO);
	}
	
	/**
	 * Commit the resources from an existing container to the CVS repository
	 */
	public IResource[] commitResources(IContainer container, String[] hierarchy) throws CoreException, TeamException {
		IResource[] resources = getResources(container, hierarchy);
		commitResources(resources, IResource.DEPTH_ZERO);
		return resources;
	}
	
	protected void commitResources(IResource[] resources, int depth) throws TeamException, CoreException {
		commitResources(resources, depth, "");
	}
	
	/*
	 * Commit the provided resources which must all be in the same project
	 */
	protected void commitResources(IResource[] resources, int depth, String message) throws TeamException, CoreException {
		if (resources.length == 0) return;
		executeHeadless(new CommitOperation(null, resources, new Command.LocalOption[] { Commit.makeArgumentOption(Command.MESSAGE_OPTION, message) }));
	}
	
	/**
	 * Commit the resources from an existing container to the CVS repository
	 */
	public void tagProject(IProject project, CVSTag tag, boolean force) throws TeamException {
		ITagOperation op = new TagOperation((IWorkbenchPart)null, new IResource[] {project});
		runTag(op, tag, force);
	}
	
	public void tagRemoteResource(ICVSRemoteResource resource, CVSTag tag, boolean force) throws TeamException  {
		ITagOperation op = new TagInRepositoryOperation(null, new ICVSRemoteResource[] {resource});
		runTag(op, tag, force);
	
	}
	private void runTag(ITagOperation op, CVSTag tag, boolean force) throws TeamException {
		if (force) op.moveTag();
		op.setTag(tag);
		try {
			((CVSOperation)op).run(DEFAULT_MONITOR);
		} catch (InterruptedException e) {
			fail("Tag interrupted.");
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof TeamException)  {
				throw (TeamException) e.getTargetException();
			} else  {
				e.printStackTrace();
				fail("Unexpected error while tagging");
			}
		}
	}
	public void makeBranch(IResource[] resources, CVSTag version, CVSTag branch, boolean update) throws CVSException {
		BranchOperation op = new BranchOperation(null, resources);
		op.setTags(version, branch, update);
		executeHeadless(op);
	}
	/**
	 * Return a collection of resources defined by hierarchy. The resources
	 * are added to the workspace and to the file system. If the manage flag is true, the
	 * resources are auto-managed, if false, they are left un-managed.
	 */
	public IResource[] buildResources(IContainer container, String[] hierarchy, boolean includeContainer) throws CoreException {
		List resources = new ArrayList(hierarchy.length + 1);
		resources.addAll(Arrays.asList(buildResources(container, hierarchy)));
		if (includeContainer)
			resources.add(container);
		IResource[] result = (IResource[]) resources.toArray(new IResource[resources.size()]);
		ensureExistsInWorkspace(result, true);
		for (int i = 0; i < result.length; i++) {
			if (result[i].getType() == IResource.FILE)
				// 3786 bytes is the average size of Eclipse Java files!
				 ((IFile) result[i]).setContents(getRandomContents(RANDOM_CONTENT_SIZE), true, false, null);
		}
		return result;
	}

	/*
	 * Checkout a copy of the project into a project with the given postfix
	 */
	 protected IProject checkoutCopy(IProject project, String postfix) throws TeamException {
		// Check the project out under a different name and validate that the results are the same
		IProject copy = getWorkspace().getRoot().getProject(project.getName() + postfix);
		checkout(getRepository(), copy, CVSWorkspaceRoot.getCVSFolderFor(project).getFolderSyncInfo().getRepository(), null, DEFAULT_MONITOR);
		return copy;
	 }
	 
	 protected IProject checkoutCopy(IProject project, CVSTag tag) throws TeamException {
		// Check the project out under a different name and validate that the results are the same
		IProject copy = getWorkspace().getRoot().getProject(project.getName() + tag.getName());
		checkout(getRepository(), copy, 
			CVSWorkspaceRoot.getCVSFolderFor(project).getFolderSyncInfo().getRepository(), 
			tag, DEFAULT_MONITOR);
		return copy;
	 }
	 
	public static void checkout(
		final ICVSRepositoryLocation repository,
		final IProject project,
		final String sourceModule,
		final CVSTag tag,
		IProgressMonitor monitor)
		throws TeamException {
		
		RemoteFolder remote = new RemoteFolder(null, repository, sourceModule == null ? project.getName() : sourceModule, tag);
		executeHeadless(new CheckoutSingleProjectOperation(null, remote, project, null, false /* the project is not preconfigured */) {
			public boolean promptToOverwrite(String title, String msg) {
				return true;
			}
		});

	}

	protected IProject checkoutProject(IProject project, String moduleName, CVSTag tag) throws TeamException {
	 	if (project == null)
	 		project = getWorkspace().getRoot().getProject(new Path(moduleName).lastSegment());
		checkout(getRepository(), project, moduleName, tag, DEFAULT_MONITOR);
		return project;
	 }
	/*
	 * This method creates a project with the given resources, imports
	 * it to CVS and checks it out
	 */
	protected IProject createProject(String prefix, String[] resources) throws CoreException, TeamException {
		IProject project = getUniqueTestProject(prefix);
		buildResources(project, resources, true);
		shareProject(project);
		assertValidCheckout(project);
		return project;
	}
	
	/*
	 * Create a test project using the currently running test case as the project name prefix
	 */
	protected IProject createProject(String[] strings) throws CoreException {
		return createProject(getName(), strings);
	}
	
	/*
	 * Compare two projects by comparing thier providers
	 */
	protected void assertEquals(IProject project1, IProject project2) throws CoreException, IOException {
		assertEquals(project1, project2, false, false);
	}
	
	protected void assertEquals(IProject project1, IProject project2, boolean includeTimestamps, boolean includeTags) throws CoreException, IOException {
		assertEquals(getProvider(project1), getProvider(project2), includeTimestamps, includeTags);
	}
	
	/*
	 * Compare CVS team providers by comparing the cvs resource corresponding to the provider's project
	 */
	protected void assertEquals(CVSTeamProvider provider1, CVSTeamProvider provider2, boolean includeTimestamps, boolean includeTags) throws CoreException, IOException {
		assertEquals(Path.EMPTY, CVSWorkspaceRoot.getCVSFolderFor(provider1.getProject()), 
			CVSWorkspaceRoot.getCVSFolderFor(provider2.getProject()), 
			includeTimestamps, includeTags);
	}
	
	protected void assertContentsEqual(IContainer c1, IContainer c2) throws CoreException {
		assertTrue("The number of resource in " + c1.getProjectRelativePath().toString() + " differs", 
			c1.members().length == c2.members().length);
		IResource[] resources = c1.members();
		for (int i= 0;i <resources.length;i++) {
			assertContentsEqual(resources[i], c2.findMember(resources[i].getName()));
		}
	}
	
	protected void assertContentsEqual(IResource resource, IResource resource2) throws CoreException {
		if (resource.getType() == IResource.FILE) {
			assertContentsEqual((IFile)resource, (IFile)resource2);
		} else {
			assertContentsEqual((IContainer)resource, (IContainer)resource2);
		}
	}

	protected void assertContentsEqual(IFile resource, IFile resource2) throws CoreException {
		assertTrue("Contents of " + resource.getProjectRelativePath() + " do not match", compareContent(resource.getContents(), resource2.getContents()));
	}
	/*
	 * Compare resources by casting them to their prpoer type
	 */
	protected void assertEquals(IPath parent, ICVSResource resource1, ICVSResource resource2, boolean includeTimestamps, boolean includeTags) throws CoreException, CVSException, IOException {
		assertEquals("Resource types do not match for " + parent.append(resource1.getName()), resource1.isFolder(), resource2.isFolder());
		if (!resource1.isFolder())
			assertEquals(parent, (ICVSFile)resource1, (ICVSFile)resource2, includeTimestamps, includeTags);
		else 
			assertEquals(parent, (ICVSFolder)resource1, (ICVSFolder)resource2, includeTimestamps, includeTags);
	}
	
	/*
	 * Compare folders by comparing their folder sync info and there children
	 * 
	 * XXX What about unmanaged children?
	 */
	protected void assertEquals(IPath parent, ICVSFolder container1, ICVSFolder container2, boolean includeTimestamps, boolean includeTags) throws CoreException, CVSException, IOException {
		IPath path = parent.append(container1.getName());
		assertEquals(path, container1.getFolderSyncInfo(), container2.getFolderSyncInfo(), includeTags);
		assertTrue("The number of resource in " + path.toString() + " differs", 
			container1.members(ICVSFolder.ALL_EXISTING_MEMBERS).length 
			== container2.members(ICVSFolder.ALL_EXISTING_MEMBERS).length);
		ICVSResource[] resources = container1.members(ICVSFolder.ALL_EXISTING_MEMBERS);
		for (int i= 0;i <resources.length;i++) {
			assertEquals(path, resources[i], container2.getChild(resources[i].getName()), includeTimestamps, includeTags);
		}

	}
	
	/*
	 * Compare the files contents and sync information
	 */
	protected void assertEquals(IPath parent, ICVSFile file1, ICVSFile file2, boolean includeTimestamps, boolean includeTags) throws CoreException, CVSException, IOException {
		if (file1.getName().equals(".project")) return;
		// Getting the contents first is important as it will fetch the proper sync info if one of the files is a remote handle
		assertTrue("Contents of " + parent.append(file1.getName()) + " do not match", compareContent(getContents(file1), getContents(file2)));
		assertEquals(parent.append(file1.getName()), file1.getSyncInfo(), file2.getSyncInfo(), includeTimestamps, includeTags);
	}
	
	/*
	 * Compare sync info by comparing the entry line generated by the sync info
	 */
	protected void assertEquals(IPath path, ResourceSyncInfo info1, ResourceSyncInfo info2, boolean includeTimestamp, boolean includeTag) throws CoreException, CVSException, IOException {
		if (info1 == null || info2 == null) {
			if (info1 == info2) return;
			if (info1 == null) {
				fail("Expected no resource sync info  for " + path.toString() + " but it was " + info2 + " instead");
			}
			if (info2 == null) {
				fail("Expected resource sync info of " + info1 + " for " + path.toString() + " but there was no sync info.");
			}
			fail("Shouldn't be able to get here");
			return;
		}
		String line1;
		String line2;
		if(includeTimestamp) {
			line1 = info1.getEntryLine();
			line2 = info2.getEntryLine();
		} else {
			line1 = info1.getServerEntryLine(null);
			line2 = info2.getServerEntryLine(null);
		}
		if (!includeTag) {
			// Strip everything past the last slash
			line1 = line1.substring(0, line1.lastIndexOf('/'));
			line2 = line2.substring(0, line2.lastIndexOf('/'));
		}
		assertEquals("Resource Sync info differs for " + path.toString(), line1, line2);
	}
	
	/*
	 * Use the equals of folder sync info unless the tag is not included in which case we just
	 * compare the root and repository
	 */
	protected void assertEquals(IPath path, FolderSyncInfo info1, FolderSyncInfo info2, boolean includeTag) throws CoreException, CVSException, IOException {
		if (info1 == null && info2 == null) {
			return;
		} else if (info1 == null) {
			fail("Expected " + path.toString() + " not to be a CVS folder but it is.");
		} else if (info2 == null) {
			fail("Expected " + path.toString() + " to be a CVS folder but it isn't.");
		}
		
		if (includeTag) {
			assertTrue("Folder sync info differs for " + path.toString(), info1.equals(info2));
		} else {
			assertTrue("Repository Root differs for " + path.toString(), info1.getRoot().equals(info2.getRoot()));
			assertTrue("Repository relative path differs for " + path.toString(), info1.getRepository().equals(info2.getRepository()));
		}
	}
	
	
	/*
	 * Compare folders by comparing their folder sync info and there children
	 * 
	 * XXX What about unmanaged children?
	 */
	protected void assertEquals(IPath parent, RemoteFolder container1, RemoteFolder container2, boolean includeTags) throws CoreException, TeamException, IOException {
		IPath path = parent.append(container1.getName());
		assertEquals(path, container1.getFolderSyncInfo(), container2.getFolderSyncInfo(), includeTags);
		ICVSRemoteResource[] members1 = container1.getMembers(DEFAULT_MONITOR);
		ICVSRemoteResource[] members2 = container2.getMembers(DEFAULT_MONITOR);
		assertTrue("Number of members differ for " + path, members1.length == members2.length);
		Map memberMap2 = new HashMap();
		for (int i= 0;i <members2.length;i++) {
			memberMap2.put(members2[i].getName(), members2[i]);
		}
		for (int i= 0;i <members1.length;i++) {
			ICVSRemoteResource member2 = (ICVSRemoteResource)memberMap2.get(members1[i].getName());
			assertNotNull("Resource does not exist: " + path.append(members1[i].getName()) + member2);
			assertEquals(path, members1[i], member2, includeTags);
		}
	}
	protected void assertEquals(IPath parent, ICVSRemoteResource resource1, ICVSRemoteResource resource2, boolean includeTags) throws CoreException, TeamException, IOException {
		assertEquals("Resource types do not match for " + parent.append(resource1.getName()), resource1.isContainer(), resource2.isContainer());
		if (resource1.isContainer())
			assertEquals(parent, (RemoteFolder)resource1, (RemoteFolder)resource2, includeTags);
		else 
			assertEquals(parent, (ICVSFile)resource1, (ICVSFile)resource2, false, includeTags);
	}
	
	
	/*
	 * Compare the local project with the remote state by checking out a copy of the project.
	 */
	protected void assertLocalStateEqualsRemote(IProject project) throws TeamException, CoreException, IOException {
		assertEquals(getProvider(project), getProvider(checkoutCopy(project, "-remote")), false, true);
	}
	
	/*
	 * Compare the local project with the remote state indicated by the given tag by checking out a copy of the project.
	 */
	protected void assertLocalStateEqualsRemote(String message, IProject project, CVSTag tag) throws TeamException, CoreException, IOException {
		assertEquals(getProvider(project), getProvider(checkoutCopy(project, tag)), true, false);
	}
	
	protected void assertHasNoRemote(String prefix, IResource[] resources) throws TeamException {
		for (int i=0;i<resources.length;i++) 
			assertHasNoRemote(prefix, resources[i]);
	}
	
	protected void assertHasNoRemote(String prefix, IResource resource) throws TeamException {
		assertTrue(prefix + " resource should not have a remote", !CVSWorkspaceRoot.hasRemote(resource));
	}
	
	protected void assertHasRemote(String prefix, IResource[] resources) throws TeamException {
		for (int i=0;i<resources.length;i++) 
			assertHasRemote(prefix, resources[i]);
	}
	
	protected void assertHasRemote(String prefix, IResource resource) throws TeamException {
		assertTrue(prefix + " resource should have a remote", CVSWorkspaceRoot.hasRemote(resource));
	}
	
	protected void assertIsModified(String prefix, IResource[] resources) throws TeamException {
		for (int i=0;i<resources.length;i++) 
			assertIsModified(prefix, resources[i]);
	}
	
	protected void assertIsModified(String prefix, IResource resource) throws TeamException {
		// Only check for files as CVS doesn't dirty folders
		if (resource.getType() == IResource.FILE)
			assertTrue(prefix + " resource " + resource.getFullPath() + " should be dirty.", ((ICVSFile)getCVSResource(resource)).isModified(null));
	}
	
	protected void assertNotModified(String prefix, IResource[] resources) throws TeamException {
		for (int i=0;i<resources.length;i++) 
			assertNotModified(prefix, resources[i]);
	}
	
	protected void assertNotModified(String prefix, IResource resource) throws TeamException {
		assertTrue(prefix + " resource should be dirty", !((ICVSFile)getCVSResource(resource)).isModified(null));
	}
	
	protected void assertIsIgnored(IResource resource, boolean ignoredState) throws TeamException {
		assertEquals("Resource " + resource.getFullPath() + " should be ignored but isn't.", 
						ignoredState, getCVSResource(resource).isIgnored());
	}
	
	protected void assertValidCheckout(IProject project) {
		// NOTE: Add code to ensure that the project was checkout out properly
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(project);
		assertNotNull(provider);
	}

	protected void assertReadOnly(IResource[] resources, final boolean isReadOnly, final boolean recurse) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.FILE) {
						assertEquals(isReadOnly, resource.isReadOnly());
					}
					return recurse;
				}
			});
		}
	}
	
	protected InputStream getContents(ICVSFile file) throws CVSException, IOException {
		if (file instanceof ICVSRemoteFile)
			return ((RemoteFile)file).getContents(DEFAULT_MONITOR);
		else
			return new BufferedInputStream(file.getContents());
	}
	
	/*
	 * Get the CVS Resource for the given resource
	 */
	protected ICVSResource getCVSResource(IResource resource) throws CVSException {
		return CVSWorkspaceRoot.getCVSResourceFor(resource);
	}
	
	protected IProject getNamedTestProject(String name) throws CoreException {
		IProject target = getWorkspace().getRoot().getProject(name);
		if (!target.exists()) {
			target.create(null);
			target.open(null);		
		}
		assertExistsInFileSystem(target);
		return target;
	}
	protected CVSTeamProvider getProvider(IResource resource) throws TeamException {
		return (CVSTeamProvider)RepositoryProvider.getProvider(resource.getProject());
	}
	protected static InputStream getRandomContents(int sizeAtLeast) {
		StringBuffer randomStuff = new StringBuffer(sizeAtLeast + 100);
		while (randomStuff.length() < sizeAtLeast) {
			randomStuff.append(getRandomSnippet() + eol);
		}
		return new ByteArrayInputStream(randomStuff.toString().getBytes());
	}
	/**
	 * Return String with some random text to use
	 * as contents for a file resource.
	 */
	public static String getRandomSnippet() {
		switch ((int) Math.round(Math.random() * 10)) {
			case 0 :
				return "este e' o meu conteudo (portuguese)";
			case 1 :
				return "Dann brauchen wir aber auch einen deutschen Satz!";
			case 2 :
				return "I'll be back";
			case 3 :
				return "don't worry, be happy";
			case 4 :
				return "there is no imagination for more sentences";
			case 5 :
				return "customize yours";
			case 6 :
				return "foo";
			case 7 :
				return "bar";
			case 8 :
				return "foobar";
			case 9 :
				return "case 9";
			default :
				return "these are my contents";
		}
	}
	protected IProject getUniqueTestProject(String prefix) throws CoreException {
		// manage and share with the default stream create by this class
		return getNamedTestProject(prefix + "-" + Long.toString(System.currentTimeMillis()));
	}
	
	protected CVSRepositoryLocation getRepository() {
		return CVSTestSetup.repository;
	}
	protected void importProject(IProject project) throws TeamException {
		
		// Create the root folder for the import operation
		ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(project);

		// Perform the import
		IStatus status;
		Session s = new Session(getRepository(), root);
		s.open(DEFAULT_MONITOR, true /* open for modification */);
		try {
			status = Command.IMPORT.execute(s,
				Command.NO_GLOBAL_OPTIONS,
				new LocalOption[] {Import.makeArgumentOption(Command.MESSAGE_OPTION, "Initial Import")},
				new String[] { project.getName(), getRepository().getUsername(), "start" },
				null,
				DEFAULT_MONITOR);
		} finally {
			s.close();
		}

		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			throw new CVSServerException(status);
		}
	}
	
	protected void shareProject(IProject project) throws TeamException, CoreException {
		mapNewProject(project);
		commitNewProject(project);
	}
	
	protected void mapNewProject(IProject project) throws TeamException {
		shareProject(getRepository(), project, null, DEFAULT_MONITOR);
	}
	
	/**
	 * Map the given local project to remote folder, creating the remote folder or any of
	 * its ancestors as necessary.
	 * @param location
	 * @param project
	 * @param moduleName
	 * @param default_monitor
	 */
	protected void shareProject(CVSRepositoryLocation location, IProject project, String moduleName, IProgressMonitor default_monitor) throws CVSException {
		ShareProjectOperation op = new ShareProjectOperation(null, location, project, moduleName);
		executeHeadless(op);
	}
	
	protected void commitNewProject(IProject project) throws CoreException, CVSException, TeamException {
		List resourcesToAdd = new ArrayList();
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++) {
			if ( ! CVSWorkspaceRoot.getCVSResourceFor(members[i]).isIgnored()) {
				resourcesToAdd.add(members[i]);
			}
		}
		addResources((IResource[]) resourcesToAdd.toArray(new IResource[resourcesToAdd.size()]));
		commitResources(new IResource[] {project}, IResource.DEPTH_INFINITE);
		// Pause to ensure that future operations happen later than timestamp of committed resources
		waitMsec(1500);
	}
	
	/**
	 * Return an input stream with some random text to use
	 * as contents for a file resource.
	 */
	public InputStream getRandomContents() {
		return getRandomContents(RANDOM_CONTENT_SIZE);
	}
	
	protected void setContentsAndEnsureModified(IFile file) throws CoreException, TeamException {
		setContentsAndEnsureModified(file, getRandomContents().toString());
	}
	
	protected void setContentsAndEnsureModified(IFile file, String contents) throws CoreException, CVSException {
		ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
		int count = 0;
		if (contents == null) contents ="";
		do {
			file.setContents(new ByteArrayInputStream(contents.getBytes()), false, false, null);
			assertTrue("Timestamp granularity is too small. Increase test wait factor", count <= CVSTestSetup.WAIT_FACTOR);
			if (!cvsFile.isModified(null)) {
				waitMsec(1500);
				count++;
			}
		} while (!cvsFile.isModified(null));
	}
	
	public void waitMsec(int msec) {	
		try {
			Thread.sleep(msec);
		} catch(InterruptedException e) {
			fail("wait-problem");
		}
	}
	
	public static void waitForJobCompletion(Job job) {
		// process UI events first, give the main thread a chance
		// to handle any syncExecs or asyncExecs posted as a result
		// of the event processing thread.
		while (Display.getCurrent().readAndDispatch()) {};
		
		// wait for the event handler to process changes.
		while(job.getState() != Job.NONE) {
			while (Display.getCurrent().readAndDispatch()) {};
			try {
				Thread.sleep(10);		
			} catch (InterruptedException e) {
			}
		}
		while (Display.getCurrent().readAndDispatch()) {};
	}
	
	public static void waitForIgnoreFileHandling() {
		waitForJobCompletion(SyncFileChangeListener.getDeferredHandler().getEventHandlerJob());
	}
	
	public static void waitForSubscriberInputHandling(SubscriberSyncInfoCollector input) {
		input.waitForCollector(new IProgressMonitor() {
			public void beginTask(String name, int totalWork) {
			}
			public void done() {
			}
			public void internalWorked(double work) {
			}
			public boolean isCanceled() {
				return false;
			}
			public void setCanceled(boolean value) {
			}
			public void setTaskName(String name) {
			}
			public void subTask(String name) {
			}
			public void worked(int work) {
				while (Display.getCurrent().readAndDispatch()) {}
			}
		});
	}

	protected static void executeHeadless(CVSOperation op) throws CVSException {
		try {
			try {
				// Bypass contxt by executing run(IProgressMonitor) directly
				op.run(DEFAULT_MONITOR);
			} catch (InvocationTargetException e1) {
				throw CVSException.wrapException(e1);
			}
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (CVSTestSetup.logListener != null) {
			try {
				CVSTestSetup.logListener.checkErrors();
			} catch (CoreException e) {
				if (CVSTestSetup.FAIL_IF_EXCEPTION_LOGGED) {
					fail("Exception written to log: ", e);
				} else {
					// Write the log to standard out so it can be more easily seen
					write(e.getStatus(), 0);
				}
			}
		}
	}

	protected void write(IStatus status, int indent) {
		PrintStream output = System.out;
		indent(output, indent);
		output.println("Severity: " + status.getSeverity());

		indent(output, indent);
		output.println("Plugin ID: " + status.getPlugin());

		indent(output, indent);
		output.println("Code: " + status.getCode());

		indent(output, indent);
		output.println("Message: " + status.getMessage());
		
		Throwable t = status.getException();
		if (t != null) {
			t.printStackTrace(output);
			if (t instanceof CoreException) {
				write(((CoreException)t).getStatus(), indent + 1);
			}
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++)
				write(children[i], indent + 1);
		}
	}
	
	protected static void indent(OutputStream output, int indent) {
		for (int i = 0; i < indent; i++)
			try {
				output.write("  ".getBytes());
			} catch (IOException e) {
				// ignore
			}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#runBare()
	 */
	public void runBare() throws Throwable {
		try {
			super.runBare();
		} catch (CVSException e) {
			// If a communication exception occurred
			// perhaps it is a server problem
			// Try again, just in case it is
			if (containsCommunicationException(e)) {
				super.runBare();
			} else {
				throw e;
			}
		}
	}

	private boolean containsCommunicationException(CVSException e) {
		if (e instanceof CVSCommunicationException) return true;
		IStatus status = e.getStatus();
		if (status.getException() instanceof CVSCommunicationException) return true;
		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				IStatus child = children[i];
				if (child.getException() instanceof CVSCommunicationException) return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.harness.EclipseWorkspaceTest#ensureDoesNotExistInWorkspace(org.eclipse.core.resources.IResource)
	 */
	public void ensureDoesNotExistInWorkspace(IResource resource) {
		// Overridden to change how the workspace is deleted on teardown
		if (resource.getType() == IResource.ROOT) {
			// Delete each project individually
			Job[] allJobs = Platform.getJobManager().find(null /* all families */);
			IProject[] projects = ((IWorkspaceRoot)resource).getProjects();
			try {
				ensureDoesNotExistInWorkspace(projects);
			} catch (AssertionFailedError e) {
				// The delete failed. Write the active jobs to stdout
				System.out.println("Jobs active at time of deletion failure: "); //$NON-NLS-1$
				if (allJobs.length == 0) {
					System.out.println("None"); //$NON-NLS-1$
				}
				for (int i = 0; i < allJobs.length; i++) {
					Job job = allJobs[i];
					System.out.println(job.getName());
				}
				if (CVSTestSetup.FAIL_IF_EXCEPTION_LOGGED) {
					throw e;
				}
			}
		} else {
			super.ensureDoesNotExistInWorkspace(resource);
		}
	}
	
    protected void assertStatusContainsCode(IStatus status, int code) {
        if (status.isMultiStatus()) {
            IStatus[] children = status.getChildren();
            for (int i = 0; i < children.length; i++) {
                IStatus child = children[i];
                if (child.getCode() == code)
                    return;
            }
            fail("Expected status code was not present");
        } else {
            assertEquals("Status code is not what is expected", status.getCode(), code);
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    protected void runTest() throws Throwable {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Policy.recorder = new PrintStream(os);
        try {
            try {
                // Override the runTest method in order to print the entire trace of a
                // test that failed due to a CoreException including nested exceptions
                super.runTest();
            } catch (CoreException e) {
                e.printStackTrace();
                write(e.getStatus(), 0);
                throw e;
            }
        } catch (Throwable e) {
            // Transfer the recorded debug info to stdout
            Policy.recorder.close();
            System.out.println(new String(os.toByteArray()));
            throw e;
        } finally {
            Policy.recorder.close();
            Policy.recorder = null;
        }
    }
}

