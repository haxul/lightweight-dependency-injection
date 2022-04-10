package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.collector.FileType;
import ru.haxul.ldi.util.Pair;

import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public class ActionContext {

    private final EnumMap<FileType, ActionExecutor> context = new EnumMap<>(FileType.class);

    public ActionContext() {
        context.put(FileType.CLASS, new ClassActionExecutor());
        context.put(FileType.UNKNOWN, new UnknownActionExecutor());
        context.put(FileType.FOLDER, new FolderActionExecutor());
    }

    public void call(FileType key, DataContext data) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(data);

        context.get(key).execute(data);
    }

    public record DataContext(String filename, String dotPkg, String slashPkg, List<Class<?>> classes,
                              Deque<Pair<String, String>> dq) {
    }
}
