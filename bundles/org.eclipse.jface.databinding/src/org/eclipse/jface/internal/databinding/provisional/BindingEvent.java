/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.internal.databinding.provisional.observable.IDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * The event that is passed to a #bindingEvent method of an IBindingListener.
 * This class is not intended to be subclassed by clients.
 * 
 * @since 1.0
 */
public class BindingEvent {
	/**
	 * (Non-API Method) Construct a BindingEvent.
	 * 
	 * @param model 
	 * @param target 
	 * @param diff
	 * @param copyType 
	 * @param pipelinePosition
	 *            The initial processing pipeline position.
	 */
	public BindingEvent(IObservable model, IObservable target, IDiff diff, int copyType,
			int pipelinePosition) {
		this.model = model;
		this.target = target;
		this.diff = diff;
		this.copyType = copyType;
		this.pipelinePosition = pipelinePosition;
		createSymbolTable();
	}

	/**
	 * The model observable for the change that is being processed.
	 */
	public final IObservable model;

	/**
	 * The target observable for the change that is being processed.
	 */
	public final IObservable target;
	
	/**
	 * The diff describing the change, or <code>null</code> if no diff is
	 * available.
	 */
	public final IDiff diff;

	/**
	 * The direction in which data is copied, either EVENT_COPY_TO_TARGET
	 * or EVENT_COPY_TO_MODEL.
	 */
	public final int copyType;

	/**
	 * The position in the processing pipeline where this event is occuring. One
	 * of the PIPELINE_* constants. The order in which these events occur may be
	 * version or implementation dependent. The contract is that these events
	 * will accurately reflect the internal processing that the data binding
	 * framework is currently performing.
	 * <p>
	 * Although this value is not declared final, changing it does not have any
	 * effect.
	 */
	public int pipelinePosition;
	
	/**
	 * The current ValidationError object (if there is one).
	 */
	public ValidationError validationError;

	/**
	 * Holds the value that was retrieved from the source updatable. Setting the
	 * value of this field changes the value that will be processed by all
	 * subsequent steps in the data flow pipeline.
	 */
	public Object originalValue = null;

	/**
	 * Holds the value that will be copied into the final updatable. This value
	 * is null if the original value has not been converted into the final
	 * updatable's data type or if no conversion will be performed. Setting the
	 * value of this field changes the value that will be processed by all
	 * subsequent steps in the data flow pipeline.
	 */
	public Object convertedValue = null;

	/**
	 * A constant indicating that this event is occuring during a copy from
	 * model to target.
	 */
	public static final int EVENT_COPY_TO_TARGET = 0;

	/**
	 * A constant indicating that this event is occuring during a copy from
	 * target to model.
	 */
	public static final int EVENT_COPY_TO_MODEL = 1;

	/**
	 * A constant indicating that this event is occuring during a partial
	 * validation event.
	 */
	public static final int EVENT_PARTIAL_VALIDATE = 2;

	/**
	 * A constant indicating that this event is occuring during an element
	 * remove operation.
	 */
	public static final int EVENT_REMOVE = 3;
	
	/**
	 * A constant indicating that this event is occuring during a lazy list
	 * insert operation.
	 */
	public static final int EVENT_LAZY_INSERT = 4;
	
	/**
	 * A constant indicating that this event is occuring during a lazy list
	 * delete operation.
	 */
	public static final int EVENT_LAZY_DELETE = 5;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * value to copy has been gotten from its IUpdatable.
	 */
	public static final int PIPELINE_AFTER_GET = 0;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * value has been validated as being possible to convert to the other
	 * updatable's data type.
	 */
	public static final int PIPELINE_AFTER_VALIDATE = 1;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * original value has been converted to the other updatable's data type.
	 */
	public static final int PIPELINE_AFTER_CONVERT = 2;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * business rule validation has occured.
	 */
	public static final int PIPELINE_AFTER_BUSINESS_VALIDATE = 3;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * converted value has been set/changed on the updatable.
	 */
	public static final int PIPELINE_AFTER_CHANGE = 4;
	
	/**
	 * A constant indicating that this event is occuring due to either a validation
	 * error or warning occuring.
	 */
	public static final int PIPELINE_VALIDATION_ERROR_OR_WARNING = 5;

	/**
	 * A Map of Integer --> String mapping the integer constants for the
	 * pipeline events defined in this class to their String symbols.
	 */
	public final Map pipelineConstants = new HashMap();
	private HashMap eventConstants = new HashMap();

	/**
	 * Creates a table of constants from this class.
	 */
	private void createSymbolTable() {
		eventConstants.put(new Integer(0), "EVENT_COPY_TO_TARGET"); //$NON-NLS-1$
		eventConstants.put(new Integer(1), "EVENT_COPY_TO_MODEL"); //$NON-NLS-1$
		eventConstants.put(new Integer(2), "EVENT_PARTIAL_VALIDATE"); //$NON-NLS-1$
		eventConstants.put(new Integer(3), "EVENT_REMOVE"); //$NON-NLS-1$

		pipelineConstants.put(new Integer(0), "PIPELINE_AFTER_GET"); //$NON-NLS-1$
		pipelineConstants.put(new Integer(1), "PIPELINE_AFTER_VALIDATE"); //$NON-NLS-1$
		pipelineConstants.put(new Integer(2), "PIPELINE_AFTER_CONVERT"); //$NON-NLS-1$
		pipelineConstants.put(new Integer(3),
				"PIPELINE_AFTER_BUSINESS_VALIDATE"); //$NON-NLS-1$
		pipelineConstants.put(new Integer(4), "PIPELINE_AFTER_CHANGE"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("(" + eventConstants.get(new Integer(copyType)) + ", "); //$NON-NLS-1$ //$NON-NLS-2$
		result.append(pipelineConstants.get(new Integer(pipelinePosition)));
		result.append("): Diff(" + diff + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		return result.toString();
	}

}
