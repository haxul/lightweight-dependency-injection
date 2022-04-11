package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.util.Pair;

import java.util.Deque;
import java.util.List;

public class FolderActionExecutor implements ActionExecutor {

    @Override
    public void execute(ActionCollectorContext.DataContext dataContext, List<Class<?>> classes, Deque<Pair<String, String>> dq) {
        var left = dataContext.dotPkg() + "." + dataContext.filename();
        var right = dataContext.slashPkg() + "/" + dataContext.filename();
        final var newPair = Pair.of(left, right);
        dq.addLast(newPair);
    }
}
