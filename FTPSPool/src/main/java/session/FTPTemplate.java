package session;

import core.FtpClientFactory;
import core.FtpClientPool;
import core.FtpProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.stereotype.Component;
import session.CallBack.ClientCallBack;
import session.CallBack.SessionCallBack;
import session.CallBack.SessionCallBackImpl;

import java.io.InputStream;

@Component
@Slf4j
public class FTPTemplate {

    private final FtpClientPool clientPool;

    private final FtpClientFactory clientFactory;

    private final FtpProperties properties;

    public FTPTemplate(FtpProperties properties) throws Exception {
        this.properties = properties;
        clientFactory = new FtpClientFactory(properties);
        clientPool = new FtpClientPool(clientFactory, properties);
    }

    private final SessionCallBack sessionCallBack = new SessionCallBackImpl();

    public void execute(ClientCallBack callBack) throws Exception {
        FTPSClient client = null;
        try {
            client = clientPool.borrowObject();
            callBack.execute(new FTPSession(client));
        } catch (Exception e) {
            throw e;
        } finally {
            clientPool.returnObject(client);
        }
    }

    public boolean pathExist(String path) throws Exception {
        FTPSClient client = null;
        try {
            client = clientPool.borrowObject();

            assert client != null;

            FTPFile[] listFiles = client.listFiles(path);

            return listFiles.length != 0;

        } catch (Exception e) {
            log.error("Unable to know if path exist : {} , error : {}", path, e.getMessage());
        } finally {
            clientPool.returnObject(client);
        }

        return false;
    }

    public boolean store(String path, InputStream data) throws Exception {
        return store(path, data, sessionCallBack);
    }

    public boolean store(String path, InputStream data, SessionCallBack callBack) throws Exception {
        FTPSClient client = null;

        try {
            client = clientPool.borrowObject();

            assert client != null;

            boolean successful = client.storeFile(path, data);

            if(successful)
                callBack.onFinally(new FTPSession(client));

            return successful;
        } catch (Exception e) {
            log.error("Unable to store file : {}", path);
            callBack.onError(e.getMessage());
        } finally {
            clientPool.returnObject(client);
        }

        return false;
    }

    public boolean deleteFile(String path) throws Exception {
        return deleteFile(path, sessionCallBack);
    }

    public boolean deleteFile(String path, SessionCallBack callBack) throws Exception {
        FTPSClient client = null;
        try {
            client = clientPool.borrowObject();

            boolean pathDel = client.deleteFile(path);

            if(pathDel)
                callBack.onFinally(new FTPSession(client));

            return pathDel;
        } catch (Exception e) {
            log.error("Unable to Delete Path : {}", path);
            callBack.onError(e.getMessage());
        } finally {
            clientPool.returnObject(client);
        }

        return false;
    }

    /*
     * This method Delete only empty directories
     *  */

    public boolean deleteDir(String dir) throws Exception {
        return deleteDir(dir, sessionCallBack);
    }

    /*
     * This method Delete only empty directories
     *  */

    public boolean deleteDir(String dir, SessionCallBack callBack) throws Exception {
        FTPSClient client = null;

        try {
            client = clientPool.borrowObject();

            boolean dirRem = client.removeDirectory(dir);

            if(dirRem)
                callBack.onFinally(new FTPSession(client));

            clientPool.returnObject(client);

            return dirRem;
        } catch (Exception e) {
            log.error("Unable to delete dir : {}", dir);
            callBack.onError(e.getMessage());
        } finally {
            clientPool.returnObject(client);
        }

        return false;
    }

    public boolean createDir(String dir) throws Exception {
        return createDir(dir, sessionCallBack);
    }

    public boolean createDir(String dir, SessionCallBack callBack) throws Exception {
        FTPSClient client = null;

        try {
            client = clientPool.borrowObject();

            boolean dirCreate = client.makeDirectory(dir);

            if(dirCreate)
                callBack.onFinally(new FTPSession(client));

            return dirCreate;
        } catch (Exception e) {
            log.error("Unable to create dir : {}", dir);
            callBack.onError(e.getMessage());
        } finally {
            clientPool.returnObject(client);
        }

        return false;
    }

}
