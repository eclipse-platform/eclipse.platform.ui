/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

public class SharedTextChangeTests {

	private SimpleTestProject fProject;

	private static class Participant extends RenameParticipant {
		private IFile fFile;
		@Override
		protected boolean initialize(Object element) {
			fFile= (IFile)element;
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
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			TextChange change= getTextChange(fFile);
			change.addEdit(new ReplaceEdit(20, 3, "four"));
			return null;
		}
	}

	private static class Processor extends RenameProcessor {

		private IFile fFile;

		public Processor(IFile file) {
			fFile= file;
		}
		@Override
		public Object[] getElements() {
			return new Object[] { fFile };
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
			TextFileChange result= new TextFileChange("", fFile);
			MultiTextEdit root= new MultiTextEdit();
			root.addChild(new ReplaceEdit(8, 3, "three"));
			result.setEdit(root);
			return result;
		}
		@Override
		public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
			Participant participant= new Participant();
			participant.initialize(this, fFile, new RenameArguments("", false));
			return new RefactoringParticipant[] { participant };
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
	public void testSharedUpdating() throws Exception {
		IFolder folder= fProject.createFolder("test");
		IFile file= fProject.createFile(folder, "test.txt", "section one section two");
		RenameRefactoring refactoring= new RenameRefactoring(new Processor(file));
		PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		ResourcesPlugin.getWorkspace().run(op, null);
		String actual= fProject.getContent(file);
		assertEquals("section three section four", actual);
	}
}
