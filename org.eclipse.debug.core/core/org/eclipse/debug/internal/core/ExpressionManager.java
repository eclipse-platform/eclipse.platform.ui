/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;

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
	private Vector fExpressions = null;
	
	/**
	 * List of expression listeners
	 */
	private ListenerList fListeners = null;
	
	/**
	 * List of (multi) expressions listeners
	 */
	private ListenerList fExpressionsListeners = null;
	
	private Map fWatchExpressionDelegates= new HashMap();	
	
	// Constants for add/remove/change notification
	private static final int ADDED = 1;
	private static final int CHANGED = 2;
	private static final int REMOVED = 3;
	
	public ExpressionManager() {
		loadPersistedExpressions();
		loadWatchExpressionDelegates();
	}
	
	/**
	 * Loads the mapping of debug models to watch expression delegates
	 * from the org.eclipse.debug.core.watchExpressionDelegates
	 * extension point.
	 */
	private void loadWatchExpressionDelegates() {
		IExtensionPoint extensionPoint = Platform.getPluginRegistry().getExtensionPoint("org.eclipse.debug.core.watchExpressionDelegates"); //$NON-NLS-1$
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			if (element.getName().equals("watchExpressionDelegate")) { //$NON-NLS-1$
				String debugModel = element.getAttribute("debugModel"); //$NON-NLS-1$
				if (debugModel == null || debugModel.length() == 0) {
					continue;
				}
				try {
					IWatchExpressionDelegate delegate = (IWatchExpressionDelegate) element.createExecutableExtension("delegateClass"); //$NON-NLS-1$
					fWatchExpressionDelegates.put(debugModel, delegate);
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
		}
	}
	
	/**
	 * @see IExpressionManager#getWatchExpressionDelegate(String)
	 */
	public IWatchExpressionDelegate getWatchExpressionDelegate(String debugModel) {
		return (IWatchExpressionDelegate) fWatchExpressionDelegates.get(debugModel);
	}

	/**
	 * TODO: Implement watch expression persistance
	 */
	private void loadPersistedExpressions() {
	}

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
		if (fExpressions == null) {
			fExpressions = new Vector(expressions.length);
		}
		boolean wasEmpty = fExpressions.isEmpty();
		List added = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (fExpressions.indexOf(expression) == -1) {
				added.add(expression);
				fExpressions.add(expression);
			}				
		}
		if (wasEmpty) {
			DebugPlugin.getDefault().addDebugEventListener(this);	
		}
		if (!added.isEmpty()) {
			fireUpdate((IExpression[])added.toArray(new IExpression[added.size()]), ADDED);
		}
	}	

	/**
	 * @see IExpressionManager#getExpressions()
	 */
	public IExpression[] getExpressions() {
		if (fExpressions == null) {
			return new IExpression[0];
		}
		IExpression[] temp= new IExpression[fExpressions.size()];
		fExpressions.copyInto(temp);
		return temp;
	}

	/**
	 * @see IExpressionManager#getExpressions(String)
	 */
	public IExpression[] getExpressions(String modelIdentifier) {
		if (fExpressions == null) {
			return new IExpression[0];
		}
		ArrayList temp= new ArrayList(fExpressions.size());
		Iterator iter= fExpressions.iterator();
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
		if (fExpressions == null) {
			return;
		}
		List removed = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (fExpressions.remove(expression)) {
				removed.add(expression);
				expression.dispose();
			}				
		}
		if (fExpressions.isEmpty()) {
			DebugPlugin.getDefault().removeDebugEventListener(this);
		}
		if (!removed.isEmpty()) {
			fireUpdate((IExpression[])removed.toArray(new IExpression[removed.size()]), REMOVED);
		}
	}	
	
	/**
	 * @see IExpressionManager#addExpressionListener(IExpressionListener)
	 */
	public void addExpressionListener(IExpressionListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList(2);
		}
		fListeners.add(listener);
	}

	/**
	 * @see IExpressionManager#removeExpressionListener(IExpressionListener)
	 */
	public void removeExpressionListener(IExpressionListener listener) {
		if (fListeners == null) {
			return;
		}
		fListeners.remove(listener);
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
		getExpressionNotifier().notify(expressions, update);
		
		// multi listeners
		getExpressionsNotifier().notify(expressions, update);
	}	

	/**
	 * @see IExpressionManager#hasExpressions()
	 */
	public boolean hasExpressions() {
		return fExpressions != null;
	}

	/**
	 * @see org.eclipse.debug.core.IExpressionManager#addExpressionListener(org.eclipse.debug.core.IExpressionsListener)
	 */
	public void addExpressionListener(IExpressionsListener listener) {
		if (fExpressionsListeners == null) {
			fExpressionsListeners = new ListenerList(2);
		}
		fExpressionsListeners.add(listener);
	}

	/**
	 * @see org.eclipse.debug.core.IExpressionManager#removeExpressionListener(org.eclipse.debug.core.IExpressionsListener)
	 */
	public void removeExpressionListener(IExpressionsListener listener) {
		if (fExpressionsListeners == null) {
			return;
		}
		fExpressionsListeners.remove(listener);
	}
	
	private ExpressionNotifier getExpressionNotifier() {
		return new ExpressionNotifier();
	}
	
	/**
	 * Notifies an expression listener (single expression) in a safe runnable to
	 * handle exceptions.
	 */
	class ExpressionNotifier implements ISafeRunnable {
		
		private IExpressionListener fListener;
		private int fType;
		private IExpression fExpression;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, DebugCoreMessages.getString("ExpressionManager.An_exception_occurred_during_expression_change_notification._1"), exception); //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.expressionAdded(fExpression);
					break;
				case REMOVED:
					fListener.expressionRemoved(fExpression);
					break;
				case CHANGED:
					fListener.expressionChanged(fExpression);		
					break;
			}			
		}

		/**
		 * Notifies listeners of the add/change/remove
		 * 
		 * @param expression the expression that has changed
		 * @param update the type of change
		 */
		public void notify(IExpression[] expressions, int update) {
			if (fListeners != null) {
				fType = update;
				Object[] copiedListeners= fListeners.getListeners();
				for (int i= 0; i < copiedListeners.length; i++) {
					fListener = (IExpressionListener)copiedListeners[i];
					for (int j = 0; j < expressions.length; j++) {
						fExpression = expressions[j];
						Platform.run(this);
					}
				}			
			}
			fListener = null;
			fExpression = null;
		}
	}
	
	private ExpressionsNotifier getExpressionsNotifier() {
		return new ExpressionsNotifier();
	}
	
	/**
	 * Notifies an expression listener (multiple expressions) in a safe runnable
	 * to handle exceptions.
	 */
	class ExpressionsNotifier implements ISafeRunnable {
		
		private IExpressionsListener fListener;
		private int fType;
		private IExpression[] fExpressions;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, DebugCoreMessages.getString("ExpressionManager.An_exception_occurred_during_expression_change_notification._1"), exception); //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.expressionsAdded(fExpressions);
					break;
				case REMOVED:
					fListener.expressionsRemoved(fExpressions);
					break;
				case CHANGED:
					fListener.expressionsChanged(fExpressions);		
					break;
			}			
		}

		/**
		 * Notifies listeners of the adds/changes/removes
		 * 
		 * @param expressions the expressions that changed
		 * @param update the type of change
		 */
		public void notify(IExpression[] expressions, int update) {
			if (fExpressionsListeners != null) { 
				fExpressions = expressions;
				fType = update;
				Object[] copiedListeners = fExpressionsListeners.getListeners();
				for (int i= 0; i < copiedListeners.length; i++) {
					fListener = (IExpressionsListener)copiedListeners[i];
					Platform.run(this);
				}
			}	
			fExpressions = null;
			fListener = null;				
		}
	}		

}
