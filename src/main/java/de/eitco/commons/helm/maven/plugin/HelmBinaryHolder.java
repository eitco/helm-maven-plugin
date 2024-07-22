/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 20.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

public enum HelmBinaryHolder {

    INSTANCE;

    private String binary;

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }
}
