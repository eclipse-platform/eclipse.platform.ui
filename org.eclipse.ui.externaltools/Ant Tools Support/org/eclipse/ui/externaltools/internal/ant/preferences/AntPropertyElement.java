package org.eclipse.ui.externaltools.internal.ant.preferences;



public class AntPropertyElement {

	private String name;
	private String value;

	public AntPropertyElement(String name, String value) {
		this.name= name;
		this.value= value;
	}

	/**
	 * Gets the name
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name= name;
	}
	
	/*
	 * @see Object#equals()
	 */	
	public boolean equals(Object other) {
		if (other.getClass().equals(getClass())) {
			AntPropertyElement elem= (AntPropertyElement)other;
			return name.equals(elem.getName());
		}
		return false;
	}
	
	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return name.hashCode();
	}	
	/**
	 * Returns the value.
	 * @return String
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 * @param value The value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
