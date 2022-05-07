package ru.haxul.ldi.core;

import ru.haxul.ldi.collector.FileTypeViaNameDefiner;
import ru.haxul.ldi.collector.SingletonClassCollector;
import ru.haxul.ldi.collector.action.ActionCollectorContext;
import ru.haxul.ldi.exception.CycleDependencyException;
import ru.haxul.ldi.exception.InjectorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Injector {

    private static final Map<Class<?>, Object> container = new HashMap<>();

    public void init(final Class<?> entryPoint) {

        if (entryPoint == null) throw new InjectorException("entry point is null");

        final var singletonClassCollector =
                new SingletonClassCollector(new FileTypeViaNameDefiner(), new ActionCollectorContext());

        List<Class<?>> singletonClasses = singletonClassCollector.find(entryPoint.getPackageName());

        for (var clazz : singletonClasses) {
            saveSingleton(clazz, new HashMap<>());
        }
    }

    // based on depth first search algo
    private void saveSingleton(final Class<?> singletonClass,
                               final Map<Class<?>, List<Class<?>>> dependencyClassInvokedOnStackTracker) {

        // validation
        if (singletonClass.isInterface()) {
            final var errMsg = "class " + singletonClass.getName() +
                    " must not be interface. @Singleton type OneToOne is only for classes";
            throw new InjectorException(errMsg);
        }

        // finite case
        if (container.containsKey(singletonClass)) return;

        // check if there is a dependency cycle
        if (dependencyClassInvokedOnStackTracker.containsKey(singletonClass)) {
            final String cycledClassesStr = dependencyClassInvokedOnStackTracker.get(singletonClass)
                    .stream()
                    .map(Class::toString)
                    .collect(Collectors.joining(",", "", ""));

            throw new CycleDependencyException(singletonClass + " in a cycle with " + cycledClassesStr);
        }

        // get classes which were on the stack before
        final var classesOnStack = dependencyClassInvokedOnStackTracker
                .getOrDefault(singletonClass, new ArrayList<>());

        for (var constructor : singletonClass.getConstructors()) {
            final Class<?>[] paramClasses = constructor.getParameterTypes();
            final Object[] params = new Object[paramClasses.length];

            for (int i = 0; i < paramClasses.length; i++) {

                final var paramClass = paramClasses[i];

                if (!container.containsKey(paramClass)) {
                    // if container does not have the dependency , try to create this dependency
                    classesOnStack.add(paramClass);

                    //add mark that the class is on the stack
                    dependencyClassInvokedOnStackTracker.put(singletonClass, classesOnStack);

                    saveSingleton(paramClass, dependencyClassInvokedOnStackTracker);
                }
                final Object dependency = container.get(paramClass);
                params[i] = dependency;
            }

            final Object singleton = tryToCreateSingleton(singletonClass, paramClasses, params);
            container.put(singletonClass, singleton);

            // remove mark that the class is not on stack
            dependencyClassInvokedOnStackTracker.remove(singletonClass);
        }
    }

    private Object tryToCreateSingleton(Class<?> clazz, Class<?>[] paramsClasses, Object[] params) {
        try {
            return clazz.getConstructor(paramsClasses).newInstance(params);
        } catch (Exception e) {
            throw new InjectorException("singleton object " + clazz + " cannot be created", e);
        }
    }
}
