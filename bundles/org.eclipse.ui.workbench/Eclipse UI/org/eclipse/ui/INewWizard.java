package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Interface for creation wizards.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in a wizard contributed to the workbench's creation wizard extension point 
 * (named <code>"org.eclipse.ui.newWizards"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.newWizards"&GT;
 *   &LT;wizard
 *       id="com.example.myplugin.new.blob"
 *       name="Blob"
 *       class="com.example.myplugin.BlobCreator"
 *       icon="icons/new_blob_wiz.gif"&GT;
 *     &LT;description&GT;Create a new BLOB file&LT;/description&GT;
 *     &LT;selection class="org.eclipse.core.resources.IResource" /&GT; 
 *   &LT;/wizard&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p>
 *
 * @see org.eclipse.jface.wizard.IWizard
 */
public interface INewWizard extends IWorkbenchWizard {
}
