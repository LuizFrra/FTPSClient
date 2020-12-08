import core.FtpProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import session.FTPTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FtpProperties.class, FTPTemplate.class})
@EnableConfigurationProperties
@ActiveProfiles("test")
public class TestClass {

    @Autowired
    public FTPTemplate ftpTemplate;

    @Test
    public void fileExist_Not() throws Exception {
        boolean exist = ftpTemplate.pathExist(UUID.randomUUID().toString());
        Assert.assertFalse(exist);
    }

    @Test
    public void createFile_AndFileExist_AndDelete() throws Exception {
        ByteArrayInputStream data = new ByteArrayInputStream("createFileAndFileExistTest".
                getBytes(StandardCharsets.UTF_8));
        String fileName = UUID.randomUUID().toString();

        boolean fileWasCreated = ftpTemplate.store(fileName, data);

        boolean fileExist = ftpTemplate.pathExist(fileName);

        boolean fileDel = ftpTemplate.deleteFile(fileName);

        Assert.assertTrue(fileWasCreated & fileExist & fileDel);
    }

    @Test
    public void createDir_AndDelete() throws Exception {
        String dir = UUID.randomUUID().toString();

        boolean dirWasCreated = ftpTemplate.createDir(dir);

        boolean dirWasDeleted = ftpTemplate.deleteDir(dir);

        Assert.assertTrue(dirWasCreated & dirWasDeleted);
    }

}
