/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - bug 227877
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.contextlaunching.ContextRunner;

/**
 * Re-launches the last launch.
 * 
 * @see ContextRunner
 * @see ILaunchConfiguration
 * @see RunLastAction
 * @see DebugLastAction
 * @see ProfileLastAction
 * @since 3.8
 * @deprecated This class has been promoted to API as <code>org.eclipse.debug.ui.actions.RelaunchLastAction</code>. Clients
 * should discontinue use of this class and use the new API version.
 */
public abstract class RelaunchLastAction extends org.eclipse.debug.ui.actions.RelaunchLastAction {

}
