/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.mirror;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;

/**
 * Local mirror site.  Read/Write
 */
public class MirrorSite extends Site {
	private final static String INDENT = "   ";
	private SiteModelFactory factory;
	/**
	 * plugin entries 
	 */
	private Collection downloadedPluginEntries = new ArrayList();
	private Collection downloadedFeatureReferenceModels = new ArrayList();
	public MirrorSite(SiteModelFactory factory) {
		this.factory = factory;
	}

	/**
	 * Install the specified feature and listed optional features on this site.
	 * @see ISite#install(IFeature, IVerificationListener, IProgressMonitor)
	 * @exception CoreException
	 */
	public void mirrorAndExpose(
		ISite remoteSite,
		ISiteFeatureReference[] sourceFeatureRefs,
		IFeatureReference[] optionalfeatures)
		throws CoreException {

		mirrorAndExposeFeatures(
			remoteSite,
			sourceFeatureRefs,
			optionalfeatures);

		System.out.println(
			"Installing features finished. Updating categories ...");
		updateCategories(remoteSite);
		System.out.println(
			"Updating categories finished. Updating site description ...");
		updateDescription(remoteSite);
		System.out.println(
			"Updating site description finished. Saving site.xml ...");
		save();
	}
	private void mirrorAndExposeFeatures(
		ISite remoteSite,
		ISiteFeatureReference[] sourceFeatureRefs,
		IFeatureReference[] optionalfeatures)
		throws CoreException {

		// Features that failed will be retried once again
		Collection failedFeatures = new ArrayList();
		for (int i = 0; i < sourceFeatureRefs.length; i++) {
			try {
				IFeature sourceFeature =
					sourceFeatureRefs[i].getFeature(new NullProgressMonitor());
				SiteFeatureReferenceModel featureRef =
					mirrorFeature(
						remoteSite,
						sourceFeature,
						optionalfeatures,
						1);
				// Set categories of the new feature
				ICategory remoteCategories[] =
					sourceFeatureRefs[i].getCategories();
				for (int j = 0; j < remoteCategories.length; j++) {
					featureRef.addCategoryName(remoteCategories[j].getName());
				}

				addFeatureReferenceModel(remoteSite, featureRef);
			} catch (CoreException ce) {
				failedFeatures.add(sourceFeatureRefs[i]);
			}
		}

		// do we need to retry?
		if (failedFeatures.size() > 0) {
			sourceFeatureRefs =
				(ISiteFeatureReference[]) failedFeatures.toArray(
					new ISiteFeatureReference[failedFeatures.size()]);
		} else {
			return;
		}

		for (int i = 0; i < sourceFeatureRefs.length; i++) {
			IFeature sourceFeature =
				sourceFeatureRefs[i].getFeature(new NullProgressMonitor());
			SiteFeatureReferenceModel featureRef =
				mirrorFeature(remoteSite, sourceFeature, optionalfeatures, 1);
			// Set categories of the new feature
			ICategory remoteCategories[] = sourceFeatureRefs[i].getCategories();
			for (int j = 0; j < remoteCategories.length; j++) {
				featureRef.addCategoryName(remoteCategories[j].getName());
			}

			addFeatureReferenceModel(remoteSite, featureRef);
		}
	}

