package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.PrintWriter;
import java.util.Comparator;
import org.eclipse.update.core.ICategory;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.model.SiteCategoryModel;
import org.eclipse.update.core.model.URLEntryModel;
import org.eclipse.update.internal.core.*;

/**
 * Default Implementation of ICategory
 */
public class Category extends SiteCategoryModel implements ICategory {
	
	/**
	 * Default Constructor
	 */
	public Category(){}


	/**
	 * Constructor
	 */
	public Category(String name, String label){
		setName(name);
		setLabel(label);
	}

	/*
	 * @see ICategory#getDescription()
	 */
	public IURLEntry getDescription() {
		return (IURLEntry)getDescriptionModel();
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(IURLEntry description) {
		setDescriptionModel((URLEntryModel) description);
	}
}

