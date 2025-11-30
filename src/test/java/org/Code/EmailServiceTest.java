package org.Code;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    @Test
    void testConstructor_NullUsername_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new EmailService(null, "pass")
        );
        assertEquals("SMTP credentials must not be null", ex.getMessage());
    }

    @Test
    void testConstructor_NullPassword_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new EmailService("user@mail.com", null)
        );
        assertEquals("SMTP credentials must not be null", ex.getMessage());
    }

    @Test
    void testConstructor_ValidArgs_DoesNotThrow() {
        assertDoesNotThrow(() -> new EmailService("user@mail.com", "secret"));
    }


    @Test
    void testSendEmail_NullRecipient_ThrowsIllegalArgumentException() {
        EmailService service = new EmailService("user@mail.com", "secret");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.sendEmail(null, "Subject", "Body")
        );
        assertEquals("Recipient email must not be empty", ex.getMessage());
    }

    @Test
    void testSendEmail_BlankRecipient_ThrowsIllegalArgumentException() {
        EmailService service = new EmailService("user@mail.com", "secret");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.sendEmail("   ", "Subject", "Body")
        );
        assertEquals("Recipient email must not be empty", ex.getMessage());
    }


    @Test
    void testBuildSmtpProperties_HasCorrectSettings() throws Exception {
        EmailService service = new EmailService("user@mail.com", "secret");

        // نستدعي الميثود الخاصة باستخدام reflection
        Method m = EmailService.class.getDeclaredMethod("buildSmtpProperties");
        m.setAccessible(true);
        Properties props = (Properties) m.invoke(service);

        assertEquals("true", props.getProperty("mail.smtp.auth"));
        assertEquals("true", props.getProperty("mail.smtp.starttls.enable"));
        assertEquals("smtp.gmail.com", props.getProperty("mail.smtp.host"));
        assertEquals("587", props.getProperty("mail.smtp.port"));
    }

}
