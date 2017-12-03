from pages.base_page import BasePageObject
from pages.base_element import *


class BountiesHeader(BaseText):

    def __init__(self, driver):
        super(BountiesHeader, self).__init__(driver)
        self.locator = self.Locator.css_selector('.open-bounties-header')


class TopHuntersHeader(BaseText):

    def __init__(self, driver):
        super(TopHuntersHeader, self).__init__(driver)
        self.locator = self.Locator.css_selector('.top-hunters-header')


class BountiesPage(BasePageObject):
    def __init__(self, driver):
        super(BountiesPage, self).__init__(driver)
        self.driver = driver

        self.bounties_header = BountiesHeader(self.driver)
        self.top_hunters_header = TopHuntersHeader(self.driver)
