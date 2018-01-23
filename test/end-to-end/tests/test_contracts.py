import pytest
from os import environ
from pages.openbounty.landing import LandingPage
from tests.basetestcase import BaseTestCase


@pytest.mark.sanity
class TestLogin(BaseTestCase):

    def test_deploy_new_contract(self):
        landing = LandingPage(self.driver)
        landing.get_landing_page()
        github = landing.login_button.click()
        github.sign_in('anna04test',
                       'f@E23D3H15Rd')
        assert github.permission_type.text == 'Personal user data'
        bounties_page = github.authorize_sob.click()
        github.install_sob_plugin()
        assert bounties_page.bounties_header.text == 'Bounties'
        assert bounties_page.top_hunters_header.text == 'Top 5 hunters'
        github.create_new_bounty()
        github.get_deployed_contract()
