package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.team.internal.ccvs.core.IUserInfo;

/**
 * @version 	1.0
 * @author
 */
public class UserInfo implements IUserInfo {

	private String username;
	private String password;
	private boolean isUsernameMutable;
	
	protected UserInfo(String username, String password, boolean isUsernameMutable) {
		this.username = username;
		this.password = password;
		this.isUsernameMutable = isUsernameMutable;
	}
	
	/*
	 * @see IUserInfo#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	protected String getPassword() {
		return password;
	}
	
	/*
	 * @see IUserInfo#isUsernameMutable()
	 */
	public boolean isUsernameMutable() {
		return false;
	}

	/*
	 * @see IUserInfo#setPassword(String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/*
	 * @see IUserInfo#setUsername(String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

}
