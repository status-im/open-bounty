from pages.base_page import BasePageObject
from pages.base_element import *
from tests import test_data
import logging


class ActivityDescription(BaseText):

    def __init__(self, driver, status, issue_title):
        super(ActivityDescription, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//div[@class="description"]/div[contains(.,"' + status + '")]/a[contains(.,"' + issue_title + '")]')


class ActivityPage(BasePageObject):
    def __init__(self, driver):
        super(ActivityPage, self).__init__(driver)
        self.driver = driver


    def get_activity_page(self):
        self.driver.get(test_data.config['Common']['url'] + 'app#/activity')

    def check_activity_is_presented(self, status, issue_title):
        logging.info('Check that activity "' + status + issue_title + '" is displayed')
        ActivityDescription(self.driver, status, issue_title).find_element()


