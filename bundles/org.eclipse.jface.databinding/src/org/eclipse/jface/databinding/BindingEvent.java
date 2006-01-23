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

package org.eclipse.jface.databinding;

/**
 * The event that is passed to a #bindingEvent method of an IBindingListener.
 * 
 * @since 3.2
 */
public class BindingEvent {
	/**
	 * The type of copy that is occuring.  One of the COPY_TO_* constants.
	 */
	public int copyType;
	
	/**
	 * The position in the processing pipeline where this event is occuring.
	 * One of the PIPELINE_* constants.
	 */
	public int pipelinePosition;
	
	/**
	 * Holds the value that was retrieved from the source updatable.  Setting
	 * the value of this field changes the value that will be processed by
	 * all subsequent steps in the data flow pipeline.
	 */
	public Object originalValue = null;
	
	/**
	 * Holds the value that will be copied into the final updatable.  This
	 * value is null if the original value has not been converted into the
	 * final updatable's data type yet.    Setting the value of this field 
	 * changes the value that will be processed by all subsequent steps in 
	 * the data flow pipeline.
	 */
	public Object convertedValue = null;
	
	/**
	 * A constant indicating that this event is occuring during a copy from
	 * model to target. 
	 */
	public static final int COPY_TO_TARGET = 0;
	/**
	 * A constant indicating that this event is occuring during a copy from
	 * target to model. 
	 */
	public static final int COPY_TO_MODEL = 1;
	
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
	 * converted value has been set on the updatable.
	 */
	public static final int PIPELINE_AFTER_SET = 4;
}
