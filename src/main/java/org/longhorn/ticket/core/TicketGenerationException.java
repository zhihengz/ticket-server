package org.longhorn.ticket.core;

public class TicketGenerationException extends RuntimeException {

    public TicketGenerationException( String msg ) {
        super(msg);
    }

    public TicketGenerationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
