package org.eclipse.team.tests.core;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;

public class RepositoryProviderTests extends TeamTest {
	public RepositoryProviderTests() {
		super();
	}
	
	public RepositoryProviderTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(RepositoryProviderTests.class);
		return new TestSetup(suite);
		//return new testSetup(new RepositoryProviderTests("test"));
	}
	
	public void testProvidersRegistered() throws CoreException, TeamException {
		List repoProviderIds = new ArrayList(Arrays.asList(RepositoryProvider.getAllProviderTypeIds()));
		assertEquals(true, repoProviderIds.contains(RepositoryProviderBic.NATURE_ID));
		assertEquals(true, repoProviderIds.contains(RepositoryProviderNaish.NATURE_ID));
		assertEquals(false, repoProviderIds.contains(RepositoryProviderOtherSport.NATURE_ID));
	}
	
	public void testGetProviderGeneric() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("testGetProviderGeneric");
		IProject project2 = getUniqueTestProject("testGetProviderGeneric");
		
		// test that adding a non-team nature will not return a provider
		Team.addNatureToProject(project, RepositoryProviderOtherSport.NATURE_ID, null);
		assertTrue(RepositoryProvider.getProvider(project) == null);
		
		// adding a valid team provider should be fine
		Team.addNatureToProject(project, RepositoryProviderBic.NATURE_ID, null);
		Team.addNatureToProject(project2, RepositoryProviderNaish.NATURE_ID, null);
		RepositoryProvider provider1 = RepositoryProvider.getProvider(project);
		RepositoryProvider provider2 = RepositoryProvider.getProvider(project2);
		assertTrue(provider1 != null && provider1.getID().equals(RepositoryProviderBic.NATURE_ID));
		assertTrue(provider2 != null && provider2.getID().equals(RepositoryProviderNaish.NATURE_ID));
		assertTrue(provider1.getProject().equals(project) && provider2.getProject().equals(project2));
		
		// adding another provider should be prohibited by core
		try {
			Team.addNatureToProject(project, RepositoryProviderNaish.NATURE_ID, null);	
			fail("cannot add two team providers to the same project");
		} catch(TeamException e) {
			provider1 = RepositoryProvider.getProvider(project);
			assertTrue(provider1 != null && provider1.getID().equals(RepositoryProviderBic.NATURE_ID));
		}
		
		// adding another non-team provider should be ok but should not be returned as a provider
		Team.addNatureToProject(project2, RepositoryProviderOtherSport.NATURE_ID, null);
		provider2 = RepositoryProvider.getProvider(project2);
		assertTrue(provider2 != null && provider2.getID().equals(RepositoryProviderNaish.NATURE_ID));
		
		// closed or non-existant projects cannot be associated with a provider
		IProject closedProject = getUniqueTestProject("testGetProviderGenericClosed");
		IProject nonExistantProject = ResourcesPlugin.getWorkspace().getRoot().getProject("nonExistant");
		closedProject.close(null);
		assertTrue(RepositoryProvider.getProvider(closedProject) == null);
		assertTrue(RepositoryProvider.getProvider(nonExistantProject) == null);
		
		// removing the nature removes the provider association
		Team.removeNatureFromProject(project, RepositoryProviderBic.NATURE_ID, null);
		Team.removeNatureFromProject(project2, RepositoryProviderNaish.NATURE_ID, null);
		assertTrue(RepositoryProvider.getProvider(project)==null);
		assertTrue(RepositoryProvider.getProvider(project2)==null);
	}
	
	public void testGetProviderById() throws CoreException, TeamException {
		IProject project1 = getUniqueTestProject("testGetProviderById_1");
		IProject project2 = getUniqueTestProject("testGetProviderById_2");
		
		// test that adding a non-team nature will not return a provider
		Team.addNatureToProject(project1, RepositoryProviderOtherSport.NATURE_ID, null);
		assertTrue(RepositoryProvider.getProvider(project1, RepositoryProviderOtherSport.NATURE_ID) == null);
		
		// adding a valid team provider should be fine
		Team.addNatureToProject(project1, RepositoryProviderBic.NATURE_ID, null);
		Team.addNatureToProject(project2, RepositoryProviderNaish.NATURE_ID, null);
		assertTrue(RepositoryProvider.getProvider(project1, RepositoryProviderBic.NATURE_ID)!=null);
		assertTrue(RepositoryProvider.getProvider(project2, RepositoryProviderNaish.NATURE_ID)!=null);
		
		// adding another provider should be prohibited by core
		try {
			Team.addNatureToProject(project1, RepositoryProviderNaish.NATURE_ID, null);	
			fail("cannot add two team providers to the same project");
		} catch(TeamException e) {
			assertTrue(RepositoryProvider.getProvider(project1, RepositoryProviderBic.NATURE_ID)!=null);
		}
		
		// closed or non-existant projects cannot be associated with a provider
		IProject closedProject = getUniqueTestProject("testGetProviderGenericClosed");
		IProject nonExistantProject = ResourcesPlugin.getWorkspace().getRoot().getProject("nonExistant");
		closedProject.close(null);
		assertTrue(RepositoryProvider.getProvider(closedProject, "id") == null);
		assertTrue(RepositoryProvider.getProvider(nonExistantProject, "id") == null);
		
		// removing the nature removes the provider association
		Team.removeNatureFromProject(project1, RepositoryProviderBic.NATURE_ID, null);
		Team.removeNatureFromProject(project2, RepositoryProviderNaish.NATURE_ID, null);
		assertTrue(RepositoryProvider.getProvider(project1, RepositoryProviderBic.NATURE_ID)==null);
		assertTrue(RepositoryProvider.getProvider(project2, RepositoryProviderNaish.NATURE_ID)==null);
	}
	
	public void testFileModificationValidator() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("testFileModificationValidator");
		
		// adding a valid team provider should be fine
		Team.addNatureToProject(project, RepositoryProviderBic.NATURE_ID, null);
		RepositoryProviderBic bicProvider = (RepositoryProviderBic)RepositoryProvider.getProvider(project, RepositoryProviderBic.NATURE_ID);
		assertTrue(bicProvider!=null);
		
		// test that validator gets called by team core dispatching
		final boolean[] called = new boolean[] {false};
		bicProvider.setModificationValidator(new IFileModificationValidator() {
			// can't test validate edit here because it is only called from editors
			public IStatus validateEdit(IFile[] files, Object context) {
				return null;
			}
			public IStatus validateSave(IFile file) {
				called[0] = true;
				return getTeamTestStatus(IStatus.OK);
			}
		});
		IFile file = project.getFile("test.txt");
		file.create(new ByteArrayInputStream("test".getBytes()), true, null);
		file.setContents(new ByteArrayInputStream("test2".getBytes()), true, false, null);
		assertTrue(called[0] == true);
		
		// test that validator can veto a setContents
		called[0] = false;
		bicProvider.setModificationValidator(new IFileModificationValidator() {
			// can't test validate edit here because it is only called from editors
			public IStatus validateEdit(IFile[] files, Object context) {
				return null;
			}
			public IStatus validateSave(IFile file) {
				called[0] = true;
				return getTeamTestStatus(IStatus.ERROR);
			}
		});
		try {
			file.setContents(new ByteArrayInputStream("test3".getBytes()), true, false, null);
			fail("validate hook should veto this setContents");
		} catch(CoreException e) {
			assertTrue(called[0] == true);
		}
		
		// test that default validator allows the modification
		bicProvider.setModificationValidator(null);
		file.setContents(new ByteArrayInputStream("test4".getBytes()), true, false, null);	
	}
	
	public void testMoveDeleteHook() throws CoreException, TeamException {
		final IProject project = getUniqueTestProject("testMoveDeleteHook");
		
		// adding a valid team provider should be fine
		Team.addNatureToProject(project, RepositoryProviderBic.NATURE_ID, null);
		RepositoryProviderBic bicProvider = (RepositoryProviderBic)RepositoryProvider.getProvider(project, RepositoryProviderBic.NATURE_ID);
		assertTrue(bicProvider!=null);
		
		// only testing that dispatching works, resources plugin is testing the rest of the API
		final boolean[] called = new boolean[] {false, false, false, false, false, false};
		IMoveDeleteHook hook = new IMoveDeleteHook() {
			public boolean deleteFile(IResourceTree tree,	IFile file,	int updateFlags, IProgressMonitor monitor) {
				called[0] = true;
				return false;
			}
			public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor) {
				called[1] = true;
				return false;
			}
			public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor) {
				called[2] = true;
				return false;
			}
			public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) {
				called[3] = true;
				return false;
			}
			public boolean moveFolder(IResourceTree tree,	IFolder source,	IFolder destination, int updateFlags, IProgressMonitor monitor) {
				called[4] = true;
				return false;
			}
			public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
				called[5] = true;
				return false;
			}
		};
		bicProvider.setMoveDeleteHook(hook);
		
		IResource[] resources = buildResources(project, new String[] {"deleteFile.txt", "moveFile.txt", "deletedFolder/", "moveFolder/"});
		ensureExistsInWorkspace(resources, true);
		resources[0].delete(false, null);
		resources[1].move(resources[1].getFullPath().removeLastSegments(1).append("movedFile_NEW"), false, null);
		resources[2].delete(false, null);
		resources[3].move(resources[3].getFullPath().removeLastSegments(1).append("movedFolder"), false, null);
		// moving using the description allows the moved project to have natures ids or origination
		IProjectDescription desc = project.getDescription();
		desc.setName("movedProject");
		project.move(desc, false, null);
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject("movedProject");
		bicProvider = (RepositoryProviderBic)RepositoryProvider.getProvider(newProject);
		bicProvider.setMoveDeleteHook(hook);
		newProject.delete(true, null);
		for (int i = 0; i < called.length; i++) {
			assertTrue(called[i]);
		}
	}
	
	public void testMoveDeleteHookBetweenProjects() throws CoreException, TeamException {
		final IProject projectA = getUniqueTestProject("testMoveDeleteHookBetweenProjects_A");
		final IProject projectB = getUniqueTestProject("testMoveDeleteHookBetweenProjects_B");
		final IProject projectC = getUniqueTestProject("testMoveDeleteHookBetweenProjects_C");
		
		// adding a valid team provider should be fine
		Team.addNatureToProject(projectA, RepositoryProviderBic.NATURE_ID, null);
		final RepositoryProviderBic bicProvider = (RepositoryProviderBic)RepositoryProvider.getProvider(projectA, RepositoryProviderBic.NATURE_ID);
		Team.addNatureToProject(projectB, RepositoryProviderNaish.NATURE_ID, null);
		final RepositoryProviderNaish naishProvider = (RepositoryProviderNaish)RepositoryProvider.getProvider(projectB, RepositoryProviderNaish.NATURE_ID);
		assertTrue(bicProvider!=null && naishProvider!=null);
		
		// only testing that dispatching works, resources plugin is testing the rest of the API
		final boolean[] calledProjectA = new boolean[] {false, false};
		bicProvider.setMoveDeleteHook(new IMoveDeleteHook() {
			public boolean deleteFile(IResourceTree tree,	IFile file,	int updateFlags, IProgressMonitor monitor) {
				return false;
			}
			public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor) {
				return false;
			}
			public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor) {				
				return false;
			}
			public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) {
				assertTrue(bicProvider.getProject().equals(source.getProject()));
				calledProjectA[0] = true;
				return false;
			}
			public boolean moveFolder(IResourceTree tree,	IFolder source,	IFolder destination, int updateFlags, IProgressMonitor monitor) {
				assertTrue(bicProvider.getProject().equals(source.getProject()));
				calledProjectA[1] = true;
				return false;
			}
			public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
				return false;
			}
		});
		
		final boolean[] calledProjectB = new boolean[] {false, false};
		naishProvider.setMoveDeleteHook(new IMoveDeleteHook() {
			public boolean deleteFile(IResourceTree tree,	IFile file,	int updateFlags, IProgressMonitor monitor) {
				return false;
			}
			public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor) {
				return false;
			}
			public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor) {				
				return false;
			}
			public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) {
				assertTrue(bicProvider.getProject().equals(destination.getProject()));
				calledProjectB[0] = true;
				return false;
			}
			public boolean moveFolder(IResourceTree tree, IFolder source,	IFolder destination, int updateFlags, IProgressMonitor monitor) {
				assertTrue(bicProvider.getProject().equals(destination.getProject()));
				calledProjectB[1] = true;
				return false;
			}
			public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
				return false;
			}
		});
		
		// test that moving files/folders between two projects with providers calls the destination
		IResource[] resources = buildResources(projectA, new String[] {"moveFile.txt", "moveFolder/"});
		ensureExistsInWorkspace(resources, true);
		resources[0].move(projectB.getFullPath().append("moveFile_new.txt"), false, null);
		resources[1].move(projectB.getFullPath().append("movedFolder"), false, null);
		for (int i = 0; i < calledProjectA.length; i++) {
			assertTrue(calledProjectA[i]  && calledProjectB[i]==false);
		}
		
		// test that moving files/folders from a project with a provider to a project without a provider calls the
		// hooks for the source
		calledProjectA[0] = false; calledProjectA[1] = false;
		calledProjectB[0] = false; calledProjectB[1] = false;
		resources = buildResources(projectA, new String[] {"anotherMovedFiled.txt", "anotherMovedFolder/"});
		ensureExistsInWorkspace(resources, true);
		resources[0].move(projectC.getFullPath().append("moveFileOther_new.txt"), false, null);
		resources[1].move(projectC.getFullPath().append("movedFolderOther"), false, null);
		for (int i = 0; i < calledProjectA.length; i++) {
			assertTrue(calledProjectA[i] && calledProjectB[i]==false);
		}
	}
}