package ru.haxul.ldi.core;

import ru.haxul.ldi.annotation.Singleton;
import ru.haxul.ldi.annotation.SingletonType;
import ru.haxul.ldi.exception.InjectorException;
import ru.haxul.ldi.exception.SingletonNotFoundException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Injector {

    private static final SingletonClassContainer container = new SingletonClassContainer();

    public <T> T getSingleton(final Class<T> type) {
        final Object singleton = container.get(type);

        if (singleton == null) throw new SingletonNotFoundException(type.getName());

        return type.cast(singleton);
    }

    private void addOneToOneSingleton(final Class<?> singletonClass) throws Exception {
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

            final Object singleton = singletonClass.getConstructor(paramClasses).newInstance(params);
            container.put(singletonClass, singleton);
        }
    }

    public void init(final Class<?> entryPoint) {
        if (entryPoint == null) throw new InjectorException("entry point is null");
        final var packageName = entryPoint.getPackageName();
        final String slashPkg = packageName.replaceAll("\\.", "/");

        try (final InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(slashPkg);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

            String line;

            while ((line = reader.readLine()) != null) {

                final var classStr = ".class";

                if (!line.endsWith(classStr)) continue;

                final var name = line.substring(0, line.length() - classStr.length());

                final var classNameWithPackage = packageName + "." + name;

                final Class<?> clazz = Class.forName(classNameWithPackage);

                if (!clazz.isAnnotationPresent(Singleton.class)) continue;

                final Singleton annotation = clazz.getAnnotation(Singleton.class);

                switch (annotation.type()) {
                    case ONE_TO_ONE -> addOneToOneSingleton(clazz);
                    case ONE_TO_MANY -> {/*TODO*/}
                }
            }

            container.becameImmutable();
        } catch (InjectorException ex) {
            throw ex;
        } catch (ClassNotFoundException ex) {
            throw new InjectorException("Injector cannot find class", ex);
        } catch (Exception ex) {
            throw new InjectorException("something gets wrong during injector initialization", ex);
        }
    }
}
