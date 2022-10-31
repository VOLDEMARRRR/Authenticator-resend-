<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
    <#elseif section = "form">
        <div class="${properties.kcFormClass!}">
            <#if isResend!false>
                <span style="color:red">Новый SMS-код отправлен</span><br>
            </#if>
            <form id="kc-sms-code-resend-form"
                  class="${properties.kcFormClass!}"
                  action="${url.loginAction}"
                  method="post">
                <input type="hidden" id="resend" name="resend" value="resend"/>
                <div id="kc-form-buttons-resend" class="${properties.kcFormButtonsClass!}">
                    <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                   type="submit" value="Запросить SMS-код повторно"/>
                        </div>
                    </div>
                </div>
            </form>
            <form id="kc-sms-code-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}"
                  method="post">
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcLabelWrapperClass!}">
                        <label for="code" class="${properties.kcLabelClass!}">Введите SMS-код</label>
                    </div>
                    <div class="${properties.kcInputWrapperClass!}">
                        <input type="text" id="code" name="code" class="${properties.kcInputClass!}" autofocus/>
                    </div>
                </div>
                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                               type="submit" value="Отправить код"/>
                    </div>
                </div>
            </form>
        </div>
    <#elseif section = "info" ></#if>
</@layout.registrationLayout>