package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class State implements Comparable {

	public final static String TAG = "state";

	public static State create(Configuration configuration, Locale locale, 
		Platform platform, Scope scope)
		throws IllegalArgumentException {
		return new State(configuration, locale, platform, scope);
	}

	public static State read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		Configuration configuration = 
			Configuration.read(memento.getChild(Configuration.TAG));
		Locale locale = Locale.read(memento.getChild(Locale.TAG));
		Platform platform = Platform.read(memento.getChild(Platform.TAG));
		Scope scope = Scope.read(memento.getChild(Scope.TAG));
		return State.create(configuration, locale, platform, scope);
	}

	public static void write(IMemento memento, State state)
		throws IllegalArgumentException {
		if (memento == null || state == null)
			throw new IllegalArgumentException();	
			
		Configuration.write(memento.createChild(Configuration.TAG),
			state.getConfiguration());
		Locale.write(memento.createChild(Locale.TAG), state.getLocale());
		Platform.write(memento.createChild(Platform.TAG), state.getPlatform());
		Scope.write(memento.createChild(Scope.TAG), state.getScope());
	}

	private Configuration configuration;
	private Locale locale;
	private Platform platform;
	private Scope scope;

	private State(Configuration configuration, Locale locale, 
		Platform platform, Scope scope)
		throws IllegalArgumentException {
		super();

		if (configuration == null || locale == null || platform == null || 
			scope == null)
			throw new IllegalArgumentException();
		
		this.configuration = configuration;
		this.locale = locale;
		this.platform = platform;
		this.scope = scope;
	}
	
	public Configuration getConfiguration() {
		return configuration;	
	}
	
	public Locale getLocale() {
		return locale;	
	}
	
	public Platform getPlatform() {
		return platform;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public int match(State state) {
		int configurationMatch = configuration.match(state.configuration);
		
		if (configurationMatch == -1)
			return -1;
			
		int localeMatch = locale.match(state.locale);
		
		if (localeMatch == -1)
			return -1;

		int platformMatch = platform.match(state.platform);
		
		if (platformMatch == -1)
			return -1;
			
		int scopeMatch = scope.match(state.scope);
		
		if (scopeMatch == -1)
			return -1;		
				
		return (scopeMatch << 24) + (configurationMatch << 16) + 
			(platformMatch << 8) + (localeMatch);
	}

	public int compareTo(Object object) {
		if (!(object instanceof State))
			throw new ClassCastException();	
		
		State state = (State) object;
		int compareTo = configuration.compareTo(state.configuration);
		
		if (compareTo == 0) {
			compareTo = locale.compareTo(state.locale);
			
			if (compareTo == 0) {
				compareTo = platform.compareTo(state.platform);	
				
				if (compareTo == 0)
					compareTo = scope.compareTo(state.scope);
			}
		}
		
		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof State)) 
			return false;
		
		State state = (State) object;
		return configuration.equals(state.configuration) && 
			locale.equals(state.locale) && platform.equals(state.platform) && 
			scope.equals(state.scope);
	}
}



