from pages.base_page import BasePageObject
from pages.base_element import *
from tests import test_data


class LoginButton(BaseButton):
    def __init__(self, driver):
        super(LoginButton, self).__init__(driver)
        self.locator = self.Locator.id('button-login')

    def navigate(self):
        from pages.thirdparty.github import GithubPage
        return GithubPage(self.driver)


class LandingPage(BasePageObject):
    def __init__(self, driver):
        super(LandingPage, self).__init__(driver)
        self.driver = driver

        self.login_button = LoginButton(self.driver)

    def get_landing_page(self):
        self.driver.get(test_data.config['Common']['url'])
