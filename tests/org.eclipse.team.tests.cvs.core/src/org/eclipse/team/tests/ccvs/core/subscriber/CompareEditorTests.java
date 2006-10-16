/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.subscriber;

import junit.framework.Test;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.SharedDocumentAdapter;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.internal.ui.synchronize.DialogSynchronizePageSite;
import org.eclipse.team.internal.ui.synchronize.actions.OpenInCompareAction;
import org.eclipse.team.tests.ccvs.ui.ModelParticipantSyncInfoSource;
import org.eclipse.ui.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Test the behavior of compare editors opened on diffs.
 */
public class CompareEditorTests extends CVSSyncSubscriberTest {

	public static Test suite() {
		return suite(CompareEditorTests.class);
	}
	
	public CompareEditorTests() {
		super();
	}
	
	public CompareEditorTests(String name) {
		super(name);
	}
	
	protected CVSSyncTreeSubscriber getSubscriber() throws TeamException {
		return (CVSSyncTreeSubscriber)getWorkspaceSubscriber();
	}
	
	private IEditorInput openEditor(Subscriber subscriber, Object element) {
		IEditorInput input = OpenInCompareAction.openCompareEditor(ModelParticipantSyncInfoSource.getParticipant(subscriber), element, false, new DialogSynchronizePageSite(Utils.getShell(null), false));
		assertNotNull(input);
		assertEditorOpen(input);
		return input;
	}
	
	private IEditorInput openEditor(Object element) throws TeamException {
		return openEditor(getSubscriber(), element);
	}
	
	private void assertEditorOpen(IEditorInput input) {
		while (Display.getCurrent().readAndDispatch()) {};
		IEditorPart part = findOpenEditor(input);
		assertNotNull("The editor is not open", part);
	}

	private void assertEditorClosed(IEditorInput input) {
		while (Display.getCurrent().readAndDispatch()) {};
		IEditorPart part = findOpenEditor(input);
		assertNull("The editor is not closed", part);
	}
	
	private void closeAllEditors() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}
	
	private IEditorPart findOpenEditor(IEditorInput input) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] editorRefs = page.getEditorReferences();						
		for (int i = 0; i < editorRefs.length; i++) {
			final IEditorPart part = editorRefs[i].getEditor(false /* don't restore editor */);
			if(part != null) {
				IEditorInput testInput = part.getEditorInput();
				if (testInput == input)
					return part;
			}
		}
		return null;
	}
	
	private void dirtyEditor(IFile file, IEditorInput input) {
		FileEditorInput fileEditorInput = new FileEditorInput(file);
		IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(fileEditorInput);
		IDocument document = provider.getDocument(fileEditorInput);
		document.set("dirty");
	}
	
	protected void tearDown() throws Exception {
		closeAllEditors();
		super.tearDown();
	}

	public void testCloseOnUpdate() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		
		IProject copy = checkoutCopy(project, "-copy");
		setContentsAndEnsureModified(copy.getFile("file1.txt"));
		commitProject(copy);
		
		refresh(getSubscriber(), project);
		IEditorInput input = openEditor(project.getFile("file1.txt"));
		updateProject(project, null, false);
		waitForCollectors();
		assertEditorClosed(input);
	}

	private void waitForCollectors() throws TeamException {
		((ModelParticipantSyncInfoSource)getSyncInfoSource()).waitForCollectionToFinish(getSubscriber());
	}
	
	public void testCloseOnCommit() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		
		refresh(getSubscriber(), project);
		IEditorInput input = openEditor(project.getFile("file1.txt"));
		commitProject(project);
		waitForCollectors();
		assertEditorClosed(input);
	}
	
	public void testStayOpenOnUpdateWhenDirty() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		
		IProject copy = checkoutCopy(project, "-copy");
		setContentsAndEnsureModified(copy.getFile("file1.txt"));
		commitProject(copy);
		
		refresh(getSubscriber(), project);
		IEditorInput input = openEditor(project.getFile("file1.txt"));
		dirtyEditor(project.getFile("file1.txt"), input);
		// TODO: Should get a prompt to save the editor before updating
		updateProject(project, null, false);
		waitForCollectors();
		assertEditorOpen(input);
	}
	
	public void testStayOpenOnCommitWhenDirty() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		
		refresh(getSubscriber(), project);
		IEditorInput input = openEditor(project.getFile("file1.txt"));
		dirtyEditor(project.getFile("file1.txt"), input);
		// TODO: Should get a prompt to save the editor before committing
		commitProject(project);
		waitForCollectors();
		assertEditorOpen(input);
	}
	
	public void testCloseOnParticipantDispose() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		CVSTag v1 = new CVSTag("v1", CVSTag.VERSION);
		tagProject(project, v1, true);
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		commitProject(project);
		
		Subscriber subscriber = getSyncInfoSource().createCompareSubscriber(project, v1);
		
		refresh(subscriber, project);
		IEditorInput input = openEditor(subscriber, project.getFile("file1.txt"));
		ModelParticipantSyncInfoSource.getParticipant(subscriber).dispose();
		assertEditorClosed(input);
	}
	
	public void testStayOpenOnParticipantDisposeWhenDirty() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		CVSTag v1 = new CVSTag("v1", CVSTag.VERSION);
		tagProject(project, v1, true);
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		commitProject(project);
		
		Subscriber subscriber = getSyncInfoSource().createCompareSubscriber(project, v1);
		
		refresh(subscriber, project);
		IEditorInput input = openEditor(subscriber, project.getFile("file1.txt"));
		dirtyEditor(project.getFile("file1.txt"), input);
		ModelParticipantSyncInfoSource.getParticipant(subscriber).dispose();
		assertEditorOpen(input);
	}
	
	public void testUpdateOnRemoteChange() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		
		// First open the editor
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		IEditorInput input = openEditor(project.getFile("file1.txt"));
		assertRevisionsEquals(input, project.getFile("file1.txt"));
		
		// Now change the remote and refresh the project
		IProject copy = checkoutCopy(project, "-copy");
		setContentsAndEnsureModified(copy.getFile("file1.txt"));
		commitProject(copy);
		refresh(getSubscriber(), project);
		
		// The input revision should now match the remote revision
		assertRevisionsEquals(input, copy.getFile("file1.txt"));
		
	}
	
	public void testNoUpdateOnRemoteChangeWhenDirty() throws CoreException {
		IProject project = createProject(new String[] { "file1.txt"});
		
		// First open the editor and dirty it
		setContentsAndEnsureModified(project.getFile("file1.txt"));
		IEditorInput input = openEditor(project.getFile("file1.txt"));
		assertRevisionsEquals(input, project.getFile("file1.txt"));
		dirtyEditor(project.getFile("file1.txt"), input);
		
		// Now change the remote and refresh the project
		IProject copy = checkoutCopy(project, "-copy");
		setContentsAndEnsureModified(copy.getFile("file1.txt"));
		commitProject(copy);
		refresh(getSubscriber(), project);
		
		// The input revision should still match the local project
		assertRevisionsEquals(input, project.getFile("file1.txt"));
		
	}

	private void assertRevisionsEquals(IEditorInput input, IFile file) throws CVSException {
		CompareEditorInput cei = (CompareEditorInput)input;
		DiffNode node = (DiffNode)cei.getCompareResult();
		String remoteRevision = ((FileRevisionTypedElement)node.getRight()).getContentIdentifier();
		String localRevision = CVSWorkspaceRoot.getCVSFileFor(file).getSyncInfo().getRevision();
		assertEquals(localRevision, remoteRevision);
	}
	
}
