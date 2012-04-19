package org.longhorn.ticket.core;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TicketGenerator {

    public static final int TOTAL_BITS_LENGTH = 63;
    public static final int NODE_BITS_LENGTH = 10;
    public static final int TIME_BITS_LENGTH = 41;

    private static final int COUNT_BITS_LENGTH = TOTAL_BITS_LENGTH - NODE_BITS_LENGTH - TIME_BITS_LENGTH;
    private static final long TIME_BITS_MASK = (1l << TIME_BITS_LENGTH) - 1L;
    private static final int TIME_BITS_SHIFT_SIZE = TOTAL_BITS_LENGTH - TIME_BITS_LENGTH;
    private static final int MAX_COUNTER = 1 << COUNT_BITS_LENGTH;
    private static final int NODE_BITS_MASK = (1 << NODE_BITS_LENGTH) - 1;

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
        if ( currentMillisecond < lastMillisecond ) {
            throw new TicketGenerationException
                ("time is out of sync by " 
                 + (lastMillisecond - currentMillisecond) + "ms");
        }
        long id = currentMillisecond & TIME_BITS_MASK;
        logBytes( "step 1", id );
        id = id << TIME_BITS_SHIFT_SIZE;
        logBytes( "step 2", id );
        if ( currentMillisecond == lastMillisecond ) {
            counter++;
            if ( counter >= MAX_COUNTER ) {
                throw new TicketGenerationException
                    ( "too much requests cause counter overflow" );
            }
        } else {
            counter = 0;
        }
        logBytes( "counter", counter );
        int id2 = ((workerId & NODE_BITS_MASK) << COUNT_BITS_LENGTH) + counter;
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
