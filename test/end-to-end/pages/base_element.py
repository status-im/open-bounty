import logging
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions


class BaseElement(object):
    class Locator(object):

        def __init__(self, by, value):
            self.by = by
            self.value = value

        @classmethod
        def xpath_selector(locator, value):
            return locator(By.XPATH, value)

        @classmethod
        def css_selector(locator, value):
            return locator(By.CSS_SELECTOR, value)

        @classmethod
        def id(locator, value):
            return locator(By.ID, value)

        @classmethod
        def name(locator, value):
            return locator(By.NAME, value)

        def __str__(self, *args):
            return "%s:%s" % (self.by, self.value)

    def __init__(self, driver):
        self.driver = driver
        self.locator = None

    @property
    def name(self):
        return self.__class__.__name__

    def navigate(self):
        return None

    def find_element(self):
        logging.info('Looking for %s' % self.name)
        return self.wait_for_element()

    def find_elements(self):
        logging.info('Looking for %s' % self.name)
        return self.driver.find_elements(self.locator.by,
                                         self.locator.value)

    def wait_for_element(self, seconds=5):
        return WebDriverWait(self.driver, seconds).until(
            expected_conditions.presence_of_element_located((self.locator.by, self.locator.value)))

    def wait_for_clickable(self, seconds=5):
        return WebDriverWait(self.driver, seconds).until(
            expected_conditions.element_to_be_clickable((self.locator.by, self.locator.value)))

    def is_element_present(self, sec=5):
        try:
            self.wait_for_element(sec)
            return True
        except TimeoutException:
            return False


class BaseEditBox(BaseElement):

    def __init__(self, driver):
        super(BaseEditBox, self).__init__(driver)

    def send_keys(self, value):
        self.find_element().send_keys(value)
        logging.info('Type %s to %s' % (value, self.name))

    def clear(self):
        self.find_element().clear()
        logging.info('Clear text in %s' % self.name)


class BaseText(BaseElement):

    def __init__(self, driver):
        super(BaseText, self).__init__(driver)

    @property
    def text(self):
        text = self.find_element().text
        logging.info('%s is %s' % (self.name, text))
        return text


class BaseButton(BaseElement):

    def __init__(self, driver):
        super(BaseButton, self).__init__(driver)

    def click(self):
        self.wait_for_clickable().click()
        logging.info('Tap on %s' % self.name)
        return self.navigate()
