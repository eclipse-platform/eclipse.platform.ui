package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.ICategory;

/**
 *
 * 
 */

public class FeatureReference implements IFeatureReference {

	private URL url;

	private ISite site;

	private String featureType;
	
	private Feature feature;

	/**
	 * category String; From teh XML file
	 */
	private List categoryString;

	/**
	 * category : delegate to teh site
	 */
	private List categories;

	/**
	 * Constructor
	 */
	public FeatureReference(ISite site, URL url) {
		super();
		this.site = site;
		this.url = url;
	}

	/**
	 * Returns the URL that points at the Feature.
	 * This URL is the unique identifier of the feature
	 * within the site.
	 * 
	 * The URL is declared in the <code>feature.xml</code> file.	
	 * 
	 * @return the URL identifying feature in the Site.
	 */
	public URL getURL() {
		return url;
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
			List categoriesAsString = getCategoryString();
			if (categoriesAsString != null && !categoriesAsString.isEmpty()) {
				Iterator iter = categoriesAsString.iterator();
				while (iter.hasNext()) {
					categories.add(((Site) site).getCategory((String) iter.next()));
				}
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
	 *  @return teh feature on teh Site
	 */
	public IFeature getFeature() throws CoreException {

		if (feature == null) {
			if (featureType == null || featureType.equals("")) {
				// ask the Site for the default type 
				feature = (Feature) ((Site) site).getDefaultFeature(url);
				feature.initializeFeature();
			} else {
				Assert.isTrue(false, "Not implemented Yet... do not use 'type' in the feature tag of site.xml");
				//FIXME: manages creation of feature...
			}
		}
		return feature;
	}

	/**
	 * Gets the categoryString
	 * @return Returns a String
	 */
	private List getCategoryString() {
		if (categoryString == null)
			categoryString = new ArrayList(0);
		return categoryString;
	}

	/**
	 * Adds a categoryString
	 * @param categoryString The categoryString to add
	 */
	public void addCategoryString(String categoryString) {
		if (this.categoryString == null)
			this.categoryString = new ArrayList(0);
		this.categoryString.add(categoryString);
	}

	/**
	 * Sets the categoryString
	 * @param categoryString The categoryString to set
	 */
	public void setCategoryString(String[] categoryString) {
		if (categoryString != null) {
			for (int i = 0; i < categoryString.length; i++) {
				addCategoryString(categoryString[i]);
			}
		}
	}
	/**
	 * Sets the featureType.
	 * @param featureType The featureType to set
	 */
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}

	/*
	 * @see IFeatureReference#addCategory(ICategory)
	 */
	public void addCategory(ICategory category) {
		this. addCategoryString(category.getName());
	}

}