package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/** 
 * A listener which is notified when a label provider's state changes.
 *
 * @see IBaseLabelProvider#addListener
 * @see IBaseLabelProvider#removeListener
 */
public interface ILabelProviderListener {
/**
 * Notifies this listener that the state of the label provider 
 * has changed in a way that affects the labels it computes.
 * <p>
 * A typical response would be to refresh all labels by 
 * re-requesting them from the label provider.
 * </p>
 *
 * @param event the label provider change event
 */
public void labelProviderChanged(LabelProviderChangedEvent event);
}
