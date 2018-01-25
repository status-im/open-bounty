import pytest, sys
from selenium import webdriver
from selenium.common.exceptions import WebDriverException
from tests.postconditions import remove_application, remove_installation
from os import environ, path
from tests import test_data


class BaseTestCase:

    @property
    def sauce_username(self):
        return environ.get('SAUCE_USERNAME')


    @property
    def sauce_access_key(self):
        return environ.get('SAUCE_ACCESS_KEY')


    @property
    def executor_sauce_lab(self):
        return 'http://%s:%s@ondemand.saucelabs.com:80/wd/hub' % (self.sauce_username, self.sauce_access_key)

    def print_sauce_lab_info(self, driver):
        sys.stdout = sys.stderr
        print("SauceOnDemandSessionID=%s job-name=%s" % (driver.session_id,
                                                         pytest.config.getoption('build')))

    @property
    def capabilities_sauce_lab(self):

        desired_caps = dict()
        desired_caps['name'] = test_data.test_name
        desired_caps['build'] = pytest.config.getoption('build')
        desired_caps['platform'] = "MAC"
        desired_caps['browserName'] = 'Chrome'
        desired_caps['screenResolution'] = '2048x1536'
        desired_caps['captureHtml'] = False
        return desired_caps

    @property
    def environment(self):
        return pytest.config.getoption('env')

    def setup_method(self):

        self.errors = []
        self.cleanup = None

        if self.environment == 'local':
            options = webdriver.ChromeOptions()
            options.add_argument('--start-fullscreen')
            options.add_extension(
            path.abspath(test_data.config['Paths']['tests_absolute'] + 'resources/metamask3_12_0.crx'))
            # for chromedriver 2.35
            self.driver = webdriver.Chrome(chrome_options=options)
        if self.environment == 'sauce':
            self.driver = webdriver.Remote(self.executor_sauce_lab,
                                           desired_capabilities=self.capabilities_sauce_lab)
        self.driver.implicitly_wait(5)



    def verify_no_errors(self):
        if self.errors:
            msg = ''
            for error in self.errors:
                msg += (error + '\n')
            pytest.fail(msg, pytrace=False)

    def teardown_method(self):
        if self.cleanup:
            remove_application(self.driver)
            remove_installation(self.driver)

        try:
            self.print_sauce_lab_info(self.driver)
            self.driver.quit()
        except WebDriverException:
            pass
