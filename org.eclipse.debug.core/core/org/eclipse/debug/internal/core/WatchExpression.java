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

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;

/**
 * Base watch expression implementation.
 * <p>
 * Clients may extend this class.
 * </p>
 * @since 3.0
 */
public class WatchExpression implements IWatchExpression {
	
	protected String fExpressionText;
	protected IWatchExpressionResult fResult;
	protected IDebugTarget fDebugTarget;
	private boolean fEnabled= true;
	private boolean fObsolete= false;
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
	 * @see org.eclipse.debug.core.model.IWatchExpression#setExpressionContext(java.lang.Object)
	 */
	public void setExpressionContext(IDebugElement context) {
		setObsolete(false);
		if (context == null) {
			fResult= null;
			return;
		}
		if (!isEnabled()) {
			if (fResult != null) {
				setObsolete(true);
			}
			return;
		}
		
		fDebugTarget= context.getDebugTarget();
			
		IWatchExpressionListener listener= new IWatchExpressionListener() {
			/**
			 * @see org.eclipse.debug.core.model.IWatchExpressionListener#watchEvaluationFinished(org.eclipse.debug.core.model.IValue)
			 */
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				fResult= result;
				setPending(false);
			}
		};
		setPending(true);
		IWatchExpressionDelegate delegate= DebugPlugin.getDefault().getExpressionManager().getWatchExpressionDelegate(context.getModelIdentifier());
		delegate.evaluateExpression(getExpressionText(), context, listener);
	}

	/**
	 * Set a change debug event, with this as the source, to notified that the
	 * status/content of the expression has changed.
	 */
	public void fireChanged() {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(this, DebugEvent.CHANGE)});
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
	public IValue getValue() {
		if (fResult == null) {
			return null;
		}
		return fResult.getValue();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
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
		return DebugPlugin.getUniqueIdentifier();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		fEnabled= enabled;
		fireChanged();
	}

	/**
	 * @param expression
	 */
	public void setExpressionText(String expression) {
		fExpressionText= expression;
		fireChanged();
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
	public boolean isPending() {
		return fPending;
	}
	
	/**
	 * Sets the pending state of this expression.
	 * 
	 * @param pending whether or not this expression should be
	 * 		flagged as pending
	 */
	protected void setPending(boolean pending) {
		fPending= pending;
		fireChanged();
	}

	/**
	 * @see org.eclipse.debug.core.model.IWatchExpression#hasError()
	 */
	public boolean hasErrors() {
		return fResult != null && fResult.hasErrors();
	}
	
	public String[] getErrorMessages() {
		if (fResult == null) {
			return new String[0];
		}
		return fResult.getErrorMessages();
	}

	/**
	 * @see org.eclipse.debug.core.model.IWatchExpression#isObsolete()
	 */
	public boolean isObsolete() {
		return fObsolete;
	}
	
	/**
	 * Sets the obsolete state of this expression.
	 * 
	 * @param obsolete whether or not this expression should be
	 * 		flagged as obsolete
	 */
	protected void setObsolete(boolean obsolete) {
		fObsolete= obsolete;
		fireChanged();
	}

}
