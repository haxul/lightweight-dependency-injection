package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.annotation.Singleton;
import ru.haxul.ldi.exception.InjectorException;

public class ClassActionExecutor implements ActionExecutor {

    @Override
    public void execute(ActionContext.DataContext dataContext) {
        try {
            final var name = dataContext.filename().substring(0, dataContext.filename().length() - ".class".length());
            final var classNameWithPackage = dataContext.dotPkg() + "." + name;
            final Class<?> clazz = Class.forName(classNameWithPackage);
            if (clazz.isAnnotationPresent(Singleton.class)) dataContext.classes().add(clazz);
        } catch (ClassNotFoundException ex) {
            throw new InjectorException("Injector cannot find class", ex);
        }
    }
}

