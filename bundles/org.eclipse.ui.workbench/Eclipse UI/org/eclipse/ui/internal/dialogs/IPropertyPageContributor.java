package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.*;
import org.eclipse.core.runtime.IAdaptable;
/**
 * Implement this interface in order to register property
 * pages for a given object. During property dialog building
 * sequence, all property page contributors for a given object
 * are given a chance to add their pages.
 */
public interface IPropertyPageContributor extends IObjectContributor {
/**
 * Implement this method to add instances of PropertyPage class to the
 * property page manager.
 * @return true if pages were added, false if not.
 */	

public boolean contributePropertyPages(PropertyPageManager manager, IAdaptable object);
}
