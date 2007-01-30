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
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Instances of this class provide a description of a particular event that
 * occurred in a binding. It is passed to
 * {@link IBindingListener#handleBindingEvent(BindingEvent)}.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * @since 1.0
 */
public class BindingEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8410698137884587257L;

	/**
	 * The binding object from which this event originated.
	 */
	public final Binding binding;

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
	 * Holds the value that was retrieved from the source observable. Setting
	 * the value of this field changes the value that will be processed by all
	 * subsequent steps in the data flow pipeline.
	 */
	public Object originalValue = null;

	/**
	 * Holds the value that will be copied into the final observable. This value
	 * is null if the original value has not been converted into the final
	 * observable's data type or if no conversion will be performed. Setting the
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
	 * A constant indication that this event is occurring immediately after a
	 * value changing event fired of an observable.
	 */
	public static final int PIPELINE_VALUE_CHANGING = 1;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * value to copy has been gotten from its observable.
	 */
	public static final int PIPELINE_AFTER_GET = 2;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * original value has been converted to the other observable's data type.
	 */
	public static final int PIPELINE_AFTER_CONVERT = 3;

	/**
	 * A constant indicating that this event is occurring immediately before the
	 * converted value has been set/changed on the observable.
	 */
	public static final int PIPELINE_BEFORE_CHANGE = 4;

	/**
	 * A constant indicating that this event is occuring immedately after the
	 * converted value has been set/changed on the observable.
	 */
	public static final int PIPELINE_AFTER_CHANGE = 5;

	/**
	 * A Map of Integer --> String mapping the integer constants for the
	 * pipeline events defined in this class to their String symbols. Can be
	 * used for debugging purposes.
	 */
	public static final Map PIPELINE_CONSTANTS;
	static {
		Map constants = new HashMap();
		constants.put(new Integer(BindingEvent.PIPELINE_VALUE_CHANGING),
				"PIPELINE_VALUE_CHANGING"); //$NON-NLS-1$
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

	/**
	 * A Map of Integer --> String mapping the integer constants for the event
	 * constants defined in this class to their String symbols. Can be used for
	 * debugging purposes.
	 */
	public static final Map EVENT_CONSTANTS;
	static {
		Map constants = new HashMap();
		constants.put(new Integer(BindingEvent.EVENT_COPY_TO_TARGET),
				"EVENT_COPY_TO_TARGET"); //$NON-NLS-1$
		constants.put(new Integer(BindingEvent.EVENT_COPY_TO_MODEL),
				"EVENT_COPY_TO_MODEL"); //$NON-NLS-1$
		EVENT_CONSTANTS = Collections.unmodifiableMap(constants);
	}

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
	/* package */BindingEvent(Binding binding, IDiff diff, int copyType,
			int pipelinePosition) {
		super(binding);
		this.binding = binding;
		this.diff = diff;
		this.copyType = copyType;
		this.pipelinePosition = pipelinePosition;
	}

	/**
	 * Returns a string representation of this event for debugging purposes. The
	 * format of the string representation is not guaranteed to remain the same.
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(EVENT_CONSTANTS.get(new Integer(copyType))
				+ ": Diff(" + diff + "): "); //$NON-NLS-1$ //$NON-NLS-2$
		result.append("("); //$NON-NLS-1$
		result.append(PIPELINE_CONSTANTS.get(new Integer(pipelinePosition)));
		result.append(")"); //$NON-NLS-1$
		return result.toString();
	}

}
