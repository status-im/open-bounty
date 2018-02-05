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

        # Waiting for deployed contract; test_data.issue created here
        self.github_org.create_new_bounty()
        self.github_org.get_deployed_contract()

        # Navigate and check top bounty in "Open bounties"
        bounties_page = BountiesPage(self.driver_org)
        bounties_page.get_bounties_page()
        titles = bounties_page.bounty_titles.find_elements()
        assert titles[0].text == test_data.issue['title']








