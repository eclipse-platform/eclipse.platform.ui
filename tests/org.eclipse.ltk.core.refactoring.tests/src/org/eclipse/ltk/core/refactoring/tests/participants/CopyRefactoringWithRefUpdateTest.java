/*******************************************************************************
 * Copyright (c) 2024, 2026 Advantest Europe GmbH and others.
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
 *     Felix Schmid - adapted for copy refactoring test
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.CopyParticipant;
import org.eclipse.ltk.core.refactoring.participants.CopyProcessor;
import org.eclipse.ltk.core.refactoring.participants.CopyRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.CopyResourceChange;
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

class CopyRefactoringWithRefUpdateTest {

	private SimpleTestProject project;

	private static class RefUpdateParticipant extends CopyParticipant {
		private IFile file;
		private IContainer destination;

		@Override
		protected boolean initialize(Object element) {
			file= (IFile) element;
			destination= (IContainer) getArguments().getDestination();
			return true;
		}

		@Override
		public String getName() {
			return "copy participant";
		}

		@Override
		public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
			return new RefactoringStatus();
		}

		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			IPath path = destination.getFullPath().append(file.getName());
			return new CopyFileChange(path);
		}
	}

	private static class CopyFileChange extends Change {

		private final IPath newFile;

		protected CopyFileChange(final IPath newFile) {
			this.newFile = newFile;
		}

		@Override
		public void initializeValidationData(final IProgressMonitor pm) {
			// nothing to do
		}

		@Override
		public RefactoringStatus isValid(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}

		@Override
		public String getName() {
			return "copy file change";
		}

		@Override
		public Change perform(final IProgressMonitor pm) throws CoreException {
			IFile file = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(newFile);

			TextFileChange result= new TextFileChange("", file);
			MultiTextEdit root= new MultiTextEdit();
			root.addChild(new ReplaceEdit(9, 12, "destFolder"));
			result.setEdit(root);
			result.perform(pm);

			return null; // no undo change necessary, the element will be deleted
		}

		@Override
		public IResource getModifiedElement() {
			return null; // not needed for test
		}

		@Override
		public Object[] getAffectedObjects() {
			return null; // not needed for test
		}
	}

	private static class TestCopyProcessor extends CopyProcessor {

		private IFile origin;
		private IFolder destination;
		private ReorgExecutionLog log;

		public TestCopyProcessor(IFile origin, IFolder destination) {
			this.origin = origin;
			this.destination = destination;
			log = new ReorgExecutionLog();
		}

		@Override
		public Object[] getElements() {
			return new Object[] { origin };
		}

		@Override
		public String getIdentifier() {
			return "org.eclipse.ltk.core.refactoring.tests.CopyProcessor";
		}

		@Override
		public String getProcessorName() {
			return "copy processor";
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
			return new CopyResourceChange(origin, log, destination);
		}

		@Override
		public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
			RefUpdateParticipant participant= new RefUpdateParticipant();
			participant.initialize(this, origin, new CopyArguments(destination, log));
			return new RefactoringParticipant[] { participant };
		}
	}

	@BeforeEach
	public void setUp() throws Exception {
		project= new SimpleTestProject();
	}

	@AfterEach
	public void tearDown() throws Exception {
		project.delete();
	}

	@Test
	public void testCopyRefactoringWithParticipants() throws Exception {
		IFolder srcFold= project.createFolder("originFolder");
		IFolder destination= project.createFolder("destFolder");
		// the copy should specify the package as "destFolder", while the origin says "originFolder"
		IFile origin= project.createFile(srcFold, "testFile.txt", "package: originFolder");

		CopyRefactoring refactoring= new CopyRefactoring(new TestCopyProcessor(origin, destination));
		PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		ResourcesPlugin.getWorkspace().run(op, null);

		IFile copy = this.project.getProject().getFolder("destFolder").getFile("testFile.txt");
		assertTrue(copy.exists(), "File is not copied");

		// original file was not changed
		String originContent= project.getContent(origin);
		assertEquals("package: originFolder", originContent);

		// package of copy was changed
		String copyContent = project.getContent(copy);
		assertEquals("package: destFolder", copyContent);
	}
}
