package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.annotation.Singleton;
import ru.haxul.ldi.exception.InjectorException;
import ru.haxul.ldi.util.Pair;

import java.util.Deque;
import java.util.List;

public class ClassActionExecutor implements ActionExecutor {

    @Override
    public void execute(ActionCollectorContext.DataContext dataContext, List<Class<?>> classes, Deque<Pair<String, String>> dq) {
        try {
            final var name = dataContext.filename().substring(0, dataContext.filename().length() - ".class".length());
            final var classNameWithPackage = dataContext.dotPkg() + "." + name;
            final Class<?> clazz = Class.forName(classNameWithPackage);
            if (clazz.isAnnotationPresent(Singleton.class)) classes.add(clazz);
        } catch (ClassNotFoundException ex) {
            throw new InjectorException("Injector cannot find class", ex);
        }

    }
}

