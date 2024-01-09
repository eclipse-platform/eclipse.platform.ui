/*******************************************************************************
 * Copyright (c) 2024 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveProcessor;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

public class MoveRefactoringWithRefUpdateTest {

	private SimpleTestProject fProject;

	/**
	 * This should create the exact same Change which is created at
	 * {@link TestMoveProcessor#postCreateChange(Change[], IProgressMonitor)}.
	 */
	private static class RefUpdateParticipant extends MoveParticipant {
		private IFile fFile;

		@Override
		protected boolean initialize(Object element) {
			fFile= (IFile) element;
			return true;
		}

		@Override
		public String getName() {
			return "participant";
		}

		@Override
		public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
			return new RefactoringStatus();
		}

		@Override
		public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			TextFileChange result= new TextFileChange("", fFile);
			MultiTextEdit root= new MultiTextEdit();
			root.addChild(new ReplaceEdit(6, 25, "dest.fileToMove.txt"));
			result.setEdit(root);
			return result;
		}

		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			//we don't need any change here.
			return null;
		}
	}

	private static class TestMoveProcessor extends MoveProcessor {

		private IFile fileToUpdate;

		private IFile fileToMove;

		private IFolder dest;

		public TestMoveProcessor(IFile file, IFile fileToMove, IFolder destFold) {
			fileToUpdate= file;
			this.fileToMove= fileToMove;
			this.dest= destFold;
		}

		@Override
		public Object[] getElements() {
			return new Object[] { fileToUpdate };
		}

		@Override
		public String getIdentifier() {
			return "org.eclipse.ltk.core.refactoring.tests.Processor";
		}

		@Override
		public String getProcessorName() {
			return "processor";
		}

		@Override
		public boolean isApplicable() throws CoreException {
			return true;
		}

		@Override
		public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}

		@Override
		public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}

		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			Change moveChange= new MoveResourceChange(fileToMove, dest);
			return moveChange;
		}

		@Override
		public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
			RefUpdateParticipant participant= new RefUpdateParticipant();
			participant.initialize(this, fileToUpdate, new MoveArguments(dest, false));
			return new RefactoringParticipant[] { participant };
		}

		@Override
		public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException, OperationCanceledException {
			Collection<TextChange> postChanges= new ArrayList<>();

			//change same as participant
			TextFileChange referenceChange= new TextFileChange("", fileToUpdate);
			MultiTextEdit root= new MultiTextEdit();
			root.addChild(new ReplaceEdit(6, 25, "dest.fileToMove.txt"));
			referenceChange.setEdit(root);

			postChanges.add(referenceChange);
			if (postChanges.isEmpty())
				return null;

			List<IFile> alreadyTouchedFiles= new ArrayList<>();
			getModifiedFiles(alreadyTouchedFiles, participantChanges);

			CompositeChange mergedChange= new CompositeChange("TestMoveProcessor Post Change");
			mergedChange.markAsSynthetic();
			for (TextChange textChange : postChanges) {
				TextFileChange change= (TextFileChange) textChange;
				if (!alreadyTouchedFiles.contains(change.getFile())) {
					mergedChange.add(change);
				}
			}
			return mergedChange;
		}

		private void getModifiedFiles(List<IFile> result, Change[] changes) {
			for (Change change : changes) {
				Object modifiedElement= change.getModifiedElement();
				if (modifiedElement instanceof IAdaptable) {
					IFile file= ((IAdaptable) modifiedElement).getAdapter(IFile.class);
					if (file != null)
						result.add(file);
				}
				if (change instanceof CompositeChange) {
					getModifiedFiles(result, ((CompositeChange) change).getChildren());
				}
			}
		}
	}

	@Before
	public void setUp() throws Exception {
		fProject= new SimpleTestProject();
	}

	@After
	public void tearDown() throws Exception {
		fProject.delete();
	}

	@Test
	public void testMoveRefactoringWithParticipants() throws Exception {
		IFolder srcFold= fProject.createFolder("testFolder");
		//we need to update testFolder --> dest once the fileToMove.txt is move to dest.
		IFile fileToUpdate= fProject.createFile(srcFold, "referencer.txt", "using testFolder.fileToMove.txt;\nusing someOther.txt");
		IFile fileToMove= fProject.createFile(srcFold, "fileToMove.txt", "");
		IFolder destFold= fProject.createFolder("dest");

		MoveRefactoring refactoring= new MoveRefactoring(new TestMoveProcessor(fileToUpdate, fileToMove, destFold));
		PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		ResourcesPlugin.getWorkspace().run(op, null);

		assertTrue("File is not moved", this.fProject.getProject().getFolder("dest").getFile("fileToMove.txt").exists());

		String actual= fProject.getContent(fileToUpdate);
		//reference has to be updated only once despite two changes are supplied.
		assertEquals("using dest.fileToMove.txt;\nusing someOther.txt", actual);
	}
}
