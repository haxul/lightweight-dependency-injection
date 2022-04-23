package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.util.Pair;

import java.util.Deque;
import java.util.List;

public class FolderActionExecutor implements ActionExecutor {

    @Override
    public void execute(ActionCollectorContext.Data data, List<Class<?>> singletons, Deque<Pair<String, String>> dq) {
        var left = data.dotPkg() + "." + data.filename();
        var right = data.slashPkg() + "/" + data.filename();
        final var newPair = Pair.of(left, right);
        dq.addLast(newPair);
    }
}
