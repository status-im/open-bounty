import time
from pages.base_page import BasePageObject
from pages.base_element import *
from selenium.webdriver import ActionChains


class BasePluginButton(BaseButton):

    def click(self):
        time.sleep(2)
        self.find_element().click()


class AcceptButton(BasePluginButton):

    def __init__(self, driver):
        super(AcceptButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//button[.='Accept']")


class PrivacyText(BaseText):

    def __init__(self, driver):
        super(PrivacyText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//a[.='Privacy']")


class ExportDenButton(BaseButton):

    def __init__(self, driver):
        super(ExportDenButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//p[.='Import Existing DEN']")


class SecretPhraseEditBox(BaseEditBox):

    def __init__(self, driver):
        super(SecretPhraseEditBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//textarea")


class PasswordEditBox(BaseEditBox):

    def __init__(self, driver):
        super(PasswordEditBox, self).__init__(driver)
        self.locator = self.Locator.id('password-box')


class PasswordConfirmEditBox(BaseEditBox):

    def __init__(self, driver):
        super(PasswordConfirmEditBox, self).__init__(driver)
        self.locator = self.Locator.id('password-box-confirm')


class OkButton(BasePluginButton):

    def __init__(self, driver):
        super(OkButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//button[.='OK']")


class MetaMaskPlugin(BasePageObject):
    def __init__(self, driver):
        super(MetaMaskPlugin, self).__init__(driver)
        self.driver = driver

        self.accept_button = AcceptButton(self.driver)
        self.privacy_text = PrivacyText(self.driver)
        self.enter_secret_phrase = SecretPhraseEditBox(self.driver)
        self.export_den_button = ExportDenButton(self.driver)
        self.password_edit_box = PasswordEditBox(self.driver)
        self.password_box_confirm = PasswordConfirmEditBox(self.driver)
        self.ok_button = OkButton(self.driver)

    def recover_access(self, passphrase, password, confirm_password):

        self.get_url('chrome-extension://nkbihfbeogaeaoehlefnkodbefgpgknn/popup.html')
        self.accept_button.click()
        ActionChains(self.driver).move_to_element(self.privacy_text.find_element()).perform()
        self.accept_button.click()

        self.export_den_button.click()
        self.enter_secret_phrase.send_keys(passphrase)
        self.password_edit_box.send_keys(password)
        self.password_box_confirm.send_keys(confirm_password)
        self.ok_button.click()

