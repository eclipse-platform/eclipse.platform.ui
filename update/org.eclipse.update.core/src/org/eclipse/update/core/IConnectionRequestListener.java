package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2002, 2003
 *  All  Rights Reserved.
 */
 
 /**
  * Connection Request listener. This interface abstract the user interaction
  * that may be required as a result of processed connection.When a connection
  * is cancelled or sucessful, the listener can process the connection request. 
  * <p>
  * Clients may implement this interface.
  * </p>
  * @since 2.1
  */
public interface IConnectionRequestListener {
	
	/**
	 * Called when the connection is either sucessful or canceled.
	 * Implementors should check the status of the request.
	 * 
	 * @param request the processed request
	 */
	public void requestProcessed(ConnectionRequest request);

}
