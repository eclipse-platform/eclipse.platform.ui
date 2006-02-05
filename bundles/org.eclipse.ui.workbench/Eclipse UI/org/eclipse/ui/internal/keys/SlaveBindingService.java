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

package org.eclipse.ui.internal.keys;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.keys.IBindingService;

/**
 * A binding service which delegates almost all responsibility to the parent
 * service.
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class SlaveBindingService implements IBindingService, INestable {
	private IBindingService fParentService;

	private boolean fParentEnabledFlag = false;

	private boolean fLocalEnabledFlag = false;

	private boolean fLocalEnabledFlagSet = false;

	/**
	 * Build the slave binding service.
	 * 
	 * @param parent
	 *            the parent binding service. This service must not be
	 *            <code>null</code>.
	 */
	public SlaveBindingService(IBindingService parent) {
		if (parent == null) {
			throw new NullPointerException(
					"The parent command service must not be null"); //$NON-NLS-1$
		}
		fParentService = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getActiveBindingsFor(org.eclipse.core.commands.ParameterizedCommand)
	 */
	public TriggerSequence[] getActiveBindingsFor(
			ParameterizedCommand parameterizedCommand) {
		return fParentService.getActiveBindingsFor(parameterizedCommand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getActiveBindingsFor(java.lang.String)
	 */
	public TriggerSequence[] getActiveBindingsFor(String commandId) {
		return fParentService.getActiveBindingsFor(commandId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getActiveScheme()
	 */
	public Scheme getActiveScheme() {
		return fParentService.getActiveScheme();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBestActiveBindingFor(java.lang.String)
	 */
	public TriggerSequence getBestActiveBindingFor(String commandId) {
		return fParentService.getBestActiveBindingFor(commandId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBestActiveBindingFormattedFor(java.lang.String)
	 */
	public String getBestActiveBindingFormattedFor(String commandId) {
		return fParentService.getBestActiveBindingFormattedFor(commandId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBindings()
	 */
	public Binding[] getBindings() {
		return fParentService.getBindings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getBuffer()
	 */
	public TriggerSequence getBuffer() {
		return fParentService.getBuffer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getDefaultSchemeId()
	 */
	public String getDefaultSchemeId() {
		return fParentService.getDefaultSchemeId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getDefinedSchemes()
	 */
	public Scheme[] getDefinedSchemes() {
		return fParentService.getDefinedSchemes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getLocale()
	 */
	public String getLocale() {
		return fParentService.getLocale();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getPartialMatches(org.eclipse.jface.bindings.TriggerSequence)
	 */
	public Map getPartialMatches(TriggerSequence trigger) {
		return fParentService.getPartialMatches(trigger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getPerfectMatch(org.eclipse.jface.bindings.TriggerSequence)
	 */
	public Binding getPerfectMatch(TriggerSequence trigger) {
		return fParentService.getPerfectMatch(trigger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getPlatform()
	 */
	public String getPlatform() {
		return fParentService.getPlatform();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getScheme(java.lang.String)
	 */
	public Scheme getScheme(String schemeId) {
		return fParentService.getScheme(schemeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#isKeyFilterEnabled()
	 */
	public boolean isKeyFilterEnabled() {
		return fParentService.isKeyFilterEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#isPartialMatch(org.eclipse.jface.bindings.TriggerSequence)
	 */
	public boolean isPartialMatch(TriggerSequence trigger) {
		return fParentService.isPartialMatch(trigger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#isPerfectMatch(org.eclipse.jface.bindings.TriggerSequence)
	 */
	public boolean isPerfectMatch(TriggerSequence trigger) {
		return fParentService.isPerfectMatch(trigger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#openKeyAssistDialog()
	 */
	public void openKeyAssistDialog() {
		fParentService.openKeyAssistDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#readRegistryAndPreferences(org.eclipse.ui.commands.ICommandService)
	 */
	public void readRegistryAndPreferences(ICommandService commandService) {
		fParentService.readRegistryAndPreferences(commandService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#savePreferences(org.eclipse.jface.bindings.Scheme,
	 *      org.eclipse.jface.bindings.Binding[])
	 */
	public void savePreferences(Scheme activeScheme, Binding[] bindings)
			throws IOException {
		fParentService.savePreferences(activeScheme, bindings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#setKeyFilterEnabled(boolean)
	 */
	public void setKeyFilterEnabled(boolean enabled) {
		// this will try and remember the parent service flag
		// so it can be reset
		// TODO: Problem ... if the parent were programatically changed while 
		// this service is active, we'll have stale information
		fParentEnabledFlag = fParentService.isKeyFilterEnabled();
		fLocalEnabledFlagSet = true;
		fLocalEnabledFlag = enabled;
		fParentService.setKeyFilterEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		if (fLocalEnabledFlagSet) {
			fParentService.setKeyFilterEnabled(fParentEnabledFlag);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#activate()
	 */
	public void activate() {
		if (fLocalEnabledFlagSet) {
			fParentEnabledFlag = fParentService.isKeyFilterEnabled();
			fParentService.setKeyFilterEnabled(fLocalEnabledFlag);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#deactivate()
	 */
	public void deactivate() {
		if (fLocalEnabledFlagSet) {
			fParentService.setKeyFilterEnabled(fParentEnabledFlag);
		}
	}
}
