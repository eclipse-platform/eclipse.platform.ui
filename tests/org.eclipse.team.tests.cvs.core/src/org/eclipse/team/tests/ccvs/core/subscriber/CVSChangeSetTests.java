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
package org.eclipse.team.tests.ccvs.core.subscriber;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.subscriber.ChangeLogDiffNode;
import org.eclipse.team.internal.ccvs.ui.subscriber.ChangeLogModelManager;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.tests.ccvs.ui.SynchronizeViewTestAdapter;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.ui.PartInitException;

/**
 * Tests the change set mode of the synchronize view
 */
public class CVSChangeSetTests extends CVSSyncSubscriberTest {

	public static Test suite() {
	    return suite(CVSChangeSetTests.class);
	}
	
	public CVSChangeSetTests() {
		super();
	}
	
	public CVSChangeSetTests(String name) {
		super(name);
	}
	
    private void assertIncomingChangesInSets(IFile[][] files, String[] messages) throws CoreException {
        // Get the workspace subscriber which also creates a participant and page in the sync view
        Subscriber workspaceSubscriber = getWorkspaceSubscriber();
        enableCommitSets(workspaceSubscriber);
        refresh(workspaceSubscriber);
        ISynchronizeModelElement root = getModelRoot(workspaceSubscriber);
        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            ChangeLogDiffNode node = getCommitSetFor(root, message);
            assertNotNull("The commit set for '" + message + "' is not in the sync view", node);
            List filesInSet = new ArrayList();
            getFileChildren(node, filesInSet);
            assertTrue("The number of files in the set do not match the expected number", files[i].length == filesInSet.size());
            for (int j = 0; j < files[i].length; j++) {
                IFile file = files[i][j];
                assertTrue("File " + file.getFullPath() + " is not in the set", filesInSet.contains(file));
            }
        }
    }
    
    /**
     * Adds IFiles to the list
     */
    private void getFileChildren(ISynchronizeModelElement node, List list) {
        IResource resource = node.getResource();
        if (resource != null && resource.getType() == IResource.FILE) {
            list.add(resource);
        }
        IDiffElement[] children = node.getChildren();
        for (int i = 0; i < children.length; i++) {
            IDiffElement child = children[i];
            getFileChildren((ISynchronizeModelElement)child, list);
        }
        return;
    }

    private ChangeLogDiffNode getCommitSetFor(ISynchronizeModelElement root, String message) {
        IDiffElement[] children = root.getChildren();
        for (int i = 0; i < children.length; i++) {
            IDiffElement element = children[i];
            if (element instanceof ChangeLogDiffNode) {
                ChangeLogDiffNode node = (ChangeLogDiffNode)element;
                if (node.getComment().getComment().equals(message)) {
                    return node;
                }
            }
        }
        return null;
    }

    private void refresh(Subscriber workspaceSubscriber) throws TeamException {
        workspaceSubscriber.refresh(workspaceSubscriber.roots(), IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
    }

    private void enableCommitSets(Subscriber workspaceSubscriber) throws PartInitException {
        ISynchronizeParticipant participant = SynchronizeViewTestAdapter.getParticipant(workspaceSubscriber);
        SubscriberParticipantPage page = (SubscriberParticipantPage)SynchronizeViewTestAdapter.getSyncViewPage(participant);
        ChangeLogModelManager manager = (ChangeLogModelManager)page.getConfiguration().getProperty(SynchronizePageConfiguration.P_MODEL_MANAGER);
        manager.setCommitSetsEnabled(true);
    }

    /*
     * Wait until all the background handlers have settled and then return the root element in the sync view
     */
    private ISynchronizeModelElement getModelRoot(Subscriber workspaceSubscriber) throws CoreException {
        SynchronizeViewTestAdapter.getCollector(workspaceSubscriber);
        ISynchronizeParticipant participant = SynchronizeViewTestAdapter.getParticipant(workspaceSubscriber);
        SubscriberParticipantPage page = (SubscriberParticipantPage)SynchronizeViewTestAdapter.getSyncViewPage(participant);
        ChangeLogModelManager manager = (ChangeLogModelManager)page.getConfiguration().getProperty(SynchronizePageConfiguration.P_MODEL_MANAGER);
        AbstractSynchronizeModelProvider provider = (AbstractSynchronizeModelProvider)manager.getActiveModelProvider();
        provider.waitForUpdateHandler(new IProgressMonitor() {
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
        return provider.getModelRoot();
    }

    public void testSimpleCommit() throws CoreException {
	    IProject project = createProject(new String[] { "file1.txt", "file2.txt", "folder1/", "folder1/a.txt", "folder1/b.txt"});
	    
	    // Modify a file in a copy
	    IProject copy = checkoutCopy(project, CVSTag.DEFAULT);
	    setContentsAndEnsureModified(copy.getFile("file1.txt"));
	    String message1 = "Commit 1";
	    commitResources(new IResource[] {copy}, IResource.DEPTH_INFINITE, message1);
	    assertIncomingChangesInSets(new IFile[][] {{ project.getFile("file1.txt") }}, new String[] {message1});
	    
	    // Modify the copy some more
	    setContentsAndEnsureModified(copy.getFile("file2.txt"));
	    setContentsAndEnsureModified(copy.getFile("folder1/a.txt"));
	    String message2 = "Commit 2";
	    commitResources(new IResource[] {copy}, IResource.DEPTH_INFINITE, message2);
	    assertIncomingChangesInSets(new IFile[][] {
	            { project.getFile("file1.txt") },
	            { project.getFile("file2.txt"), project.getFile("folder1/a.txt") }
	            }, new String[] {message1, message2});
	    
	    // Modify the copy some more
	    setContentsAndEnsureModified(copy.getFile("file2.txt"));
	    String message3 = "Commit 3";
	    commitResources(new IResource[] {copy}, IResource.DEPTH_INFINITE, message3);
	    assertIncomingChangesInSets(new IFile[][] {
	            { project.getFile("file1.txt") },
	            { project.getFile("folder1/a.txt") },
	            { project.getFile("file2.txt")}
	            }, new String[] {message1, message2, message3});
	}
	
}
