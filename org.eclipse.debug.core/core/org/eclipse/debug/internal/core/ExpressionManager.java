package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;

/**
 * The expression manager manages all registered expressions
 * for the debug plugin. It is instantiated by the debug plugin
 * at startup.
 * 
 * [XXX: expression persistence not yet implemented]
 *
 * @see IExpressionManager
 */
public class ExpressionManager implements IExpressionManager, IDebugEventListener {
	
	/**
	 * Collection of registered expressions.
	 */
	private Vector fExpressions = new Vector(10);
	
	/**
	 * List of expression listeners
	 */
	private ListenerList fListeners = new ListenerList(2);
	
	/**
	 * @see IExpressionManager#addExpression(IExpression, String)
	 */
	public void addExpression(IExpression expression) {
		if (getExpressions0().indexOf(expression) == -1) {
			getExpressions0().add(expression);
			fireExpressionAdded(expression);
		}
	}

	/**
	 * @see IExpressionManager#getExpressions()
	 */
	public IExpression[] getExpressions() {
		Vector expressions = getExpressions0();
		IExpression[] temp= new IExpression[expressions.size()];
		expressions.copyInto(temp);
		return temp;
	}

	/**
	 * @see IExpressionManager#getExpressions(String)
	 */
	public IExpression[] getExpressions(String modelIdentifier) {
		Vector expressions = getExpressions0();
		ArrayList temp= new ArrayList(expressions.size());
		Iterator iter= expressions.iterator();
		while (iter.hasNext()) {
			IExpression expression= (IExpression) iter.next();
			String id= expression.getModelIdentifier();
			if (id != null && id.equals(modelIdentifier)) {
				temp.add(expression);
			}
		}
		return (IExpression[]) temp.toArray(new IExpression[temp.size()]);
	}

	/**
	 * @see IExpressionManager#removeExpression(IExpression)
	 */
	public void removeExpression(IExpression expression) {
		if (getExpressions0().indexOf(expression) >= 0) {
			getExpressions0().remove(expression);
			expression.dispose();
			fireExpressionRemoved(expression);
		}
	}

	/**
	 * @see IExpressionManager#addExpressionListener(IExpressionListener)
	 */
	public void addExpressionListener(IExpressionListener listener) {
		fListeners.add(listener);
	}

	/**
	 * @see IExpressionManager#removeExpressionListener(IExpressionListener)
	 */
	public void removeExpressionListener(IExpressionListener listener) {
		fListeners.remove(listener);
	}
	
	/**
	 * Called be the debug plug-in when starting up.
	 */
	public void startup() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * Called by the debug plug-in when shutting down.
	 */
	public void shutdown() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/**
	 * Returns the list of registered expressions as
	 * a vector.
	 * 
	 * @return vector of registered expressions
	 */
	protected Vector getExpressions0() {
		return fExpressions;
	}
	
	/**
	 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvent(DebugEvent event) {
		if (event.getSource() instanceof IExpression) {
			switch (event.getKind()) {
				case DebugEvent.CHANGE:
					fireExpressionChanged((IExpression)event.getSource());
					break;
				case DebugEvent.TERMINATE:
					removeExpression((IExpression)event.getSource());
					break;
				default:
					break;
			}
		} 
	}
	
	/**
	 * Notifies listeners that the given expression has been
	 * added.
	 * 
	 * @param expression the newly added expression
	 */
	protected void fireExpressionAdded(IExpression expression) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IExpressionListener)listeners[i]).expressionAdded(expression);
		}
	}
	
	/**
	 * Notifies listeners that the given expression has been
	 * removed.
	 * 
	 * @param expression the removed expression
	 */
	protected void fireExpressionRemoved(IExpression expression) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IExpressionListener)listeners[i]).expressionRemoved(expression);
		}
	}	
	
	/**
	 * Notifies listeners that the given expression has changed.
	 * 
	 * @param expression the changed expression
	 */
	protected void fireExpressionChanged(IExpression expression) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IExpressionListener)listeners[i]).expressionChanged(expression);
		}
	}		

	/**
	 * @see IExpressionManager#hasExpressions()
	 */
	public boolean hasExpressions() {
		return !getExpressions0().isEmpty();
	}

}
