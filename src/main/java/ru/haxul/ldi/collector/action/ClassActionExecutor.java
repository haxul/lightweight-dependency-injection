package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.annotation.Singleton;
import ru.haxul.ldi.exception.InjectorException;
import ru.haxul.ldi.util.Pair;

import java.util.Deque;
import java.util.List;

public class ClassActionExecutor implements ActionExecutor {

    @Override
    public void execute(ActionCollectorContext.Data data, List<Class<?>> singletons, Deque<Pair<String, String>> dq) {
        try {
            final var name = data.filename().substring(0, data.filename().length() - ".class".length());
            final var classNameWithPackage = data.dotPkg() + "." + name;
            final Class<?> clazz = Class.forName(classNameWithPackage);
            if (clazz.isAnnotationPresent(Singleton.class)) singletons.add(clazz);
        } catch (ClassNotFoundException ex) {
            throw new InjectorException("Injector cannot find class", ex);
        }
    }
}

