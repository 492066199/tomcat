package com.sailing.tomcat.loader;


public interface Reloader {

    void addRepository(String repository);

    String[] findRepositories();

    boolean modified();
}
