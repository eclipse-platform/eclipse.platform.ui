/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

public abstract class AbstractItem {
	
	protected String id;

	public AbstractItem() {
		super();
	}
	
	public void setID(String id){
		this.id = id;
	}
	
	public String getID(){
		return id;	
	}
	
}
