package session;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FTPSession {

    private final FTPSClient client;

    public FTPSession(FTPSClient client) {
        this.client = client;
    }

    public boolean deleteFile(String path) throws IOException {
        return client.deleteFile(path);
    }

    public FTPFile[] list(String path) throws IOException {
        return client.listFiles(path);
    }

    public String[] listNames(String path) throws IOException {
        return this.client.listNames(path);
    }

    public boolean read(String path, OutputStream fos) throws IOException {
        return this.client.retrieveFile(path, fos);
    }

    public boolean store(InputStream inputStream, String path) throws IOException {
        return this.client.storeFile(path, inputStream);
    }

    public boolean append(InputStream inputStream, String path) throws IOException {
        return this.client.appendFile(path, inputStream);
    }

    public boolean mkdir(String remoteDirectory) throws IOException {
        return this.client.makeDirectory(remoteDirectory);
    }

    public boolean rmdir(String directory) throws IOException {
        return this.client.removeDirectory(directory);
    }
}
