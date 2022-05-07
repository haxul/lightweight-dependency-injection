package ru.haxul.ldi.collector;

import ru.haxul.ldi.collector.action.ActionCollectorContext;
import ru.haxul.ldi.exception.InjectorException;
import ru.haxul.ldi.util.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static java.util.Optional.*;

public record SingletonClassCollector(
        FileTypeDefiner<String> fileTypeDefiner,
        ActionCollectorContext actionCollectorContext
) implements Collector<Class<?>, String> {


    // based on bfs algo
    @Override
    public List<Class<?>> find(final String entryPoint) {
        Objects.requireNonNull(entryPoint);

        final var classes = new ArrayList<Class<?>>(30);
        final var dq = new ArrayDeque<Pair<String, String>>();

        dq.add(Pair.of(entryPoint, entryPoint.replaceAll("\\.", "/")));

        while (!dq.isEmpty()) {
            final Pair<String, String> pair = dq.pollFirst();
            final String curDotPkg = pair.left();
            final String curSlashPkg = pair.right();

            try (final InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(curSlashPkg);
                 final BufferedReader reader = new BufferedReader(
                         new InputStreamReader(ofNullable(stream).orElseThrow(() -> new IllegalStateException("stream is null")))
                 )
            ) {

                String fileName;

                while ((fileName = reader.readLine()) != null) {
                    final var fileType = fileTypeDefiner.define(fileName);
                    final var dataContext = new ActionCollectorContext.Data(fileName, curDotPkg, curSlashPkg);
                    actionCollectorContext.call(fileType, dataContext, classes, dq);
                }
            } catch (Exception ex) {
                throw new InjectorException("something gets wrong during injector initialization", ex);
            }
        }
        return classes;
    }
}
