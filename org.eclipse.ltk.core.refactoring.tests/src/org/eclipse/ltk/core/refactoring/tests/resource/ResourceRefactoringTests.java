package org.eclipse.ltk.core.refactoring.tests.resource;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.resource.MoveResourcesDescriptor;

import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

public class ResourceRefactoringTests extends TestCase {

	public static Test suite() {
		TestSuite suite= new TestSuite("All LTK Refactoring Resource Tests"); //$NON-NLS-1$
		suite.addTestSuite(ResourceRefactoringTests.class);
		suite.addTestSuite(ResourceRefactoringUndoTests.class);
		return suite;
	}
	
	private SimpleTestProject fProject;

	protected void setUp() throws Exception {
		fProject= new SimpleTestProject();
	}

	protected void tearDown() throws Exception {
		fProject.delete();
	}

	public void testMoveChange1() throws Exception {
		
		String content= "hello";
		
		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");
		
		Change undoChange= perform(new MoveResourceChange(file, destination));

		IResource movedResource= assertMove(file, destination, content);
		
		perform(undoChange);

		assertMove(movedResource, file.getParent(), content);
	}
	
	public void testMoveChange2() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		Change undoChange= perform(new MoveResourceChange(testFolder, destination));


		IFolder movedResource= (IFolder) assertMove(testFolder, destination, null);
		assertTrue(movedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertMove(movedResource, testFolder.getParent(), null);
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}
	
	public void testMoveChange3() throws Exception {
		// move with overwrite
		
		String content1= "hello";
		String content2= "world";
		
		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);
		
		Change undoChange= perform(new MoveResourceChange(file1, destination));

		IResource movedResource= assertMove(file1, destination, content1);
		
		perform(undoChange);

		assertMove(movedResource, file1.getParent(), content1);

		assertTrue(content2.equals(fProject.getContent(file2)));
	}

	public void testMoveRefactoring1() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { file });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IResource movedResource= assertMove(file, destination, content);

		perform(undoChange);

		assertMove(movedResource, file.getParent(), content);
	}
	
	public void testMoveRefactoring2() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { testFolder });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IFolder movedResource= (IFolder) assertMove(testFolder, destination, null);
		assertTrue(movedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertMove(movedResource, testFolder.getParent(), null);
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}
	
	public void testMoveRefactoring3() throws Exception {
		// move with overwrite

		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { file1 });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IResource movedResource= assertMove(file1, destination, content1);

		perform(undoChange);

		assertMove(movedResource, file1.getParent(), content1);
		assertTrue(content2.equals(fProject.getContent(file2)));
	}
	

	private Change perform(Change change) throws CoreException {
		PerformChangeOperation op= new PerformChangeOperation(change);
		op.run(null);
		assertTrue(op.changeExecuted());
		return op.getUndoChange();
	}

	private Change perform(RefactoringDescriptor descriptor) throws CoreException {
		RefactoringStatus status= new RefactoringStatus();
		Refactoring refactoring= descriptor.createRefactoring(status);
		assertTrue(status.isOK());
		
		PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		op.run(null);
		RefactoringStatus validationStatus= op.getValidationStatus();
		assertTrue(!validationStatus.hasFatalError() && !validationStatus.hasError());
		return op.getUndoChange();
	}
	
	private IResource assertMove(IResource source, IContainer destination, String content) throws CoreException, IOException {
		IResource res= destination.findMember(source.getName());

		assertTrue(res != null);
		assertTrue(res.getType() == source.getType());
		
		if (res instanceof IFile) {
			assertTrue(content.equals(fProject.getContent((IFile) res)));
		}
		return res;
	}
	
	
}
