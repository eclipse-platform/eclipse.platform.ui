package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPropertyListener;

public class MockPropertyListener implements IPropertyListener {
	private CallHistory callTrace;
	private Object sourceMask;
	private int sourceId;
	
	/**
	 * @param source the event source that fires the event to this listener
	 * @param id the property id for the event
	 */
	public MockPropertyListener( Object source, int id )
	{
		sourceMask = source;
		sourceId = id;
		callTrace = new CallHistory();
	}	
	
	/**
	 * @see IPropertyListener#propertyChanged(Object, int)
	 */
	public void propertyChanged(Object source, int propId) {	
		if( source == sourceMask && propId == sourceId )	
			callTrace.add( this, "propertyChanged" );
	}
	
	public CallHistory getCallHistory()
	{
		return callTrace;
	}	
}

