package org.iot.dsa.dslink.smartthings;

import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * The main and only node of this link.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode {
    private static String ACT_ADD_HUB = "Add Hub";

    
    public MainNode() {
    }


    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(ACT_ADD_HUB, getAddHubAction());
        // Change the following URL to your README
        declareDefault("Help",
                       DSString.valueOf("https://github.com/iot-dsa-v2/dslink-java-v2-example"))
                .setTransient(true)
                .setReadOnly(true);
    }

   private DSAction getAddHubAction() {
       DSAction act = new DSAction() {
           @Override
           public void prepareParameter(DSInfo target, DSMap parameter) {
           }
           
           @Override
           public ActionResult invoke(DSInfo target, ActionInvocation request) {
               ((MainNode) target.get()).addHub(request.getParameters());
               return null;
           }
       };
       act.addParameter("Name", DSValueType.STRING, "");
       act.addDefaultParameter("Port", DSLong.valueOf(8020), "Port to listen on");
       return act;
   }
   
   private void addHub(DSMap parameters) {
       String name = parameters.getString("Name");
       HubNode hn = new HubNode(parameters);
       add(name, hn);
   }

}
