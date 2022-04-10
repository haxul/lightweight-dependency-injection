package ru.haxul.ldi.core;

import ru.haxul.ldi.annotation.Singleton;
import ru.haxul.ldi.collector.FileTypeViaNameDefiner;
import ru.haxul.ldi.collector.SingletonClassCollector;
import ru.haxul.ldi.exception.InjectorException;
import ru.haxul.ldi.exception.SingletonNotFoundException;

import java.util.List;

public class Injector {

    private static final SingletonClassContainer container = new SingletonClassContainer();

    public void init(final Class<?> entryPoint) {
        if (entryPoint == null) throw new InjectorException("entry point is null");
        final var singletonClassCollector = new SingletonClassCollector(new FileTypeViaNameDefiner());
        List<Class<?>> singletonClasses = singletonClassCollector.find(entryPoint.getPackageName());

        for (var clazz : singletonClasses) {
            final Singleton annotation = clazz.getAnnotation(Singleton.class);
            switch (annotation.type()) {
                case ONE_TO_ONE -> addOneToOneSingleton(clazz);
                case ONE_TO_MANY -> {/*TODO*/}
                default -> throw new IllegalStateException("unknown @singleton type(): " + annotation.type());
            }
        }

        container.becameImmutable();
    }

    public <T> T getSingleton(final Class<T> type) {
        final Object singleton = container.get(type);

        if (singleton == null) throw new SingletonNotFoundException(type.getName());

        return type.cast(singleton);
    }

    private void addOneToOneSingleton(final Class<?> singletonClass) {
        if (singletonClass.isInterface()) {
            final var errMsg = "class " + singletonClass.getName() +
                    " must not be interface. @Singleton type OneToOne is only for classes";
            throw new InjectorException(errMsg);
        }

        if (container.containsKey(singletonClass)) return;

        for (var constructor : singletonClass.getConstructors()) {
            final Class<?>[] paramClasses = constructor.getParameterTypes();
            final Object[] params = new Object[paramClasses.length];

            for (int i = 0; i < paramClasses.length; i++) {

                final var paramClass = paramClasses[i];
                if (!container.containsKey(paramClass)) addOneToOneSingleton(paramClass);
                final Object dependency = container.get(paramClass);
                params[i] = dependency;
            }

            final Object singleton = tryToCreateSingleton(singletonClass, paramClasses, params);
            container.put(singletonClass, singleton);
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
