package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Description of a workbench part. The part descriptor contains 
 * the information needed to create part instances.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IWorkbenchPartDescriptor{
/**
 * Returns the part id.
 *
 * @return the id of the part
 */
public String getId();
/**
 * Returns the descriptor of the image for this part.
 *
 * @return the descriptor of the image to display next to this part
 */
public ImageDescriptor getImageDescriptor();
/**
 * Returns the label to show for this part.
 *
 * @return the part label
 */
public String getLabel();
}
