package fix.iDebugger.terminal;

public interface ILocalCommandExecutor {
    ExecuteResult executeCommand(String[] command, String[] envp, long timeout);
}
