package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class Contributor implements Comparable {

	public final static String TAG = "contributor";
	private final static String ATTRIBUTE_VALUE = "value";
	
	public static Contributor create(String value) {
		return new Contributor(value);
	}

	public static Contributor read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return Contributor.create(memento.getString(ATTRIBUTE_VALUE));
	}

	public static void write(IMemento memento, Contributor contributor)
		throws IllegalArgumentException {
		if (memento == null || contributor == null)
			throw new IllegalArgumentException();
			
		memento.putString(ATTRIBUTE_VALUE, contributor.getValue());
	}	
	
	private String value;
	
	private Contributor(String value) {
		super();
		this.value = value;	
	}
	
	public String getValue() {
		return value;	
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Contributor))
			throw new ClassCastException();
			
		return Util.compare(value, ((Contributor) object).value);			
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Contributor))
			return false;
		
		String value = ((Contributor) object).value;		
		return this.value == null ? value == null : this.value.equals(value);
	}
}
