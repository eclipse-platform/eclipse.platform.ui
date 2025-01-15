/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri;

import java.io.Serializable;

/**
 * A qualified name: a combination of an optional namespace prefix
 * and an local name.
 */
public class QName implements Serializable {
    private static final long serialVersionUID = 7616199282015091496L;

    private final String prefix;
    private final String name;
    private final String qualifiedName;

    /**
     * Create a new QName.
     * @param qualifiedName value
     */
    public QName(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
        final int index = qualifiedName.indexOf(':');
        prefix = index < 0 ? null : qualifiedName.substring(0, index);
        name = index < 0 ? qualifiedName : qualifiedName.substring(index + 1);
    }

    /**
     * Create a new QName.
     * @param prefix ns
     * @param localName String
     */
    public QName(final String prefix, final String localName) {
        this.prefix = prefix;
        this.name = localName;
        this.qualifiedName = prefix == null ? localName : prefix + ':' + localName;
    }

    /**
     * Gets the prefix of this QName.
     * @return String
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the local name.
     * @return String
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof QName)) {
            return false;
        }
        return qualifiedName.equals(((QName) object).qualifiedName);
    }
}