	/**
	 * Install the specified feature and listed optional features on this site.
	 * @see ISite#install(IFeature, IVerificationListener, IProgressMonitor)
	 * @exception CoreException
	 */
	private SiteFeatureReferenceModel mirrorFeature(
		ISite remoteSite,
		IFeature sourceFeature,
		IFeatureReference[] optionalfeatures,
		int indent)
		throws CoreException {
		String tab = "";
		for (int i = 0; i < indent; i++)
			tab += " ";
		System.out.println(
			tab
				+ "Mirroring feature "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		SiteFeatureReferenceModel existingFeatures[] =
			getDownloadedFeatureReferenceModels();
		for (int e = 0; e < existingFeatures.length; e++) {
			if (existingFeatures[e]
				.getVersionedIdentifier()
				.equals(sourceFeature.getVersionedIdentifier())) {
				System.out.println(
					tab
						+ "Feature "
						+ sourceFeature.getVersionedIdentifier()
						+ " already exists.  Skipping downloading.");
				return existingFeatures[e];
			}
		}

		final IFeatureContentProvider provider =
			sourceFeature.getFeatureContentProvider();
		System.out.println(
			tab
				+ "Getting plugin entries for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		final IPluginEntry[] sourceFeaturePluginEntries =
			sourceFeature.getPluginEntries();

		// determine list of plugins to install
		// find the intersection between the plugin entries already contained
		// on the target site, and plugin entries packaged in source feature
		IFeatureReference alreadyInstalledFeature = null;

		IPluginEntry[] pluginsToInstall =
			UpdateManagerUtils.diff(
				sourceFeaturePluginEntries,
				getDownloadedPluginEntries());

		System.out.println(
			tab
				+ "Getting non plugin entries for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		final INonPluginEntry[] nonPluginsToInstall =
			sourceFeature.getRawNonPluginEntries();

		System.out.println(
			tab
				+ "Getting included features for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		IFeatureReference[] children =
			sourceFeature.getRawIncludedFeatureReferences();
		if (optionalfeatures != null) {
			children =
				UpdateManagerUtils.optionalChildrenToInstall(
					children,
					optionalfeatures);
		}

		System.out.println(
			tab
				+ "Downloading feature archives for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		// download feature archives
		provider.getFeatureEntryArchiveReferences(null);

		System.out.println(
			tab
				+ "Downloading plug-in archives for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		// download plugin archives
		for (int i = 0; i < pluginsToInstall.length; i++) {
			provider.getPluginEntryArchiveReferences(pluginsToInstall[i], null);
		}

		//		System.out.println(
		//			tab
		//				+ "Downloading non plug-in archives for "
		//				+ sourceFeature.getVersionedIdentifier()
		//				+ " ...");
		//		// download non-plugin archives
		//		for (int i = 0; i < nonPluginsToInstall.length; i++) {
		//			provider.getNonPluginEntryArchiveReferences(
		//				nonPluginsToInstall[i],
		//				null);
		//		}

		System.out.println(
			tab
				+ "Installing child features for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		// install child features first
		for (int i = 0; i < children.length; i++) {
			IFeature childFeature = children[i].getFeature(null);
			mirrorFeature(
				remoteSite,
				childFeature,
				optionalfeatures,
				indent + 1);
		}

		// if feature already installed, skip installing its archives
		//		ISiteFeatureReference existingFeatures[]=getFeatureReferences();
		//		for(int i=0; i<existingFeatures.length; i++){
		//			if(existingFeatures[i].getVersionedIdentifier().equals(sourceFeature.getVersionedIdentifier())){
		//				System.out.println("Feature "+sourceFeature.getVersionedIdentifier().getIdentifier()+" "+sourceFeature.getVersionedIdentifier().getVersion()+ " already installed.");
		//				return null;
		//			}
		//		}

		System.out.println(
			tab
				+ "Storing plug-in archives for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		// store plugins' archives
		for (int i = 0; i < pluginsToInstall.length; i++) {
			ContentReference[] references =
				provider.getPluginEntryArchiveReferences(
					pluginsToInstall[i],
					null);
			storePluginArchive(references[0]);
			addDownloadedPluginEntry(pluginsToInstall[i]);
		}

		System.out.println(
			tab
				+ "Storing feature archives for "
				+ sourceFeature.getVersionedIdentifier()
				+ " ...");
		// store feature archive
		ContentReference[] references =
			provider.getFeatureEntryArchiveReferences(null);
		storeFeatureArchive(references[0]);

		System.out.println(
			tab
				+ "Adding feature "
				+ sourceFeature.getVersionedIdentifier()
				+ " to model ...");

		// add feature model to site model
		SiteFeatureReferenceModel featureRef =
			factory.createFeatureReferenceModel();
		featureRef.setSiteModel(this);
		//featureRef.setURLString(featureURL.toExternalForm());
		featureRef.setType(ISite.DEFAULT_PACKAGED_FEATURE_TYPE);
		featureRef.setFeatureIdentifier(
			sourceFeature.getVersionedIdentifier().getIdentifier());
		featureRef.setFeatureVersion(
			sourceFeature.getVersionedIdentifier().getVersion().toString());
		addDownloadedFeatureReferenceModel(featureRef);

		System.out.println(
			tab
				+ "Mirroring feature "
				+ sourceFeature.getVersionedIdentifier()
				+ " finished.");
		return featureRef;

	}
	/**
	 * Adds a feature reference model to this site,
	 * and exposes in site.xml if remote site exposes given feature.
	 */
	public void addFeatureReferenceModel(
		ISite remoteSite,
		SiteFeatureReferenceModel featureReference) {
		// check if remote site exposes this feature
		ISiteFeatureReference remoteFeatures[] =
			remoteSite.getRawFeatureReferences();
		for (int i = 0; i < remoteFeatures.length; i++) {
			ISiteFeatureReference remoteFeatureRef = remoteFeatures[i];
			try {
				if (remoteFeatureRef
					.getVersionedIdentifier()
					.equals(featureReference.getVersionedIdentifier())) {
					addFeatureReferenceModel(featureReference);
				}
			} catch (CoreException ce) {
			}
		}
		save();
		System.out.println(
			"Feature "
				+ featureReference.getVersionedIdentifier()
				+ " added to site.xml.");
	}
	/**
	 * Adds feature model to site model, removing old feature
	 */
	public void addFeatureReferenceModel(SiteFeatureReferenceModel featureReference) {
		SiteFeatureReferenceModel[] existingModels =
			getFeatureReferenceModels();
		for (int j = 0; j < existingModels.length; j++) {
			if (existingModels[j]
				.getVersionedIdentifier()
				.equals(featureReference.getVersionedIdentifier())) {
				super.removeFeatureReferenceModel(existingModels[j]);
			}
		}
		super.addFeatureReferenceModel(featureReference);
	}

	/**
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	private void storeFeatureArchive(ContentReference contentReference)
		throws CoreException {
		InputStream inStream = null;
		String featurePath = null;

		try {
			URL newURL =
				new URL(
					this.getURL(),
					Site.DEFAULT_INSTALLED_FEATURE_PATH
						+ contentReference.getIdentifier()
						+ ".jar");
			featurePath = newURL.getFile();
			inStream = contentReference.getInputStream();
			UpdateManagerUtils.copyToLocal(inStream, featurePath, null);
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind("MirrorSite.ErrorCreatingFile", featurePath),
				e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
				}
			}
		}

	}
	/**
	* @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	*/
	private void storePluginArchive(ContentReference contentReference)
		throws CoreException {

		InputStream inStream = null;
		String pluginPath = null;
		try {
			URL newURL = new URL(getURL(), contentReference.getIdentifier());
			pluginPath = newURL.getFile();
			inStream = contentReference.getInputStream();
			UpdateManagerUtils.copyToLocal(inStream, pluginPath, null);
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind("MirrorSite.ErrorCreatingFile", pluginPath),
				e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	private void save() {
		FileOutputStream fos = null;
		try {
			URL siteURL = new URL(this.getURL(), "site.xml");
			fos = new FileOutputStream(new File(siteURL.getFile()));
			PrintWriter writer = new PrintWriter(fos);
			save(writer);
			writer.flush();
		} catch (IOException ioe) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe2) {
				}
			}
		}
	}
	private void save(PrintWriter writer) {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		//writer.println("<!DOCTYPE site SYSTEM \"dtd/site.dtd\">");
		writeSite("", writer);
	}

	private void writeSite(String indent, PrintWriter writer) {
		writer.print(indent + "<site");
		String indent2 = indent + INDENT;
		// default type
		//writeIfDefined(indenta, writer, "type", getType());
		// stored relative to site.xml
		//writeIfDefined(indenta, writer, "url", getURL());
		writer.println(">");
		URLEntryModel description = getDescriptionModel();
		if (description != null) {
			writer.println();
			writeDescription(indent2, writer, description);
			writer.println();
		}
		writeFeatures(indent2, writer);
		writeCategories(indent2, writer);
		writer.println(indent + "</site>");
	}
	private void writeFeatures(String indent, PrintWriter writer) {
		SiteFeatureReferenceModel[] featureReferenceModels =
			getFeatureReferenceModels();
		for (int i = 0; i < featureReferenceModels.length; i++) {
			writer.print(indent);
			writer.print("<feature");
			writer.print(
				" url=\"features/"
					+ featureReferenceModels[i].getFeatureIdentifier()
					+ "_"
					+ featureReferenceModels[i].getFeatureVersion()
					+ ".jar\"");
			writer.print(
				" id=\""
					+ featureReferenceModels[i].getFeatureIdentifier()
					+ "\"");
			writer.print(
				" version=\""
					+ featureReferenceModels[i].getFeatureVersion()
					+ "\"");
			writer.println(">");

			String[] categoryNames =
				featureReferenceModels[i].getCategoryNames();
			for (int cn = 0; cn < categoryNames.length; cn++) {
				writer.print(indent + INDENT);
				writer.println(
					"<category name=\"" + categoryNames[cn] + "\" />");

			}

			writer.print(indent);
			writer.println("</feature>");
			writer.println();
		}
	}
	private void writeCategories(String indent, PrintWriter writer) {
		CategoryModel[] categoryModels = getCategoryModels();
		if (categoryModels.length <= 0) {
			return;
		}
		for (int i = 0; i < categoryModels.length; i++) {
			writer.print(indent);
			writer.print("<category-def");
			writer.print(
				" name=\""
					+ categoryModels[i].getName()
					+ "\" label=\""
					+ categoryModels[i].getLabel()
					+ "\"");
			writer.println(">");
			writeDescription(
				indent + INDENT,
				writer,
				categoryModels[i].getDescriptionModel());
			writer.print(indent);
			writer.println("</category-def>");
			writer.println();
		}
	}
	private void writeDescription(
		String indent,
		PrintWriter writer,
		URLEntryModel urlEntryModel) {
		String url = urlEntryModel.getURLString();
		String text = urlEntryModel.getAnnotationNonLocalized();
		if (url == null && text == null && text.length() <= 0) {
			return;
		}
		writer.print(indent);
		writer.print("<description");
		if (url != null)
			writer.print(" url=\"" + url + "\"");
		if (text == null || text.length() <= 0) {
			writer.println(" />");
		} else {
			writer.println(">");
			if (text != null) {
				writer.println(
					indent + INDENT + UpdateManagerUtils.Writer.xmlSafe(text));
			}
			writer.println(indent + "</description>");
		}
	}
	/**
	 * Adds a plugin entry 
	 * Either from parsing the file system or 
	 * installing a feature
	 * 
	 * We cannot figure out the list of plugins by reading the Site.xml as
	 * the archives tag are optionals
	 */
	public void addDownloadedPluginEntry(IPluginEntry pluginEntry) {
		downloadedPluginEntries.add(pluginEntry);
	}
	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	private IPluginEntry[] getDownloadedPluginEntries() {
		return (IPluginEntry[]) downloadedPluginEntries.toArray(
			new IPluginEntry[downloadedPluginEntries.size()]);
	}
	/**
	 * Adds a plugin entry 
	 * Either from parsing the file system or 
	 * installing a feature
	 * 
	 * We cannot figure out the list of plugins by reading the Site.xml as
	 * the archives tag are optionals
	 */
	public void addDownloadedFeatureReferenceModel(SiteFeatureReferenceModel featureModel) {
		downloadedFeatureReferenceModels.add(featureModel);
	}
	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	private SiteFeatureReferenceModel[] getDownloadedFeatureReferenceModels() {
		return (
			SiteFeatureReferenceModel[]) downloadedFeatureReferenceModels
				.toArray(
			new SiteFeatureReferenceModel[downloadedFeatureReferenceModels
				.size()]);
	}
	/**
	 * Checks if mirror site contains a feature with given ID and version
	 * @param featureRefModel
	 * @return true if such feature exists
	 */
	private boolean contains(SiteFeatureReferenceModel featureRefModel) {
		ISiteFeatureReference featureRefs[] = getRawFeatureReferences();
		for (int i = 0; i < featureRefs.length; i++) {
			try {
				if (featureRefs[i]
					.getVersionedIdentifier()
					.equals(featureRefModel.getVersionedIdentifier())) {
					return true;
				}
			} catch (CoreException ce) {
				ce.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * Updates description of this site
	 * from description of the remote site.
	 */
	private void updateDescription(ISite remoteSite) {
		IURLEntry urlEntry = remoteSite.getDescription();
		if (urlEntry != null) {
			URLEntryModel newUrlEntryModel = new URLEntryModel();
			URL url = urlEntry.getURL();
			newUrlEntryModel.setAnnotation(urlEntry.getAnnotation());
			newUrlEntryModel.setURLString(url.toExternalForm());
			this.setDescriptionModel(newUrlEntryModel);
		}
	}
	/**
	 * Updates all categories used by features on this site
	 * from categories defined on remote site.
	 * Categories not defined on remote site are unchanged.
	 */
	private void updateCategories(ISite remoteSite) {
		// collect name of categories used on this site
		Set usedCategoryNames = new HashSet();
		SiteFeatureReferenceModel featureRefModels[] =
			getFeatureReferenceModels();
		for (int f = 0; f < featureRefModels.length; f++) {
			String[] featureCategoryNames =
				featureRefModels[f].getCategoryNames();

			for (int c = 0; c < featureCategoryNames.length; c++) {
				usedCategoryNames.add(featureCategoryNames[c]);
			}
		}

		Collection newCategoryModels = new ArrayList();
		for (Iterator it = usedCategoryNames.iterator(); it.hasNext();) {
			String name = (String) it.next();
			ICategory remoteCategory = remoteSite.getCategory(name);
			if (remoteCategory == null) {
				// remote site does not define this category
				CategoryModel oldCategory = null;
				try {
					oldCategory = (CategoryModel) getCategory(name);
				} catch (NullPointerException npe) {
					// cannot reproduce npe anymore
				}
				if (oldCategory != null) {
					newCategoryModels.add(oldCategory);
				}
			} else {
				newCategoryModels.add(remoteCategory);
			}

		}
		setCategoryModels(
			(CategoryModel[]) newCategoryModels.toArray(
				new CategoryModel[newCategoryModels.size()]));

	}
}
