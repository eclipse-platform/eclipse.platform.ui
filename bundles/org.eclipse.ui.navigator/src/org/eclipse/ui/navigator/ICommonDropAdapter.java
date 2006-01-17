package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * The common drop adapter provides a handle to useful
 * information for {@link ICommonDropActionDelegate}s.
 * 
 * This interface is particularly unstable as of 3.2M4. 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.2
 *
 */
public interface ICommonDropAdapter {

	/**
	 * @param transferType
	 * @return the selected object only if the transferType is of the type
	 *         LocalSelectionTransfer.getInstance()
	 */
	Object getSelectedObject(TransferData transferType);

	/**
	 * @param transferType
	 * @param data The data involved in the transfer.
	 * @return the selected object only if the transferType is of the type
	 *         LocalSelectionTransfer.getInstance()
	 */
	Object getSelectedObject(TransferData transferType, Object data);

    /**
     * Returns a constant describing the position of the mouse relative to the
     * target (before, on, or after the target.  
     *
     * @return one of the <code>LOCATION_* </code> constants defined in this type
     *
	 *  
	 */
	int getCurrentLocation();

    /**
     * Returns the current operation.
     *
     * @return a <code>DROP_*</code> constant from class <code>DND</code>
     *
     * @see DND#DROP_COPY
     * @see DND#DROP_MOVE
     * @see DND#DROP_LINK
     * @see DND#DROP_NONE 
	 */
	int getCurrentOperation();

    /**
     * Returns the target object currently under the mouse.
     *
     * @return the current target object 
	 */
	Object getCurrentTarget();
 

    /**
     * Returns the object currently selected by the viewer.
     *
     * @return the selected object, or <code>null</code> if either no object or 
     *   multiple objects are selected
     *    
	 */
	Object getSelectedObject();
	

	/**
	 * Return the current transfer data.
	 * 
	 * @return The current transfer data for this drop.
	 *  
	 */ 
	TransferData getCurrentTransfer(); 
	
    /**
     * Returns whether visible insertion feedback should be presented to the user.
     * <p>
     * Typical insertion feedback is the horizontal insertion bars that appear 
     * between adjacent items while dragging.
     * </p>
     *
     * @return <code>true</code> if visual feedback is desired, and <code>false</code> if not
     * @see ViewerDropAdapter#getFeedbackEnabled()
     */
    public boolean getFeedbackEnabled(); 
}