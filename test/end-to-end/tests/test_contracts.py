import pytest
from pages.openbounty.bounties import BountiesPage
from pages.openbounty.activity import ActivityPage
from tests.basetestcase import BaseTestCase
from tests import test_data


@pytest.mark.sanity
class TestLogin(BaseTestCase):

     # def test_deploy_new_contract(self):
     #
     #    # Waiting for deployed contract; test_data.issue created here
     #    self.github_org.create_new_bounty()
     #    self.github_org.get_deployed_contract()
     #
     #    # Navigate and check top bounty in "Open bounties"
     #    bounties_page = BountiesPage(self.driver_dev)
     #    bounties_page.get_bounties_page()
     #    titles = bounties_page.bounty_titles.find_elements()
     #    assert titles[0].text == test_data.issue['title']

     def test_new_claim(self):
        self.github_dev.create_pr_git('test_branch_%s' % self.github_dev.time_now)
        # self.github_dev.open_pr_github('Fixes')
        #
        # # check new claim in "Open bounties"
        # bounties_page = BountiesPage(self.driver_dev)
        # bounties_page.get_bounties_page()
        # bounties_page.check_bounty_claims_amount(test_data.issue['title'], '1 open claim')
        #
        # # check new claim in "Activity"
        # activity_page = ActivityPage(self.driver_dev)
        # activity_page.get_activity_page()
        # activity_page.check_activity_is_presented('Submitted a claim for ', test_data.issue['title'])








