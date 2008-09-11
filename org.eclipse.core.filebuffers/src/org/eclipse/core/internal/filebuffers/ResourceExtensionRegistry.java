/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IAnnotationModelFactory;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.LocationKind;


/**
 * This is a special {@link ExtensionsRegistry} that is
 * optimized for <code>IFile<code>s..
 *
 * @since 3.3
 */
public class ResourceExtensionRegistry extends ExtensionsRegistry {

	/**
	 * Returns the set of content types for the given location.
	 *
	 * @param location the location for which to look up the content types
	 * @param locationKind the kind of the given location
	 * @return the set of content types for the location
	 */
	protected IContentType[] findContentTypes(IPath location, LocationKind locationKind) {
		if (locationKind != LocationKind.LOCATION) {
			IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
			if (file != null)
				return findContentTypes(file);
		}
		return fContentTypeManager.findContentTypesFor(location.lastSegment());
	}

	/**
	 * Returns the sharable document factory for the given file.
	 *
	 * @param file the file for which to looked up the factory
	 * @return the sharable document factory
	 * @deprecated As of 3.5
	 */
	org.eclipse.core.filebuffers.IDocumentFactory getDocumentFactory(IFile file) {
		org.eclipse.core.filebuffers.IDocumentFactory factory= getDocumentFactory(findContentTypes(file));
		if (factory == null) {
			factory= getDocumentFactory(file.getFullPath().lastSegment());
		}
		if (factory == null)
			factory= getDocumentFactory(file.getFileExtension());
		if (factory == null)
			factory= getDocumentFactory(WILDCARD);
		return factory;
	}

	/**
	 * Returns the sharable annotation model factory for the given file.
	 *
	 * @param file the file for which to look up the factory
	 * @return the sharable annotation model factory
	 */
	IAnnotationModelFactory getAnnotationModelFactory(IFile file) {
		IAnnotationModelFactory factory= getAnnotationModelFactory(findContentTypes(file));
		if (factory == null)
			factory= getAnnotationModelFactory(file.getFullPath().lastSegment());
		if (factory == null)
			factory= getAnnotationModelFactory(file.getFileExtension());
		if (factory == null)
			factory= getAnnotationModelFactory(WILDCARD);
		return factory;
	}

	/**
	 * Returns the set of content types for the given location.
	 *
	 * @param file the file for which to look up the content types
	 * @return the set of content types for the location
	 */
	private IContentType[] findContentTypes(IFile file) {
		try {
			IContentDescription contentDescription= file.getContentDescription();
			if (contentDescription != null) {
				IContentType contentType= contentDescription.getContentType();
				if (contentType != null)
					return new IContentType[] {contentType};
			}
		} catch (CoreException x) {
			// go for the default
		}
		return fContentTypeManager.findContentTypesFor(file.getFullPath().lastSegment());
	}

	/**
	 * Returns the sharable set of document setup participants for the given file.
	 *
	 * @param file the file for which to look up the setup participants
	 * @return the sharable set of document setup participants
	 */
	IDocumentSetupParticipant[] getDocumentSetupParticipants(IFile file) {
		Set participants= new HashSet();

		List p= getDocumentSetupParticipants(findContentTypes(file));
		if (p != null)
			participants.addAll(p);

		p= getDocumentSetupParticipants(file.getFullPath().lastSegment());
		if (p != null)
			participants.addAll(p);

		p= getDocumentSetupParticipants(file.getFileExtension());
		if (p != null)
			participants.addAll(p);

		p= getDocumentSetupParticipants(WILDCARD);
		if (p != null)
			participants.addAll(p);

		IDocumentSetupParticipant[] result= new IDocumentSetupParticipant[participants.size()];
		participants.toArray(result);
		return result;
	}

}
