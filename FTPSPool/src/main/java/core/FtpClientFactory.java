package core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class FtpClientFactory extends BasePooledObjectFactory<FTPSClient> {

    private final FtpProperties properties;

    public FtpClientFactory(FtpProperties properties) {
        this.properties = properties;
    }

    @Override
    public FTPSClient create() {
        FTPSClient client = new FTPSClient("TLS");

        client.setControlEncoding(properties.getEncoding());
        client.setConnectTimeout(properties.getConnectTimeout());

        if (properties.isEnableDebug())
            client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        try {
            client.connect(properties.getHost(), properties.getPort());

            if (!client.login(properties.getUsername(), properties.getPassword())) {
                client.disconnect();
                log.error("Unable to login, Check the credentials.");
                return null;
            }

            client.setFileType(properties.getTransferFileType());
            client.setBufferSize(properties.getBufferSize());
            client.execPBSZ(0);
            client.execPROT("P");

            if (properties.isPassiveMode())
                client.enterLocalPassiveMode();

        } catch (IOException e) {
            log.error("Failed When create FTPSClient Object.");
        }

        return client;
    }

    @Override
    public PooledObject<FTPSClient> wrap(FTPSClient ftpsClient) {
        return new DefaultPooledObject<>(ftpsClient);
    }

    @Override
    public void destroyObject(PooledObject<FTPSClient> p) throws Exception {
        if (p == null) return;

        FTPSClient client = p.getObject();

        try {
            if (client.isConnected())
                client.logout();
        } catch (IOException e) {
            log.error("Unable to logout client : {}", e.getMessage());
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                log.error("Unable to disconnect client : {}", e.getMessage());
            }
        }

        super.destroyObject(p);
    }

    @Override
    public boolean validateObject(PooledObject<FTPSClient> p) {
        FTPSClient client = p.getObject();
        try {
            return client.sendNoOp();
        } catch (IOException e) {
            log.error("Failed to validate client : {}", e.getMessage());
        }
        return false;
    }
}
