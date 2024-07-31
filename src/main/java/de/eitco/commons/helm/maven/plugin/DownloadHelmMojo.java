/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 19.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import com.deviceinsight.helm.ResolveHelmMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This goal downloads the helm executable specified.
 */
@Mojo(
    name = "download-helm",
    defaultPhase = LifecyclePhase.INITIALIZE
)
public class DownloadHelmMojo extends ResolveHelmMojo {

    @Override
    public void execute() {
        super.execute();

        HelmBinaryHolder.INSTANCE.setBinary(helm);
    }
}
