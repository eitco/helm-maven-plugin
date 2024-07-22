/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 06.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import org.apache.maven.wagon.ConnectionException;

public class WagonDisconnectionException extends RuntimeException {

    public WagonDisconnectionException(ConnectionException cause) {
        super(cause);
    }

    @Override
    public synchronized ConnectionException getCause() {
        return (ConnectionException) super.getCause();
    }
}
