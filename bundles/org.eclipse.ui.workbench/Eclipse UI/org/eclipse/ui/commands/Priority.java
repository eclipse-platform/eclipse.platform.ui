package org.eclipse.ui.commands;

import org.eclipse.ui.internal.util.Util;

public final class Priority implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = Priority.class.getName().hashCode();

    public final static Priority LEGACY = new Priority(3);

    public final static Priority LOW = new Priority(2);

    public final static Priority NORMAL = new Priority(1);

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private int priority;

    private transient String string;

    private Priority(int priority) {
        this.priority = priority;
    }

    public int compareTo(Object object) {
        Priority castedObject = (Priority) object;
        int compareTo = Util.compare(-priority, -castedObject.priority);
        return compareTo;
    }

    int getPriority() {
        return priority;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(priority);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("[priority="); //$NON-NLS-1$
            stringBuffer.append(priority);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}