/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.commands;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.IDisconnectCommand;
import org.eclipse.debug.core.commands.IDropToFrameCommand;
import org.eclipse.debug.core.commands.IResumeCommand;
import org.eclipse.debug.core.commands.IStepFiltersCommand;
import org.eclipse.debug.core.commands.IStepIntoCommand;
import org.eclipse.debug.core.commands.IStepOverCommand;
import org.eclipse.debug.core.commands.IStepReturnCommand;
import org.eclipse.debug.core.commands.ISuspendCommand;
import org.eclipse.debug.core.commands.ITerminateCommand;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IDropToFrame;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;

/**
 * Adapter factory for debug commands.
 * 
 * @since 3.3
 *
 */
public class CommandAdapterFactory implements IAdapterFactory {
	
	
	private static ITerminateCommand fgTerminateCommand = new TerminateCommand();
	private static IStepOverCommand fgStepOverCommand = new StepOverCommand();
	private static IStepIntoCommand fgStepIntoCommand = new StepIntoCommand();
	private static IStepReturnCommand fgStepReturnCommand = new StepReturnCommand();
	private static IDropToFrameCommand fgDropToFrameCommand = new DropToFrameCommand();
	private static IDisconnectCommand fgDisconnectCommand = new DisconnectCommand();
	private static ISuspendCommand fgSuspendCommand = new SuspendCommand();
	private static IResumeCommand fgResumeCommand = new ResumeCommand();
	private static IStepFiltersCommand fgStepFiltersCommand = new StepFiltersCommand();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IStepFiltersCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof IDebugElement ||
				adaptableObject instanceof ILaunch || 
				adaptableObject instanceof IProcess) {
				return fgStepFiltersCommand;
			}
		}
		
		if (ITerminateCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof ITerminate) {
				return fgTerminateCommand;
			}
		}
		if (IStepOverCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof IStep) {
				return fgStepOverCommand;
			}
		}
		if (IStepIntoCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof IStep) {
				return fgStepIntoCommand;
			}
		}
		if (IStepReturnCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof IStep) {
				return fgStepReturnCommand;
			}
		}
		if (ISuspendCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof ISuspendResume) {
				return fgSuspendCommand;
			}
		}
		if (IResumeCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof ISuspendResume) {
				return fgResumeCommand;
			}
		}
		if (IDisconnectCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof IDisconnect) {
				return fgDisconnectCommand;
			}
		}
		if (IDropToFrameCommand.class.equals(adapterType)) {
			if (adaptableObject instanceof IDropToFrame) {
				return fgDropToFrameCommand;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{
				ITerminateCommand.class,
				IStepOverCommand.class,
				IStepIntoCommand.class,
				IStepReturnCommand.class,
				ISuspendCommand.class,
				IResumeCommand.class,
				IDropToFrameCommand.class,
				IDisconnectCommand.class,
				IStepFiltersCommand.class};
	}

}
