package org.eclipse.ui.about;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.ui.services.IServiceLocator;

/**
 * An installation dialog page.
 * 
 * The counterpart, {@link IInstallationPageContainer}, may be accessed using
 * the service locator.
 * 
 * <em>This API is experimental and will change before 3.5 ships</em>
 * 
 * @since 3.5
 */
public abstract class InstallationPage extends DialogPage {

	public abstract void init(IServiceLocator locator);
	
	/**
     * Sets or clears the message for this page.
     * <p>
     * This message has no effect when the receiver is used in an 
     * IInstallationPageContainer.
     * </p>
     * 
     * @param newMessage
     *            the message, or <code>null</code> to clear the message
     */
    public void setMessage(String newMessage) {
        super.setMessage(newMessage);
    }

    /**
     * Sets the message for this page with an indication of what type of message
     * it is.
     * <p>
     * The valid message types are one of <code>NONE</code>,
     * <code>INFORMATION</code>,<code>WARNING</code>, or
     * <code>ERROR</code>.
     * </p>
     * <p>
     * This message has no effect when the receiver is used in an
     * IInstallationPageContainer.
     * </p>
     * 
     * @param newMessage
     *            the message, or <code>null</code> to clear the message
     * @param newType
     *            the message type
     * @since 2.0
     */
    public void setMessage(String newMessage, int newType) {
        super.setMessage(newMessage, newType);
    }


}
