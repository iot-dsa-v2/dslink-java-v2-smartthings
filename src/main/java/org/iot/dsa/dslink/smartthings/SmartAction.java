package org.iot.dsa.dslink.smartthings;

import java.util.HashMap;
import java.util.Map;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.util.DSException;

public class SmartAction extends DSAction {
    
    private Map<String, ParamBounds> paramBounds = new HashMap<String, ParamBounds>();
    private String name;
    private HubNode hub;
    
    public SmartAction(HubNode hub, String name, DSMap body) {
        this.name = name;
        this.hub = hub;
        for (Entry entry : body) {
            String key = entry.getKey();
            DSElement value = entry.getValue();
            if (key.equals("$params")) {
                if (!value.isList()) {
                    throw new RuntimeException("$params not a list");
                }
                DSList params  = value.toList();
                paramBounds.clear();
                for (DSElement paramE: params) {
                    if (!paramE.isMap()) {
                        throw new RuntimeException("parameter definition not a map");
                    }
                    DSMap param = paramE.toMap();
                    String paramName = param.getString("name");
                    String paramType = param.getString("type");
                    if ("enum".equals(paramType)) {
                        DSList range = param.getList("enum");
                        if (range != null && range.size() > 0) {
                            addParameter(paramName, DSFlexEnum.valueOf(range.getString(0), range), null);
                        }
                    } else if ("number".equals(paramType)) {
                        ParamBounds bounds = ParamBounds.fromParamMap(param);
                        String description = null;
                        if (bounds != null) {
                            paramBounds.put(paramName, bounds);
                            description = bounds.toDescription(false);
                        }
                        addParameter(paramName, DSValueType.NUMBER, description);
                    } else if ("string".equals(paramType)) {
                        ParamBounds bounds = ParamBounds.fromParamMap(param);
                        String description = null;
                        if (bounds != null) {
                            paramBounds.put(paramName, bounds);
                            description = bounds.toDescription(true);
                        }
                        addParameter(paramName, DSValueType.STRING, description);
                    }
                }
            }
        }
    }

    @Override
    public ActionResult invoke(DSInfo target, ActionInvocation request) {
        DSMap parameters = request.getParameters();
        for (Entry entry: parameters) {
            String paramName = entry.getKey();
            DSElement paramValue = entry.getValue();
            ParamBounds bounds = paramBounds.get(paramName);
            if (bounds != null && !bounds.isLegal(paramValue)) {
                DSException.throwRuntime(new IllegalArgumentException("Parameter " + paramName + "out of bounds: " + bounds.toDescription(paramValue.isString())));
                return null;
            }
        }
        hub.sendInvocation(name, target, parameters);
        return null;
    }

    @Override
    public void prepareParameter(DSInfo target, DSMap parameter) {
    }

    public void updateTree(String target, DSMap body) {
        if (target.isEmpty() || target.replaceAll("/", "").isEmpty()) {
            
        } else {
            throw new RuntimeException("Action must be a leaf");
        }
        
    }
    
    private static class ParamBounds {
        private Integer min = null;
        private Integer max = null;
        
        ParamBounds(Integer min, Integer max) {
            this.min = min;
            this.max = max;
        }
        
        static ParamBounds fromParamMap(DSMap param) {
            DSElement minElem = param.get("min");
            DSElement maxElem = param.get("max");
            Integer min, max;
            if (minElem != null && minElem.isNumber()) {
                min = minElem.toInt();
            } else {
                min = null;
            }
            if (maxElem != null && maxElem.isNumber()) {
                max = maxElem.toInt();
            } else {
                max = null;
            }
            if (min == null && max == null) {
                return null;
            }
            return new ParamBounds(min, max);
        }
        
        public String toDescription(boolean stringType) {
            StringBuilder sb = new StringBuilder();
            if (min != null) {
                sb.append("Minimum ");
                sb.append(stringType ? "length" : "value");
                sb.append(" = ");
                sb.append(min);
                if (max != null) {
                    sb.append("; ");
                }
                if (max != null) {
                    sb.append("Maximum ");
                    sb.append(stringType ? "length" : "value");
                    sb.append(" = ");
                    sb.append(max);
                }
            }
            return sb.toString();
        }
        
        public boolean isLegal(DSElement value) {
            double d;
            if (value.isNumber()) {
                d = value.toDouble();
            } else if (value.isString()) {
                d = value.toString().length();
            } else {
                return false;
            }
            if (min != null && d < min) {
                return false;
            }
            if (max != null && d > max) {
                return false;
            }
            return true;
        }
    }
}
