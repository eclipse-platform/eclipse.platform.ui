package org.eclipse.debug.internal.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.IExpressionsListener;
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
public class ExpressionManager implements IExpressionManager, IDebugEventSetListener {
	
	/**
	 * Collection of registered expressions.
	 */
	private Vector fExpressions = new Vector(10);
	
	/**
	 * List of expression listeners
	 */
	private ListenerList fListeners = new ListenerList(2);
	
	/**
	 * List of (multi) expressions listeners
	 */
	private ListenerList fExpressionsListeners = new ListenerList(2);	
	
	// Constants for add/remove/change notification
	private static final int ADDED = 1;
	private static final int CHANGED = 2;
	private static final int REMOVED = 3;
	
	/**
	 * @see IExpressionManager#addExpression(IExpression)
	 */
	public void addExpression(IExpression expression) {
		addExpressions(new IExpression[]{expression});
	}
	
	/**
	 * @see IExpressionManager#addExpressions(IExpression[])
	 */
	public void addExpressions(IExpression[] expressions) {
		List added = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (getExpressions0().indexOf(expression) == -1) {
				added.add(expression);
				getExpressions0().add(expression);
			}				
		}
		if (!added.isEmpty()) {
			fireUpdate((IExpression[])added.toArray(new IExpression[added.size()]), ADDED);
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
		removeExpressions(new IExpression[] {expression});
	}

	/**
	 * @see IExpressionManager#removeExpressions(IExpression[])
	 */
	public void removeExpressions(IExpression[] expressions) {
		List removed = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (getExpressions0().remove(expression)) {
				removed.add(expression);
			}				
		}
		if (!removed.isEmpty()) {
			fireUpdate((IExpression[])removed.toArray(new IExpression[removed.size()]), REMOVED);
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
	 * @see IDebugEventSetListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			List changed = null;
			DebugEvent event = events[i];
			if (event.getSource() instanceof IExpression) {
				switch (event.getKind()) {
					case DebugEvent.CHANGE:
						if (changed == null) {
							changed = new ArrayList(1);
						}
						changed.add(event.getSource());
						break;
					default:
						break;
				}
			} 
			if (changed != null) {
				IExpression[] array = (IExpression[])changed.toArray(new IExpression[changed.size()]);
				fireUpdate(array, CHANGED);
			}
		}
	}

	/**
	 * Notifies listeners of the adds/removes/changes
	 * 
	 * @param breakpoints associated breakpoints
	 * @param deltas or <code>null</code>
	 * @param update type of change
	 */
	private void fireUpdate(IExpression[] expressions, int update) {
		// single listeners
		Object[] copiedListeners= fListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			IExpressionListener listener = (IExpressionListener)copiedListeners[i];
			for (int j = 0; j < expressions.length; j++) {
				IExpression expression = expressions[j];
				switch (update) {
					case ADDED:
						listener.expressionAdded(expression);
						break;
					case REMOVED:
						listener.expressionRemoved(expression);
						break;
					case CHANGED:
						listener.expressionChanged(expression);		
						break;
				}				
			}
		}
		
		// multi listeners
		copiedListeners = fExpressionsListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			IExpressionsListener listener = (IExpressionsListener)copiedListeners[i];
			switch (update) {
				case ADDED:
					listener.expressionsAdded(expressions);
					break;
				case REMOVED:
					listener.expressionsRemoved(expressions);
					break;
				case CHANGED:
					listener.expressionsChanged(expressions);		
					break;
			}
		}		
	}	

	/**
	 * @see IExpressionManager#hasExpressions()
	 */
	public boolean hasExpressions() {
		return !getExpressions0().isEmpty();
	}

	/**
	 * @see org.eclipse.debug.core.IExpressionManager#addExpressionListener(org.eclipse.debug.core.IExpressionsListener)
	 */
	public void addExpressionListener(IExpressionsListener listener) {
		fExpressionsListeners.add(listener);
	}

	/**
	 * @see org.eclipse.debug.core.IExpressionManager#removeExpressionListener(org.eclipse.debug.core.IExpressionsListener)
	 */
	public void removeExpressionListener(IExpressionsListener listener) {
		fExpressionsListeners.remove(listener);
	}

}
