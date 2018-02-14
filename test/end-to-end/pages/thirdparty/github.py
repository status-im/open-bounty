import time, pytest
from pages.base_element import *
from pages.base_page import BasePageObject
from tests import test_data
from git import Repo
import os
import shutil
import logging


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
            self.locator = self.Locator.css_selector("[data-name='748942015']")

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
class IssueId(BaseText):
    def __init__(self, driver):
        super(IssueId, self).__init__(driver)
        self.locator = self.Locator.css_selector(".gh-header-number")

class ForkButton(BaseButton):
    def __init__(self, driver):
        super(ForkButton, self).__init__(driver)
        self.locator = self.Locator.css_selector("[href='#fork-destination-box']")

class HeaderInForkPopup(BaseText):
    def __init__(self, driver):
        super(HeaderInForkPopup, self).__init__(driver)
        self.locator = self.Locator.css_selector("#facebox-header")

class UserAccountInForkPopup(BaseButton):
    def __init__(self, driver):
        super(UserAccountInForkPopup, self).__init__(driver)
        self.locator = self.Locator.css_selector("[value=%s]"%test_data.config['DEV']['gh_username'])


class ForkedRepoText(BaseText):
    def __init__(self, driver):
        super(ForkedRepoText, self).__init__(driver)
        self.locator = self.Locator.css_selector(".commit-tease")

class DeleteRepo(BaseButton):
    def __init__(self, driver):
        super(DeleteRepo, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//button[text()[contains(.,' Delete this repository')]]")

class RepoNameBoxInPopup(BaseEditBox):
    def __init__(self, driver):
        super(RepoNameBoxInPopup, self).__init__(driver)
        self.locator = self.Locator.css_selector("input[aria-label='Type in the name of the repository to confirm that you want to delete this repository.']")

class ConfirmDeleteButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmDeleteButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//button[text()[contains(.,'I understand the consequences, delete')]]")

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
        self.issue_id = IssueId(self.driver)
        self.fork_button = ForkButton(self.driver)
        self.header_in_fork_popup = HeaderInForkPopup(self.driver)
        self.user_account_in_fork_popup = UserAccountInForkPopup(self.driver)
        self.forked_repo_text = ForkedRepoText(self.driver)

        self.delete_repo = DeleteRepo(self.driver)
        self.repo_name_confirm_delete = RepoNameBoxInPopup(self.driver)
        self.confirm_delete = ConfirmDeleteButton(self.driver)


    def get_issues_page(self):
        self.driver.get(test_data.config['ORG']['gh_repo'] + 'issues')

    def get_issue_page(self, issue_id):
        self.driver.get(test_data.config['ORG']['gh_repo'] + 'issues/' + issue_id)

    def get_sob_plugin_page(self):
        self.driver.get(test_data.config['Common']['sob_test_app'])

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
        test_data.issue = dict()
        test_data.issue['title'] = 'auto_test_bounty_%s' % self.time_now
        self.issue_title_input.send_keys(test_data.issue['title'])
        self.labels_button.click()
        self.bounty_label.click()
        self.cross_button.click()
        self.submit_new_issue_button.click()
        test_data.issue['id'] = self.issue_id.text[1:]
        logging.info("Issue title is %s" %  test_data.issue['title'])

    def fork_repo(self, initial_repo, wait=60):
        self.driver.get(initial_repo)
        self.fork_button.click()
        if self.header_in_fork_popup.text == 'Where should we fork this repository?':
            self.user_account_in_fork_popup.click()
        self.forked_repo_text.wait_for_element(wait)

    def get_login_page(self):
        self.driver.get(test_data.config['Common']['gh_login'])

    def get_deployed_contract(self, wait=120):
        for i in range(wait):
            self.refresh()
            try:
                return self.contract_body.text
            except TimeoutException:
                time.sleep(10)
                pass
        pytest.fail('Contract is not deployed in %s minutes!' % str(wait/60))

    #cloning via HTTPS
    def clone_repo(self, initial_repo=None, username=None, repo_name=None, repo_path='git_repo'):
        os.mkdir(repo_path)
        os.chdir(repo_path)
        test_data.local_repo_path = os.getcwd()
        fork = 'https://github.com/%s/%s.git' % (username, repo_name)
        logging.info(('Cloning from %s to %s' % (fork, repo_path)))
        r = Repo.clone_from(fork, repo_path)
        logging.info(('Successefully cloned to:  %s' % test_data.local_repo_path))
        logging.info('Set upstream to %s'% initial_repo)
        upstream = r.create_remote('upstream', initial_repo)
        upstream.fetch()
        assert upstream.exists()
        r.heads.master.checkout()

    def clean_repo_local_folder(self):
        logging.info('Removing %s' % test_data.local_repo_path)
        if test_data.local_repo_path:
            shutil.rmtree(test_data.local_repo_path)

    def delete_fork(self):
        self.get_url(test_data.config['DEV']['gh_forked_repo'] + 'settings')
        self.delete_repo.click()
        self.repo_name_confirm_delete.send_keys(test_data.config['ORG']['gh_repo_name'])
        self.confirm_delete.click()





