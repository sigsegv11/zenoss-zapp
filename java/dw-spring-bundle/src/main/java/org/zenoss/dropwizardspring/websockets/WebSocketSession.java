package org.zenoss.dropwizardspring.websockets;

import com.google.common.base.Preconditions;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Session data object for WebSockets.  This object contains the request and connection objects.
 *
 */
public class WebSocketSession {
    private final HttpServletRequest request;
    private final WebSocket.Connection connection;


    public WebSocketSession( HttpServletRequest request, Connection connection) {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(connection);
        this.request = request;
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Send byte message over connection
     * @param data
     * @param offset
     * @param length
     * @throws IOException
     * @see Connection#sendMessage(byte[], int, int)
     */
    public void sendMessage(byte[] data, int offset, int length) throws IOException {
        connection.sendMessage(data, offset, length);
    }

    /**
     * Send string message over connection
     * @param data
     * @param offset
     * @param length
     * @throws IOException
     * @see Connection#sendMessage(String)
     */
    public void sendMessage(String message) throws IOException {
        connection.sendMessage(message);
    }

    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    /**
     * Get the parameter value from the http servlet request
     * @param name
     * @return the servlet request parameter value, null if it doesn't exist
     * @see HttpServletRequest#getParameter(String)
     */
    public String getParameter(String name) {
        return request.getParameter(name);
    }
}