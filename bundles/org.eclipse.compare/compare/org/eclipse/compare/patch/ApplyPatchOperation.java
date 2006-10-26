package org.eclipse.compare.patch;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.patch.PatchWizard;
import org.eclipse.compare.internal.patch.PatchWizardDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;

/**
 * An operation that provides an interface to the Apply Patch Wizard. Users specify
 * the input in terms of an <code>IStorage</code> (note: input must be in unified diff
 * format), an <code>IResource</code> target to apply the patch to and can provide <code>CompareConfiguration</code>
 * elements to supply the label and images used on the preview page and hunk merge page. Finally, the
 * user can also supply a title and image to override the default ones provided by the Apply Patch Wizard.
 * Note that the Apply Patch Wizard does not require any particular set of inputs, and in the absence of
 * any user supplied values, it will work in default mode.
 * 
 * @since 3.3
 *
 */
public class ApplyPatchOperation implements Runnable {

	IWorkbenchPart part;
	
	/*
	 * The configurations to use for Compare UI elements
	 */
	/**
	 * Used for the Preview Patch page.
	 */
	private CompareConfiguration previewPatchConfiguration;
	
	/**
	 * Used for the Manual Hunk Merge page.
	 */
	private CompareConfiguration hunkMergeConfiguration;
	
	
	/**
	 * The patch to use as an input into the Apply Patch wizard
	 */
	private IStorage patch;
	
	/**
	 * Specific <code>IResource</code> target to patch.
	 */
	private IResource target;
	
	/**
	 * An optional image for the patch wizard
	 */
	private ImageDescriptor patchWizardImage;
	
	
	/**
	 * An optional title for the patchWizard
	 */
	private String patchWizardTitle;
	
	/**
	 * Creates a new ApplyPatchOperation with the supplied compare configuration, patch and target.
	 * The behaviour of the Apply Patch wizard is controlled by the number of parameters supplied:
	 * - If a patch is supplied, the initial input page is skipped. If a patch is not supplied the wizard
	 * will open on the input page.
	 * 
	 * - If the patch is a workspace patch, the target selection page is skipped and the preview page is 
	 * displayed.
	 * 
	 * - If the patch is not a workspace patch and the target is specified, then the target page is skipped
	 * and the preview page is displayed. If a target is not specified, the wizard will open on the target selection
	 * page.
	 * 
	 * - The user can specify two compare configurations to use in the Apply Patch wizard. The previewHunkConfiguration is
	 * used to provide UI elements on the PreviewPatchPage. The hunkMergeConfiguration is used to provide UI elements on the 
	 * Manual Hunk Merge Page. In the absence of either of these configurations, the defaults will be used.  
	 * 
	 * @param part 	an IWorkbenchPart 
	 * @param patch		an IStorage containing a patch in unified diff format or <code>null</code>
	 * @param target	an IResource which the patch is to be applied to or <code>null</code>
	 * @param previewPatchConfiguration	a CompareConfiguration supplying the labels and images for the preview patch page or <code>null</code>
	 * @param hunkMergeConfiguration 	a CompareConfiguration supplying the labels and images for the hunk merge page or <code>null</code>
	 */
	public ApplyPatchOperation(IWorkbenchPart part, IStorage patch, IResource target, CompareConfiguration previewPatchConfiguration, CompareConfiguration hunkMergeConfiguration) {
		Assert.isNotNull(part);
		this.part = part;
		this.patch = patch;
		this.target = target;
		this.previewPatchConfiguration = previewPatchConfiguration;
		this.hunkMergeConfiguration = hunkMergeConfiguration;
	}
	
	/**
	 * Create an operation for the given part and resource. This method is a convenience
	 * method that calls {@link #ApplyPatchOperation(IWorkbenchPart, IStorage, IResource, CompareConfiguration, CompareConfiguration)}
	 * with null for the other parameters.
	 * @param targetPart an IResource which the patch is to be applied to or <code>null</code>
	 * @param resource an IResource which the patch is to be applied to or <code>null</code>
	 * @see #ApplyPatchOperation(IWorkbenchPart, IStorage, IResource, CompareConfiguration, CompareConfiguration)
	 */
	public ApplyPatchOperation(IWorkbenchPart targetPart, IResource resource) {
		this(targetPart, null, resource, null, null);
	}

	/**
	 * Open the Apply Patch wizard using the values associated with this operation.
	 * This method must be called from the UI thread.
	 */
	public void openWizard() {
		
		saveAllEditors();
		
		PatchWizard wizard = new PatchWizard(patch, target, previewPatchConfiguration, hunkMergeConfiguration, patchWizardTitle, patchWizardImage);
		
		PatchWizardDialog dialog = new PatchWizardDialog(CompareUIPlugin.getShell(), wizard);
		wizard.setDialog(dialog);
		dialog.open();
	}
	
	/**
	 * This method will save all dirty editors. It will prompt the user if the Compare preference to save
	 * dirty editors before viewing a patch is <code>false</code>. Clients can use this or provide their own
	 * implementation.
	 */
	protected void saveAllEditors(){
		IDE.saveAllEditors(new IResource[]{ResourcesPlugin.getWorkspace().getRoot()}, !ComparePreferencePage.getSaveAllEditors());
	}
	
	/**
	 * Sets the title of the patch wizard. Needs to be set before {@link #openWizard()} is called.
	 * @param title	a string to display in the title bar
	 */
	public void setPatchWizardTitle(String title){
		this.patchWizardTitle = title;
	}
	
	/**
	 * Sets the image descriptor to use in the patch wizard. Needs to be set before  {@link #openWizard()} is called.
	 * @param descriptor an image descriptor
	 */
	public void setPatchWizardImageDescriptor(ImageDescriptor descriptor){
		this.patchWizardImage = descriptor;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		openWizard();
	}
	
}
