package org.iot.dsa.dslink.smartthings;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    private DSInfo actionDefNode = getInfo("ACTION_DEFINITIONS");
    private Map<String, SmartAction> virtActions = new HashMap<String, SmartAction>();
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("ACTION_DEFINITIONS", new DSNode()).setPrivate(true);
    }
    
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
            String childName;
            try {
                childName = URLDecoder.decode(arr[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Unsupported Encoding exception", e);
            }
            target = String.join("/", Arrays.asList(arr).subList(1, arr.length));
            DSIObject child = get(childName);
            SmartNode childEntity;
            boolean atLeaf = target.isEmpty() || target.replaceAll("/", "").isEmpty();
            if (atLeaf && body.contains("$invokable")) {
                actionDefNode.getNode().put(childName, body.copy()).setPrivate(true);
                return;
            } else if (atLeaf && body.contains("$type")) {
                if (child instanceof SmartValueNode) {
                    childEntity = (SmartValueNode) child;
                } else {
                    childEntity = new SmartValueNode();
                    put(childName, (SmartValueNode) childEntity).setReadOnly(true);
                }
            } else {
                if (child instanceof SmartNode) {
                    childEntity = (SmartNode) child;
                } else {
                    childEntity = new SmartNode();
                    put(childName, (SmartNode) childEntity);
                }
            }
            
            childEntity.updateTree(target, body);
        }
    }
    
    protected void setValueType(String type) {
        //Override point
    }
    
    protected void setValue(DSElement value) {
        //Override point
    }
    
    @Override
    public void getVirtualActions(DSInfo target, Collection<String> bucket) {
        super.getVirtualActions(target, bucket);
        if (target == getInfo()) {
            for (DSInfo child: actionDefNode.getNode()) {
                if (child.isValue()) {
                    DSElement val = child.getValue().toElement();
                    if (val.isMap()) {
                        bucket.add(child.getName());
                    }
                }
            }
        }
    }
    
    @Override
    public DSInfo getVirtualAction(DSInfo target, String name) {
        if (target == getInfo()) {
            SmartAction act = virtActions.get(name);
            if (act == null) {
                DSIObject obj = actionDefNode.getNode().get(name);
                if (obj instanceof DSMap) {
                    DSMap actionDefn = (DSMap) obj;
                    act = new SmartAction(getHub(), name, actionDefn);
                    virtActions.put(name, act);
                }
            }
            if (act != null) {
                return actionInfo(name, act);
            }
        }
        return super.getVirtualAction(target, name);
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
