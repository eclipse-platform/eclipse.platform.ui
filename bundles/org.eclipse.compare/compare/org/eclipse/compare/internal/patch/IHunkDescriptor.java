package org.eclipse.compare.internal.patch;

/**
 * Interface used for providing information about hunks that form a patch input. This interface is
 * still under development and is to be considered <strong>EXPERIMENTAL</strong>.
 * since 3.3
 *
 */
public interface IHunkDescriptor {
	
	/**
	 * Return the start position of the hunk in the target file.
	 * @return the start position of the hunk in the target file.
	 */
	int getStartPosition();
}
