package org.eclipse.ui.internal.keybindings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public final class KeyBinding implements Comparable {

	public final static String ROOT = "keybindings";	
	public final static String TAG = "keybinding";

	public static KeyBinding create(KeySequence keySequence, State state, 
		Contributor contributor, Action action)
		throws IllegalArgumentException {
		return new KeyBinding(keySequence, state, contributor, action);
	}

	public static KeyBinding read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		KeySequence sequence = 
			KeySequence.read(memento.getChild(KeySequence.TAG));
		State state = State.read(memento.getChild(State.TAG));
		Contributor contributor = 
			Contributor.read(memento.getChild(Contributor.TAG));
		Action action = Action.read(memento.getChild(Action.TAG));
		return KeyBinding.create(sequence, state, contributor, action);
	}

	public static void write(IMemento memento, KeyBinding binding)
		throws IllegalArgumentException {
		if (memento == null || binding == null)
			throw new IllegalArgumentException();
			
		KeySequence.write(memento.createChild(KeySequence.TAG), 
			binding.getKeySequence());
		State.write(memento.createChild(State.TAG), binding.getState());
		Contributor.write(memento.createChild(Contributor.TAG), 
			binding.getContributor());
		Action.write(memento.createChild(Action.TAG), binding.getAction());			
	}

	public static List readBindingsFromReader(Reader reader)
		throws IOException {
		try {
			XMLMemento xmlMemento = XMLMemento.createReadRoot(reader);
			return readBindings(xmlMemento);
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();	
		}
	}

	public static void writeBindingsToWriter(Writer writer, String root, 
		List bindings)
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(root);
		writeBindings(xmlMemento, bindings);
		xmlMemento.save(writer);
	}

	public static List readBindings(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			
		
		IMemento[] mementos = memento.getChildren(KeyBinding.TAG);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List bindings = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			bindings.add(KeyBinding.read(mementos[i]));
		
		return bindings;		
	}

	public static void writeBindings(IMemento memento, List bindings)
		throws IllegalArgumentException {
		if (memento == null || bindings == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext())
			KeyBinding.write(memento.createChild(KeyBinding.TAG), 
				(KeyBinding) iterator.next()); 
	}

	public static void filterAction(List bindings, Set actions, 
		boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding binding = (KeyBinding) iterator.next();
			Action action = binding.getAction();
			
			if (exclusive ^ !actions.contains(action))
				iterator.remove();
		}
	}

	public static void filterConfiguration(List bindings, Set configurations,
		boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding binding = (KeyBinding) iterator.next();
			State state = binding.getState();			
			Configuration configuration = state.getConfiguration();
			
			if (exclusive ^ !configurations.contains(configuration))
				iterator.remove();
		}
	}

	public static void filterLocale(List bindings, Set locales, 
		boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding binding = (KeyBinding) iterator.next();
			State state = binding.getState();			
			Locale locale = state.getLocale();
			
			if (exclusive ^ !locales.contains(locale))
				iterator.remove();
		}
	}

	public static void filterPlatform(List bindings, Set platforms, 
		boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding binding = (KeyBinding) iterator.next();
			State state = binding.getState();			
			Platform platform = state.getPlatform();
			
			if (exclusive ^ !platforms.contains(platform))
				iterator.remove();
		}
	}

	public static void filterScope(List bindings, Set scopes, 
		boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding binding = (KeyBinding) iterator.next();
			State state = binding.getState();			
			Scope scope = state.getScope();
			
			if (exclusive ^ !scopes.contains(scope))
				iterator.remove();
		}
	}

	private KeySequence keySequence;
	private State state;
	private Contributor contributor;
	private Action action;

	private KeyBinding(KeySequence keySequence, State state, 
		Contributor contributor, Action action)
		throws IllegalArgumentException {
		super();
		
		if (keySequence == null || state == null || contributor == null ||
			action == null)
			throw new IllegalArgumentException();	
		
		this.keySequence = keySequence;
		this.state = state;
		this.contributor = contributor;
		this.action = action;
	}

	public KeySequence getKeySequence() {
		return keySequence;	
	}
	
	public State getState() {
		return state;
	}

	public Contributor getContributor() {
		return contributor;
	}
	
	public Action getAction() {
		return action;
	}

	public int compareTo(Object object) {
		if (!(object instanceof KeyBinding))
			throw new ClassCastException();		
		
		KeyBinding binding = (KeyBinding) object;
		int compareTo = keySequence.compareTo(binding.keySequence);

		if (compareTo == 0) {
			compareTo = state.compareTo(binding.state);

			if (compareTo == 0)
				compareTo = contributor.compareTo(binding.contributor);
			
				if (compareTo == 0)
					compareTo = action.compareTo(binding.action);
		}
		
		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBinding))
			return false;
		
		KeyBinding binding = (KeyBinding) object;
		return keySequence.equals(binding.keySequence) && 
			state.equals(binding.state) && 
			contributor.equals(binding.contributor) && 
			action.equals(binding.action);
	}
}
