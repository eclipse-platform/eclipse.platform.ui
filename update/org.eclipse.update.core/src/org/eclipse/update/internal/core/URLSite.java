package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISiteChangedListener;

public class URLSite extends AbstractSite {

	/**
	 * Constructor for HTTPSite
	 */
	public URLSite(URL siteReference) {
		super(siteReference);
	}

	/**
	 * @see AbstractSite#getInputStream(IFeature, String)
	 */
	public InputStream getInputStream(IFeature sourceFeature, String streamKey) {
		URL jarURL = null;
		InputStream result = null;
		try {
			jarURL = new URL(getURL().getPath()+streamKey+".jar");
			jarURL.openConnection();
			result = jarURL.openStream();
		} catch (MalformedURLException e){
			//FIXME:
			e.printStackTrace();
		} catch (IOException e){
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractSite#createExecutableFeature(IFeature)
	 */
	public AbstractFeature createExecutableFeature(IFeature sourceFeature) {
		return null;
	}

	/**
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public void install(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		return null;
	}

	/**
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return getPluginEntries().length;
	}

	/**
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public int getDownloadSize(IPluginEntry entry) {
		return 0;
	}

	/**
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public int getInstallSize(IPluginEntry entry) {
		return 0;
	}

	/**
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry pluginEntry,String contentKey,InputStream inStream) {
		//FIXME: should not be called should it ? Can I store in any URL Site ?
	}

}

