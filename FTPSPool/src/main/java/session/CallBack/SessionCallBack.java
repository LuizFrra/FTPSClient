package session.CallBack;

import session.FTPSession;

public interface SessionCallBack {
    void onFinally(FTPSession session);

    void onError(String error);
}
