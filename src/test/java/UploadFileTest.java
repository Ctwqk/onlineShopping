import org.junit.jupiter.api.Test;

public class UploadFileTest {

    @Test
    public void test(){
        String fileName = "whatever.jpg";
        fileName = fileName.substring(fileName.lastIndexOf('.'));
        System.out.println(fileName);
    }

    @Test
    public void testSth(){
        String a = "wqwer";
        System.out.println(a+1);
    }
}
