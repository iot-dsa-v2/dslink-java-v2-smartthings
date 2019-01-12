package org.iot.dsa.dslink.smartthings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSMap;

public class HubNode extends SmartNode {
    private static final String PARAMS = "parameters";
    
    private DSMap parameters;
    private Server server;
    
    public HubNode() {
        
    }
    
    public HubNode(DSMap parameters) {
        this.parameters = parameters;
    }
    
    @Override
    protected void onStarted() {
        super.onStarted();
        if (this.parameters == null) {
            DSIObject o = get(PARAMS);
            if (o instanceof DSMap) {
                this.parameters = (DSMap) o;
            }
        } else {
            put(PARAMS, parameters.copy()).setPrivate(true);
        }
    }
    
    @Override
    protected void onStable() {
        super.onStable();
        init();
    }
    
    @Override
    protected void onStopped() {
        super.onStopped();
        end();
    }
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
    }
    
    private void init() {
        int port = parameters.getInt("Port");
        RequestHandler handler = new RequestHandler(this);
        server = new Server(port);
        server.setHandler(handler);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            warn("", e);
            end();
        }
    }
    
    private void end() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                warn("", e);
            }
        }
        server = null;
    }
    
    public void handleRequest(String target, Request baseRequest, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String method = request.getMethod();
        if (HttpMethod.POST.toString().equals(method) || HttpMethod.PUT.toString().equals(method)) {
            BufferedReader in = request.getReader();
            JsonReader reader = new JsonReader(in);
            DSMap body = reader.getMap();
            reader.close();
            updateTree(target, body);
        }
        
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginMap().key("target").value(target).key("method").value(request.getMethod()).endMap();
        writer.close();
        baseRequest.setHandled(true);
    }
    
//    @Override
//    protected void updateTree(String target, DSMap body) {
//        
//    }
}
