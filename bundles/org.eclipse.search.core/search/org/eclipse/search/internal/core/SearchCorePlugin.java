package org.eclipse.search.internal.core;

import java.util.Collections;
import java.util.Map;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;

import org.eclipse.search.internal.core.text.IDirtyFileSearchParticipant;
import org.eclipse.search.internal.core.text.TextSearchEngineRegistry;

public class SearchCorePlugin extends Plugin {
	/**
	 * Search Plug-in Id (value <code>"org.eclipse.search"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.search.core"; //$NON-NLS-1$
	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR = 1;

	private static SearchCorePlugin fgSearchPlugin;

	private TextSearchEngineRegistry fTextSearchEngineRegistry;
	private IDirtyFileSearchParticipant fDirtyFileSearchParticipant;

	/**
	 * @return Returns the search plugin instance.
	 */
	public static SearchCorePlugin getDefault() {
		return fgSearchPlugin;
	}

	public SearchCorePlugin() {
		super();
		Assert.isTrue(fgSearchPlugin == null);
		fgSearchPlugin = this;
		fTextSearchEngineRegistry = null;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		// do nothing
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// do nothing
	}

	public TextSearchEngineRegistry getTextSearchEngineRegistry() {
		if (fTextSearchEngineRegistry == null) {
			fTextSearchEngineRegistry = new TextSearchEngineRegistry();
		}
		return fTextSearchEngineRegistry;
	}

	public void setDirtyFileDiscovery(IDirtyFileSearchParticipant participant) {
		this.fDirtyFileSearchParticipant = participant;
	}
	public IDirtyFileSearchParticipant getDirtyFileDiscovery() {
		if (fDirtyFileSearchParticipant == null) {
			return new IDirtyFileSearchParticipant() {
				@Override
				public Map<IFile, IDocument> findDirtyFiles() {
					// TODO Auto-generated method stub
					return Collections.EMPTY_MAP;
				}
			};
		}
		return fDirtyFileSearchParticipant;
	}

	/**
	 * Log status to platform log
	 * 
	 * @param status
	 *            the status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, SearchCoreMessages.SearchPlugin_internal_error,
				e));
	}

	public static String getID() {
		return PLUGIN_ID;
	}
}
