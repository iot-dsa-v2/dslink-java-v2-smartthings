package org.iot.dsa.dslink.smartthings;

import java.util.Arrays;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSNode;

public class SmartNode extends DSNode implements DSIMetadata {
    
    private DSMap metadata = new DSMap();
    private HubNode hub = null;
    
    public void updateTree(String target, DSMap body) {
        if (target.isEmpty() || target.replaceAll("/", "").isEmpty()) {
            for (Entry entry : body) {
                String key = entry.getKey();
                DSElement value = entry.getValue();
                if (key.charAt(0) == '$' || key.charAt(0) == '?' || key.charAt(0) == '@') {
                    if (key.equals("$type")) {
                        setValueType(value.toString());
                    } else if (key.equals("?value")) {
                        setValue(value);
                    } else {
                        metadata.put(key, value);
                    }
                } else {
                    if (!value.isMap()) {
                        throw new RuntimeException("Node description not a map");
                    }
                    updateTree(key, value.toMap());
                }
            }
        } else {
            target = target.charAt(0) == '/' ? target.substring(1) : target;
            String[] arr = target.split("/");
            if (arr.length < 1) {
                throw new RuntimeException("Malformed target: " + target + " - This should not be possible");
            }
            String childName = arr[0];
            target = String.join("/", Arrays.asList(arr).subList(1, arr.length));
            DSIObject child = get(childName);
            SmartNode childNode;
            if ((target.isEmpty() || target.replaceAll("/", "").isEmpty()) && body.contains("$type")) {
                if (child instanceof SmartValueNode) {
                    childNode = (SmartValueNode) child;
                } else {
                    DSInfo childInfo = put(childName, new SmartValueNode());
                    childNode = (SmartValueNode) childInfo.getNode();
                }
            } else {
                if (child instanceof SmartNode) {
                    childNode = (SmartNode) child;
                } else { 
                    DSInfo childInfo = put(childName, new SmartNode());
                    childNode = (SmartNode) childInfo.getNode();
                }
            }
            
            childNode.updateTree(target, body);
        }
    }
    
    protected void setValueType(String type) {
        //Override point
    }
    
    protected void setValue(DSElement value) {
        //Override point
    }

    @Override
    public void getMetadata(DSMap bucket) {
        for (Entry entry: metadata) {
            bucket.put(entry.getKey(), entry.getValue());
        }
    }
    
    protected HubNode getHub() {
        if (hub == null) {
            hub = (HubNode) getAncestor(HubNode.class);
        }
        return hub;
    }

}
