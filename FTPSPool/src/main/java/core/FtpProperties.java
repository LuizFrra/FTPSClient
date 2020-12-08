package core;

import lombok.Data;
import org.apache.commons.net.ftp.FTP;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ftp.client")
@Data
public class FtpProperties {

    private String host;

    private Integer port = 21;

    private String username;

    private String password;

    private boolean passiveMode = false;

    private String encoding = "UTF-8";

    private Integer connectTimeout = 1000;

    private Integer bufferSize = 2048;

    private Integer transferFileType = FTP.BINARY_FILE_TYPE;

    private long minLogin = 1;

    private boolean enableDebug = true;

}
