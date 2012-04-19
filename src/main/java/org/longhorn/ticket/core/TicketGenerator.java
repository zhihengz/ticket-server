package org.longhorn.ticket.core;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TicketGenerator {

    public static final int TOTAL_BITS_LENGTH = 63;
    public static final int NODE_BITS_LENGTH = 10;
    public static final int TIME_BITS_LENGTH = 41;
    private static final Logger logger = LoggerFactory.getLogger( TicketGenerator.class );

    private int workerId;
    private int counter;
    private long lastMillisecond;

    public TicketGenerator( int workerId ) {
        this.workerId = workerId;
    }

    public synchronized Long nextTicket() {
        long currentMillisecond = System.currentTimeMillis();
        logBytes( "step 0", currentMillisecond );
        //long id = currentMillisecond & 0xFFFFFFFFFl;
        long id = currentMillisecond & ( (1l << TIME_BITS_LENGTH) - 1l );
        logBytes( "step 1", id );
        id = id << (TOTAL_BITS_LENGTH - TIME_BITS_LENGTH);
        logBytes( "step 2", id );
        if ( currentMillisecond == lastMillisecond ) {
            counter = (++counter) % ( 1 << (TOTAL_BITS_LENGTH - NODE_BITS_LENGTH - TIME_BITS_LENGTH) );
        } else {
            counter = 0;
        }
        logBytes( "counter", counter );
        int id2 = ((workerId & ( (1 << NODE_BITS_LENGTH) - 1)) << 8) + counter;
        logBytes( "id2", id2);
        id += id2;
        logBytes( "step 3", id );
        lastMillisecond = currentMillisecond;
        return id;
    }

    private void logBytes( String msg, long number ) {
        logger.debug( msg + ": {}", Long.toBinaryString( number ) );
    }
    public static final void main(String[] args) {
        
        int node = 0;
        if ( args.length > 0 ) {
            node = Integer.parseInt(args[0]);
        }
        TicketGenerator generator = new TicketGenerator(0);
        for ( int i = 0; i < 10; i++) {
            System.out.println( generator.nextTicket() );
        }
        
    }
}
