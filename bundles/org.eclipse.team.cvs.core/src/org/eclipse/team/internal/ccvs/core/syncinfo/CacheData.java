package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.team.internal.ccvs.core.util.*;

public class CacheData {
	private Object data;
	private Object id;
	private Date expirationDate;
	
	public CacheData(Object id, Object data, int minutesToLive) {
		this.id = id;
		this.data = data;
		this.expirationDate = null; // if not set then default is to live forever
		if(minutesToLive>0) {
			expirationDate = new Date();
			Calendar currentDate = Calendar.getInstance();
			currentDate.add(Calendar.MINUTE, minutesToLive);
			expirationDate = currentDate.getTime();
		}
	}
	
	public CacheData(Object id, Object data) {
		this(id, data, 0);
	}
	
	public boolean isExpired() {
		if(expirationDate==null) {
			return false;
		} else {
			return expirationDate.before(new Date());
		}
	}
	
	public Object getId() {
		return id;
	}
	
	public Object getData() {
		return data;
	}
}
