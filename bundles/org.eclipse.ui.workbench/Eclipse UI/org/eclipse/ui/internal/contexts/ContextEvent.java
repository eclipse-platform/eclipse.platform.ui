package org.eclipse.ui.internal.contexts;

import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextEvent;
import org.eclipse.ui.internal.commands.util.Util;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class ContextEvent 
	implements IContextEvent {

	private IContext context;

	/**
	 * TODO javadoc
	 * 
	 * @param context
	 * @throws IllegalArgumentException
	 */	
	public ContextEvent(IContext context)
		throws IllegalArgumentException {		
		super();
		
		if (context == null)
			throw new IllegalArgumentException();
		
		this.context = context;
	}

	/**
	 * TODO javadoc
	 * 
	 * @param object
	 */		
	public boolean equals(Object object) {
		if (!(object instanceof ContextEvent))
			return false;

		ContextEvent contextEvent = (ContextEvent) object;	
		return Util.equals(context, contextEvent.context);
	}
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */		
	public IContext getContext() {
		return context;
	}
}
