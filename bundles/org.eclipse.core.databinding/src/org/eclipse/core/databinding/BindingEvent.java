/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159768
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

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
	public BindingEvent(IObservable model, IObservable target, IDiff diff,
			int copyType, int pipelinePosition) {
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
	 * The direction in which data is copied, either EVENT_COPY_TO_TARGET or
	 * EVENT_COPY_TO_MODEL.
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
	 * The current validation status (if there is one).
	 */
	public IStatus validationStatus = Status.OK_STATUS;

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
	 * original value has been converted to the other observable's data type.
	 */
	public static final int PIPELINE_AFTER_CONVERT = 1;

	/**
	 * A constant indicating that this event is occurring immediately before the
	 * converted value has been set/changed on the observable.
	 */
	public static final int PIPELINE_BEFORE_CHANGE = 2;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * converted value has been set/changed on the updatable.
	 */
	public static final int PIPELINE_AFTER_CHANGE = 3;

	/**
	 * A Map of Integer --> String mapping the integer constants for the
	 * pipeline events defined in this class to their String symbols.
	 */
	public static final Map PIPELINE_CONSTANTS;
	static {
		Map constants = new LinkedHashMap();
		constants.put(new Integer(BindingEvent.PIPELINE_AFTER_GET),
				"PIPELINE_AFTER_GET"); //$NON-NLS-1$
		constants.put(new Integer(BindingEvent.PIPELINE_AFTER_CONVERT),
				"PIPELINE_AFTER_CONVERT"); //$NON-NLS-1$
		constants.put(new Integer(BindingEvent.PIPELINE_BEFORE_CHANGE),
				"PIPELINE_BEFORE_CHANGE"); //$NON-NLS-1$
		constants.put(new Integer(BindingEvent.PIPELINE_AFTER_CHANGE),
				"PIPELINE_AFTER_CHANGE"); //$NON-NLS-1$
		PIPELINE_CONSTANTS = Collections.unmodifiableMap(constants);
	}
	private HashMap eventConstants = new HashMap();

	/**
	 * Creates a table of constants from this class.
	 */
	private void createSymbolTable() {
		eventConstants.put(new Integer(BindingEvent.EVENT_COPY_TO_TARGET),
				"EVENT_COPY_TO_TARGET"); //$NON-NLS-1$
		eventConstants.put(new Integer(BindingEvent.EVENT_COPY_TO_MODEL),
				"EVENT_COPY_TO_MODEL"); //$NON-NLS-1$
		eventConstants.put(new Integer(BindingEvent.EVENT_PARTIAL_VALIDATE),
				"EVENT_PARTIAL_VALIDATE"); //$NON-NLS-1$
		eventConstants.put(new Integer(BindingEvent.EVENT_REMOVE),
				"EVENT_REMOVE"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(eventConstants.get(new Integer(copyType))
				+ ": Diff(" + diff + "): "); //$NON-NLS-1$ //$NON-NLS-2$
		result.append("("); //$NON-NLS-1$
		result.append(PIPELINE_CONSTANTS.get(new Integer(pipelinePosition)));
		result.append(")"); //$NON-NLS-1$
		return result.toString();
	}

}
