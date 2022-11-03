package authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;
import sms.SmsServiceFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;



public class SmsAuthenticator implements Authenticator {

    private static final String TPL_CODE = "login-sms.ftl";
    private static final String TPL_CODE2 = "login-sms-resent.ftl";


    @Override
    public void authenticate(AuthenticationFlowContext context) {

        // Получение пользователя
        KeycloakSession session = context.getSession();
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        UserModel user = context.getUser();

        //Проверка на наличие аутентификации
        String smsAuth = user.getFirstAttribute("sms_auth");
        if (!smsAuth.equals("enable")) {
            context.success();
            return;
        }

        // Получение конфигураций и генерация кода и смс
        int length = Integer.parseInt(config.getConfig().get("length"));
        int ttl = Integer.parseInt(config.getConfig().get("ttl"));
        String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
        String smsText = String.format("Your SMS code is %s and is valid for %s minutes.", code, Math.floorDiv(ttl, 60));
        String mobileNumber = user.getFirstAttribute("mobile_number");

        // Получение сессии пользователя
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        // Отправка сообщения
        try {
            SmsServiceFactory.get(config.getConfig()).send(mobileNumber, smsText);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Установка формы входа
        context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TPL_CODE2));

        // установка атрибутов сессии для дальнейшей проверки
        authSession.setAuthNote("code", code);
        authSession.setAuthNote("ttl", Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        //Получение введенного кода
        String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

        // получение сессии и связанных с ней параметров
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = authSession.getAuthNote("code");
        String ttl = authSession.getAuthNote("ttl");

        // Флаг
        String resend = context.getHttpRequest().getDecodedFormParameters().getFirst("resend");

        if (resend != null) {

            AuthenticatorConfigModel config = context.getAuthenticatorConfig();
            UserModel user = context.getUser();

            int length = Integer.parseInt(config.getConfig().get("length"));
            int ttl1 = Integer.parseInt(config.getConfig().get("ttl"));
            String code1 = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
            String smsText = String.format("Your SMS code is %s and is valid for %s minutes.", code1, Math.floorDiv(ttl1, 60));
            String mobileNumber = user.getFirstAttribute("mobile_number");

            try {
                SmsServiceFactory.get(config.getConfig()).send(mobileNumber, smsText);
            } catch (IOException e) {
                e.printStackTrace();
            }

            context.challenge(context.form().setAttribute("realm", context.getRealm()).setAttribute("isResend", true).createForm(TPL_CODE2));
            authSession.setAuthNote("code", code1);
            authSession.setAuthNote("ttl", Long.toString(System.currentTimeMillis() + (ttl1 * 1000L)));

        } else {

            boolean isValid = enteredCode.equals(code);

            if (isValid) {
                if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                    // expired
                    context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                            context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
                } else {
                    // valid
                    context.success();
                }
            } else {
                // invalid
                AuthenticationExecutionModel execution = context.getExecution();
                if (execution.isRequired()) {
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                            context.form().setAttribute("realm", context.getRealm())
                                    .setError("smsAuthCodeInvalid").createForm(TPL_CODE2));
                } else if (execution.isConditional() || execution.isAlternative()) {
                    context.attempted();
                }
            }
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }

}
