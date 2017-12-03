import time, pytest
from pages.base_element import *
from pages.base_page import BasePageObject


class EmailEditbox(BaseEditBox):

    def __init__(self, driver):
        super(EmailEditbox, self).__init__(driver)
        self.locator = self.Locator.id('login_field')


class PasswordEditbox(BaseEditBox):

    def __init__(self, driver):
        super(PasswordEditbox, self).__init__(driver)
        self.locator = self.Locator.id('password')


class SignInButton(BaseButton):

    def __init__(self, driver):
        super(SignInButton, self).__init__(driver)
        self.locator = self.Locator.name('commit')


class AuthorizeStatusOpenBounty(BaseButton):
    def __init__(self, driver):
        super(AuthorizeStatusOpenBounty, self).__init__(driver)
        self.locator = self.Locator.css_selector('[data-octo-click="oauth_application_authorization"]')

    def navigate(self):
        from pages.openbounty.bounties import BountiesPage
        return BountiesPage(self.driver)


class PermissionTypeText(BaseText):
    def __init__(self, driver):
        super(PermissionTypeText, self).__init__(driver)
        self.locator = self.Locator.css_selector('.permission-title')


class InstallButton(BaseButton):
    def __init__(self, driver):
        super(InstallButton, self).__init__(driver)
        self.locator = self.Locator.css_selector('.btn-primary')


class OrganizationButton(BaseButton):
    def __init__(self, driver):
        super(OrganizationButton, self).__init__(driver)
        self.locator = self.Locator.css_selector('[alt="@Org4"]')


class AllRepositoriesButton(BaseButton):
    def __init__(self, driver):
        super(AllRepositoriesButton, self).__init__(driver)
        self.locator = self.Locator.id('install_target_all')


class IntegrationPermissionsGroup(BaseText):
    def __init__(self, driver):
        super(IntegrationPermissionsGroup, self).__init__(driver)
        self.locator = self.Locator.css_selector('.integrations-permissions-group')


class NewIssueButton(BaseButton):
    def __init__(self, driver):
        super(NewIssueButton, self).__init__(driver)
        self.locator = self.Locator.css_selector(".subnav [role='button']")


class IssueTitleEditBox(BaseEditBox):
    def __init__(self, driver):
        super(IssueTitleEditBox, self).__init__(driver)
        self.locator = self.Locator.id("issue_title")


class LabelsButton(BaseButton):
    def __init__(self, driver):
        super(LabelsButton, self).__init__(driver)
        self.locator = self.Locator.css_selector("button[aria-label='Apply labels to this issue']")

    class BountyLabel(BaseButton):
        def __init__(self, driver):
            super(LabelsButton.BountyLabel, self).__init__(driver)
            self.locator = self.Locator.css_selector("[data-name='bounty']")

    class CrossButton(BaseButton):
        def __init__(self, driver):
            super(LabelsButton.CrossButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector(
                "//span[text()='Apply labels to this issue']/../*[@aria-label='Close']")


class SubmitNewIssueButton(BaseButton):
    def __init__(self, driver):
        super(SubmitNewIssueButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//button[contains(text(), "
                                                   "'Submit new issue')]")


class ContractBody(BaseText):
    def __init__(self, driver):
        super(ContractBody, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//tbody//p[contains(text(), "
                                                   "'Current balance: 0.000000 ETH')]")


class GithubPage(BasePageObject):
    def __init__(self, driver):
        super(GithubPage, self).__init__(driver)

        self.driver = driver

        self.email_input = EmailEditbox(self.driver)
        self.password_input = PasswordEditbox(self.driver)
        self.sign_in_button = SignInButton(self.driver)

        self.authorize_sob = AuthorizeStatusOpenBounty(self.driver)
        self.permission_type = PermissionTypeText(self.driver)

        self.install_button = InstallButton(self.driver)
        self.organization_button = OrganizationButton(self.driver)
        self.all_repositories_button = AllRepositoriesButton(self.driver)
        self.integration_permissions_group = IntegrationPermissionsGroup(self.driver)

        self.new_issue_button = NewIssueButton(self.driver)
        self.issue_title_input = IssueTitleEditBox(self.driver)
        self.labels_button = LabelsButton(self.driver)
        self.bounty_label = LabelsButton.BountyLabel(self.driver)
        self.cross_button = LabelsButton.CrossButton(self.driver)
        self.submit_new_issue_button = SubmitNewIssueButton(self.driver)
        self.contract_body = ContractBody(self.driver)

    def get_issues_page(self):
        self.driver.get('https://github.com/Org4/nov13/issues')

    def get_sob_plugin_page(self):
        self.driver.get('http://github.com/apps/status-open-bounty-app-test')

    def sign_in(self, email, password):
        self.email_input.send_keys(email)
        self.password_input.send_keys(password)
        self.sign_in_button.click()

    def install_sob_plugin(self):
        initial_url = self.driver.current_url
        self.get_sob_plugin_page()
        self.install_button.click()
        self.organization_button.click()
        self.all_repositories_button.click()
        self.install_button.click()
        self.driver.get(initial_url)

    def create_new_bounty(self):
        self.get_issues_page()
        self.new_issue_button.click()
        self.issue_title_input.send_keys('auto_test_bounty_%s' % self.time_now)
        self.labels_button.click()
        self.bounty_label.click()
        self.cross_button.click()
        self.submit_new_issue_button.click()

    def get_deployed_contract(self, wait=120):
        for i in range(wait):
            self.refresh()
            try:
                return self.contract_body.text
            except TimeoutException:
                time.sleep(10)
                pass
        pytest.fail('Contract is not deployed in %s minutes!' % str(wait/60))
