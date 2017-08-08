/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria, Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class NaturesAndProjectsContentAssistProcessor implements IContentAssistProcessor {

	public NaturesAndProjectsContentAssistProcessor() {
		// TODO Auto-generated constructor stub
	}

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        String text = viewer.getDocument().get();
        String natureTag= "<nature>";
        String projectReferenceTag="<project>";
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        int natureTagLength = natureTag.length();
		if (text.length() >= natureTagLength && offset >= natureTagLength && text.substring(offset - natureTagLength, offset).equals(natureTag)) {
            IProjectNatureDescriptor[] natureDescriptors= workspace.getNatureDescriptors();
            ICompletionProposal[] proposals = new ICompletionProposal[natureDescriptors.length];
            for (int i= 0; i < natureDescriptors.length; i++) {
                IProjectNatureDescriptor descriptor= natureDescriptors[i];
                proposals[i] = new CompletionProposal(descriptor.getNatureId(), offset, 0, descriptor.getNatureId().length());
            }
            return proposals;
        }
        int projectReferenceTagLength = projectReferenceTag.length();
		if (text.length() >= projectReferenceTagLength && offset >= projectReferenceTagLength && text.substring(offset - projectReferenceTagLength, offset).equals(projectReferenceTag)) {
            IProject[] projects= workspace.getRoot().getProjects();
            //TODO - filter out the project this file is in
            ICompletionProposal[] proposals = new ICompletionProposal[projects.length];
            for (int i= 0; i < projects.length; i++) {
                proposals[i]=new CompletionProposal(projects[i].getName(), offset, 0, projects[i].getName().length());
            }
            return proposals;
        }
        return new ICompletionProposal[0];
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

}