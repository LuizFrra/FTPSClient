package session.CallBack;

import session.FTPSession;

public class SessionCallBackImpl implements SessionCallBack{

    @Override
    public void onFinally(FTPSession session) {

    }

    @Override
    public void onError(String error) {
        /*  does nothing */
    }
}
