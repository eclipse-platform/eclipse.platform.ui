package org.eclipse.update.tests.implementation;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;

public class SiteFTP implements ISite {

	private URL url;
	public SiteFTP(URL url){
		this.url = url;
	}

	/*
	 * @see ISite#getFeatureReferences()
	 */
	public IFeatureReference[] getFeatureReferences() {
		return null;
	}

	/*
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public IFeatureReference install(IFeature feature, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	/*
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
	}

	/*
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
	}

	/*
	 * @see ISite#getURL()
	 */
	public URL getURL() {
		return url;
	}

	/*
	 * @see ISite#getType()
	 */
	public String getType() {
		return "org.eclipse.update.tests.ftp";
	}

	/*
	 * @see ISite#getInfoURL()
	 */
	public URL getInfoURL() {
		return null;
	}

	/*
	 * @see ISite#getCategories()
	 */
	public ICategory[] getCategories() {
		return null;
	}

	/*
	 * @see ISite#getArchives()
	 */
	public IArchiveEntry[] getArchives() {
		return null;
	}
 
	/*
	 * @see ISite#addCategory(ICategory)
	 */
	public void addCategory(ICategory category) {
	}

	/*
	 * @see ISite#save()
	 */
	public void save() throws CoreException {
	}

	/*
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		return null;
	}

	/*
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return 0;
	}

	/*
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public long getDownloadSize(IPluginEntry entry) {
		return 0;
	}

	/*
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public long getInstallSize(IPluginEntry entry) {
		return 0;
	}

	/*
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
	}

	/*
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(IPluginEntry entry, String name, InputStream inStream) throws CoreException {
	}

	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * @see IPluginContainer#remove(IPluginEntry)
	 */
	public void remove(IPluginEntry entry) throws CoreException {
	}

	/*
	 * @see ISite#setSiteContentConsumer(ISiteContentConsumer)
	 */
	public void setSiteContentConsumer(ISiteContentConsumer contentConsumer) {
	}

	/*
	 * @see ISite#setSiteContentProvider(ISiteContentProvider)
	 */
	public void setSiteContentProvider(ISiteContentProvider siteContentProvider) {
	}

	/*
	 * @see ISite#getSiteContentProvider()
	 */
	public ISiteContentProvider getSiteContentProvider() {
		return null;
	}

	/*
	 * @see ISite#getDefaultExecutableFeatureType()
	 */
	public String getDefaultExecutableFeatureType() {
		return null;
	}

	/*
	 * @see ISite#getDefaultInstallableFeatureType()
	 */
	public String getDefaultInstallableFeatureType() {
		return null;
	}

	/*
	 * @see ISite#store(IFeature, String, InputStream, IProgressMonitor)
	 */
	public void store(IFeature feature, String name, InputStream inStream, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream, IProgressMonitor)
	 */
	public void store(IPluginEntry entry, String name, InputStream inStream, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * @see IPluginContainer#remove(IPluginEntry, IProgressMonitor)
	 */
	public void remove(IPluginEntry entry, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * @see ISite#getCategory(String)
	 */
	public ICategory getCategory(String key) {
		return null;
	}

	/*
	 * @see ISite#createSiteContentConsumer(IFeature)
	 */
	public ISiteContentConsumer createSiteContentConsumer(IFeature feature) throws CoreException {
		return null;
	}

}
