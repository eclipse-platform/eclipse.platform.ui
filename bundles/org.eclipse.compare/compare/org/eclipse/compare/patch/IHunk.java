package org.eclipse.compare.patch;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface that represents a hunk. A hunk is a portion of a patch.
 * It identifies where the hunk is to be located in the target file.
 * One use of this interface is a means to communicate to content merge viewers
 * that one of the sides of a compare input is a patch hunk. Clients can determine
 * which side it is by adapting the side to this interface (see {@link IAdaptable}.
 * <p>
 * This interface is not intended to be implemented by clients but should instead
 * subclass {@link AbstractHunk}.
 * <p>
 * This interface is
 * still under development and is to be considered <strong>EXPERIMENTAL</strong>.
 * since 3.3
 *
 */
public interface IHunk {
	
	/**
	 * Return the start position of the hunk in the target file.
	 * @return the start position of the hunk in the target file.
	 */
	int getStartPosition();
}
