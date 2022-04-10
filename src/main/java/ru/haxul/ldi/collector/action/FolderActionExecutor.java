package ru.haxul.ldi.collector.action;

import ru.haxul.ldi.util.Pair;

public class FolderActionExecutor implements ActionExecutor {

    @Override
    public void execute(ActionContext.DataContext dataContext) {
        var left = dataContext.dotPkg() + "." + dataContext.filename();
        var right = dataContext.slashPkg() + "/" + dataContext.filename();
        final var newPair = Pair.of(left, right);
        dataContext.dq().addLast(newPair);
    }
}
