package org.longhorn.ticket.core;

public class TicketServiceRuntimeException extends RuntimeException {

    public TicketServiceRuntimeException( String msg ) {
        super(msg);
    }

    public TicketServiceRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
