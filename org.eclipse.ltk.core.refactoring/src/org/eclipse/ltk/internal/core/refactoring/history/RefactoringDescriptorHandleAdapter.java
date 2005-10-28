package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorHandle;

/**
 * Adapter class which adapts refactoring descriptors to handles.
 * 
 * @since 3.2
 */
public final class RefactoringDescriptorHandleAdapter extends RefactoringDescriptorHandle {

	/** The encapsulated descriptor */
	private final RefactoringDescriptor fDescriptor;

	/**
	 * Creates a new refactoring descriptor handle adapter.
	 * 
	 * @param descriptor
	 *            the descriptor to encapsulate
	 */
	public RefactoringDescriptorHandleAdapter(final RefactoringDescriptor descriptor) {
		super(descriptor.getDescription(), descriptor.getTimeStamp());
		fDescriptor= descriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public final RefactoringDescriptor resolveDescriptor() {
		return fDescriptor;
	}
}