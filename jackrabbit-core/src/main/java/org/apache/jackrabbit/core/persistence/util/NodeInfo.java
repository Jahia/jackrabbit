/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.persistence.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameConstants;

/**
 * Holds structural information about a node. Used by the consistency checker and garbage collector.
 */
public final class NodeInfo {

    /**
     * The node id
     */
    private final NodeId nodeId;

    /**
     * The parent node id
     */
    private final NodeId parentId;

    /**
     * The child ids
     */
    private List<NodeId> children;

    /**
     * Map of reference property names of this node with their node id values
     */
    private Map<Name, List<NodeId>> references;

    /**
     * Whether this node is referenceable or not
     */
    private boolean isReferenceable;

    /**
     * Whether this node has blob properties in data storage
     */
    private boolean hasBlobsInDataStore;
    
    /**
     * The node type name
     */
    private final Name nodeTypeName;

    /**
     * The original (unparsed) string value of the jcr:created property 
     */
    private String created;

    /**
     * Create a new NodeInfo object from a bundle
     *
     * @param bundle the node bundle
     */
    public NodeInfo(final NodePropBundle bundle) {
        nodeId = bundle.getId();
        parentId = bundle.getParentId();
        nodeTypeName = bundle.getNodeTypeName();

        List<NodePropBundle.ChildNodeEntry> childNodeEntries = bundle.getChildNodeEntries();
        if (!childNodeEntries.isEmpty()) {
            children = new ArrayList<NodeId>(childNodeEntries.size());
            for (NodePropBundle.ChildNodeEntry entry : bundle.getChildNodeEntries()) {
                children.add(entry.getId());
            }
        } else {
            children = Collections.emptyList();
        }

        for (NodePropBundle.PropertyEntry entry : bundle.getPropertyEntries()) {
            if (entry.getType() == PropertyType.REFERENCE) {
                if (references == null) {
                    references = new HashMap<Name, List<NodeId>>(4);
                }
                List<NodeId> values = new ArrayList<NodeId>(entry.getValues().length);
                for (InternalValue value : entry.getValues()) {
                    values.add(value.getNodeId());
                }
                references.put(entry.getName(), values);
            } else if (entry.getType() == PropertyType.BINARY) {
                for (InternalValue internalValue : entry.getValues()) {
                    if (internalValue.isInDataStore()) {
                        hasBlobsInDataStore = true;
                        break;
                    }
                }
            } else if (entry.getType() == PropertyType.DATE && entry.getName().equals(NameConstants.JCR_CREATED)) {
                InternalValue[] values = entry.getValues();
                if (values != null && values.length > 0) {
                    try {
                        created = values[0].getString();
                    } catch (RepositoryException e) {
                        // should not happen
                    }
                }
            }
        }
        
        if (references == null) {
            references = Collections.emptyMap();
        }
        isReferenceable = bundle.isReferenceable();
    }

    /**
     * @return the node id of this node
     */
    public NodeId getId() {
        return nodeId;
    }

    /**
     * @return the parent id of this node
     */
    public NodeId getParentId() {
        return parentId;
    }

    /**
     * @return the child ids of this node
     */
    public List<NodeId> getChildren() {
        return children;
    }

    /**
     * @return the reference properties along with their node id values of this node
     */
    public Map<Name, List<NodeId>> getReferences() {
        return references;
    }

    /**
     * @return whether the node represented by this node info is referenceable
     */
    public boolean isReferenceable() {
        return isReferenceable;
    }

    /**
     * @return whether the node has blob properties that are inside the data storage
     */
    public boolean hasBlobsInDataStore() {
        return hasBlobsInDataStore;
    }

    /**
     * Returns the node type name.
     * 
     * @return the node type name
     */
    public Name getNodeTypeName() {
        return nodeTypeName;
    }

    /**
     * Returns the the original (unparsed) string value of the jcr:created property.
     * 
     * @return the the original (unparsed) string value of the jcr:created property
     */
    public String getCreated() {
        return created;
    }
}
