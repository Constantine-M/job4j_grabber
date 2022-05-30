import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class CompilationCheckTest {

    @Test
    public void onePlusOne() {
        CompilationCheck compCheck = new CompilationCheck();
        Assert.assertTrue(compCheck.onePlusOne());
    }
}