/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.Properties;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ui.IWorkbenchPage;

/**
 * Interface to handle various search scopes in the UI.
 */
public interface IScopeDescription {

	public String getLabel();

	public IResource[] getRoots(IWorkbenchPage page);

	public void restore(IDialogSettings section);

	public void restore(Properties props, String keyPrefix);

	public void store(IDialogSettings section);

	public void store(Properties props, String keyPrefix);

}
