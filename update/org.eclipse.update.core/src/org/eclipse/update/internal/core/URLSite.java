package org.eclipse.update.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISiteChangedListener;
import org.eclipse.update.core.VersionedIdentifier;

public class URLSite extends AbstractSite {

	/**
	 * plugin entries
	 */
	private List pluginEntries = new ArrayList(0);
	/**
	 * Constructor for HTTPSite
	 */
	public URLSite(URL siteReference) {
		super(siteReference);
	}

	/**
	 * @see AbstractSite#getInputStream(IFeature, String)
	 * In this default implementation we can deduct the URL of the 
	 * archive based on teh name of teh ID
	 * In other implementations, we may have to use the site.xml archive tag, that maps
	 * an id and a URL
	 */
	public InputStream getInputStream(IFeature sourceFeature, String archiveId) {
		URL contentURL = null;
		InputStream result = null;
		try {
			//FIXME: delete ?
			contentURL = getArchiveURLfor(archiveId);
			if (contentURL==null) contentURL = new URL(getURL(),"plugins/"+archiveId);
			result = contentURL.openStream();
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
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		// FIXME: I do not like this pattern... either go all teh way Array
		// or find better pattern
		IPluginEntry[] result = null;
		if (!pluginEntries.isEmpty()){
			result = (IPluginEntry[])pluginEntries.toArray(new IPluginEntry[0]);
		}
		return result;
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
		pluginEntries.add(pluginEntry);
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry pluginEntry,String contentKey,InputStream inStream) {
		//FIXME: should not be called should it ? Can I store in any URL Site ?
	}
	
	/**
	 * store Feature files
	 */
	public void storeFeatureInfo(VersionedIdentifier featureIdentifier,String contentKey,InputStream inStream){
		//FIXME: should not be called should it ?
	}

}

