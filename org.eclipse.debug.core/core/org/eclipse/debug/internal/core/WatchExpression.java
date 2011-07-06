/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;

/**
 * Base watch expression implementation.
 * 
 * @since 3.0
 */
public class WatchExpression implements IWatchExpression {
	
	protected String fExpressionText;
	protected IWatchExpressionResult fResult;
	protected IDebugElement fCurrentContext;
	private boolean fEnabled= true;
	private boolean fPending= false;
	
	/**
	 * Creates a new watch expression with the given expression
	 * text.
	 * @param expression the text of the expression to be evaluated.
	 */
	public WatchExpression(String expression) {
		fExpressionText= expression;
	}

	/**
	 * Creates a new watch expression with the given expression
	 * and the given enablement;
	 * 
	 * @param expressionText the text of the expression to be evaluated
	 * @param enabled whether or not the new expression should be enabled
	 */
	public WatchExpression(String expressionText, boolean enabled) {
		this(expressionText);
		fEnabled= enabled;
	}
	
	/**
	 * @see org.eclipse.debug.core.model.IWatchExpression#evaluate()
	 */
	public void evaluate() {
		IDebugElement context= fCurrentContext;
		if (context == null) {
			return;
		}
			
		IWatchExpressionListener listener= new IWatchExpressionListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.debug.core.model.IWatchExpressionListener#watchEvaluationFinished(org.eclipse.debug.core.model.IWatchExpressionResult)
			 */
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				setResult(result);
			}
		};
		setPending(true);
		IWatchExpressionDelegate delegate= DebugPlugin.getDefault().getExpressionManager().newWatchExpressionDelegate(context.getModelIdentifier());
		if (delegate != null) {
			delegate.evaluateExpression(getExpressionText(), context, listener);
		} else {
			// No delegate provided
			listener.watchEvaluationFinished(new IWatchExpressionResult() {
				public IValue getValue() {
					return null;
				}
				public boolean hasErrors() {
					return true;
				}
				public String[] getErrorMessages() {
					return new String[] { DebugCoreMessages.WatchExpression_0 }; 
				}
				public String getExpressionText() {
					return WatchExpression.this.getExpressionText();
				}
				public DebugException getException() {
					return null;
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IWatchExpression#setExpressionContext(org.eclipse.debug.core.model.IDebugElement)
	 */
	public void setExpressionContext(IDebugElement context) {
		synchronized (this) {
			fCurrentContext= context;
		}
		if (context == null) {
			setResult(null);
			return;
		}
		if (!isEnabled()) {
			return;
		}
		
		evaluate();
	}

	/**
	 * Sets the result of the last expression and fires notification that
	 * this expression's value has changed.
	 * 
	 * @param result result of a watch expression
	 */
	public void setResult(IWatchExpressionResult result) {
		synchronized (this) {
			fResult= result;
			fPending = false;
		}
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.STATE)); // pending state
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT)); // value change
	}

	/**
	 * Fires the given debug event
	 * @param event the {@link DebugEvent}
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event});
	}
	
	/**
	 * Notifies the expression manager that this watch expression's
	 * values have changed so the manager can update the
	 * persisted expression.
	 */
	private void watchExpressionChanged() {
		((ExpressionManager)DebugPlugin.getDefault().getExpressionManager()).watchExpressionChanged(this);
	}

	/**
	 * @see org.eclipse.debug.core.model.IExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpressionText;
	}

	/**
	 * @see org.eclipse.debug.core.model.IExpression#getValue()
	 */
	public synchronized IValue getValue() {
		if (fResult == null) {
			return null;
		}
		return fResult.getValue();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		IDebugElement element = fCurrentContext;
		if (element != null) {
			return element.getDebugTarget();
		}
		return null;
	}

	/**
	 * @see org.eclipse.debug.core.model.IExpression#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		synchronized (this) {
			IValue value = getValue();
			if (value != null) {
				return value.getModelIdentifier();
			}
			if (fCurrentContext != null) {
				return fCurrentContext.getModelIdentifier();
			}			
		}
		return DebugPlugin.getUniqueIdentifier();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		IDebugTarget debugTarget = getDebugTarget();
		if (debugTarget != null) {
		    return debugTarget.getLaunch();
		}
		return null; 
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		//CONTEXTLAUNCHING
		if(adapter.equals(ILaunchConfiguration.class)) {
			ILaunch launch = getLaunch();
			if(launch != null) {
				return launch.getLaunchConfiguration();
			}
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * Set the enabled state of the {@link WatchExpression}
	 * 
	 * @param enabled the new enabled state
	 */
	public void setEnabled(boolean enabled) {
		fEnabled= enabled;
		watchExpressionChanged();
		evaluate();
	}

	/**
	 * Set the text of the expression
	 * @param expression the new expression text
	 */
	public void setExpressionText(String expression) {
		fExpressionText= expression;
		watchExpressionChanged();
		evaluate();
	}

	/**
	 * @return Whether or not this watch expression is currently enabled.
	 * 		Enabled watch expressions will continue to update their value
	 * 		automatically. Disabled expressions require a manual update.
	 */
	public boolean isEnabled() {
		return fEnabled;
	}

	/**
	 * @see org.eclipse.debug.core.model.IWatchExpression#isPending()
	 */
	public synchronized boolean isPending() {
		return fPending;
	}
	
	/**
	 * Sets the pending state of this expression.
	 * 
	 * @param pending whether or not this expression should be
	 * 		flagged as pending
	 */
	protected void setPending(boolean pending) {
		synchronized (this) {
			fPending= pending;
		}
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.STATE));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IErrorReportingExpression#hasErrors()
	 */
	public boolean hasErrors() {
		return fResult != null && fResult.hasErrors();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IErrorReportingExpression#getErrorMessages()
	 */
	public String[] getErrorMessages() {
		if (fResult == null) {
			return new String[0];
		}
		return fResult.getErrorMessages();
	}

}
