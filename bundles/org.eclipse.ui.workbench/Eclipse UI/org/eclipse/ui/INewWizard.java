/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Interface for creation wizards.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in a wizard contributed to the workbench's creation wizard extension point
 * (named <code>"org.eclipse.ui.newWizards"</code>). For example, the plug-in's
 * XML markup might contain:
 * </p>
 * 
 * <pre>
 * &lt;extension point="org.eclipse.ui.newWizards"&gt;
 *   &lt;wizard
 *       id="com.example.myplugin.new.blob"
 *       name="Blob"
 *       class="com.example.myplugin.BlobCreator"
 *       icon="icons/new_blob_wiz.gif"&gt;
 *     &lt;description&gt;Create a new BLOB file&lt;/description&gt;
 *     &lt;selection class="org.eclipse.core.resources.IResource" /&gt;
 *   &lt;/wizard&gt;
 * &lt;/extension&gt;
 * </pre>
 *
 * @see org.eclipse.jface.wizard.IWizard
 */
public interface INewWizard extends IWorkbenchWizard {
}
