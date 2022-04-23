package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.collector.FileType;
import ru.haxul.ldi.util.Pair;

import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public class ActionCollectorContext {

    private final EnumMap<FileType, ActionExecutor> context = new EnumMap<>(FileType.class);

    public ActionCollectorContext() {
        context.put(FileType.CLASS, new ClassActionExecutor());
        context.put(FileType.UNKNOWN, new UnknownActionExecutor());
        context.put(FileType.FOLDER, new FolderActionExecutor());
    }

    public void call(FileType key, Data data, List<Class<?>> classes, Deque<Pair<String, String>> dq) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(data);

        context.get(key).execute(data, classes, dq);
    }

    public record Data(String filename, String dotPkg, String slashPkg) {
    }
}
