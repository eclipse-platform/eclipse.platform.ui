package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Adapter class which adapts refactoring descriptors to refactoring descriptor
 * proxies.
 * 
 * @since 3.2
 */
public final class RefactoringDescriptorProxyAdapter extends RefactoringDescriptorProxy {

	/** The encapsulated descriptor */
	private final RefactoringDescriptor fDescriptor;

	/**
	 * Creates a new refactoring descriptor proxy adapter.
	 * 
	 * @param descriptor
	 *            the descriptor to encapsulate
	 */
	public RefactoringDescriptorProxyAdapter(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int compareTo(final Object object) {
		return fDescriptor.compareTo(object);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getDescription() {
		return fDescriptor.getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getProject() {
		return fDescriptor.getProject();
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getTimeStamp() {
		return fDescriptor.getTimeStamp();
	}

	/**
	 * {@inheritDoc}
	 */
	public final RefactoringDescriptor requestDescriptor(final IProgressMonitor monitor) {
		return fDescriptor;
	}
}