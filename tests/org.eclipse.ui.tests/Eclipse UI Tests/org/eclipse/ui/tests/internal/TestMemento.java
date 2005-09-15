package org.eclipse.ui.tests.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.ui.IMemento;

public class TestMemento implements IMemento {
	
	String typeName;
	String id;
	HashSet children = new HashSet();
	Hashtable values = new Hashtable();
	String textData;
	
	public TestMemento(String type, String id){
		typeName = type;
		this.id = id;
	}

	public IMemento createChild(String type) {
		return createChild(type, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#createChild(java.lang.String, java.lang.String)
	 */
	public IMemento createChild(String type, String id) {
		IMemento child  = new TestMemento(typeName,id);
		children.add(child);
		return child;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#getChild(java.lang.String)
	 */
	public IMemento getChild(String type) {
		Iterator iterator = children.iterator();
		while(iterator.hasNext()){
			TestMemento next = (TestMemento) iterator.next();
			if(next.typeName.equals(type))
				return next;
		}
		return null;
	}

	public IMemento[] getChildren(String type) {
		Iterator iterator = children.iterator();
		Collection matches = new HashSet();
		while(iterator.hasNext()){
			TestMemento next = (TestMemento) iterator.next();
			if(next.typeName.equals(type)){
				matches.add(next);
			}
		}
		
		IMemento[] returnValue = new IMemento[matches.size()];
		matches.toArray(returnValue);
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#getFloat(java.lang.String)
	 */
	public Float getFloat(String key) {
		if(values.containsKey(key))
			return (Float) values.get(key);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#getID()
	 */
	public String getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#getInteger(java.lang.String)
	 */
	public Integer getInteger(String key) {
		if(values.containsKey(key))
			return (Integer) values.get(key);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#getString(java.lang.String)
	 */
	public String getString(String key) {
		if(values.containsKey(key))
			return (String) values.get(key);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#getTextData()
	 */
	public String getTextData() {
		return textData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#putFloat(java.lang.String, float)
	 */
	public void putFloat(String key, float value) {
		values.put(key,new Float(value));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#putInteger(java.lang.String, int)
	 */
	public void putInteger(String key, int value) {
		values.put(key,new Integer(value));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#putMemento(org.eclipse.ui.IMemento)
	 */
	public void putMemento(IMemento memento) {
		TestMemento newMemento = (TestMemento) memento;
		typeName = newMemento.typeName;
		id =  newMemento.id;
		children =  newMemento.children;
		values =  newMemento.values;
		textData =  newMemento.textData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#putString(java.lang.String, java.lang.String)
	 */
	public void putString(String key, String value) {
		values.put(key,value);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMemento#putTextData(java.lang.String)
	 */
	public void putTextData(String data) {
		textData = data;

	}

}
