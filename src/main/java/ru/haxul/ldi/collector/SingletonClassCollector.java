package ru.haxul.ldi.collector;

import ru.haxul.ldi.collector.action.ActionContext;
import ru.haxul.ldi.exception.InjectorException;
import ru.haxul.ldi.util.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public record SingletonClassCollector(
        FileTypeDefiner<String> fileTypeDefiner,
        ActionContext actionContext
) implements Collector<Class<?>, String> {


    @Override
    public List<Class<?>> find(final String entryPoint) {

        final var classes = new ArrayList<Class<?>>(30);
        final var dq = new ArrayDeque<Pair<String, String>>();

        dq.add(Pair.of(entryPoint, entryPoint.replaceAll("\\.", "/")));

        while (!dq.isEmpty()) {
            final Pair<String, String> pair = dq.pollFirst();
            final String curDotPkg = pair.left();
            final String curSlashPkg = pair.right();

            try (final InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(curSlashPkg);
                 final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

                String fileName;

                while ((fileName = reader.readLine()) != null) {
                    final var fileType = fileTypeDefiner.define(fileName);
                    final var dataContext = new ActionContext.DataContext(fileName, curDotPkg, curSlashPkg, classes, dq);
                    actionContext.call(fileType, dataContext);
                }
            } catch (Exception ex) {
                throw new InjectorException("something gets wrong during injector initialization", ex);
            }
        }
        return classes;
    }
}
