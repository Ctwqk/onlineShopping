import org.junit.jupiter.api.Test;

public class UploadFileTest {

    @Test
    public void test(){
        String fileName = "whatever.jpg";
        fileName = fileName.substring(fileName.lastIndexOf('.'));
        System.out.println(fileName);
    }
}
