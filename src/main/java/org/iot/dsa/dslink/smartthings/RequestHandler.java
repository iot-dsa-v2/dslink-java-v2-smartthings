package org.iot.dsa.dslink.smartthings;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class RequestHandler extends AbstractHandler {
    
    private HubNode hubNode;
    
    public RequestHandler(HubNode hubNode) {
        this.hubNode = hubNode;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        hubNode.handleRequest(target, baseRequest, request, response);
    }

}
