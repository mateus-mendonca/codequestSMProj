package com.codequest.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public final class TemplateConfig {
    private TemplateConfig() { }

    public static TemplateEngine create() {
        TemplateEngine engine = new TemplateEngine();
        engine.addTemplateResolver(resolver("", 1));
        engine.addTemplateResolver(resolver(".html", 2));
        return engine;
    }

    private static ClassLoaderTemplateResolver resolver(String suffix, int order) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(suffix);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(order);
        resolver.setCheckExistence(true);
        return resolver;
    }
}
