package ru.haxul.ldi.core;

import ru.haxul.ldi.annotation.Singleton;
import ru.haxul.ldi.annotation.SingletonType;
import ru.haxul.ldi.exception.InjectorException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Injector {

    private static final Map<Class<?>, Object> container = new HashMap<>();

    private static final int DEFAULT_CLASS_AMOUNTS = 40;

    public <T> T getSingleton(Class<T> type) {
        final Object object = container.get(type);

        if (object == null) return null;

        return type.cast(object);
    }

    private void addOneToOneSingleton(Class<?> singletonClass) throws Exception {

        if (container.containsKey(singletonClass)) return;

        for (var constructor : singletonClass.getConstructors()) {
            final Class<?>[] paramClasses = constructor.getParameterTypes();
            final Object[] params = new Object[paramClasses.length];
            int idx = 0;
            for (Class<?> paramClass : paramClasses) {
                if (!container.containsKey(paramClass)) {
                    addOneToOneSingleton(paramClass);
                }
                final Object dependency = container.get(paramClass);
                params[idx++] = dependency;
            }

            container.put(singletonClass, singletonClass.getConstructor(paramClasses).newInstance(params));
        }
    }

    public void init(Class<?> entryPoint) {
        if (entryPoint == null) throw new InjectorException("entry point is null");
        final var packageName = entryPoint.getPackageName();
        final String slashPkg = packageName.replaceAll("\\.", "/");

        try (InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(slashPkg)) {
            if (stream == null) {
                throw new InjectorException("cannot download " + slashPkg + " from system class loader");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.endsWith(".class")) continue;

                final var name = line.substring(0 , line.length() - 6);

                final var classNameWithPackage = packageName + "." + name;

                final Class<?> clazz = Class.forName(classNameWithPackage);

                if (!clazz.isAnnotationPresent(Singleton.class)) continue;

                final Singleton annotation = clazz.getAnnotation(Singleton.class);

                if (annotation.type() == SingletonType.ONE_TO_ONE) {
                    addOneToOneSingleton(clazz);
                }
            }

        } catch (InjectorException ex) {
            throw ex;
        } catch (ClassNotFoundException ex) {
            throw new InjectorException("Injector cannot find class", ex);
        } catch (Exception ex) {
            throw new InjectorException("some get wrong during injector initialization", ex);
        }
    }
}
