import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptVerifier {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "123456";
        String encoded = "$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqRzVafbuS7pSId.R5T6XWpSLuO2";
        System.out.println("Verifying '123456' against '$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqRzVafbuS7pSId.R5T6XWpSLuO2'");
        System.out.println("Result: " + encoder.matches(raw, encoded));
    }
}
