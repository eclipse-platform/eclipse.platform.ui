/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Shipton - original hashtable implementation
 *     Nick Edgar - added element comparer support
 *     Hendrik Still <hendrik.still@gammas.de> - Bug 414067
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * CustomHashtable associates keys with values. Keys and values cannot be null.
 * The size of the Hashtable is the number of key/value pairs it contains.
 * The capacity is the number of key/value pairs the Hashtable can hold.
 * The load factor is a float value which determines how full the Hashtable
 * gets before expanding the capacity. If the load factor of the Hashtable
 * is exceeded, the capacity is doubled.
 * <p>
 * CustomHashtable allows a custom comparator and hash code provider.
 */
/* package */final class CustomHashtable<K, V> {

    /**
     * HashMapEntry is an internal class which is used to hold the entries of a Hashtable.
     */
    private static class HashMapEntry<K, V> {
        K key;

        V value;

        HashMapEntry<K, V> next;

        HashMapEntry(K theKey, V theValue) {
            key = theKey;
            value = theValue;
        }
    }

    private static final class EmptyEnumerator<E> implements Enumeration<E> {
        public boolean hasMoreElements() {
            return false;
        }

        public E nextElement() {
            throw new NoSuchElementException();
        }
    }

    private abstract class HashEnumerator<E> implements Enumeration<E>{

        int start;

        HashMapEntry<K, V> entry;

        HashEnumerator() {
            start = firstSlot;
        }

        public boolean hasMoreElements() {
            if (entry != null) {
				return true;
			}
            while (start <= lastSlot) {
				if (elementData[start++] != null) {
                    entry = elementData[start - 1];
                    return true;
                }
			}
            return false;
        }

    }

    private class KeyHashEnumerator extends HashEnumerator<K>{

		public KeyHashEnumerator() {
			super();
		}

        public K nextElement() {
            if (hasMoreElements()) {
                return entry.key;
            }
			throw new NoSuchElementException();
        }
    }

    private class ValueHashEnumerator extends HashEnumerator<V>{

    	public ValueHashEnumerator() {
    		super();
    	}

    	public V nextElement() {
    		if (hasMoreElements()) {
    			return entry.value;
    		}
    		throw new NoSuchElementException();
    	}
    }

    transient int elementCount;

    transient HashMapEntry<K, V>[] elementData;

    private float loadFactor;

    private int threshold;

    transient int firstSlot = 0;

    transient int lastSlot = -1;

    transient private IElementComparer comparer;

    @SuppressWarnings("rawtypes")
    private static final EmptyEnumerator emptyEnumerator = new EmptyEnumerator();

    /**
     * The default capacity used when not specified in the constructor.
     */
    public static final int DEFAULT_CAPACITY = 13;

    /**
     * Constructs a new Hashtable using the default capacity
     * and load factor.
     */
    public CustomHashtable() {
        this(13);
    }

    /**
     * Constructs a new Hashtable using the specified capacity
     * and the default load factor.
     *
     * @param capacity the initial capacity
     */
    public CustomHashtable(int capacity) {
        this(capacity, null);
    }

    /**
     * Constructs a new hash table with the default capacity and the given
     * element comparer.
     *
     * @param comparer the element comparer to use to compare keys and obtain
     *   hash codes for keys, or <code>null</code>  to use the normal
     *   <code>equals</code> and <code>hashCode</code> methods
     */
    public CustomHashtable(IElementComparer comparer) {
        this(DEFAULT_CAPACITY, comparer);
    }

    /**
     * Constructs a new hash table with the given capacity and the given
     * element comparer.
     *
     * @param capacity the maximum number of elements that can be added without
     *   rehashing
     * @param comparer the element comparer to use to compare keys and obtain
     *   hash codes for keys, or <code>null</code>  to use the normal
     *   <code>equals</code> and <code>hashCode</code> methods
     */
	public CustomHashtable(int capacity, IElementComparer comparer) {
        if (capacity >= 0) {
            elementCount = 0;
            @SuppressWarnings("unchecked")
			HashMapEntry<K,V>[] newElementData = new HashMapEntry[capacity == 0 ? 1 : capacity];
            elementData = newElementData;
            firstSlot = elementData.length;
            loadFactor = 0.75f;
            computeMaxSize();
        } else {
			throw new IllegalArgumentException();
		}
        this.comparer = comparer;
    }

    /**
     * Constructs a new hash table with enough capacity to hold all keys in the
     * given hash table, then adds all key/value pairs in the given hash table
     * to the new one, using the given element comparer.
     * @param table the original hash table to copy from
     *
     * @param comparer the element comparer to use to compare keys and obtain
     *   hash codes for keys, or <code>null</code>  to use the normal
     *   <code>equals</code> and <code>hashCode</code> methods
     */
    public CustomHashtable(CustomHashtable<K, V> table, IElementComparer comparer) {
        this(table.size() * 2, comparer);
        for (int i = table.elementData.length; --i >= 0;) {
            HashMapEntry<K, V> entry = table.elementData[i];
            while (entry != null) {
                put(entry.key, entry.value);
                entry = entry.next;
            }
        }
    }

    /**
     * Returns the element comparer used  to compare keys and to obtain
     * hash codes for keys, or <code>null</code> if no comparer has been
     * provided.
     *
     * @return the element comparer or <code>null</code>
     *
     * @since 3.2
     */
    public IElementComparer getComparer() {
    	return comparer;
    }

    private void computeMaxSize() {
        threshold = (int) (elementData.length * loadFactor);
    }

    /**
     * Answers if this Hashtable contains the specified object as a key
     * of one of the key/value pairs.
     *
     * @param		key	the object to look for as a key in this Hashtable
     * @return		true if object is a key in this Hashtable, false otherwise
     */
    public boolean containsKey(K key) {
        return getEntry(key) != null;
    }

    /**
     * Answers an Enumeration on the values of this Hashtable. The
     * results of the Enumeration may be affected if the contents
     * of this Hashtable are modified.
     *
     * @return		an Enumeration of the values of this Hashtable
     */
    public Enumeration<V> elements() {
        if (elementCount == 0) {
			return emptyEnumerator;
		}
        return new ValueHashEnumerator();
    }

    /**
     * Answers the value associated with the specified key in
     * this Hashtable.
     *
     * @param		key	the key of the value returned
     * @return		the value associated with the specified key, null if the specified key
     *				does not exist
     */
    public V get(K key) {
        int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
        HashMapEntry<K,V> entry = elementData[index];
        while (entry != null) {
            if (keyEquals(key, entry.key)) {
				return entry.value;
			}
            entry = entry.next;
        }
        return null;
    }

    private HashMapEntry<K,V> getEntry(K key) {
        int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
        HashMapEntry<K,V> entry = elementData[index];
        while (entry != null) {
            if (keyEquals(key, entry.key)) {
				return entry;
			}
            entry = entry.next;
        }
        return null;
    }

    /**
     * Answers the hash code for the given key.
     */
    private int hashCode(Object key) {
        if (comparer == null) {
			return key.hashCode();
		}
		return comparer.hashCode(key);
    }

    /**
     * Compares two keys for equality.
     */
    private boolean keyEquals(K a, K b) {
        if (comparer == null) {
			return a.equals(b);
		}
		return comparer.equals(a, b);
    }

    /**
     * Answers an Enumeration on the keys of this Hashtable. The
     * results of the Enumeration may be affected if the contents
     * of this Hashtable are modified.
     *
     * @return		an Enumeration of the keys of this Hashtable
     */
    public Enumeration<K> keys() {
        if (elementCount == 0) {
			return emptyEnumerator;
		}
        return new KeyHashEnumerator();
    }

    /**
     * Associate the specified value with the specified key in this Hashtable.
     * If the key already exists, the old value is replaced. The key and value
     * cannot be null.
     *
     * @param		key	the key to add
     * @param		value	the value to add
     * @return		the old value associated with the specified key, null if the key did
     *				not exist
     */
    public V put(K key, V value) {
        if (key != null && value != null) {
            int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
            HashMapEntry<K,V> entry = elementData[index];
            while (entry != null && !keyEquals(key, entry.key)) {
				entry = entry.next;
			}
            if (entry == null) {
                if (++elementCount > threshold) {
                    rehash();
                    index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
                }
                if (index < firstSlot) {
					firstSlot = index;
				}
                if (index > lastSlot) {
					lastSlot = index;
				}
                entry = new HashMapEntry<K,V>(key, value);
                entry.next = elementData[index];
                elementData[index] = entry;
                return null;
            }
            V result = entry.value;
            entry.key = key; // important to avoid hanging onto keys that are equal but "old" -- see bug 30607
            entry.value = value;
            return result;
        }
		throw new NullPointerException();
    }

    /**
     * Increases the capacity of this Hashtable. This method is sent when
     * the size of this Hashtable exceeds the load factor.
     */
    private void rehash() {
        int length = elementData.length << 1;
        if (length == 0) {
			length = 1;
		}
        firstSlot = length;
        lastSlot = -1;
        @SuppressWarnings("unchecked")
		HashMapEntry<K,V>[] newData = new HashMapEntry[length];
        for (int i = elementData.length; --i >= 0;) {
            HashMapEntry<K,V> entry = elementData[i];
            while (entry != null) {
                int index = (hashCode(entry.key) & 0x7FFFFFFF) % length;
                if (index < firstSlot) {
					firstSlot = index;
				}
                if (index > lastSlot) {
					lastSlot = index;
				}
                HashMapEntry<K,V> next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        computeMaxSize();
    }

    /**
     * Remove the key/value pair with the specified key from this Hashtable.
     *
     * @param		key	the key to remove
     * @return		the value associated with the specified key, null if the specified key
     *				did not exist
     */
    public V remove(K key) {
        HashMapEntry<K,V> last = null;
        int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
        HashMapEntry<K,V> entry = elementData[index];
        while (entry != null && !keyEquals(key, entry.key)) {
            last = entry;
            entry = entry.next;
        }
        if (entry != null) {
            if (last == null) {
				elementData[index] = entry.next;
			} else {
				last.next = entry.next;
			}
            elementCount--;
            return entry.value;
        }
        return null;
    }

    /**
     * Answers the number of key/value pairs in this Hashtable.
     *
     * @return		the number of key/value pairs in this Hashtable
     */
    public int size() {
        return elementCount;
    }

    /**
     * Answers the string representation of this Hashtable.
     *
     * @return		the string representation of this Hashtable
     */
    @Override
	public String toString() {
        if (size() == 0) {
			return "{}"; //$NON-NLS-1$
		}

        StringBuffer buffer = new StringBuffer();
        buffer.append('{');
        for (int i = elementData.length; --i >= 0;) {
            HashMapEntry<K,V> entry = elementData[i];
            while (entry != null) {
                buffer.append(entry.key);
                buffer.append('=');
                buffer.append(entry.value);
                buffer.append(", "); //$NON-NLS-1$
                entry = entry.next;
            }
        }
        // Remove the last ", "
        if (elementCount > 0) {
			buffer.setLength(buffer.length() - 2);
		}
        buffer.append('}');
        return buffer.toString();
    }
}
