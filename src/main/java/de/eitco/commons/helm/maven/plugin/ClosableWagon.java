/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 06.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;

import java.io.File;
import java.util.List;

public class ClosableWagon implements Wagon, AutoCloseable {

    private final Wagon delegate;
    private boolean open = true;
    private String targetUrl;

    public ClosableWagon(Wagon delegate, String targetUrl) {
        this.delegate = delegate;
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    @Override
    public void get(String s, File file) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        delegate.get(s, file);
    }

    @Override
    public boolean getIfNewer(String s, File file, long l) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        return delegate.getIfNewer(s, file, l);
    }

    @Override
    public void put(File file, String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        delegate.put(file, s);
    }

    @Override
    public void putDirectory(File file, String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        delegate.putDirectory(file, s);
    }

    @Override
    public boolean resourceExists(String s) throws TransferFailedException, AuthorizationException {
        return delegate.resourceExists(s);
    }

    @Override
    public List<String> getFileList(String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        return delegate.getFileList(s);
    }

    @Override
    public boolean supportsDirectoryCopy() {
        return delegate.supportsDirectoryCopy();
    }

    @Override
    public Repository getRepository() {
        return delegate.getRepository();
    }

    @Override
    public void connect(Repository repository) throws ConnectionException, AuthenticationException {
        delegate.connect(repository);
    }

    @Override
    public void connect(Repository repository, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        delegate.connect(repository, proxyInfo);
    }

    @Override
    public void connect(Repository repository, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        delegate.connect(repository, proxyInfoProvider);
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo) throws ConnectionException, AuthenticationException {
        delegate.connect(repository, authenticationInfo);
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        delegate.connect(repository, authenticationInfo, proxyInfo);
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        delegate.connect(repository, authenticationInfo, proxyInfoProvider);
    }

    @Override
    public void openConnection() throws ConnectionException, AuthenticationException {
        delegate.openConnection();
    }

    @Override
    public void disconnect() throws ConnectionException {
        delegate.disconnect();
    }

    @Override
    public void setTimeout(int i) {
        delegate.setTimeout(i);
    }

    @Override
    public int getTimeout() {
        return delegate.getTimeout();
    }

    @Override
    public void setReadTimeout(int i) {
        delegate.setReadTimeout(i);
    }

    @Override
    public int getReadTimeout() {
        return delegate.getReadTimeout();
    }

    @Override
    public void addSessionListener(SessionListener sessionListener) {
        delegate.addSessionListener(sessionListener);
    }

    @Override
    public void removeSessionListener(SessionListener sessionListener) {
        delegate.removeSessionListener(sessionListener);
    }

    @Override
    public boolean hasSessionListener(SessionListener sessionListener) {
        return delegate.hasSessionListener(sessionListener);
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {
        delegate.addTransferListener(transferListener);
    }

    @Override
    public void removeTransferListener(TransferListener transferListener) {
        delegate.removeTransferListener(transferListener);
    }

    @Override
    public boolean hasTransferListener(TransferListener transferListener) {
        return delegate.hasTransferListener(transferListener);
    }

    @Override
    public boolean isInteractive() {
        return delegate.isInteractive();
    }

    @Override
    public void setInteractive(boolean b) {
        delegate.setInteractive(b);
    }

    @Override
    public void close() {
        try {

            if (open) {

                disconnect();
                open = false;
            }

        } catch (ConnectionException e) {

            throw new WagonDisconnectionException(e);
        }
    }
}
