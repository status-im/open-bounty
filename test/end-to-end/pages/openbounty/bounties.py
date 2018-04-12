import logging
from pages.base_page import BasePageObject
from pages.base_element import BaseText
from tests import test_data


class BountiesHeader(BaseText):

    def __init__(self, driver):
        super(BountiesHeader, self).__init__(driver)
        self.locator = self.Locator.css_selector('.open-bounties-header')


class TopHuntersHeader(BaseText):

    def __init__(self, driver):
        super(TopHuntersHeader, self).__init__(driver)
        self.locator = self.Locator.css_selector('.top-hunters-header')


class BountyTitles(BaseText):

    def __init__(self, driver):
        super(BountyTitles, self).__init__(driver)
        self.locator = self.Locator.css_selector('.open-bounty-item-content .header')


class BountyItemRows(BaseText):

    def __init__(self, driver):
        super(BountyItemRows, self).__init__(driver)
        self.locator = self.Locator.css_selector('.open-bounty-item-content .bounty-item-row')


class BountyFooters(BaseText):

    def __init__(self, driver):
        super(BountyFooters, self).__init__(driver)
        self.locator = self.Locator.css_selector('.open-bounty-item-content .footer-row')


class BountyClaimsAmount(BaseText):

    def __init__(self, driver, issue_title, claims_text):
        super(BaseText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//div[@class="header"]/a[contains(.,"%s")]/../../div[@class="footer-row"]/span[contains(.,"%s")]' % (issue_title, claims_text))


class BountiesPage(BasePageObject):
    def __init__(self, driver):
        super(BountiesPage, self).__init__(driver)

        self.driver = driver

        self.bounties_header = BountiesHeader(self.driver)
        self.top_hunters_header = TopHuntersHeader(self.driver)
        self.bounty_titles = BountyTitles(self.driver)
        self.bounty_item_rows = BountyItemRows(self.driver)
        self.bounty_footers = BountyFooters(self.driver)

    def get_bounties_page(self):
        self.driver.get(test_data.config['Common']['url'] + 'app')

    def check_bounty_claims_amount(self, issue_title, claims_text):
        logging.info('Check that bounty "%s" has "%s"' % (issue_title, claims_text))
        BountyClaimsAmount(self.driver, issue_title, claims_text).find_element()
