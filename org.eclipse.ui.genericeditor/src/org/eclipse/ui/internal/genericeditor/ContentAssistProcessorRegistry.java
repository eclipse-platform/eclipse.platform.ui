/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.PlatformUI;

/**
 * A registry of content assist processors provided by extension <code>org.eclipse.ui.genericeditor.contentAssistProcessors</code>.
 * Those extensions are specific to a given {@link IContentType}.
 * 
 * @since 1.0
 */
public class ContentAssistProcessorRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".contentAssistProcessors"; //$NON-NLS-1$

	static class ContentAssistProcessorDelegate implements IContentAssistProcessor {
		private final IContentAssistProcessor delegate;
		private IContentType targetContentType;

		public ContentAssistProcessorDelegate(IContentAssistProcessor delegate, IContentType targetContentType) {
			this.delegate = delegate;
			this.targetContentType = targetContentType;
		}

		/**
		 * @return whether the referenced contribution should contribute to the current editor.
		 */
		public boolean isActive(ITextViewer viewer) {
			String fileName = null;
			if (viewer != null && viewer.getDocument() != null) {
				IPath location = FileBuffers.getTextFileBufferManager().getTextFileBuffer(viewer.getDocument()).getLocation();
				fileName =  location.segment(location.segmentCount() - 1);
			}
			if (fileName == null) {
				fileName = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput().getName();
			}
			if (fileName != null) {
				IContentTypeManager contentTypeManager= Platform.getContentTypeManager();
				for (IContentType currentContentType : contentTypeManager.findContentTypesFor(fileName)) {
					if (currentContentType.isKindOf(targetContentType)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
			if (isActive(viewer)) {
				return delegate.computeCompletionProposals(viewer, offset);
			}
			return new ICompletionProposal[0];
		}

		@Override
		public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
			if (isActive(viewer)) {
				return delegate.computeContextInformation(viewer, offset);
			}
			return new IContextInformation[0];
		}

		@Override
		public char[] getCompletionProposalAutoActivationCharacters() {
			if (isActive(null)) {
				return delegate.getCompletionProposalAutoActivationCharacters();
			}
			return null;
		}

		@Override
		public char[] getContextInformationAutoActivationCharacters() {
			if (isActive(null)) {
				return delegate.getContextInformationAutoActivationCharacters();
			}
			return null;
		}

		@Override
		public String getErrorMessage() {
			if (isActive(null)) {
				return delegate.getErrorMessage();
			}
			return null;
		}

		@Override
		public IContextInformationValidator getContextInformationValidator() {
			if (isActive(null)) {
				return delegate.getContextInformationValidator();
			}
			return null;
		}
	}

	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<IContentAssistProcessor>> extensions = new HashMap<>();
	private boolean outOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public ContentAssistProcessorRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {
			@Override
			public void registryChanged(IRegistryChangeEvent event) {
				outOfSync = true;
			}
		}, EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link IContentAssistProcessor}s that are relevant to hook on source viewer according
	 * to document content types. 
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of {@link IContentAssistProcessor} contributed for at least one of the content types.
	 */
	public List<IContentAssistProcessor> getContentAssistProcessors(ISourceViewer sourceViewer, Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		return this.extensions.values().stream()
			.filter(ext -> contentTypes.contains(ext.targetContentType))
			.sorted(new ContentTypeSpecializationComparator<IContentAssistProcessor>())
			.map(GenericContentTypeRelatedExtension<IContentAssistProcessor>::createDelegate)
			.collect(Collectors.toList());
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension, new GenericContentTypeRelatedExtension<IContentAssistProcessor>(extension));
				} catch (Exception ex) {
					GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			this.extensions.remove(toRemove);
		}
		this.outOfSync = false;
	}
}
