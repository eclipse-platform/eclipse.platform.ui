package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.internal.misc.Assert;

/**
 * Instances of this class represent the result of a specific marker
 * query. Specifically they contain an ordered collection of marker 
 * attribute values.
 */

public class MarkerQueryResult {
	/**
	 * An ordered collection of marker attribute values.
	 */
	private String[] values;
	/**
	 * Cached hash code value
	 */
	private int hashCode;
	
	/**
	 * Creates a new marker query result with the given values.
	 * <p>
	 * The values may not be empty.
	 * </p>
	 * 
	 * @param values the target marker's attribute values
	 */
	public MarkerQueryResult(String[] markerAttributeValues) {
		Assert.isNotNull(markerAttributeValues);
		
		values = markerAttributeValues;
		computeHashCode();
	}
	
	/* (non-Javadoc)
	 * Method declared on Object.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof MarkerQueryResult))
			return false;
			
		if (o == this)
			return true;
	
		MarkerQueryResult mqr = (MarkerQueryResult)o;
		if (values.length != mqr.values.length)
			return false;
			
		for (int i = 0; i < values.length; i++) {
			if (!(values[i].equals(mqr.values[i])))
				return false;
		} 			
		
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on Object.
	 */
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Computes the hash code for this instance.
	 */
	public void computeHashCode() {
		hashCode = 19;

		for (int i = 0; i < values.length; i++) {
			hashCode = hashCode * 37 + values[i].hashCode();
		}
	}			
}
