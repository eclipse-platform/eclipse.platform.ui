/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.io.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

public class ContentTypeManager implements IContentTypeManager, IRegistryChangeListener {
	private static ContentTypeManager instance;

	public static final int BLOCK_SIZE = 0x400;
	public static final String CONTENT_TYPE_PREF_NODE = Platform.PI_RUNTIME + IPath.SEPARATOR + "content-types"; //$NON-NLS-1$
	private static final String OPTION_DEBUG_CONTENT_TYPES = Platform.PI_RUNTIME + "/contenttypes/debug"; //$NON-NLS-1$;
	static final boolean DEBUGGING = Boolean.TRUE.toString().equalsIgnoreCase(InternalPlatform.getDefault().getOption(OPTION_DEBUG_CONTENT_TYPES));
	private ContentTypeCatalog catalog;

	/** 
	 * List of registered listeners (element type: 
	 * <code>IContentTypeChangeListener</code>).
	 * These listeners are to be informed when 
	 * something in a content type changes.
	 */
	protected ListenerList contentTypeListeners = new ListenerList();

	/**
	 * Creates and initializes the platform's content type manager. A reference to the
	 * content type manager can later be obtained by calling <code>getInstance()</code>.
	 * <p>
	 * Since calling this method will cause the content type manager to register a registry change listener, 
	 * this method must be called while the extension registry is available.
	 * </p>
	 */
	public static void startup() {
		instance = new ContentTypeManager();
		Platform.getExtensionRegistry().addRegistryChangeListener(instance, Platform.PI_RUNTIME);
	}

	/**
	 * Shuts down the platform's content type manager. After this call returns,
	 * the content type manager will be closed for business.
	 * <p>
	 * Since calling this method will cause the content type manager to unregister a registry change listener, 
	 * this method must be called while the extension registry is available.
	 * </p>
	 */
	public static void shutdown() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(instance);
		instance = null;
	}

	/**
	 * Obtains this platform's content type manager. 
	 * <p>
	 * It has to have been created first by a call to <code>create()</code>,
	 * otherwise an <code>AssertionFailedException</code> will be thrown.
	 * </p> 
	 * 
	 * @return the content type manager
	 */
	public static ContentTypeManager getInstance() {
		Assert.isNotNull(instance); //$NON-NLS-1$
		return instance;
	}

	/*
	 * Returns the extension for a file name (omiting the leading '.').
	 */
	static String getFileExtension(String fileName) {
		int dotPosition = fileName.lastIndexOf('.');
		return (dotPosition == -1 || dotPosition == fileName.length() - 1) ? null : fileName.substring(dotPosition + 1);
	}

	protected static LazyInputStream readBuffer(InputStream contents) {
		return new LazyInputStream(contents, BLOCK_SIZE);
	}

	protected static LazyReader readBuffer(Reader contents) {
		return new LazyReader(contents, BLOCK_SIZE);
	}

	public ContentTypeManager() {
		// nothing to do
	}

	private ContentTypeCatalog buildCatalog() {
		ContentTypeCatalog newCatalog = new ContentTypeCatalog(this);
		createBuilder(newCatalog).buildCatalog();
		return newCatalog;
	}

	protected ContentTypeBuilder createBuilder(ContentTypeCatalog newCatalog) {
		return new ContentTypeBuilder(newCatalog);
	}

	public IContentType findContentTypeFor(InputStream contents, String fileName) throws IOException {
		IContentType[] all = findContentTypesFor(contents, fileName);
		return all.length > 0 ? all[0] : null;
	}

	public IContentType findContentTypeFor(String fileName) {
		// basic implementation just gets all content types
		IContentType[] associated = findContentTypesFor(fileName);
		return associated.length == 0 ? null : associated[0];
	}

	public IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException {
		return getCatalog().findContentTypesFor(contents, fileName);
	}

	public IContentType[] findContentTypesFor(String fileName) {
		return getCatalog().findContentTypesFor(fileName);
	}

	public IContentType[] getAllContentTypes() {
		return getCatalog().getAllContentTypes();
	}

	protected synchronized ContentTypeCatalog getCatalog() {
		if (catalog == null)
			catalog = buildCatalog();
		return catalog;
	}

	public IContentType getContentType(String contentTypeIdentifier) {
		return getCatalog().getContentType(contentTypeIdentifier);
	}

	public IContentDescription getDescriptionFor(InputStream contents, String fileName, QualifiedName[] options) throws IOException {
		return getCatalog().getDescriptionFor(contents, fileName, options);
	}

	public IContentDescription getDescriptionFor(Reader contents, String fileName, QualifiedName[] options) throws IOException {
		return getCatalog().getDescriptionFor(contents, fileName, options);
	}

	Preferences getPreferences() {
		return new InstanceScope().getNode(CONTENT_TYPE_PREF_NODE);
	}

	public synchronized void registryChanged(IRegistryChangeEvent event) {
		// no changes related to the content type registry
		if (event.getExtensionDeltas(Platform.PI_RUNTIME, ContentTypeBuilder.PT_CONTENTTYPES).length == 0)
			return;
		if (catalog == null)
			// nothing to discard			
			return;
		catalog = null;
		if (ContentTypeManager.DEBUGGING)
			Policy.debug("Event caused content type registry to be discarded: " + event); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see IContentTypeManager#addContentTypeChangeListener(IContentTypeChangeListener)
	 */
	public void addContentTypeChangeListener(IContentTypeChangeListener listener) {
		contentTypeListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see IContentTypeManager#removeContentTypeChangeListener(IContentTypeChangeListener)
	 */
	public void removeContentTypeChangeListener(IContentTypeChangeListener listener) {
		contentTypeListeners.remove(listener);
	}

	public void fireContentTypeChangeEvent(ContentType type) {
		Object[] listeners = this.contentTypeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final ContentTypeChangeEvent event = new ContentTypeChangeEvent(type);
			final IContentTypeChangeListener listener = (IContentTypeChangeListener) listeners[i];
			ISafeRunnable job = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// already logged in Platform#run()
				}

				public void run() throws Exception {
					listener.contentTypeChanged(event);
				}
			};
			Platform.run(job);
		}
	}
}