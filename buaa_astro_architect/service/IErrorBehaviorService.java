package org.buaa.buaa_astro_architect.service;

public interface IErrorBehaviorService {

    String getCompositeErrorBehavior(String viewId, String viewType);

    void setCompositeErrorBehavior(String viewId, String viewType, String compositeErrorBehavior);
}
