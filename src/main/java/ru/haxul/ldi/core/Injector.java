package ru.haxul.ldi.core;

import ru.haxul.ldi.annotation.Singleton;
import ru.haxul.ldi.exception.InjectorException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Injector {

    private static final Map<Class<?>, Object> classesSingletons = new HashMap<>();
    private static final Map<Class<?>, HashSet<Object>> interfacesSingleton = new HashMap<>();

    private static final int DEFAULT_CLASS_AMOUNTS = 40;

    public void init(Class<?> entryPoint) {
        if (entryPoint == null) throw new InjectorException("entry point is null");
        final var packageName = entryPoint.getPackageName();
        final String slashPkg = packageName.replaceAll("\\.", "/");

        try (InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(slashPkg)) {
            if (stream == null) {
                throw new InjectorException("cannot download " + slashPkg + " from system class loader");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            final var classNameSet = new HashSet<String>(DEFAULT_CLASS_AMOUNTS);


            reader.lines()
                    .filter(line -> line.endsWith(".class"))
                    .map(line -> line.substring(0, line.lastIndexOf(".")))
                    .forEach(classNameSet::add);


            for (String name : classNameSet) {

                var classNameWithPackage = packageName + "." + name;

                Class<?> clazz = Class.forName(classNameWithPackage);

                if (!clazz.isAnnotationPresent(Singleton.class)) continue;

                Singleton annotation = clazz.getAnnotation(Singleton.class);

                if (clazz.isInterface())
                    throw new InjectorException("Interface cannot be annotated by @Singleton. Only Classes");

                switch (annotation.type()) {
                    case CLASS -> classesSingletons.put(clazz, new Object());
                    case INTERFACE -> {
                        Class<?>[] interfaces = clazz.getInterfaces();
                        HashSet<Object> singletons = interfacesSingleton.getOrDefault(clazz, new HashSet<>());
                        singletons.add(new Object());
                        interfacesSingleton.put(clazz, singletons);
                    }
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
