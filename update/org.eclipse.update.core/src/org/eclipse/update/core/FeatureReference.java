package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.core.*;

/**
 *
 * 
 */

public class FeatureReference extends FeatureReferenceModel implements IFeatureReference {

	private IFeature feature;

	/**
	 * category : delegate to teh site
	 */
	private List categories;

	/**
	 * Constructor
	 */
	public FeatureReference() {
		super();
	}

	/**
	 * Returns the array of categories the feature belong to.
	 * 
	 * The categories are declared in the <code>site.xml</code> file.
	 * 
	 * @see ICategory
	 * @return the array of categories this feature belong to. Returns an empty array
	 * if there are no cateopries.
	 */
	public ICategory[] getCategories() {

		if (categories == null) {
			categories = new ArrayList();
			String[] categoriesAsString = getCategoryNames();
			for (int i = 0; i < categoriesAsString.length; i++) {
				categories.add(getSite().getCategory(categoriesAsString[i]));
			}
		}

		ICategory[] result = new ICategory[0];

		if (!(categories == null || categories.isEmpty())) {
			result = new ICategory[categories.size()];
			categories.toArray(result);
		}
		return result;
	}

	/**
	 * Returns the feature this reference points to
	 *  @return the feature on the Site
	 */
	public IFeature getFeature() throws CoreException {

		String type = getType();
		if (feature == null) {

			if (type == null || type.equals("")) { //$NON-NLS-1$
				// ask the Site for the default type 
				type = getSite().getDefaultPackagedFeatureType();
			}

			feature = createFeature(type, getURL(), getSite());
		}

		return feature;
	}

	/*
	 * @see IFeatureReference#addCategory(ICategory)
	 */
	public void addCategory(ICategory category) {
		this.addCategoryName(category.getName());
	}

	
	/**
	 * create an instance of a class that implements IFeature
	 */
	private IFeature createFeature(String featureType, URL url, ISite site) throws CoreException {
		IFeature feature = null;
		IFeatureFactory factory = FeatureTypeFactory.getInstance().getFactory(featureType);
		feature = factory.createFeature(url, site);
		return feature;
	}

	/*
	 * @see IFeatureReference#setURL(URL)
	 */
	public void setURL(URL url) throws CoreException {
		if (url != null) {
			setURLString(url.toExternalForm());
			try {
				resolve(url, null);
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, Policy.bind("FeatureReference.UnableToResolveURL", url.toExternalForm()), e); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}
	}

	/*
	 * @see IFeatureReference#getSite()
	 */
	public ISite getSite() {
		return (ISite) getSiteModel();
	}

	/*
	 * @see IFeatureReference#setSite(ISite)
	 */
	public void setSite(ISite site) {
		setSiteModel((SiteModel) site);
	}

}