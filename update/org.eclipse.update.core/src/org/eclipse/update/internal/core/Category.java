package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Comparator;
import org.eclipse.update.core.ICategory;

/**
 * Default Implementation of ICategory
 */
public class Category implements ICategory {
	
	private String name;
	private String label;
	private static Comparator comp;
	
	/**
	 * Default Constructor
	 */
	public Category(){}
	
	/**
	 * Constructor
	 */
	public Category(String name, String label){
		this.name = name;
		this.label = label;
	}


	/**
	 * @see ICategory#getName()
	 */
	public String getName() {
		return name;
	}


	/**
	 * @see ICategory#getLabel()
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * Sets the name
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Sets the label
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}


	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof ICategory){
			ICategory otherCategory = (ICategory)obj;
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
				 	
				 	ICategory cat1 = (ICategory)o1;
				 	ICategory cat2 = (ICategory)o2;
				 	
				 	if (cat1.equals(cat2)) return 0;
				 	return cat1.getName().compareTo(cat2.getName());
				 } 
			};
		}
		return comp;
	}

}

