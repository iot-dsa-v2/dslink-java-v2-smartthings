package org.iot.dsa.dslink.smartthings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HubNode extends SmartNode {
    private static final String PARAMS = "parameters";
    private static final String SUBSCRIBERS = "subscribers";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient httpClient = new OkHttpClient();
    private DSMap parameters;
    private Server server;
    private DSMap subscribers;
    
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
        
        DSIObject o = get(SUBSCRIBERS);
        if (o instanceof DSMap) {
            this.subscribers = (DSMap) o;
        } else {
            this.subscribers  = new DSMap();
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
        DSRuntime.run(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                    server.join();
                } catch (Exception e) {
                    warn("", e);
                    end();
                }
            }
        });
        
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
        } else if ("SUBSCRIBE".equals(method)) {
            String callback = request.getHeader("CALLBACK");
            if (callback != null) {
                subscribers.put(callback, true);
                put(SUBSCRIBERS, subscribers.copy()).setReadOnly(true);
            }
        } else if ("UNSUBSCRIBE".equals(method)) {
            String callback = request.getHeader("CALLBACK");
            if (callback != null) {
                subscribers.remove(callback);
                put(SUBSCRIBERS, subscribers.copy()).setReadOnly(true);
            }
        }
        
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginMap().key("target").value(target).key("method").value(request.getMethod()).endMap();
        writer.close();
        baseRequest.setHandled(true);
    }
    
    public void sendUpdate(SmartValueNode node, DSIValue value) {
        String path = node.getPath();
        String rootPath = getPath();
        if (path.startsWith(rootPath)) {
            path = path.substring(rootPath.length());
        }
        String bodyContent = new DSMap().put("path", path).put("value", value.toElement()).toString();
        
        for (Entry entry: subscribers) {
            String address = entry.getKey();
            RequestBody body = RequestBody.create(JSON, bodyContent);
            okhttp3.Request req = new okhttp3.Request.Builder()
                    .url(address)
                    .post(body)
                    .build();
            try {
                Response response = httpClient.newCall(req).execute();
                if (response.code() != 200) {
                    warn("Non-ok response to update: " + response.code() + " : " + response.message());
                }
                response.close();
            } catch (IOException e) {
                warn("Error sending update to subscriber: ", e);
            }
                   
            
        }
    }
    
    @Override
    protected HubNode getHub() {
        return this;
    }
}
