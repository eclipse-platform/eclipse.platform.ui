/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * The content type manager provides facilities for file name and content-based
 * type lookup and content description.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.core.runtime.content.IContentTypeMatcher
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
		/*
		 * The context for the setting that changed.
		 * 
		 * @since 3.1
		 */
		private IScopeContext context;

		/**
		 * Constructor for a new content type change event.
		 * 
		 * @param source the content type that changed
		 */
		public ContentTypeChangeEvent(IContentType source) {
			super(source);
		}

		/**
		 * Constructor for a new content type change event.
		 * 
		 * @param source the content type that changed
		 * @param context the context where a setting changed, or <code>null</code>
		 * @since 3.1
		 */
		public ContentTypeChangeEvent(IContentType source, IScopeContext context) {
			super(source);
			this.context = context;
		}

		/**
		 * Return the content type object associated with this change event.
		 * 
		 * @return the content type
		 */
		public IContentType getContentType() {
			return (IContentType) source;
		}

		/**
		 * Return the preference scope where the setting changed, or 
		 * <code>null</code>, if the change happened in the content type manager 
		 * default context. 
		 * 
		 * @return the context where the change happened, or <code>null</code>
		 * @since 3.1
		 */
		public IScopeContext getContext() {
			return context;
		}
	}

	/**
	 * A listener to be used to receive content type change events.
	 * <p>
	 * Clients who reference the <code>org.eclipse.core.resources</code>
	 * bundle are encouraged <em>not</em> to use this listener mechanism to
	 * detect encoding changes. The Core Resources bundle will 
	 * detect changes to content types and notify clients appropriately
	 * of potential changes to the encoding of files in the workspace 
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
	 * A policy for refining the set of content types that
	 * should be accepted during content type matching operations.
	 * <p>
	 * Clients may implement this interface.
	 * </p>
	 * 
	 * @see IContentTypeManager#getMatcher(IContentTypeManager.ISelectionPolicy, IScopeContext)
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
	 * Returns a newly created content type matcher using the given content type selection policy
	 * and preference scope. If the preference scope is <code>null</code>, the default scope 
	 * is used.
	 * 
	 * @param customPolicy a selection policy
	 * @param context a user preference context to be used by the matcher, or <code>null</code>
	 * @return a content type matcher that uses the given policy
	 * @since 3.1 
	 */
	public IContentTypeMatcher getMatcher(ISelectionPolicy customPolicy, IScopeContext context);

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
