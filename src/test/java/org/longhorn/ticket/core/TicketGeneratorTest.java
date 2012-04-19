package org.longhorn.ticket.core;

import static org.junit.Assert.*;
import org.junit.*;

public class TicketGeneratorTest {

    private TicketGenerator generator;

    @Before
    public void setUp(){
        generator = new TicketGenerator( 0 );
    }

    @Test
    public void testHappyCase() {
        
        long l1 = generator.nextTicket();
        long l2 = generator.nextTicket();
        System.out.println("l1=" + l1);
        System.out.println("l2=" + l2);
        assertFalse( l1 == l2 );
    }
}
