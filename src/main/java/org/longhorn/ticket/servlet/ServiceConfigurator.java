package org.longhorn.ticket.servlet;

import java.io.*;
import java.util.*;

public class ServiceConfigurator {

    private static final String DEFAULT_ZK_CONN = "127.0.0.1:2181";
    private String zkConn;

    private ServiceConfigurator( String zkConn ) {
        this.zkConn = zkConn;
    }

    public String getZookeeperConnectionString() {
        return zkConn;
    }

    public static final ServiceConfigurator loadConfiguration( String filePath ) throws IOException {
        if ( filePath != null && new File( filePath ).exists() ) {
            Properties p = new Properties();
            p.load( new FileInputStream( filePath ) );
            if ( p.getProperty( "zkConn" ) != null ) {
                return new ServiceConfigurator( p.getProperty( "zkConn" ) );
            }
        }
        return new ServiceConfigurator( DEFAULT_ZK_CONN );

    }
}
