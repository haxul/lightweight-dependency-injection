package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.util.Pair;

import java.util.Deque;
import java.util.List;

public class UnknownActionExecutor implements ActionExecutor {

    @Override
    public void execute(ActionCollectorContext.DataContext dataContext, List<Class<?>> classes, Deque<Pair<String, String>> dq) {

    }
}
