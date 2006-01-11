package org.eclipse.team.internal.core.mapping;

import org.eclipse.core.resources.IStorage;
import org.eclipse.team.core.mapping.IStorageMerger;

/**
 * Interface that allows the {@link DelegatingStorageMerger} to
 * delegate to IStreamMergers. We need an interface so the UI can
 * provide the implementation.
 */
public interface IStreamMergerDelegate {
	/**
	 * Find a storage merger for the given target.
	 * A storage merger will only be returned if
	 * there is a stream merger that matches the
	 * targets content type or extension.
	 * 
	 * @param target the input storage
	 * @return a storage merger for the given target
	 */
	IStorageMerger findMerger(IStorage target);
}