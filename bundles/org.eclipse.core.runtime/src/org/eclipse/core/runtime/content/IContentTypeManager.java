/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.content;

import java.util.EventObject;

/**
 * The content type manager provides facilities for file name and content-based
 * type lookup and content description.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.core.runtime.content.IContentTypeMatcher
 * @see org.eclipse.core.runtime.Platform#getContentTypeManager()
 * @since 3.0
 */
public interface IContentTypeManager extends IContentTypeMatcher {

	/**
	 * An event object which describes the details of a change to a 
	 * content type. 
	 * <p>
	 * Types of changes include a change in the file associations or 
	 * a change in the encoding setting.
	 * </p>
	 */
	public final class ContentTypeChangeEvent extends EventObject {
		/**
		 * All serializable objects should have a stable serialVersionUID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor for a new content type change event.
		 * 
		 * @param source the content type that changed
		 */
		public ContentTypeChangeEvent(IContentType source) {
			super(source);
		}

		/**
		 * Return the content type object associated with this change event.
		 * 
		 * @return the content type
		 */
		public IContentType getContentType() {
			return (IContentType) source;
		}
	}

	/**
	 * A listener to be used to receive content type change events.
	 * <p>
	 * Clients who reference the <code>org.eclipse.core.resources</code>
	 * bundle are encouraged <em>not</em> to use this listener mechanism to
	 * listen to content type changes. The Core Resources bundle will 
	 * propagate changes to content types and notify clients appropriately
	 * via the resource change mechanism.
	 * </p>
	 * <p>
	 * Clients may implement this interface.
	 * </p>
	 */
	public interface IContentTypeChangeListener {

		/**
		 * Notification that a content type has changed in the content type manager.
		 * The given event object contains the content type which changed and must not
		 * be <code>null</code>.
		 * 
		 * @param event the content type change event
		 */
		public void contentTypeChanged(ContentTypeChangeEvent event);
	}

	/**
	 * A client-provided registry containing objects that are related to content types. Implementations
	 * of this interface collaborate with the content type infrastructure in order to support lookup 
	 * of content type related objects. 
	 * <p>
	 * Clients may implement this interface.
	 * </p>
	 * 
	 * @see IContentTypeManager#findRelatedObjects(IContentType, String, IRelatedRegistry)
	 * @since 3.1
	 */
	public interface IRelatedRegistry {
		/**
		 * Returns all objects in this client-provided registry that are directly related to the given
		 * content type.
		 * 
		 * @param type a content type
		 * @return an array containing all objects directly related to the given content type
		 */
		Object[] getRelatedObjects(IContentType type);

		/**
		 * Returns all objects in this client-provided registry that are directly related to the given
		 * file name.
		 * <p>
		 * This method is optional. It only has to be implemented if this registry supports 
		 * file name based association in addition to content type based association. 
		 * Otherwise, it can throw <code>UnsupportedOperationException</code>.
		 * </p>
		 * 
		 * @param fileName a file name
		 * @return an array containing all objects directly related to the given file name
		 * @throws UnsupportedOperationException if this registry does not support file
		 * name based association
		 */
		Object[] getRelatedObjects(String fileName);
	}

	/**
	 * A policy for refining the set of content types that
	 * should be accepted during content type matching operations.
	 * <p>
	 * Clients may implement this interface.
	 * </p>
	 * 
	 * @see IContentTypeManager#getMatcher(ISelectionPolicy)
	 * @since 3.1
	 */
	public interface ISelectionPolicy {
		/**
		 * Returns a subset of the given content types sorted by using a custom criterion.
		 * <p>
		 * The given array of content types has already been sorted using 
		 * the platform rules. If this object follows the same rules, further sorting 
		 * is not necessary.  
		 * </p>
		 * <p>
		 * The type of matching being performed (name, contents or name + contents)
		 * might affect the outcome for this method. For instance, for file name-only 
		 * matching, the more general type could have higher priority. For content-based 
		 * matching,  the more specific content type could be preferred instead.
		 * </p>
		 * 
		 * @param candidates an array containing content types matching some query 
		 * @param fileName whether it is a file name-based content type matching
		 * @param content whether its a content-based content type matching
		 * @return an array of content types
		 */
		IContentType[] select(IContentType[] candidates, boolean fileName, boolean content);
	}

	/**
	 * Content type identifier constant for platform's primary 
	 * text-based content type: <code>org.eclipse.core.runtime.text</code>. 
	 * <p>
	 * All text-based content types ought to be sub types of the content type 
	 * identified by this string. This provides a simple way for detecting 
	 * whether a content type is text-based:
	 * <pre>
	 * IContentType text = Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);
	 * IContentType someType = ...;
	 * boolean isTextBased = someType.isKindOf(text);
	 * </pre> 
	 * </p>
	 */
	public final static String CT_TEXT = "org.eclipse.core.runtime.text"; //$NON-NLS-1$	

	/**
	 * Register the given listener for notification of content type changes.
	 * Calling this method multiple times with the same listener has no effect. The
	 * given listener argument must not be <code>null</code>.
	 * 
	 * @param listener the content type change listener to register
	 * @see #removeContentTypeChangeListener(IContentTypeManager.IContentTypeChangeListener)
	 * @see IContentTypeManager.IContentTypeChangeListener
	 */
	public void addContentTypeChangeListener(IContentTypeChangeListener listener);

	/**
	 * Returns all objects in the given content type-related registry  that are 
	 * related to the content type and file name specified. This method will walk 
	 * the content type hierarchy tree up to a root content type, collecting all related
	 * objects from the given registry. 
	 * <p>
	 * The file name is optional, and <em>has</em> to be omitted if the given registry 
	 * does not support file name based associations.  
	 * </p>
	 * 
	 * @param type a content type
	 * @param fileName the name of the file, or <code>null</code> 
	 * @param registry a related registry
	 * @return all objects in the related registry that are associated to the given
	 * content type
	 * @since 3.1
	 */
	public Object[] findRelatedObjects(IContentType type, String fileName, IRelatedRegistry registry);

	/**
	 * Returns all content types known by the platform. 
	 * <p>
	 * Returns an empty array if there are no content types available.
	 * </p>
	 * 
	 * @return all content types known by the platform.
	 */
	public IContentType[] getAllContentTypes();

	/**
	 * Returns the content type with the given identifier, or <code>null</code>
	 * if no such content type is known by the platform.
	 * 
	 * @param contentTypeIdentifier the identifier for the content type
	 * @return the content type, or <code>null</code>
	 */
	public IContentType getContentType(String contentTypeIdentifier);

	/**
	 * Returns a newly created content type matcher using the given content type selection policy.
	 * 
	 * @param customPolicy a selection policy
	 * @return a content type matcher that uses the given policy
	 */
	public IContentTypeMatcher getMatcher(ISelectionPolicy customPolicy);

	/**
	 * De-register the given listener from receiving notification of content type changes. 
	 * Calling this method multiple times with the same listener has no
	 * effect. The given listener argument must not be <code>null</code>.
	 * 
	 * @param listener the content type change listener to remove
	 * @see #addContentTypeChangeListener(IContentTypeManager.IContentTypeChangeListener)
	 * @see IContentTypeManager.IContentTypeChangeListener
	 */
	public void removeContentTypeChangeListener(IContentTypeChangeListener listener);
}
