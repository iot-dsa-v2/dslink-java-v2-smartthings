package org.iot.dsa.dslink.smartthings;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.node.DSValueType;

public class SmartValueNode extends SmartNode implements DSIValue {
    
    private DSInfo valueInfo;
    
    private DSInfo getValueChild() {
        if (valueInfo == null) {
            valueInfo = getInfo("value");
        }
        if (valueInfo == null) {
            valueInfo = put("value", DSNull.NULL).setPrivate(true).setReadOnly(true);
        }
        return valueInfo;
    }

    @Override
    public DSValueType getValueType() {
        return getValueChild().getValue().getValueType();
    }

    @Override
    public DSElement toElement() {
        return getValueChild().getValue().toElement();
    }

    @Override
    public DSIValue valueOf(DSElement element) {
        return getValueChild().getValue().valueOf(element);
    }
    
    @Override
    protected void setValueType(String type) {
       //TODO maybe use this?
    }
    
    @Override
    protected void setValue(DSElement value) {
        put("value", value).setPrivate(true).setReadOnly(true);
    }
    
    /**
     * This fires the VALUE_CHANGED topic when the value child changes.  Overrides should call
     * super.onChildChanged.
     */
    @Override
    public void onChildChanged(DSInfo child) {
        DSInfo info = getValueChild();
        if (child == info) {
            fire(VALUE_CHANGED, null);
        }
    }

    @Override
    public void onSet(DSIValue value) {
        put(getValueChild(), value);
        getHub().sendUpdate(this, value);
    }
}
