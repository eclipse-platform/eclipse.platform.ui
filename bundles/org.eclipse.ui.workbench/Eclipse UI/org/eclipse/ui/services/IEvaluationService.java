/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.services;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.ISources;

/**
 * Evaluate a core expression against the workbench application context and
 * report updates using a Boolean property. Clients supply an
 * <code>IPropertyChangeListener</code> that will be notified as changes
 * occur.
 * <p>
 * This can be used to implement core expressions in client extension points
 * similar to the &lt;enabledWhen&gt; of
 * <code>org.eclipse.ui.handlers/handler</code> elements.
 * </p>
 * <p>
 * The service will fire <code>Boolean.TRUE</code> and
 * <code>Boolean.FALSE</code> for the oldValue and newValue in the property
 * change events.
 * </p>
 * <p>
 * Adding the evaluation listener will fire one change with oldValue=<code>null</code>
 * and newValue=&quot;evaluated expression&quot;. Remove the
 * <code>IEvaluationReference</code> will fire one change with
 * oldValue=&quot;last evaluated value&quot; and newValue=<code>null</code>.
 * </p>
 * <p>
 * Adding a service listener will fire the {@link #PROP_NOTIFYING} property
 * change event with newValue=<code>Boolean.TRUE</code> when a source change
 * causes expression evaluations to update and another {@link #PROP_NOTIFYING}
 * property change event with newValue=<code>Boolean.FALSE</code> when the
 * changes that started with a specific source change have finished. The
 * {@link #PROP_NOTIFYING} change events will not be fired for source changes
 * caused by the outer most recalculations.
 * </p>
 * <p>
 * Note: Clients should not extend or implement this interface.
 * </p>
 * 
 * @since 3.4
 */
public interface IEvaluationService extends IServiceWithSources {
	/**
	 * A general property that can be used.
	 */
	public static final String RESULT = "org.eclipse.ui.services.result"; //$NON-NLS-1$

	/**
	 * The property used to notify any service listeners.
	 */
	public static final String PROP_NOTIFYING = "org.eclipse.ui.services.notifying"; //$NON-NLS-1$

	/**
	 * When a source change starts recalculating expressions the
	 * {@link #PROP_NOTIFYING} property change is fired with the newValue=<code>Boolean.TRUE</code>.
	 * This property is not fired for any source changes caused by the outer
	 * recalculations.
	 * 
	 * @param listener
	 *            The listener to be notified. Must not be <code>null</code>.
	 *            Has no effect if the listener has already been added.
	 */
	public void addServiceListener(IPropertyChangeListener listener);

	/**
	 * Remove the listener for {@link #PROP_NOTIFYING} property changes.
	 * 
	 * @param listener
	 *            The listener to remove. Must not be <code>null</code>. Has
	 *            no effect if the listener is not currently registered.
	 */
	public void removeServiceListener(IPropertyChangeListener listener);

	/**
	 * Add a listener that can be notified when the workbench application
	 * context causes the expression evaluation value to change.
	 * 
	 * @param expression
	 *            the core expression to evaluate.
	 * @param listener
	 *            the listener to be notified.
	 * @param property
	 *            the property contained in the notification
	 * @return a token that can be used to remove this listener.
	 */
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property);

	/**
	 * Re-add a property change listener that has already been removed by
	 * {@link #removeEvaluationListener(IEvaluationReference)}.
	 * <p>
	 * It will only accept IEvaluationReferences returned from a previous call
	 * to
	 * {@link #addEvaluationListener(Expression, IPropertyChangeListener, String)}
	 * on this service.
	 * </p>
	 * 
	 * @param ref
	 *            The listener to re-add.
	 */
	public void addEvaluationReference(IEvaluationReference ref);

	/**
	 * Remove the listener represented by the evaluation reference.
	 * 
	 * @param ref
	 *            the reference to be removed.
	 */
	public void removeEvaluationListener(IEvaluationReference ref);

	/**
	 * Get an IEvaluationContext that contains the current state of the
	 * workbench application context. This context changes with the application
	 * state, but becomes invalid when the global current selection changes.
	 * <p>
	 * Note: This context should not be modified.
	 * </p>
	 * 
	 * @return the latest context.
	 * @see ISources#ACTIVE_CURRENT_SELECTION_NAME
	 */
	public IEvaluationContext getCurrentState();
}
