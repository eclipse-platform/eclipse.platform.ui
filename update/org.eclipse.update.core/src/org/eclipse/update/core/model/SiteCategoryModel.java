package org.eclipse.update.core.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;


/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

/**
 * An object which represents a category definition in a 
 * site map.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class SiteCategoryModel extends ModelObject {
	
	private String name;
	private String label;
	private String localizedLabel;
	private URLEntryModel description;
	private static Comparator comp;	
	
	/**
	 * Creates an uninitialized model object.
	 * 
	 * @since 2.0
	 */
	public SiteCategoryModel() {
		super();
	}
	
	/**
	 * @since 2.0
	 */
	public String getName() {
		return name;
	}

	/**
	 * @since 2.0
	 */	
	public String getLabel() {
		if (localizedLabel != null)
			return localizedLabel;
		else
			return label;
	}

	/**
	 * @since 2.0
	 */	
	public String getLabelNonLocalized() {
		return label;
	}

	/**
	 * @since 2.0
	 */	
	public URLEntryModel getDescriptionModel() {
		return description;
	}

	/**
	 * @since 2.0
	 */
	public void setLabel(String label) {
		assertIsWriteable();
		this.label = label;
		this.localizedLabel = null;
	}

	/**
	 * @since 2.0
	 */
	public void setName(String name) {
		assertIsWriteable();
		this.name = name;
	}

	/**
	 * @since 2.0
	 */
	public void setDescriptionModel(URLEntryModel description) {
		assertIsWriteable();
		this.description = description;
	}
	
	/**
	 * @since 2.0
	 */
	public void markReadOnly() {		
		markReferenceReadOnly(getDescriptionModel());
	}
	
	/**
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {
		// resolve local elements
		localizedLabel = resolveNLString(bundle,label);

		// delegate to references
		resolveReference(getDescriptionModel(), base, bundle);
	}
	
	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof SiteCategoryModel){
			SiteCategoryModel otherCategory = (SiteCategoryModel)obj;
			result = getName().equalsIgnoreCase(otherCategory.getName());
		}
		return result ;
	}

	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}

	/**
	 * Gets the comp.
	 * @return Returns a Comparator
	 */
	public static Comparator getComparator() {
		if (comp==null){
			comp = new Comparator(){
				/*
				 * @see Comparator#compare(Object,Object)
				 * Returns 0 if versions are equal.
				 * Returns -1 if object1 is after than object2.
				 * Returns +1 if object1 is before than object2.
				 */
				 public int compare(Object o1, Object o2){
				 	
				 	SiteCategoryModel cat1 = (SiteCategoryModel)o1;
				 	SiteCategoryModel cat2 = (SiteCategoryModel)o2;
				 	
				 	if (cat1.equals(cat2)) return 0;
				 	return cat1.getName().compareTo(cat2.getName());
				 } 
			};
		}
		return comp;
	}
	
}
