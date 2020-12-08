package core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.pool2.BaseObjectPool;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class FtpClientPool extends BaseObjectPool<FTPSClient> {

    private final FtpProperties properties;
    private final FtpClientFactory ftpClientFactory;
    private final BlockingQueue<FTPSClient> ftpBlockingQueue;

    public FtpClientPool(FtpClientFactory clientFactory, FtpProperties properties) throws Exception {
        if (properties.getMinLogin() <= 0)
            throw new Exception("ftp.client.max-login must be greater or equal 1.");

        this.ftpClientFactory = clientFactory;
        this.properties = properties;
        this.ftpBlockingQueue = new ArrayBlockingQueue<FTPSClient>((int) properties.getMinLogin());
        initPool();
    }

    private void initPool() throws Exception {
        for(int i = 0; i < properties.getMinLogin(); i++) addObject();
    }

    @Override
    public FTPSClient borrowObject() throws Exception {
        FTPSClient client = ftpBlockingQueue.take();

        if (ObjectUtils.isEmpty(client)) {
            client = ftpClientFactory.create();
        } else if (!ftpClientFactory.validateObject(ftpClientFactory.wrap(client))) {
            invalidateObject(client);

            client = ftpClientFactory.create();
        }

        return client;
    }

    @Override
    public void returnObject(FTPSClient ftpsClient) throws Exception {
        try {
            if (ftpsClient != null && !ftpBlockingQueue.offer(ftpsClient, 3, TimeUnit.SECONDS)) {
                ftpClientFactory.destroyObject(ftpClientFactory.wrap(ftpsClient));
            }
        } catch (InterruptedException e) {
            log.error("return ftp client interrupted : {}", e.getMessage());
        }
    }

    @Override
    public void invalidateObject(FTPSClient ftpsClient) throws Exception {
        try {
            ftpsClient.changeWorkingDirectory("/");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ftpBlockingQueue.remove(ftpsClient);
        }
    }

    @Override
    public void addObject() throws Exception {
        ftpBlockingQueue.offer(ftpClientFactory.create(), 3, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        try {
            while (ftpBlockingQueue.iterator().hasNext()) {
                FTPSClient client = ftpBlockingQueue.take();
                ftpClientFactory.destroyObject(ftpClientFactory.wrap(client));
            }
        } catch (Exception e) {
            log.error("Unaible close client blockingQueue : {}", e.getMessage());
        }
    }
}
