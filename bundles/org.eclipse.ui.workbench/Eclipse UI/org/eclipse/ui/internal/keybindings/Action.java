package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class Action implements Comparable {
	
	public final static String TAG = "action";
	private final static String ATTRIBUTE_VALUE = "value";	
	
	public static Action create(String value) {
		return new Action(value);
	}

	public static Action read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return Action.create(memento.getString(ATTRIBUTE_VALUE));
	}

	public static void write(IMemento memento, Action action)
		throws IllegalArgumentException {
		if (memento == null || action == null)
			throw new IllegalArgumentException();
			
		memento.putString(ATTRIBUTE_VALUE, action.getValue());
	}
	
	private String value;
	
	private Action(String value) {
		super();
		this.value = value;	
	}
	
	public String getValue() {
		return value;	
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Action))
			throw new ClassCastException();
			
		return Util.compare(value, ((Action) object).value);			
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Action))
			return false;
		
		String value = ((Action) object).value;		
		return this.value == null ? value == null : this.value.equals(value);
	}
}
