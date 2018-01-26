import pytest
from os import environ
from pages.openbounty.landing import LandingPage
from pages.openbounty.bounties import BountiesPage
from pages.thirdparty.github import GithubPage
from tests.basetestcase import BaseTestCase
from tests import test_data


@pytest.mark.sanity
class TestLogin(BaseTestCase):

    def test_deploy_new_contract(self):
        self.cleanup = True
        landing = LandingPage(self.driver)
        landing.get_landing_page()

        # Sign Up to SOB
        github = landing.login_button.click()
        github.sign_in(test_data.config['ORG']['gh_login'],
                       test_data.config['ORG']['gh_password'])
        assert github.permission_type.text == 'Personal user data'
        bounties_page = github.authorize_sob.click()

        # SOB Plugin installation and navigate to "Open bounties"
        github.install_sob_plugin()
        assert bounties_page.bounties_header.text == 'Bounties'
        assert bounties_page.top_hunters_header.text == 'Top 5 hunters'

        # Waiting for deployed contract; test_data.issue created here
        github.create_new_bounty()
        github.get_deployed_contract()

        # Navigate and check top bounty in "Open bounties"
        bounties_page = BountiesPage(self.driver)
        bounties_page.get_bounties_page()
        titles = bounties_page.bounty_titles.find_elements()
        assert titles[0].text == test_data.issue['title']


    def test_forking_repo(self):
        github = GithubPage(self.driver)
        self.cleanup = True

        # Sign In to GH as Developer
        github.get_login_page()
        github.sign_in(test_data.config['DEV']['gh_login'],
                       test_data.config['DEV']['gh_password'])

        # Fork repo as Developer from Organization
        github.fork_repo(test_data.config['ORG']['gh_repo'])

        # Cloning repo to local git as Developer and set upstream to Organization (via HTTPS)
        github.clone_repo(test_data.config['ORG']['gh_repo'],
                          test_data.config['DEV']['gh_username'],
                          test_data.config['ORG']['gh_repo_name'],
                          'git_repo')

        github.clean_repo_local_folder()




