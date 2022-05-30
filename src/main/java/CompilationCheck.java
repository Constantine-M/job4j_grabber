public class CompilationCheck {

    public boolean onePlusOne() {
        int result = 1 + 1;
        return result > 1;
    }

    public static void main(String[] args) {
        CompilationCheck compCheck = new CompilationCheck();
        System.out.println(compCheck.onePlusOne());
    }
}
