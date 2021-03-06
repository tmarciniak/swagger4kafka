package com.stavshamir.swagger4kafka.services;

import com.stavshamir.swagger4kafka.configuration.Docket;
import com.stavshamir.swagger4kafka.dtos.KafkaEndpoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class KafkaEndpointsService {

    private final String basePackage;
    private final KafkaListenersScanner scanner;

    @Getter
    private final Set<KafkaEndpoint> endpoints;

    @Autowired
    public KafkaEndpointsService(Docket docket, KafkaListenersScanner kafkaListenersScanner) {
        this.basePackage = Optional.of(docket)
                .map(Docket::getBasePackage)
                .orElse(null);

        this.scanner = kafkaListenersScanner;
        this.endpoints = scanPackageForKafkaEndpoints();
    }

    private Set<KafkaEndpoint> scanPackageForKafkaEndpoints() {
        if (scanner == null) {
            throw new RuntimeException("This method must not be accessed before the object is fully constructed");
        }

        if (basePackage == null) {
            throw new RuntimeException("Base package not provided - please provide a Docket bean with basePackage defined");
        }

        return getClassesAnnotatedWithComponent().stream()
                    .map(scanner::getKafkaEndpointsFromClass)
                    .flatMap(Collection::stream)
                    .peek(endpoint -> log.debug("Registered endpoint: {}", endpoint))
                    .collect(toSet());
    }

    private Set<Class<?>> getClassesAnnotatedWithComponent() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));

        return provider.findCandidateComponents(basePackage).stream()
                .map(BeanDefinition::getBeanClassName)
                .peek(className -> log.debug("Found candidate class: {}", className))
                .map(this::getClass)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    private Optional<Class<?>> getClass(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            log.error("Class {} not found", className);
        }

        return Optional.empty();
    }

}
