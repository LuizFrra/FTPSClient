package session.CallBack;

import session.FTPSession;

@FunctionalInterface
public interface ClientCallBack<C, T> {

    void execute(FTPSession session);

}
